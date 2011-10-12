package me.simplex.nordic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import me.simplex.nordic.populators.Populator_Caves;
import me.simplex.nordic.populators.Populator_CustomTrees;
import me.simplex.nordic.populators.Populator_Delayed;
import me.simplex.nordic.populators.Populator_Flowers;
import me.simplex.nordic.populators.Populator_Gravel;
import me.simplex.nordic.populators.Populator_Lakes;
import me.simplex.nordic.populators.Populator_Lava_Lakes;
import me.simplex.nordic.populators.Populator_Longgrass;
import me.simplex.nordic.populators.Populator_Mushrooms;
import me.simplex.nordic.populators.Populator_Ores;
import me.simplex.nordic.populators.Populator_Trees;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Mainclass of Nordic
 * @author simplex
 */
public class Nordic extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	private Nordic_ChunkGenerator wgen;
	
	@Override
	public void onDisable() {
		log.info("[Nordic] disabled Nordic v"+getDescription().getVersion());
	}

	@Override
	public void onEnable() {
		log.info("[Nordic] enabled Nordic v"+getDescription().getVersion());	
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("player only command");
			return true;
		}
		Player player = (Player)sender;
		if (!player.hasPermission("nordic.command")) {
			player.sendMessage("Y U TRY COMMAND IF U HAVE NO PERMISSION?");
			return true;
		}
		if (command.getName().equalsIgnoreCase("nordic")) {
			String worldname = "world_nordic";
			long seed = new Random().nextLong();
			switch (args.length) {
			case 0:  // /nordic
				break;
			case 1: // /nordic penisland
				worldname 	= args[0];
				break;
			case 2: // /nordic penisland 666
				worldname 	= args[0];
				seed 		= buildSeed(args[1]);
				break;
			default: return false;
			}
			
			if (worldExists(worldname)) {
				player.sendMessage(ChatColor.BLUE+"[Nordic] World "+ChatColor.WHITE+worldname+ChatColor.BLUE+" already exists. Porting to this world...");
				World w = getServer().getWorld(worldname);
				player.teleport(w.getSpawnLocation());
				return true;
			}
			else {
				player.sendMessage(ChatColor.BLUE+"[Nordic] Generating world "+ChatColor.WHITE+worldname+ChatColor.BLUE+" with seed "+ChatColor.WHITE+seed+ChatColor.BLUE+"...");
				wgen = new Nordic_ChunkGenerator(seed, buildPopulators());
				World w = WorldCreator.name(worldname).environment(Environment.NORMAL).seed(seed).generator(wgen).createWorld();
				log.info("[Nordic] "+player.getName()+" created a new world: "+worldname+" with seed "+seed);
				player.sendMessage("done. Porting to the generated world");
				player.teleport(w.getSpawnLocation());
				return true;
			}
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
			wgen = new Nordic_ChunkGenerator(0, buildPopulators());
		}
		return wgen;
	}
	
	
	/**
	 * Checks if a world exists
	 * @param wname
	 * @return
	 */
	private boolean worldExists(String wname){
		List<World> worlds = getServer().getWorlds();
		for (World world : worlds) {
			if (world.getName().equalsIgnoreCase(wname)) {
				return true;
			}
		}
		return false;
	}
}

