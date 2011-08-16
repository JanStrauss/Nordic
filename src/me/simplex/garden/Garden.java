package me.simplex.garden;

import java.util.ArrayList;
import java.util.logging.Logger;

import me.simplex.garden.populators.Populator_Caves;
import me.simplex.garden.populators.Populator_CustomTrees;
import me.simplex.garden.populators.Populator_Delayed_Foliage;
import me.simplex.garden.populators.Populator_Flowers;
import me.simplex.garden.populators.Populator_Gravel;
import me.simplex.garden.populators.Populator_Lakes;
import me.simplex.garden.populators.Populator_Lava_Lakes;
import me.simplex.garden.populators.Populator_Longgrass;
import me.simplex.garden.populators.Populator_Mushrooms;
import me.simplex.garden.populators.Populator_Ores;
import me.simplex.garden.populators.Populator_Trees;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class Garden extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	private Generator wgen;
	
	@Override
	public void onDisable() {
		log.info("[Garden] disabled Garden v"+getDescription().getVersion());
	}

	@Override
	public void onEnable() {
		ArrayList<BlockPopulator> populators_delayed = new ArrayList<BlockPopulator>();
		populators_delayed.add(new Populator_CustomTrees());
		populators_delayed.add(new Populator_Trees());
		populators_delayed.add(new Populator_Flowers());
		populators_delayed.add(new Populator_Mushrooms());
		populators_delayed.add(new Populator_Longgrass());
		
		ArrayList<BlockPopulator> populators_main = new ArrayList<BlockPopulator>();
		populators_main.add(new Populator_Lakes());
		populators_main.add(new Populator_Gravel());
		populators_main.add(new Populator_Lava_Lakes());
		populators_main.add(new Populator_Caves());
		populators_main.add(new Populator_Ores());
		populators_main.add(new Populator_Delayed_Foliage(populators_delayed, this, getServer().getScheduler()));
		

		wgen = new Generator(1337, populators_main);
		log.info("[Garden] loading Garden v"+getDescription().getVersion());
		getServer().createWorld("world_garden", Environment.NORMAL, 1337, wgen);
		log.info("[Garden] loaded.");
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("player only command");
			return true;
		}
		Player player = (Player)sender;
		
		if (command.getName().equalsIgnoreCase("garden")) {
			World garden = getServer().getWorld("world_garden");
			player.teleport(garden.getSpawnLocation());
			return true;
		}
		return false;
	}
	
	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return wgen;
	}
}
