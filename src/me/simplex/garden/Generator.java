package me.simplex.garden;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.simplex.garden.noise.Voronoi;
import me.simplex.garden.noise.Voronoi.DistanceMetric;
import me.simplex.garden.populators.Populator_Ores;
import me.simplex.garden.populators.Populator_Caves;
import me.simplex.garden.populators.Populator_Flowers;
import me.simplex.garden.populators.Populator_Gravel;
import me.simplex.garden.populators.Populator_Longgrass;
import me.simplex.garden.populators.Populator_Mushrooms;
import me.simplex.garden.populators.Populator_Trees;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class Generator extends ChunkGenerator {
	
	private static int CoordinatesToByte(int x, int y, int z) {
		return (x * 16 + z) * 128 + y;
	}
	
	private static void setMaterialAt(byte[] chunk_data, int x, int y, int z, Material material){
		chunk_data[CoordinatesToByte(x, y, z)] = (byte) material.getId();
	}
	
	private static void setHeightmapValueAt(int[][] chunk_heightmap, int x, int z, int height){
		if (height > 126) {
			height = 126;
		}
		if (height > getCurrentHeightmapValueAt(chunk_heightmap, x, z)) {
			chunk_heightmap[x][z] = height;
		}
	}
	
	private static int getCurrentHeightmapValueAt(int[][] chunk_heightmap, int x, int z){
		return chunk_heightmap[x][z];
	}

	@Override
	public byte[] generate(World world, Random random, int x_chunk, int z_chunk) {
		
		int[][] chunk_heightmap = new int[16][16];
		byte[] chunk_data = new byte[32768];

		SimplexOctaveGenerator gen_highland		= new SimplexOctaveGenerator(new Random(world.getSeed()), 16);
		SimplexOctaveGenerator gen_base1		= new SimplexOctaveGenerator(new Random(world.getSeed()), 16);
		SimplexOctaveGenerator gen_base2		= new SimplexOctaveGenerator(new Random(world.getSeed()), 16);
		SimplexOctaveGenerator gen_hills		= new SimplexOctaveGenerator(new Random(world.getSeed()),  4);
		SimplexOctaveGenerator gen_ground  		= new SimplexOctaveGenerator(new Random(world.getSeed()), 16);
	
		Voronoi voronoi_gen_base1 				= new Voronoi(64, true, world.getSeed(), 16, DistanceMetric.Squared,	4);
		Voronoi voronoi_gen_base2 				= new Voronoi(64, true, world.getSeed(), 16, DistanceMetric.Quadratic,	4);
		Voronoi voronoi_gen_mountains 			= new Voronoi(64, true, world.getSeed(), 16, DistanceMetric.Squared,	4);
				
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				
				// ############################## Build the Heightmap ##############################
				
				// Min-Underground height
				setHeightmapValueAt(chunk_heightmap, x, z, 29); 
				
				// Base-Run 1
				setHeightmapValueAt(chunk_heightmap, x, z, gen_Base(x, z, x_chunk,z_chunk, gen_base1, voronoi_gen_base1));
				
				// Base-Run 2
				setHeightmapValueAt(chunk_heightmap, x, z, gen_Base(x, z, x_chunk,z_chunk, gen_base2, voronoi_gen_base2));
				
				// Underwater ground
				setHeightmapValueAt(chunk_heightmap, x, z, gen_Ground(x, z, x_chunk, z_chunk, gen_ground));
				
				// Some shiny hills
				setHeightmapValueAt(chunk_heightmap, x, z, gen_Hills(x, z, x_chunk, z_chunk, getCurrentHeightmapValueAt(chunk_heightmap, x, z), gen_hills));
				
				// Some mountains
				setHeightmapValueAt(chunk_heightmap, x, z, gen_Mountains(x, z, x_chunk,z_chunk, getCurrentHeightmapValueAt(chunk_heightmap, x, z), voronoi_gen_mountains));
				
				// Some highlands
				setHeightmapValueAt(chunk_heightmap, x, z, gen_Highlands(x, z, x_chunk, z_chunk, getCurrentHeightmapValueAt(chunk_heightmap, x, z), gen_highland));
				// ############################## Heightmap end ##############################
				
				
				// ############################## Build the chunk ##############################
				
				// Apply Heightmap
				applyHeightMap(x, z, chunk_data, chunk_heightmap);
				
				// Build bedrock floor
				genFloor(x, z, chunk_data);
								
				// Add a layer of grass and dirt & blank mountains
				gen_TopLayer(x,z, chunk_data, getCurrentHeightmapValueAt(chunk_heightmap, x, z));
				
				// Put the water in
				gen_Water(x, z, chunk_data);
				
				// ############################## Build the chunk end ##############################
			}
		}
		return chunk_data;
	}
	
	private void applyHeightMap(int x, int z, byte[] chunk_data, int[][] chunk_heightmap){
		for (int y = 0; y <= chunk_heightmap[x][z]; y++) {
			setMaterialAt(chunk_data, x, y, z, Material.STONE);
		}
	}

	private void genFloor(int x, int z, byte[] chunk_data){
		for (int y = 0; y < 5; y++) {			
			if (y<3) { //build solid bedrock floor
				chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.BEDROCK.getId();
			}
			else { // build 2 block height mix floor
				int rnd = new Random().nextInt(100);
				if (rnd < 40) {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.BEDROCK.getId();
				}
				else {
					chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
				}
			}
		}
	}
	
//	private void genUnderground(int x, int z, byte[] chunk_data) {
//		for (int y = 5; y < 30 ; y++) {
//			chunk_data[CoordinatesToByte(x, y, z)] = (byte) Material.STONE.getId();
//		}
//	}
	
	private int gen_Highlands(int x, int z, int xChunk, int zChunk, int current_height, SimplexOctaveGenerator gen) {
		if (current_height < 50) {
			return 0;
		}
		double noise = gen.noise((x+xChunk*16)/250.0f, (z+zChunk*16)/250.0f, 0.6, 0.6)*25;		 
		int limit = (int) (34+noise);
		return limit;
	}
	
	private int gen_Hills(int x, int z, int xChunk, int zChunk,int current_height, SimplexOctaveGenerator gen) {
		double noise = gen.noise((x+xChunk*16)/250.0f, (z+zChunk*16)/250.0f, 0.6, 0.6)*10;		
		int limit = (int) (current_height+noise);
		return limit;
	}
	
	private int gen_Ground(int x, int z,int xChunk, int zChunk, SimplexOctaveGenerator gen) {
		gen.setScale(1/128.0);
		double noise = gen.noise(x+xChunk*16, z+zChunk*16, 0.01, 0.5)*20;
		int limit = (int) (34+noise);
		return limit;
	}
		
	private int gen_Mountains(int x, int z, int xChunk, int zChunk,int current_height, Voronoi noisegen) {
		double noise = noisegen.get((x+xChunk*16)/250.0f, (z+zChunk*16)/250.0f)*100;		
		int limit = (int) (current_height+noise);
		if (limit < 30) {
			return 0;
		}
		return limit;
	}
	
	private int gen_Base(int x, int z, int xChunk, int zChunk, SimplexOctaveGenerator gen, Voronoi noisegen) {
		double noise_raw1 = gen.noise((x+xChunk*16)/1200.0f, (z+zChunk*16)/1200.0f, 0.5,0.5)*600;		
		double noise_raw2 = noisegen.get((x+xChunk*16)/800.0f, (z+zChunk*16)/800.0f)*500;		
		double noise = noise_raw1*0.5+noise_raw2*0.5;
		double limit = 29+noise;
		if (limit > 55) {
			limit = 55;
		}
		return (int) limit;
	}
		
	private void gen_TopLayer(int x, int z, byte[] chunk_data, int height) {
		boolean grass = true;
		if (height < 48) {
			grass = false;
		}
		Random rnd = new Random();
		if (height > 75) {
			for (int y = height; y >= height-rnd.nextInt(5); y--) {
				setMaterialAt(chunk_data, x, y, z, Material.DIRT);
			}
		}
		else {
			int soil_depth = rnd.nextInt(2)+1;
			if (grass) {
				setMaterialAt(chunk_data, x, height, z, Material.GRASS);
			}
			else {
				setMaterialAt(chunk_data, x, height, z, Material.DIRT);
			}
			for (int y = height-1; y >= height-soil_depth; y--) {
				setMaterialAt(chunk_data, x, y, z, Material.DIRT);
			}
		}
	}
		
	private void gen_Water(int x, int z, byte[] chunk_data) {
		int y = 48;
		while (true) {
			if (chunk_data[CoordinatesToByte(x, y, z)] == 0) {
				setMaterialAt(chunk_data, x, y, z, Material.STATIONARY_WATER);
			}
			y--;
			if (y <= 30) {
				return;
			}
		}
	}
	
	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		ArrayList<BlockPopulator> populators = new ArrayList<BlockPopulator>();
		populators.add(new Populator_Gravel());
		populators.add(new Populator_Caves());
		populators.add(new Populator_Ores());
		populators.add(new Populator_Trees());
		populators.add(new Populator_Flowers());
		populators.add(new Populator_Mushrooms());
		populators.add(new Populator_Longgrass());
		return populators;
	}
	
	@Override
	public boolean canSpawn(World world, int x, int z) {
		return true;
	}

}
