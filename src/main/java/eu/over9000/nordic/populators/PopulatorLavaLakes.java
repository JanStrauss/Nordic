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
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

/**
 * BlockPopulator that generates lava lakes.
 *
 * @author codename_B
 */
public class PopulatorLavaLakes extends BlockPopulator {
	/**
	 * @see org.bukkit.generator.BlockPopulator#populate(org.bukkit.World,
	 * java.util.Random, org.bukkit.Chunk)
	 */
	@Override
	public void populate(final World world, final Random random, final Chunk source) {
		if (!(random.nextInt(100) < 2)) {
			return;
		}
		final ChunkSnapshot snapshot = source.getChunkSnapshot();

		final int rx16 = random.nextInt(16);
		final int rx = (source.getX() << 4) + rx16;
		final int rz16 = random.nextInt(16);
		final int rz = (source.getZ() << 4) + rz16;
		if (snapshot.getHighestBlockYAt(rx16, rz16) < 4)
			return;
		final int ry = random.nextInt(20) + 20;
		final int radius = 2 + random.nextInt(4);

		final Material solidMaterial = Material.STATIONARY_LAVA;

		final ArrayList<Block> lakeBlocks = new ArrayList<Block>();
		for (int i = -1; i < 4; i++) {
			final Vector center = new BlockVector(rx, ry - i, rz);
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					final Vector position = center.clone().add(new Vector(x, 0, z));
					if (center.distance(position) <= radius + 0.5 - i) {
						lakeBlocks.add(world.getBlockAt(position.toLocation(world)));
					}
				}
			}
		}

		// Ensure it's not air or liquid already
		lakeBlocks.stream().filter(block -> !block.isEmpty() && !block.isLiquid()).forEach(block -> {
			if (block.getY() >= ry) {
				block.setType(Material.AIR);
			} else {
				block.setType(solidMaterial);
			}
		});
	}
}
