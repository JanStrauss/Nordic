package me.simplex.garden;

import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexNoiseGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class Generator extends ChunkGenerator {
	
	private int CoordinatesToByte(int x, int y, int z) {
		return (x * 16 + z) * 128 + y;
	}

	@Override
	public byte[] generate(World world, Random random, int x_chunk, int z_chunk) {
		SimplexOctaveGenerator gen_hills1 			=  new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
		SimplexOctaveGenerator gen_hills2  			=  new SimplexOctaveGenerator(new Random(world.getSeed()), 8);
		SimplexOctaveGenerator gen_ground  			=  new SimplexOctaveGenerator(new Random(world.getSeed()), 4);
		SimplexOctaveGenerator gen_mountains		=  new SimplexOctaveGenerator(new Random(world.getSeed()), 16);
		SimplexNoiseGenerator gen_mountains_noise  	=  new SimplexNoiseGenerator(new Random(world.getSeed()));
		
		gen_hills1.setScale(1/128.0);
		gen_hills2.setScale(1/512.0);
		gen_ground.setScale(1/256.0);
		gen_mountains.setScale(1/512.0);

		
		byte[] chunk_data = new byte[32768];
		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
			
				genFloor(x, z, chunk_data);

				genUnderground(x, z, chunk_data);

				genSurface_Mountains(x, z, chunk_data, x_chunk, z_chunk, gen_mountains, gen_mountains_noise);
				
				genSurface_Hills(x, z, chunk_data, x_chunk, z_chunk, gen_hills1, 16);
				
				genSurface_Hills(x, z, chunk_data, x_chunk, z_chunk, gen_hills2, 8);
				
				genSurface_Ground(x, z, chunk_data, x_chunk, z_chunk, gen_ground);
				
				genSurface_TopLayer(x,z, chunk_data);
				
				genWater(x, z, chunk_data);

			}
		}
		return chunk_data;
	}
	
	private void genFloor(int x, int z, byte[] chunk){
		for (int y = 0; y < 5; y++) {			
			if (y<3) { //build solid bedrock floor
				chunk[CoordinatesToByte(x, y, z)] = (byte) Material.BEDROCK.getId();
			}
			else { // build 2 block height mix floor
				int rnd = new Random().nextInt(100);
				if (rnd < 40) {
					chunk[CoordinatesToByte(x, y, z)] = (byte) Material.BEDROCK.getId();
				}
				else {
					chunk[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
				}
			}
		}
	}
	
	private void genUnderground(int x, int z, byte[] chunk_data) {
		for (int y = 5; y < 45 ; y++) {
			chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
		}
	}
	
	private void genSurface_Mountains(int x, int z, byte[] chunk_data, int xChunk, int zChunk, SimplexOctaveGenerator gen, SimplexNoiseGenerator noisegen) {
		double noise 		= gen.noise(x+xChunk*16, z+zChunk*16, 0.02, 0.02)*80;
		double mtn_noise 	= Math.abs(noisegen.noise(xChunk*16, zChunk*16))*2;
		for (int y = 5; y < 5+noise-mtn_noise; y++) {
			if (y < 127 && y >= 0) {
				chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
			}
		}
	}
	
	private void genSurface_Hills(int x, int z, byte[] chunk_data, int xChunk, int zChunk, SimplexOctaveGenerator gen, int multiply) {
		double noise = gen.noise(x+xChunk*16, z+zChunk*16, 0.4, 0.7)*multiply;
		for (int y = 45; y < 45+noise; y++) {
			if (chunk_data[CoordinatesToByte(x, y, z)] == 0) {
				chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.DIRT.getId();
			}
		}
	}
	
	private void genSurface_Ground(int x, int z, byte[] chunk_data, int xChunk, int zChunk, SimplexOctaveGenerator gen) {
		double noise = gen.noise(x+xChunk*16, z+zChunk*16, 0.05, 0.10)*10;
		for (int y = 45; y < 45+noise; y++) {
			if (chunk_data[CoordinatesToByte(x, y, z)] == 0) {
				chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.DIRT.getId();
			}
		}
	}
	
	private void genSurface_TopLayer(int x, int z, byte[] chunk_data) {
		int y = 127;
		while (true) {
			if (chunk_data[CoordinatesToByte(x, y, z)] != 0) {
				chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.GRASS.getId();
				return;
			}
			y--;
			if (y <= 0) {
				return;
			}
		}
	}
	
	private void genWater(int x, int z, byte[] chunk_data) {
		int y = 45;
		while (true) {
			if (chunk_data[CoordinatesToByte(x, y, z)] == 0) {
				chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.WATER.getId();
			}
			y--;
			if (y <= 20) {
				return;
			}
		}
	}
	
	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		return super.getDefaultPopulators(world);
	}
	
	@Override
	public boolean canSpawn(World world, int x, int z) {
		return true;
	}

}
