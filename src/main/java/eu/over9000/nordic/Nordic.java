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
package eu.over9000.nordic;

import eu.over9000.nordic.populators.*;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Main class of Nordic
 *
 * @author simplex
 */
public class Nordic extends JavaPlugin {
	private final Logger log = Logger.getLogger("Minecraft");
	private NordicChunkGenerator wgen;

	private static final String WORLD_NAME = "world_nordic";
	private static final List<BlockPopulator> populators = buildPopulators();

	@Override
	public void onDisable() {
	}

	@Override
	public void onEnable() {
		wgen = new NordicChunkGenerator(populators);
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("player only command");
			return true;
		}
		final Player player = (Player) sender;
		if (!player.hasPermission("nordic.command")) {
			player.sendMessage("You don't have the permission required to use this plugin");
			return true;
		}
		if (command.getName().equalsIgnoreCase("nordic")) {
			long seed = new Random().nextLong();
			switch (args.length) {
				case 0:  // /nordic
					break;
				case 1: // /nordic 1337
					seed = buildSeed(args[0]);
					break;
				default:
					return false;
			}

			if (worldExists(WORLD_NAME)) {
				player.sendMessage(ChatColor.BLUE + "[Nordic] World " + ChatColor.WHITE + WORLD_NAME + ChatColor.BLUE + " already exists. Porting to this world...");
				final World w = getServer().getWorld(WORLD_NAME);
				player.teleport(w.getSpawnLocation());
				return true;
			} else {
				player.sendMessage(ChatColor.BLUE + "[Nordic] Generating world " + ChatColor.WHITE + WORLD_NAME + ChatColor.BLUE + " with seed " + ChatColor.WHITE + seed + ChatColor.BLUE + "...");
				final World w = WorldCreator.name(WORLD_NAME).environment(Environment.NORMAL).seed(seed).generator(wgen).createWorld();
				log.info("[Nordic] " + player.getName() + " created a new world: " + WORLD_NAME + " with seed " + seed);
				player.sendMessage("done. Porting to the generated world");
				player.teleport(w.getSpawnLocation());
				return true;
			}
		}
		return false;
	}

	/**
	 * Build a List of all Populators
	 *
	 * @return a ArrayList<BlockPopulator> that contains all populators
	 */
	private static List<BlockPopulator> buildPopulators() {
		final ArrayList<BlockPopulator> populators = new ArrayList<BlockPopulator>();
		populators.add(new PopulatorLakes());
		populators.add(new PopulatorGravel());
		populators.add(new PopulatorLavaLakes());
		populators.add(new PopulatorCaves());
		populators.add(new PopulatorOres());
		populators.add(new PopulatorCustomTrees());
		populators.add(new PopulatorTrees());
		populators.add(new PopulatorFlowers());
		populators.add(new PopulatorMushrooms());
		populators.add(new PopulatorLonggrass());
		return populators;
	}

	/**
	 * Builds a seed from a string
	 *
	 * @param s seed user input
	 * @return long seed
	 */
	private long buildSeed(final String s) {
		try {
			return Long.parseLong(s);
		} catch (final NumberFormatException e) {
			return s.hashCode();
		}
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(final String worldName, final String id) {

		if (WORLD_NAME.equals(worldName)) {
			return wgen;
		}

		return null;
	}


	/**
	 * Checks if a world exists
	 */
	private boolean worldExists(final String wname) {
		return getServer().getWorld(wname) != null;
	}
}

