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
	
	@Override
	public void onDisable() {
		log.info("[Garden] disabled Garden v"+getDescription().getVersion());
	}

	@Override
	public void onEnable() {
		wgen = new Generator();
		log.info("[Garden] loaded Garden v"+getDescription().getVersion());
		getServer().createWorld("world_garden", Environment.NORMAL, 1337, wgen);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("player only command");
			return true;
		}
		Player player = (Player)sender;
		
		if (command.getName().equalsIgnoreCase("garden")) {
			World garden = getServer().createWorld("world_garden", Environment.NORMAL, 1337, wgen);
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
