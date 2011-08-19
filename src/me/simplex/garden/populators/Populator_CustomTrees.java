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
 * BlockPopulator for CustomTrees.
 * 
 * @author simplex
 * based on Pandarr's CaveGen @see Populator_Caves
 */
public class Populator_CustomTrees extends BlockPopulator {

	/**
	 * @see org.bukkit.generator.BlockPopulator#populate(org.bukkit.World,
	 *      java.util.Random, org.bukkit.Chunk)
	 */
	@Override
	public void populate(final World world, final Random random, Chunk source) {

		if (random.nextInt(100) < 2) {
			final int x = 4 + random.nextInt(8) + source.getX() * 16;
			final int z = 4 + random.nextInt(8) + source.getZ() * 16;
			Block high = world.getHighestBlockAt(x, z);
			if (!high.getRelative(BlockFace.DOWN).getType().equals(Material.GRASS)) {
				return;
			}
			int maxY = high.getY();
			if (maxY < 55) {
				return;
			}
			Set<XYZ> snake = selectBlocksForTree(world, random, x, maxY-5, z);
			buildTree(world, snake.toArray(new XYZ[0]));
			for (XYZ block : snake) {
				world.unloadChunkRequest(block.x / 16, block.z / 16);
			}
		}
	}

	private static Set<XYZ> selectBlocksForTree(World world, Random r, int blockX, int blockY, int blockZ) {
		Set<XYZ> snakeBlocks = new HashSet<XYZ>();
		int height = blockY+20+r.nextInt(5);
		XYZ block = new XYZ();
		while (true) {
			if (blockY > height ) {
				break;
			}
			
			blockY++;

			int radius = 1;
			
			if (blockY+3 > height) {
				radius = 0;
			}
			int radius2 = radius * radius + 1;
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					if (x * x + z * z < radius2) {
						block.x = blockX + x;
						block.y = blockY;
						block.z = blockZ + z;
						if (snakeBlocks.add(block)) {
							block = new XYZ();
						}
					}
				}
			}
			
		}
		return snakeBlocks;
	}

	private static void buildTree(World world, XYZ[] snakeBlocks) {
		// cut the snake into slices, this is my lab report
		HashMap<Integer, ArrayList<Block>> slices = new HashMap<Integer, ArrayList<Block>>();
		//System.out.println(snakeBlocks.length);
		for (XYZ loc : snakeBlocks) {
			Block block = world.getBlockAt(loc.x, loc.y, loc.z);
			if (block.isEmpty() && !block.isLiquid()&& block.getType() != Material.BEDROCK) {
				if (slices.containsKey(Integer.valueOf(loc.y))) {
					slices.get(Integer.valueOf(loc.y)).add(block);
				}
				else {
					slices.put(Integer.valueOf(loc.y), new ArrayList<Block>());
					slices.get(Integer.valueOf(loc.y)).add(block);
				}
			}
		}
		
		ArrayList<Integer> sortedKeys = new ArrayList<Integer>(slices.keySet());
		Collections.sort(sortedKeys);
		int low = sortedKeys.get(0);
		int high = sortedKeys.get(sortedKeys.size()-1);
		//boolean buildLayer1 = false;
		boolean buildLayer2 = false;
		boolean buildLayer3 = false;
		boolean buildLayer4 = false;
		for (Integer key : sortedKeys) {
			ArrayList<Block> slice = slices.get(key);
			for (Block b : slice) {
				b.setTypeIdAndData(17, (byte) 1, false);
			}
//			if (!buildLayer1) {
//				ArrayList<Block> toBranches = new ArrayList<Block>();
//				for (Block b : slice) {
//					if (b.getY()-low >= (high-low)-12 && checkBlockIsOnBorderOfSlice(b, slice)) {
//						toBranches.add(b);
//						buildLayer1 = true;
//					}
//				}
//				buildTreeLayer1(toBranches);
//			}
			if (!buildLayer2) {
				ArrayList<Block> toBranches = new ArrayList<Block>();
				for (Block b : slice) {
					if (b.getY()-low >= (high-low)-8 && checkBlockIsOnBorderOfSlice(b, slice)) {
						toBranches.add(b);
						buildLayer2 = true;
					}
				}
				buildTreeLayer2(toBranches);
			}
			if (!buildLayer3) {
				ArrayList<Block> toBranches = new ArrayList<Block>();
				for (Block b : slice) {
					if (b.getY()-low >= (high-low)-4 && checkBlockIsOnBorderOfSlice(b, slice)) {
						toBranches.add(b);
						buildLayer3 = true;
					}
				}
				buildTreeLayer3(toBranches);
			}
			if (!buildLayer4) {
				ArrayList<Block> toBranches = new ArrayList<Block>();
				for (Block b : slice) {
					if (b.getY()-low >= (high-low) && checkBlockIsOnBorderOfSlice(b, slice)) {
						toBranches.add(b);
						buildLayer4 = true;
					}
				}
				buildTreeLayer4(toBranches);
			}
		}
	}
	
//	private static void buildTreeLayer1(ArrayList<Block> blocks){
//		ArrayList<Block> branches = new ArrayList<Block>();
//		
//		for (Block b : blocks) {
//			BlockFace dir = getBuildDirection(b);
//			Block handle = b.getRelative(dir);
//			handle.setTypeIdAndData(17, (byte) 1, false);
//			branches.add(handle);
//			switch (dir) {
//			case NORTH: 
//				branches.add(handle.getRelative(-1, 0, 1));
//				branches.add(handle.getRelative(-1, 0, -1));
//				branches.add(handle.getRelative(-2, 0, 2));
//				branches.add(handle.getRelative(-2, 0, -2));
//				break;
//			case EAST: 
//				branches.add(handle.getRelative(-1, 0, -1));
//				branches.add(handle.getRelative(1, 0, -1));
//				branches.add(handle.getRelative(-2, 0, -2));
//				branches.add(handle.getRelative(2, 0, -2));
//				break;
//			case SOUTH:
//				branches.add(handle.getRelative(1, 0, 1));
//				branches.add(handle.getRelative(1, 0, -1));
//				branches.add(handle.getRelative(2, 0, 2));
//				branches.add(handle.getRelative(2, 0, -2));
//				break;
//			case WEST: 
//				branches.add(handle.getRelative(-1, 0, 1));
//				branches.add(handle.getRelative(1, 0, 1));
//				branches.add(handle.getRelative(-2, 0, 2));
//				branches.add(handle.getRelative(2, 0, 2));
//				break;
//			}
//		}
//		if (!branches.isEmpty()) {
//			for (Block branch : branches) {
//				branch.setTypeIdAndData(17, (byte) 1, false);
//				populateTreeBranch(branch, 2);
//			}
//		}
//	}
	
	private static void buildTreeLayer2(ArrayList<Block> blocks){
		ArrayList<Block> branches = new ArrayList<Block>();
		
		for (Block b : blocks) {
			BlockFace dir = getBuildDirection(b);
			Block handle = b.getRelative(dir);
			handle.setTypeIdAndData(17, (byte) 1, false);
			branches.add(handle);
			switch (dir) {
			case NORTH: 
				branches.add(handle.getRelative(-1, 0, 1));
				branches.add(handle.getRelative(-1, 0, -1));
				break;
			case EAST: 
				branches.add(handle.getRelative(-1, 0, -1));
				branches.add(handle.getRelative(1, 0, -1));
				break;
			case SOUTH:
				branches.add(handle.getRelative(1, 0, 1));
				branches.add(handle.getRelative(1, 0, -1));
				break;
			case WEST: 
				branches.add(handle.getRelative(-1, 0, 1));
				branches.add(handle.getRelative(1, 0, 1));
				break;
			}
		}
		if (!branches.isEmpty()) {
			for (Block branch : branches) {
				branch.setTypeIdAndData(17, (byte) 1, false);
				populateTreeBranch(branch, 2);
			}
		}
	}
	
	private static void buildTreeLayer3(ArrayList<Block> blocks){
		ArrayList<Block> branches = new ArrayList<Block>();
		
		for (Block b : blocks) {
			BlockFace dir = getBuildDirection(b);
			Block handle = b.getRelative(dir);
			handle.setTypeIdAndData(17, (byte) 1, false);
			branches.add(handle);
		}
		if (!branches.isEmpty()) {
			for (Block branch : branches) {
				branch.setTypeIdAndData(17, (byte) 1, false);
				populateTreeBranch(branch, 2);
			}
		}
	}
	
	private static void buildTreeLayer4(ArrayList<Block> blocks){
		ArrayList<Block> branches = new ArrayList<Block>();
		for (Block block : blocks) {
			branches.add(block);
		}
		if (!branches.isEmpty()) {
			for (Block branch : branches) {
				branch.setTypeIdAndData(17, (byte) 1, false);
				populateTreeBranch(branch, 2);
			}
		}
	}
	
	private static BlockFace getBuildDirection(Block b){
		BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};	
		for (BlockFace blockFace : faces) {
			if (!b.getRelative(blockFace).isEmpty()) {
				return blockFace.getOppositeFace();
			}
		}
		return BlockFace.SELF;
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
	
	private static void populateTreeBranch(Block block, int radius){
		int centerX = block.getX();
		int centerZ = block.getZ();
		int centerY = block.getY();
		World w = block.getWorld();
		
		int radius_check = radius*radius+1;
		
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				for (int y = -radius; y <= radius; y++) {
					if (x * x + y * y + z * z <= radius_check){
						Block b = w.getBlockAt(centerX+x, centerY+y, centerZ+z);
						if (b.isEmpty()) {
							b.setTypeIdAndData(18, (byte) 1, false);
						}
					}
				}
			}
		}
	}
}