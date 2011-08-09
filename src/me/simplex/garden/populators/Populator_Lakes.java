package me.simplex.garden.populators;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

public class Populator_Lakes extends BlockPopulator {

	@Override
	public void populate(World world, Random random, Chunk source) {
		int lakesize = random.nextInt(20)+5;
		
		if (random.nextInt(100) < 5) {
			
		}
	}
}
