package me.simplex.garden;

import java.util.logging.Logger;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class Garden extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEnable() {
		log.info("loaded Garden");

	}
	
	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new Generator();
	}
}
