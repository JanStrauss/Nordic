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
package eu.over9000.nordic.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class PopulatorUtil {

	public static void placeRndSphere(final World world, final Random random, final Block handle, final Material setTo, final int radius, final Predicate<Material> whitelist) {
		final float rndOffset = random.nextFloat() * 0.25f;

		final List<Location> locations = new ArrayList<>();
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					if (x * x + y * y + z * z < radius * radius) {
						locations.add(handle.getRelative(x, y, z).getLocation());
					}
				}
			}
		}

		Collections.sort(locations, (o1, o2) -> Integer.compare(calcVecDistance(o1, handle.getLocation()), calcVecDistance(o2, handle.getLocation())));

		for (int i = 0; i < locations.size(); i++) {
			final Location location = locations.get(i);

			final float percent = (float) i / (float) locations.size();

			final Material current = world.getBlockAt(location).getType();
			if (whitelist.test(current)) {
				if (percent < 0.5f + rndOffset || random.nextBoolean()) {
					world.getBlockAt(location).setType(setTo);
				}
			}
		}
	}

	private static int calcVecDistance(final Location pos1, final Location pos2) {
		final int dist_x = calcDistance(pos1.getBlockX(), pos2.getBlockX());
		final int dist_y = calcDistance(pos1.getBlockY(), pos2.getBlockY());
		final int dist_z = calcDistance(pos1.getBlockZ(), pos2.getBlockZ());
		return dist_x * dist_x + dist_y * dist_y + dist_z * dist_z;
	}

	private static int calcDistance(final int coordinate1, final int coordinate2) {
		return Math.abs(coordinate1 - coordinate2);
	}
}
