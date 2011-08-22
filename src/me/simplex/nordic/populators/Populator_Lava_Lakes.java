package me.simplex.nordic.populators;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

/**
 * BlockPopulator that generates lava lakes.
 *
 * @author codename_B
 */
public class Populator_Lava_Lakes extends BlockPopulator {
	/**
	 * @see org.bukkit.generator.BlockPopulator#populate(org.bukkit.World,
	 *      java.util.Random, org.bukkit.Chunk)
	 */
	@Override
	public void populate(World world, Random random, Chunk source) {
		if (!(random.nextInt(100) < 2)) {
			return;
		}
		ChunkSnapshot snapshot = source.getChunkSnapshot();

		int rx16 = random.nextInt(16);
		int rx = (source.getX() << 4) + rx16;
		int rz16 = random.nextInt(16);
		int rz = (source.getZ() << 4) + rz16;
		if (snapshot.getHighestBlockYAt(rx16, rz16) < 4)
			return;
		int ry = random.nextInt(20)+20;
		int radius = 2 + random.nextInt(4);

		Material solidMaterial = Material.STATIONARY_LAVA;

		ArrayList<Block> lakeBlocks = new ArrayList<Block>();
		for (int i = -1; i < 4; i++) {
			Vector center = new BlockVector(rx, ry - i, rz);
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					Vector position = center.clone().add(new Vector(x, 0, z));
					if (center.distance(position) <= radius + 0.5 - i) {
						lakeBlocks.add(world.getBlockAt(position.toLocation(world)));
					}
				}
			}
		}

		for (Block block : lakeBlocks) {
			// Ensure it's not air or liquid already
			if (!block.isEmpty() && !block.isLiquid()) {
				if (block.getY() >= ry) {
					block.setType(Material.AIR);
				} 
				else {
					block.setType(solidMaterial);
				}
			}
		}
	}
}
