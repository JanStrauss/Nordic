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

public class PopulatorMushrooms extends BlockPopulator {

	@Override
	public void populate(final World world, final Random random, final Chunk source) {
		final int chance = random.nextInt(100);
		if (chance < 7) {
			final int type = random.nextInt(100);
			final Material mushroom;
			if (type < 33) {
				mushroom = Material.RED_MUSHROOM;
			} else {
				mushroom = Material.BROWN_MUSHROOM;
			}
			final int mushroomcount = random.nextInt(3) + 2;
			int placed = 0;
			for (int t = 0; t <= mushroomcount; t++) {
				for (int flower_x = 0; flower_x < 16; flower_x++) {
					for (int flower_z = 0; flower_z < 16; flower_z++) {
						final Block handle = world.getBlockAt(flower_x + source.getX() * 16, getHighestEmptyBlockYAtIgnoreTreesAndFoliage(world, flower_x + source.getX() * 16, flower_z + source.getZ() * 16), flower_z + source.getZ() * 16);
						if (handle.getRelative(BlockFace.DOWN).getType().equals(Material.GRASS) && isRelativeTo(handle, Material.LOG) && handle.isEmpty()) {
							handle.setType(mushroom);
							placed++;
							if (placed >= mushroomcount) {
								return;
							}
						}
					}
				}
			}
		}
	}

	private boolean isRelativeTo(final Block block, final Material material) {
		for (final BlockFace blockFace : BlockFace.values()) {
			if (block.getRelative(blockFace).getType().equals(material)) {
				return true;
			}
		}
		return false;
	}

	private int getHighestEmptyBlockYAtIgnoreTreesAndFoliage(final World w, final int x, final int z) {
		for (int y = 127; y >= 1; y--) {
			final Block handle = w.getBlockAt(x, y - 1, z);
			final int id = handle.getTypeId();
			if (id != 0 && id != 17 && id != 18 && id != 37 && id != 38) {
				return y;
			}
		}
		return 0;
	}
}
