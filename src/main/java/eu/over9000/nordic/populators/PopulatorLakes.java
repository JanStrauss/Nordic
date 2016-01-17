/*
 * Copyright 2012 s1mpl3x
 * 
 * This file is part of Nordic.
 * 
 * Nordic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Nordic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Nordic If not, see <http://www.gnu.org/licenses/>.
 */
package eu.over9000.nordic.populators;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class PopulatorLakes extends BlockPopulator {


	private static final int MIN_BLOCK_COUNT = 450;
	private static final int MAX_BLOCK_COUNT = 900;
	private static final int LAKE_CHANCE = 3;
	private static final int CREEK_CHANCE = 85;
	private static final int MAX_CREEK_LENGTH = 125;

	private static final EnumSet<Material> TREE_MATERIALS = EnumSet.of(Material.LOG, Material.LEAVES);
	private static final EnumSet<Material> GROUND_MATERIALS = EnumSet.of(Material.DIRT, Material.GRASS);
	private static final EnumSet<BlockFace> FACES_TO_CHECK = EnumSet.of(BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH);


	@Override
	public void populate(final World world, final Random random, final Chunk source) {
		if (random.nextInt(100) >= LAKE_CHANCE) {
			return;
		}

		final int start_x = random.nextInt(16);
		final int start_z = random.nextInt(16);

		final Block lake_start = world.getHighestBlockAt(source.getX() * 16 + start_x, source.getZ() * 16 + start_z);

		if (lake_start.getY() - 1 <= 48) {
			return;
		}

		final Set<Block> lake_form = collectLakeLayout(world, lake_start, random);
		final Set<Block>[] form_result = startLakeBuildProcess(world, lake_form);
		if (form_result == null) {
			return;
		}
		final Block creek_start = buildLake(form_result[0], random);

		buildAirAndWaterfall(form_result[0], form_result[1], random);

		//if (creek_start == null || random.nextInt(100) >= CREEK_CHANCE) {
		//return;
		//}

//		final List<Block> creekblocks = collectCreekBlocks(world, creek_start, random);
//		if (creekblocks != null) {
//			buildCreek(world, creekblocks);
//		}
	}

	private List<Block> collectCreekBlocks(final World world, final Block creekStart, final Random random) {
		final int check_radius = 7;
		Vector main_dir = null;

		// Get the "main" direction
		int highest_diff = 0;
		for (int mod_x = -check_radius; mod_x <= check_radius; mod_x++) {
			for (int mod_z = -check_radius; mod_z <= check_radius; mod_z++) {
				final Block toCheck = world.getHighestBlockAt(mod_x + creekStart.getX(), mod_z + creekStart.getZ());
				final int diff = creekStart.getY() - toCheck.getY();
				if (diff > highest_diff) {
					highest_diff = diff;
					main_dir = new Vector(toCheck.getX() - creekStart.getX(), 0, toCheck.getZ() - creekStart.getZ());
				}
			}
		}
		if (main_dir != null) {
			final List<Block> creekblocks = new ArrayList<>();
			Location creek_current_center = creekStart.getRelative(0, 10, 0).getLocation();
			main_dir = main_dir.normalize().multiply(2);
			int steps = 0;
			while (!world.getHighestBlockAt(creek_current_center).getRelative(0, -1, 0).isLiquid() && steps < MAX_CREEK_LENGTH) {
				creekblocks.add(creek_current_center.getBlock());
				creek_current_center = creek_current_center.add(main_dir);
				main_dir = rotateVector(main_dir, random.nextDouble() * 0.5 - 0.25);
				steps++;
			}
			if (steps < MAX_CREEK_LENGTH) {
				return creekblocks;
			}
		}
		return null;
	}

	private void buildCreek(final World world, final List<Block> center_blocks) {
		final Set<Block> collected_blocks_air = new HashSet<>();
		final Set<Block> collected_blocks_water = new HashSet<>();

		final int radius = 3;
		final int radius_squared = 9;
		int last_y = world.getMaxHeight();

		final Set<Block> circle = new HashSet<>();
		for (final Block center : center_blocks) {
			circle.clear();
			for (int x_mod = -radius; x_mod <= radius; x_mod++) {
				for (int z_mod = -radius; z_mod <= radius; z_mod++) {
					if ((x_mod * x_mod + z_mod * z_mod) < radius_squared) {
						circle.add(center.getRelative(x_mod, 0, z_mod));
					}
				}
			}
			int lowest = world.getMaxHeight();
			int highest = 0;
			for (final Block block : circle) {

				final int x = block.getX();
				final int z = block.getZ();
				final int compare = world.getHighestBlockYAt(x, z);

				if (compare < lowest) {
					lowest = compare;
				}
				if (compare > highest) {
					highest = compare;
				}
			}

			if (lowest > last_y) {
				lowest = last_y;
			} else {
				last_y = lowest;
				if (last_y < 48) {
					last_y = 48;
					lowest = 48;
				}
			}
			for (final Block block : circle) {
				collected_blocks_water.add(world.getBlockAt(block.getX(), lowest - 3, block.getZ()));
				for (int y = lowest - 2; y <= highest; y++) {
					collected_blocks_air.add(world.getBlockAt(block.getX(), y, block.getZ()));
				}
			}
		}

		//actually build it
		for (final Block toWater : collected_blocks_water) {
			toWater.setType(Material.WATER);
		}
		for (final Block toAir : collected_blocks_air) {
			if (toAir.getY() <= 48) {
				toAir.setType(Material.WATER);
			} else if (toAir.getType() != Material.LOG &&
					toAir.getType() != Material.LEAVES &&
					toAir.getType() != Material.RED_MUSHROOM &&
					toAir.getType() != Material.VINE &&
					toAir.getType() != Material.GLOWSTONE) {
				toAir.setType(Material.AIR);
			}
		}
	}

	private Vector rotateVector(final Vector dir, final double angle) {
		final double new_x = dir.getX() * Math.cos(angle) - dir.getZ() * Math.sin(angle);
		final double new_z = dir.getX() * Math.sin(angle) + dir.getZ() * Math.cos(angle);
		return new Vector(new_x, 0, new_z);
	}

	private Set<Block> collectLakeLayout(final World world, final Block start, final Random random) {
		final Set<Block> result = new HashSet<>();
		final int sizelimit = MIN_BLOCK_COUNT + random.nextInt(MAX_BLOCK_COUNT - MIN_BLOCK_COUNT);
		int blockX = start.getX();
		final int blockY = start.getY();
		int blockZ = start.getZ();
		while (result.size() < sizelimit) {
			final int radius = 1 + random.nextInt(5);
			final int radius_squared = radius * radius + 1;

			for (int x_mod = -radius; x_mod <= radius; x_mod++) {
				for (int z_mod = -radius; z_mod <= radius; z_mod++) {
					if ((x_mod * x_mod + z_mod * z_mod) <= radius_squared) {
						final Block collected = world.getBlockAt(blockX + x_mod, blockY, blockZ + z_mod);
						result.add(collected);
					}
				}
			}

			if (random.nextBoolean()) {
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

		}
		return result;
	}

	private void buildAirAndWaterfall(final Set<Block> ground, final Set<Block> blocks, final Random random) {

		final List<Block> candidates = new ArrayList<>();
		final int ground_height = ground.iterator().next().getY();


		final List<Block> treeBlocksToCheck = new ArrayList<>();

		blocks.forEach(block -> {
			if (TREE_MATERIALS.contains(block.getType())) {
				treeBlocksToCheck.add(block);
			} else {
				block.setType(Material.AIR);
				if (checkBlockIsOnBorderOfSlice(block, blocks) && isWaterfallQualified(block) && block.getY() >= ground_height + 3) {
					candidates.add(block);
				}
			}
		});

		if (!candidates.isEmpty()) {
			buildWaterfall(candidates.get(random.nextInt(candidates.size())));
			if (random.nextInt(100) < 20) {
				buildWaterfall(candidates.get(random.nextInt(candidates.size())));
			}
		}

		handleTreeRemoval(treeBlocksToCheck);
	}

	private void handleTreeRemoval(final List<Block> treeBlocksToCheck) {

		while (!treeBlocksToCheck.isEmpty()) {

			final Set<Block> connectedTree = new HashSet<>();
			findConnectedTree(treeBlocksToCheck.get(0), connectedTree);

			if (!checkTreeOnSolidGround(connectedTree)) {
				connectedTree.forEach(block -> block.setType(Material.AIR));
			}

			treeBlocksToCheck.removeAll(connectedTree);
		}
	}

	private boolean checkTreeOnSolidGround(final Set<Block> connectedTree) {

		Block lowest = connectedTree.iterator().next();

		for (final Block block : connectedTree) {
			if (block.getY() < lowest.getY()) {
				lowest = block;
			}
		}

		return GROUND_MATERIALS.contains(lowest.getRelative(BlockFace.DOWN).getType());
	}

	private void findConnectedTree(final Block block, final Set<Block> result) {
		result.add(block);

		for (final BlockFace blockFace : FACES_TO_CHECK) {
			final Block candidate = block.getRelative(blockFace);
			if (TREE_MATERIALS.contains(candidate.getType()) && !result.contains(candidate)) {
				findConnectedTree(candidate, result);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Set<Block>[] startLakeBuildProcess(final World world, final Set<Block> blocks) {
		int lowest = world.getMaxHeight();
		int highest = 0;
		for (final Block block : blocks) {

			final int x = block.getX();
			final int z = block.getZ();
			final int compare = world.getHighestBlockYAt(x, z);

			if (compare < lowest) {
				lowest = compare;
			}
			if (compare > highest) {
				highest = compare;
			}
			if (compare < 48) {
				return null;
			}
		}

		if (lowest < 48 || highest - lowest > 25) {
			return null;
		}

		final Set<Block>[] result = new Set[2];
		result[0] = new HashSet<>();
		result[1] = new HashSet<>();

		for (final Block block : blocks) {
			result[0].add(world.getBlockAt(block.getX(), lowest - 1, block.getZ()));
			for (int y = lowest; y <= highest; y++) {
				result[1].add(world.getBlockAt(block.getX(), y, block.getZ()));
			}
		}
		return result;
	}

	private Block buildLake(Set<Block> top_layer, final Random random) {
		final int max_lake_depth = random.nextInt(2) + 3;

		//Make sure the lake has a border 
		final Set<Block> to_air = new HashSet<>();
		int lowering = 0;
		while (!sliceHasBorder(top_layer) && lowering <= 3) {
			to_air.addAll(top_layer);
			top_layer = lower_layer(top_layer);
			lowering++;
		}
		for (final Block block : to_air) {
			block.setType(Material.AIR);
		}

		//Build the First water layer
		for (final Block block : top_layer) {
			block.setType(Material.STATIONARY_WATER);
		}

		// "Stepped" Ground
		Set<Block> working_layer = lower_layer(top_layer);

		for (int mod_y = 0; mod_y > -max_lake_depth; mod_y--) {
			final Set<Block> next_layer = new HashSet<>();
			for (final Block block : working_layer) {
				if (checkBlockIsOnBorderOfSlice(block, working_layer)) {
					if (!block.isLiquid()) {
						block.setType(Material.DIRT);
					}
				} else {
					next_layer.add(block.getRelative(0, -1, 0));
					block.setType(Material.STATIONARY_WATER);
				}
			}
			working_layer = next_layer;
		}

		//Ground of the lake
		working_layer.stream().filter(block -> !block.isLiquid()).forEach(block -> block.setType(Material.DIRT));

		//Return the start point for a possible creek, null if this lake doesn't have a suitable block
		for (final Block block : top_layer) {
			if (checkBlockIsOnBorderOfSlice(block, top_layer)) {
				final Block candidate = block.getRelative(getUncontainedBlockFace(block, top_layer));
				if (candidate.getRelative(BlockFace.UP).isEmpty()) {
//					System.out.println("startpoint found");
//					candidate.setType(Material.GLOWSTONE);
					return candidate;
				}
			}
		}
		return null;
	}

	private Set<Block> lower_layer(final Set<Block> waterLayer) {
		return waterLayer.stream().map(block -> block.getRelative(0, -1, 0)).collect(Collectors.toSet());
	}

	private boolean checkBlockIsOnBorderOfSlice(final Block block, final Set<Block> slice) {
		final BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		return !(slice.contains(block.getRelative(faces[0]))
				&& slice.contains(block.getRelative(faces[1]))
				&& slice.contains(block.getRelative(faces[2]))
				&& slice.contains(block.getRelative(faces[3])));
	}

	private BlockFace getUncontainedBlockFace(final Block block, final Set<Block> slice) {
		final BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		for (final BlockFace face : faces) {
			if (!slice.contains(block.getRelative(face))) {
				return face;
			}
		}

		return null;
	}

	private boolean sliceHasBorder(final Set<Block> slice) {
		for (final Block block : slice) {
			if (!hasNeighbors(block)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasNeighbors(final Block block) {
		return !block.getRelative(BlockFace.WEST).isEmpty() &&
				!block.getRelative(BlockFace.EAST).isEmpty() &&
				!block.getRelative(BlockFace.NORTH).isEmpty() &&
				!block.getRelative(BlockFace.SOUTH).isEmpty();
	}

	private boolean isWaterfallQualified(final Block block) {
		final BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		for (final BlockFace f : faces) {
			final Block r = block.getRelative(f);
			if (!r.isEmpty() && !r.getRelative(BlockFace.UP).isEmpty()) {
				if (r.getType().equals(Material.DIRT) || r.getType().equals(Material.STONE)) {
					return true;
				}
			}
		}
		return false;
	}

	private void buildWaterfall(final Block block) {
		final BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		for (final BlockFace f : faces) {
			final Block r = block.getRelative(f);
			if (!r.isEmpty()) {
				r.setType(Material.WATER);
				return;
			}
		}
	}

}
