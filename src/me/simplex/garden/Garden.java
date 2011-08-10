package me.simplex.garden;

import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class Garden extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	private Generator wgen;
	public static Garden Instance;
	
	@Override
	public void onDisable() {
		log.info("[Garden] disabled Garden v"+getDescription().getVersion());
	}

	@Override
	public void onEnable() {
		Instance = this;
		wgen = new Generator();
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
