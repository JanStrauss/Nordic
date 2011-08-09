package me.simplex.garden.populators;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

public class Populator_Longgrass extends BlockPopulator {

	@Override
	public void populate(World world, Random random, Chunk source) {

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int chance = random.nextInt(100);
				if (chance < 33) {
					Block handle = world.getBlockAt(x+source.getX()*16, world.getHighestBlockYAt(x+source.getX()*16+1, z+source.getZ()*16), z+source.getZ()*16);
					if (handle.getRelative(BlockFace.DOWN).getType().equals(Material.GRASS)) {
						handle.setTypeIdAndData(Material.LONG_GRASS.getId(), (byte) 1, false);
					}
				}
			}
		}
	}

}
