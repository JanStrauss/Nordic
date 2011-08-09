package me.simplex.garden.populators;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

public class Populator_Gravel extends BlockPopulator {

	@Override
	public void populate(World world, Random random, Chunk source) {
		int chance = random.nextInt(100);
		if (chance < 40) {
			for (int gravel_x = 0; gravel_x < 16; gravel_x++) {
				for (int gravel_z = 0; gravel_z < 16; gravel_z++) {
					Block handle = world.getBlockAt(gravel_x+source.getX()*16, 48, gravel_z+source.getZ()*16);
					if (isRelativeTo(handle, Material.WATER) || isRelativeTo(handle, Material.STATIONARY_WATER)) {
						changeBlockToGravel(handle, 0, random.nextInt(35)+10, random);
						return;
					}
				}
			}
		}
	}
	
	private boolean isRelativeTo(Block block, Material material) {
	    for (BlockFace blockFace : BlockFace.values()) {
	        if (block.getRelative(blockFace).getType().equals(material)) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private void changeBlockToGravel(Block block, int distance, int max, Random random){
		if (block.getTypeId() == 2) {
			block.setType(Material.GRAVEL);
			if (distance <= max && random.nextInt(100)< 75) {
				changeBlockToGravel(block.getRelative(BlockFace.NORTH), distance+1, max, random);
				changeBlockToGravel(block.getRelative(BlockFace.EAST), distance+1, max, random);
				changeBlockToGravel(block.getRelative(BlockFace.SOUTH), distance+1, max, random);
				changeBlockToGravel(block.getRelative(BlockFace.WEST), distance+1, max, random);
			}
		}
	}
}
