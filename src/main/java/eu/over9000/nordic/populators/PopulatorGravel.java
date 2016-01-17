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

import eu.over9000.nordic.util.PopulatorUtil;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import java.util.EnumSet;
import java.util.Random;

public class PopulatorGravel extends BlockPopulator {


	private static final EnumSet<Material> valid = EnumSet.of(Material.DIRT, Material.STONE, Material.GRASS);


	@Override
	public void populate(final World world, final Random random, final Chunk source) {

		if (random.nextFloat() >= 0.04) {
			return;
		}

		final Block handle = world.getHighestBlockAt(random.nextInt(16) + source.getX() * 16, random.nextInt(16) + source.getZ() * 16);
		PopulatorUtil.placeRndSphere(world, random, handle, Material.GRAVEL, 4 + random.nextInt(8), valid::contains);
	}
}
