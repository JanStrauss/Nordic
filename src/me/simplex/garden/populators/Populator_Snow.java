package me.simplex.garden.populators;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

public class Populator_Snow extends BlockPopulator {

	@Override
	public void populate(World world, Random random, Chunk source) {

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int chance = random.nextInt(100);
				if (chance < 33) {
					Block handle = world.getHighestBlockAt(x+source.getX()*16, z+source.getZ()*16);
					if (handle.getBiome().equals(Biome.TAIGA) || handle.getBiome().equals(Biome.TUNDRA)) {
						handle.getRelative(BlockFace.UP).setType(Material.SNOW);
					}
				}
			}
		}
	}

}
