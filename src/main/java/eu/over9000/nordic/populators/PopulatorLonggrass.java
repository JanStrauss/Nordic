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

public class PopulatorLonggrass extends BlockPopulator {

	@Override
	public void populate(final World world, final Random random, final Chunk source) {
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				final int chance = random.nextInt(100);
				if (chance < 33) {
					final Block handle = world.getHighestBlockAt(x + source.getX() * 16, z + source.getZ() * 16);
					if (handle.getRelative(BlockFace.DOWN).getType().equals(Material.GRASS)) {
						handle.setTypeIdAndData(Material.LONG_GRASS.getId(), (byte) 1, false);
					}
				}
			}
		}
	}
}
