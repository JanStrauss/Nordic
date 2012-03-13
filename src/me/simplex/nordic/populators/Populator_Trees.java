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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

public class Populator_Trees extends BlockPopulator {

	@Override
	public void populate(World world, Random random, Chunk source) {
		int treecount = random.nextInt(3);
		
		for (int t = 0; t <= treecount; t++) {
			int tree_x = random.nextInt(15);
			int tree_z = random.nextInt(15);
			
			Block block = world.getHighestBlockAt(tree_x+source.getX()*16, tree_z+source.getZ()*16);
			Location high = block.getLocation();
			if (!block.getRelative(BlockFace.DOWN).getType().equals(Material.GRASS)) {
				return;
			}
			if (random.nextInt(10) < 1) {
				world.generateTree(high, TreeType.TALL_REDWOOD);
				
			}
			else {
				world.generateTree(high, TreeType.REDWOOD);
			}
		}
	}
}
