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
 * basen on Pandarr's CaveGen
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

	static Set<XYZ> selectBlocksForLake(World world, Random random, int blockX, int blockY, int blockZ) {
		Set<XYZ> snakeBlocks = new HashSet<XYZ>();
		int blockY_start = blockY;
		int airHits = 0;
		XYZ block = new XYZ();
		while (true) {
			if (airHits > 1000) {
				break;
			}

			if (random.nextInt(20) == 0) {
				if (!(blockY_start-blockY > 2)) {
					blockY -= 2;
				}
			} 
			else if (world.getBlockTypeIdAt(blockX, blockY + 2, blockZ) == 0) {
				if (!(blockY_start-blockY > 2)) {
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

	static void buildLake(World world, XYZ[] snakeBlocks) {
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
		
		ArrayList<Integer> sortedKeys = new ArrayList<Integer>(slices.keySet());
		Collections.sort(sortedKeys);
		
		for (Integer key : sortedKeys) {
			ArrayList<Block> blocks = slices.get(key);
			if (SliceHasBorder(blocks)) {
				for (Block block : blocks) {
					block.setType(Material.STATIONARY_WATER);
				}
			}
			else {
				if (key.intValue() == lowest_y) {
					return;
				}
				for (Block block : blocks) {
					block.setType(Material.AIR);
				}
			}
		}
		for (Block block : slices.get(Integer.valueOf(lowest_y))) {
			block.getRelative(BlockFace.DOWN).setType(Material.DIRT);
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
}