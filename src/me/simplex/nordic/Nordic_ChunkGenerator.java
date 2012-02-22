package me.simplex.nordic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.simplex.nordic.noise.Voronoi;
import me.simplex.nordic.noise.Voronoi.DistanceMetric;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

/**
 * The ChunkGenerator of this Plugin
 * @author simplex
 *
 */
public class Nordic_ChunkGenerator extends ChunkGenerator {
	
	private SimplexOctaveGenerator gen_highland	;
	private SimplexOctaveGenerator gen_base1;
	private SimplexOctaveGenerator gen_base2;
	private SimplexOctaveGenerator gen_hills;
	private SimplexOctaveGenerator gen_ground;
	
	private Voronoi voronoi_gen_base1;
	private Voronoi voronoi_gen_base2;
	private Voronoi voronoi_gen_mountains;
	
	private ArrayList<BlockPopulator> populators;
	
	private long usedSeed;
	
	/**
	 * Constructor for this Class. 
	 * @param seed
	 * @param populators
	 */
	public Nordic_ChunkGenerator(long seed, ArrayList<BlockPopulator> populators) {
		gen_highland			= new SimplexOctaveGenerator(new Random(seed), 16);
		gen_base1				= new SimplexOctaveGenerator(new Random(seed), 16);
		gen_base2				= new SimplexOctaveGenerator(new Random(seed), 16);
		gen_hills				= new SimplexOctaveGenerator(new Random(seed),  4);
		gen_ground  			= new SimplexOctaveGenerator(new Random(seed), 16);
		
		voronoi_gen_base1 		= new Voronoi(64, true, seed, 16, DistanceMetric.Squared,	4);
		voronoi_gen_base2 		= new Voronoi(64, true, seed, 16, DistanceMetric.Quadratic,	4);
		voronoi_gen_mountains 	= new Voronoi(64, true, seed, 16, DistanceMetric.Squared,	4);
		
		this.populators = populators;
		this.usedSeed = seed;
	}
	
	/**
	 * Convents coordinates to the chunk byte array index
	 * @param x coordinate
	 * @param y coordinate
	 * @param z coordinate
	 * @return the int for the chunk byte array
	 */
	private static int CoordinatesToByte(int x, int y, int z) {
		return (x * 16 + z) * 128 + y;
	}
	
	/**
	 *  Sets the Material at the given Location
	 * @param chunk_data Chunk byte array
	 * @param x coordinate
	 * @param y coordinate
	 * @param z coordinate
	 * @param material to set at the coordinates
	 */
	private static void setMaterialAt(byte[] chunk_data, int x, int y, int z, Material material){
		chunk_data[CoordinatesToByte(x, y, z)] = (byte) material.getId();
	}
	
	/**
	 * Sets the Value in the Heightmap to height if the current value is < height
	 * @param chunk_heightmap
	 * @param x
	 * @param z
	 * @param height
	 */
	private static void setHeightmapValueAt(int[][] chunk_heightmap, int x, int z, int height){
		if (height > 126) {
			height = 126;
		}
		if (height > getCurrentHeightmapValueAt(chunk_heightmap, x, z)) {
			chunk_heightmap[x][z] = height;
		}
	}
	
	/**
	 * Gives the current Height at the given location in the Heightmap
	 * @param chunk_heightmap
	 * @param x
	 * @param z
	 * @return
	 */
	private static int getCurrentHeightmapValueAt(int[][] chunk_heightmap, int x, int z){
		return chunk_heightmap[x][z];
	}

	@Override
	public byte[] generate(World world, Random random, int x_chunk, int z_chunk) {
		
		checkSeed(world.getSeed());
		
		int[][] chunk_heightmap = new int[16][16];
		byte[] chunk_data = new byte[32768];
							
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
	
	/**
	 * Writes the Value from the heightmap to the Chunk byte array
	 * @param x
	 * @param z
	 * @param chunk_data
	 * @param chunk_heightmap
	 */
	private void applyHeightMap(int x, int z, byte[] chunk_data, int[][] chunk_heightmap){
		for (int y = 0; y <= chunk_heightmap[x][z]; y++) {
			setMaterialAt(chunk_data, x, y, z, Material.STONE);
		}
	}

	/**
	 * Generates the bedrock floor
	 * @param x
	 * @param z
	 * @param chunk_data
	 */
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
	
	/**
	 * Generates some changes to the Continental Areas
	 * @param x
	 * @param z
	 * @param xChunk
	 * @param zChunk
	 * @param current_height
	 * @param gen
	 * @return generated height
	 */
	private int gen_Highlands(int x, int z, int xChunk, int zChunk, int current_height, SimplexOctaveGenerator gen) {
		if (current_height < 50) {
			return 0;
		}
		double noise = gen.noise((x+xChunk*16)/250.0f, (z+zChunk*16)/250.0f, 0.6, 0.6)*25;		 
		return (int) (34+noise);
	}
	
	/**
	 * Generates the Base-Hills
	 * @param x
	 * @param z
	 * @param xChunk
	 * @param zChunk
	 * @param current_height
	 * @param gen
	 * @return generated height
	 */
	private int gen_Hills(int x, int z, int xChunk, int zChunk, int current_height, SimplexOctaveGenerator gen) {
		double noise = gen.noise((x+xChunk*16)/250.0f, (z+zChunk*16)/250.0f, 0.6, 0.6)*10;	
		return  (int) (current_height-2+noise);
	}
	
	/**
	 * Generates the Ocean ground
	 * @param x
	 * @param z
	 * @param xChunk
	 * @param zChunk
	 * @param gen
	 * @return generated height
	 */
	private int gen_Ground(int x, int z,int xChunk, int zChunk, SimplexOctaveGenerator gen) {
		gen.setScale(1/128.0);
		double noise = gen.noise(x+xChunk*16, z+zChunk*16, 0.01, 0.5)*20;
		int limit = (int) (34+noise);
		return limit;
	}
	
	/**
	 * Generates the higher Hills
	 * @param x
	 * @param z
	 * @param xChunk
	 * @param zChunk
	 * @param current_height
	 * @param noisegen
	 * @return generated height
	 */
	private int gen_Mountains(int x, int z, int xChunk, int zChunk,int current_height, Voronoi noisegen) {
		double noise = noisegen.get((x+xChunk*16)/250.0f, (z+zChunk*16)/250.0f)*100;
		int limit = (int) (current_height+noise);
		if (limit < 30) {
			return 0;
		}
		return limit;
	}
	
	/**
	 * Generates the Continental-base
	 * @param x
	 * @param z
	 * @param xChunk
	 * @param zChunk
	 * @param gen
	 * @param noisegen
	 * @return
	 */
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
	
	/**
	 * Puts a Dirt/Grass Layer over the Chunk
	 * @param x
	 * @param z
	 * @param chunk_data
	 * @param height
	 */
	private void gen_TopLayer(int x, int z, byte[] chunk_data, int height) {
		boolean grass = true;
		if (height < 48) {
			grass = false;
		}
		Random rnd = new Random();
		if (height > 80) {
			return;
		}
		if (height > 77 && rnd.nextBoolean()) {
			return;
		}
		else {
			int soil_depth = rnd.nextInt(4);
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
	
	/**
	 * Fills the Oceans with water
	 * @param x
	 * @param z
	 * @param chunk_data
	 */
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
		return populators;
	}
	
	@Override
	public Location getFixedSpawnLocation(World world, Random random) {
		while (true) {
			int x = random.nextInt(512) - 256;
			int z = random.nextInt(512) - 256;
			Block b = world.getHighestBlockAt(x, z).getRelative(0, -1, 0);
			System.out.println("Highest Y: "+b.getY());
			if (!b.isLiquid() && b.getY() > 48 && b.getY() <= 54) {
				System.out.println("SpawnLoc = "+b.getLocation());
				return b.getLocation().add(0, 1, 0);
			}
		}
	}
	
	
	/**
	 * Sets the Noise generators to use the specified seed
	 * @param seed
	 */
	public void changeSeed(Long seed){
		gen_highland			= new SimplexOctaveGenerator(new Random(seed), 16);
		gen_base1				= new SimplexOctaveGenerator(new Random(seed), 16);
		gen_base2				= new SimplexOctaveGenerator(new Random(seed), 16);
		gen_hills				= new SimplexOctaveGenerator(new Random(seed),  4);
		gen_ground  			= new SimplexOctaveGenerator(new Random(seed), 16);
		
		voronoi_gen_base1 		= new Voronoi(64, true, seed, 16, DistanceMetric.Squared,	4);
		voronoi_gen_base2 		= new Voronoi(64, true, seed, 16, DistanceMetric.Quadratic,	4);
		voronoi_gen_mountains 	= new Voronoi(64, true, seed, 16, DistanceMetric.Squared,	4);
	}
	
	/**
	 * Checks if the Seed that is currently used by the Noise generators is the same as the given seed. 
	 * If not {@link Generator.changeSeed()} is called.
	 * @param worldSeed
	 */
	private void checkSeed(Long worldSeed){
		if (worldSeed != usedSeed) {
			changeSeed(worldSeed);
			usedSeed = worldSeed;
		}
	}
	
	

}
