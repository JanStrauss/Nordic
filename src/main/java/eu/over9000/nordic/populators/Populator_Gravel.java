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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class Populator_Gravel extends BlockPopulator {

	@Override
	public void populate(World world, Random random, Chunk source) {
		int chance = random.nextInt(100);
		if (chance < 40) {
			for (int gravel_x = 0; gravel_x < 16; gravel_x++) {
				for (int gravel_z = 0; gravel_z < 16; gravel_z++) {
					Block handle = world.getBlockAt(gravel_x + source.getX() * 16, 48, gravel_z + source.getZ() * 16);
					if (isRelativeTo(handle, Material.WATER) || isRelativeTo(handle, Material.STATIONARY_WATER)) {
						changeBlockToGravel(handle, 0, random.nextInt(35) + 10, random);
						return;
					}
				}
			}
		}
	}

	private boolean isRelativeTo(Block block, Material material) {
		for (BlockFace blockFace : BlockFace.values()) {
			if (block.getRelative(blockFace).getType().equals(material)) {
				return true;
			}
		}
		return false;
	}

	private void changeBlockToGravel(Block block, int distance, int max, Random random) {
		if (block.getTypeId() == 2) {
			block.setType(Material.GRAVEL);
			if (distance <= max && random.nextInt(100) < 75) {
				changeBlockToGravel(block.getRelative(BlockFace.NORTH), distance + 1, max, random);
				changeBlockToGravel(block.getRelative(BlockFace.EAST), distance + 1, max, random);
				changeBlockToGravel(block.getRelative(BlockFace.SOUTH), distance + 1, max, random);
				changeBlockToGravel(block.getRelative(BlockFace.WEST), distance + 1, max, random);
			}
		}
	}
}
