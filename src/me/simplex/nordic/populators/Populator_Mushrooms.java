package me.simplex.nordic.populators;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

public class Populator_Mushrooms extends BlockPopulator {

	@Override
	public void populate(World world, Random random, Chunk source) {
		int chance = random.nextInt(100);
		if (chance < 7) {
			int type = random.nextInt(100);
			Material mushroom;
			if (type < 33) {
				mushroom = Material.RED_MUSHROOM;
			}
			else {
				mushroom = Material.BROWN_MUSHROOM;
			}
			int mushroomcount = random.nextInt(3)+2;
			int placed = 0;
			for (int t = 0; t <= mushroomcount; t++) {
				for (int flower_x = 0; flower_x < 16; flower_x++) {
					for (int flower_z = 0; flower_z < 16; flower_z++) {
						Block handle = world.getBlockAt(flower_x+source.getX()*16, getHighestEmptyBlockYAtIgnoreTreesAndFoliage(world, flower_x+source.getX()*16, flower_z+source.getZ()*16), flower_z+source.getZ()*16);
						if (handle.getRelative(BlockFace.DOWN).getType().equals(Material.GRASS) && isRelativeTo(handle, Material.LOG) && handle.isEmpty()) {
							handle.setType(mushroom);
							placed++;
							if (placed >= mushroomcount) {
								return;	
							}
						}
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
	
	private int getHighestEmptyBlockYAtIgnoreTreesAndFoliage(World w, int x, int z){
		for (int y = 127; y >= 1; y--) {
			Block handle = w.getBlockAt(x, y-1, z);
			int id = handle.getTypeId();
			if (id != 0 &&  id != 17 && id != 18 && id != 37 && id != 38) {
				return y;
			}
		}
		return 0;
	}
}
