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
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Random;

public class PopulatorDelayed extends BlockPopulator {
	private final ArrayList<BlockPopulator> toProcess;
	private final JavaPlugin p;
	private final BukkitScheduler s;

	public PopulatorDelayed(final ArrayList<BlockPopulator> toProcess, final JavaPlugin p, final BukkitScheduler s) {
		this.toProcess = toProcess;
		this.p = p;
		this.s = s;
	}

	@Override
	public void populate(final World world, final Random random, final Chunk source) {
		s.scheduleSyncDelayedTask(p, new Runnable() {

			public void run() {
				for (final BlockPopulator p : toProcess) {
					p.populate(world, random, source);
				}

			}
		});

	}

}
