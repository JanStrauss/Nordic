package me.simplex.garden.populators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import me.simplex.garden.util.XYZ;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

/**
 * BlockPopulator for snake-based Lakes.
 * 
 * @author simplex
 * based on Pandarr's CaveGen
 */
public class Populator_Lakes extends BlockPopulator {

	/**
	 * @see org.bukkit.generator.BlockPopulator#populate(org.bukkit.World,
	 *      java.util.Random, org.bukkit.Chunk)
	 */
	@Override
	public void populate(final World world, final Random random, Chunk source) {

		if (random.nextInt(100) < 3) {
			final int x = 4 + random.nextInt(8) + source.getX() * 16;
			final int z = 4 + random.nextInt(8) + source.getZ() * 16;
			int maxY = world.getHighestBlockYAt(x, z);
			if (maxY < 54) {
				return;
			}
			Set<XYZ> snake = selectBlocksForLake(world, random, x, maxY, z);
			buildLake(world, snake.toArray(new XYZ[0]));
			for (XYZ block : snake) {
				world.unloadChunkRequest(block.x / 16, block.z / 16);
			}
		}
	}

	private static Set<XYZ> selectBlocksForLake(World world, Random random, int blockX, int blockY, int blockZ) {
		Set<XYZ> snakeBlocks = new HashSet<XYZ>();
		int blockY_start = blockY;
		int airHits = 0;
		XYZ block = new XYZ();
		while (true) {
			if (airHits > 4500) {
				break;
			}

			if (random.nextInt(20) == 0) {
				if (!(blockY_start-blockY > 3)) {
					blockY -= 2;
				}
			} 
			else if (world.getBlockTypeIdAt(blockX, blockY + 2, blockZ) == 0) {
				if (!(blockY_start-blockY > 3)) {
					blockY--;
				}
			} 
			else if (world.getBlockTypeIdAt(blockX + 2, blockY, blockZ) == 0) {
				blockX++;
			} 
			else if (world.getBlockTypeIdAt(blockX - 2, blockY, blockZ) == 0) {
				blockX--;
			} 
			else if (world.getBlockTypeIdAt(blockX, blockY, blockZ + 2) == 0) {
				blockZ++;
			} 
			else if (world.getBlockTypeIdAt(blockX, blockY, blockZ - 2) == 0) {
				blockZ--;
			} 
			else if (world.getBlockTypeIdAt(blockX + 1, blockY, blockZ) == 0) {
				blockX++;
			} 
			else if (world.getBlockTypeIdAt(blockX - 1, blockY, blockZ) == 0) {
				blockX--;
			} 
			else if (world.getBlockTypeIdAt(blockX, blockY, blockZ + 1) == 0) {
				blockZ++;
			} 
			else if (world.getBlockTypeIdAt(blockX, blockY, blockZ - 1) == 0) {
				blockZ--;
			} 
			else if (random.nextBoolean()) {
				if (random.nextBoolean()) {
					blockX++;
				} else {
					blockZ++;
				}
			} else {
				if (random.nextBoolean()) {
					blockX--;
				} else {
					blockZ--;
				}
			}

			if (world.getBlockTypeIdAt(blockX, blockY, blockZ) != 0) {
				int radius = 1 + random.nextInt(5);
				int radius2 = radius * radius + 1;
				for (int x = -radius; x <= radius; x++) {
					for (int y = -radius; y <= radius; y++) {
						for (int z = -radius; z <= radius; z++) {
							if (x * x + y * y + z * z <= radius2 && y >= 0&& y < 128) {
								if (world.getBlockTypeIdAt(blockX + x, blockY+ y, blockZ + z) == 0) {
									airHits++;
								} else {
									block.x = blockX + x;
									block.y = blockY + y;
									block.z = blockZ + z;
									if (snakeBlocks.add(block)) {
										block = new XYZ();
									}
								}
							}
						}
					}
				}
			} else {
				airHits++;
			}
		}

		return snakeBlocks;
	}

	private static void buildLake(World world, XYZ[] snakeBlocks) {
		// cut the snake into slices, this is my lab report
		HashMap<Integer, ArrayList<Block>> slices = new HashMap<Integer, ArrayList<Block>>();
		int lowest_y = 127;
		for (XYZ loc : snakeBlocks) {
			Block block = world.getBlockAt(loc.x, loc.y, loc.z);
			if (!block.isEmpty() && !block.isLiquid()&& block.getType() != Material.BEDROCK) {
				for (int y = loc.y; y < world.getHighestBlockYAt(loc.x, loc.z); y++) {
					if (slices.containsKey(Integer.valueOf(y))) {
						slices.get(Integer.valueOf(y)).add(world.getBlockAt(loc.x, y, loc.z));
					}
					else {
						slices.put(Integer.valueOf(y), new ArrayList<Block>());
						slices.get(Integer.valueOf(y)).add(world.getBlockAt(loc.x, y, loc.z));;
					}
				}
			}
			if (loc.y < lowest_y) {
				lowest_y = loc.y;
			}
		}
		// sort the slices 
		ArrayList<Integer> sortedKeys = new ArrayList<Integer>(slices.keySet());
		Collections.sort(sortedKeys);
		
		// if a slice has a solid border, fill with border, else make it air
		ArrayList<ArrayList<Block>> water_slices = new ArrayList<ArrayList<Block>>();
		ArrayList<ArrayList<Block>> air_slices = new ArrayList<ArrayList<Block>>();
		for (Integer key : sortedKeys) {
			ArrayList<Block> slice = slices.get(key);
			if (SliceHasBorder(slice)) {
				for (Block block : slice) {
					block.setType(Material.STATIONARY_WATER);
				}
				water_slices.add(slice);
			}
			else {
				if (key.intValue() == lowest_y) {
					return;
				}
				for (Block block : slice) {
					block.setType(Material.AIR);
				}
				air_slices.add(slice);
			}
		}
		// dirt on ground of the lake
		for (Block block : slices.get(Integer.valueOf(lowest_y))) {
			block.getRelative(BlockFace.DOWN).setType(Material.DIRT);
		}
		// stair'd ground
		for (int steps = water_slices.size()-1; steps >= 0; steps--) {
			for (int layer = 0; layer < steps; layer++) {
				ArrayList<Block> blocks = water_slices.get(layer);
				ArrayList<Block> toRemove = new ArrayList<Block>();
				for (Block b : blocks) {
					if (checkBlockIsOnBorderOfSlice(b, blocks)) {
						b.setType(Material.DIRT);
						toRemove.add(b);
					}
				}
				for (Block b : toRemove) {
					blocks.remove(b);
				}			
			}
		}
		//shiny waterfalls
		
		//ignore 2 lowest slices
		if (air_slices.size()>1) {
			air_slices.remove(0);
		}
		if (air_slices.size()>1) {
			air_slices.remove(0);
		}
		
		//select possible blocks
		ArrayList<Block> waterfall_candidates = new ArrayList<Block>();
		int limit = 6;
		if (air_slices.size() < limit) {
			limit = air_slices.size();
		}
		for (int i = 0; i < limit; i++) {
			ArrayList<Block> slice = air_slices.get(i);
			for (Block b : slice) {
				boolean checkBorder = checkBlockIsOnBorderOfSlice(b, slice);
				boolean checkQualified = isWaterfallQualified(b);
				//System.out.println(checkBorder +" | "+checkQualified);
				if (checkBorder && checkQualified) {
					waterfall_candidates.add(b);
				}
			}
		}
		//build it
		//System.out.println(waterfall_candidates.size());
		Random r = new Random();
		if (!waterfall_candidates.isEmpty()) {
			buildWaterfall(waterfall_candidates.get(r.nextInt(waterfall_candidates.size())));
		}
		if (r.nextInt(100)< 10) {
			buildWaterfall(waterfall_candidates.get(r.nextInt(waterfall_candidates.size())));
		}
	}
	
	private static boolean SliceHasBorder(ArrayList<Block> slice){
		for (Block block : slice) {
			if (!hasNeighbors(block)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean hasNeighbors(Block block){
		if (!block.getRelative(BlockFace.WEST).isEmpty() && !block.getRelative(BlockFace.EAST).isEmpty() && !block.getRelative(BlockFace.NORTH).isEmpty() && !block.getRelative(BlockFace.SOUTH).isEmpty()) {
			return true;
		}
		return false;
	}
	
	private static boolean isWaterfallQualified(Block block){
		BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};	
		for (BlockFace f : faces) {
			Block r = block.getRelative(f);
			if (!r.isEmpty() && !r.getRelative(BlockFace.UP).isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	private static void buildWaterfall(Block block){
		BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};	
		for (BlockFace f : faces) {
			Block r = block.getRelative(f);
			if (!r.isEmpty()) {
				r.setType(Material.WATER);
				return;
			}
		}
	}
		
	private static boolean checkBlockIsOnBorderOfSlice(Block block, ArrayList<Block> slice){
		BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
			if (slice.contains(block.getRelative(faces[0])) 
			 && slice.contains(block.getRelative(faces[1])) 
			 && slice.contains(block.getRelative(faces[2])) 
			 && slice.contains(block.getRelative(faces[3]))) {
				return false;
			}
		return true;
	}
	

}