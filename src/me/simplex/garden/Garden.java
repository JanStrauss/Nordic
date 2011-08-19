package me.simplex.garden;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import me.simplex.garden.populators.Populator_Caves;
import me.simplex.garden.populators.Populator_CustomTrees;
import me.simplex.garden.populators.Populator_Delayed;
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

/**
 * Mainclass of Garden
 * @author simplex
 */
public class Garden extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	private Generator wgen;
	
	@Override
	public void onDisable() {
		log.info("[Garden] disabled Garden v"+getDescription().getVersion());
	}

	@Override
	public void onEnable() {
		log.info("[Garden] enabled Garden v"+getDescription().getVersion());	
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("player only command");
			return true;
		}
		Player player = (Player)sender;
		
		if (command.getName().equalsIgnoreCase("garden")) {
			String worldname = "world_garden";
			long seed = new Random().nextLong();
			switch (args.length) {
			case 0:  // /garden
				break;
			case 1: // /garden penisland
				worldname 	= args[0];
				break;
			case 2: // /garden penisland 666
				worldname 	= args[0];
				seed 		= buildSeed(args[1]);
				break;
			default: return false;
			}
			
			wgen = new Generator(seed, buildPopulators());
			World garden = getServer().createWorld(worldname, Environment.NORMAL, seed, wgen);
			player.teleport(garden.getSpawnLocation());
			return true;
		}
		return false;
	}
	
	/**
	 * Build a List of all Populators
	 * @return a ArrayList<BlockPopulator> that contains all populators for a garden world
	 */
	private ArrayList<BlockPopulator> buildPopulators(){
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
		populators_main.add(new Populator_Delayed(populators_delayed, this, getServer().getScheduler()));
		
		return populators_main;
	}
	
	/**
	 * Builds a seed from a string
	 * 
	 * @param String seed user input
	 * @return long seed
	 */
	private long buildSeed(String s){
		long ret;
		try {
			ret = Long.parseLong(s);
		} catch (NumberFormatException e) {
			ret = s.hashCode();
		}
		return ret;
	}
	
	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		if (wgen == null) {
			wgen = new Generator(0, buildPopulators());
		}
		return wgen;
	}
}

