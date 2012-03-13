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
package me.simplex.nordic.populators;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

public class Populator_Flowers extends BlockPopulator {

	@Override
	public void populate(World world, Random random, Chunk source) {
		int chance = random.nextInt(100);
		if (chance < 10) {
			int flowercount = random.nextInt(3)+2;
			int type = random.nextInt(100);
			for (int t = 0; t <= flowercount; t++) {
				int flower_x = random.nextInt(15);
				int flower_z = random.nextInt(15);
				
				Block handle = world.getBlockAt(flower_x+source.getX()*16, world.getHighestBlockYAt(flower_x+source.getX()*16, flower_z+source.getZ()*16), flower_z+source.getZ()*16);
				if (handle.getRelative(BlockFace.DOWN).getType().equals(Material.GRASS)) {
					if (type < 33) {
						handle.setType(Material.RED_ROSE);
					}
					else {
						handle.setType(Material.YELLOW_FLOWER);
					}
				}
			}
		}
	}
}
