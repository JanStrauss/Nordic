/*
 * Copyright 2012 s1mpl3x
 * 
 * This file is part of Nordic.
 * 
 * Nordic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Nordic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Nordic If not, see <http://www.gnu.org/licenses/>.
 */
package eu.over9000.nordic;

import eu.over9000.nordic.noise.Voronoi;
import eu.over9000.nordic.noise.Voronoi.DistanceMetric;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The ChunkGenerator of this Plugin
 *
 * @author simplex
 */
public class Nordic_ChunkGenerator extends ChunkGenerator {

	private SimplexOctaveGenerator gen_highland;
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
	 *
	 * @param seed
	 * @param populators
	 */
	public Nordic_ChunkGenerator(long seed, ArrayList<BlockPopulator> populators) {
		gen_highland = new SimplexOctaveGenerator(new Random(seed), 16);
		gen_base1 = new SimplexOctaveGenerator(new Random(seed), 16);
		gen_base2 = new SimplexOctaveGenerator(new Random(seed), 16);
		gen_hills = new SimplexOctaveGenerator(new Random(seed), 4);
		gen_ground = new SimplexOctaveGenerator(new Random(seed), 16);

		voronoi_gen_base1 = new Voronoi(64, true, seed, 16, DistanceMetric.Squared, 4);
		voronoi_gen_base2 = new Voronoi(64, true, seed, 16, DistanceMetric.Quadratic, 4);
		voronoi_gen_mountains = new Voronoi(64, true, seed, 16, DistanceMetric.Squared, 4);

		this.populators = populators;
		this.usedSeed = seed;
	}

	/**
	 * Sets the Material at the given Location
	 *
	 * @param chunk_data Chunk byte array
	 * @param x          coordinate
	 * @param y          coordinate
	 * @param z          coordinate
	 * @param material   to set at the coordinates
	 */
	private static void setMaterialAt(byte[][] chunk_data, int x, int y, int z, Material material) {
		int sec_id = (y >> 4);
		int yy = y & 0xF;
		if (chunk_data[sec_id] == null) {
			chunk_data[sec_id] = new byte[4096];
		}
		chunk_data[sec_id][(yy << 8) | (z << 4) | x] = (byte) material.getId();
	}

	private static Material getMaterialAt(byte[][] chunk_data, int x, int y, int z) {
		int sec_id = (y >> 4);
		int yy = y & 0xF;
		if (chunk_data[sec_id] == null) {
			return Material.AIR;
		} else {
			return Material.getMaterial(chunk_data[sec_id][(yy << 8) | (z << 4) | x]);
		}
	}

	@Override
	public byte[][] generateBlockSections(World world, Random random, int x_chunk, int z_chunk, BiomeGrid biomes) {
		checkSeed(world.getSeed());
		byte[][] result = new byte[16][];

		int currheight;

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {

				// ############################## Build the Heightmap
				// ##############################

				// Min-Underground height
				currheight = 29;

				// Base-Run 1
				currheight = Math.max(currheight, gen_Base(x, z, x_chunk, z_chunk, gen_base1, voronoi_gen_base1));

				// Base-Run 2
				currheight = Math.max(currheight, gen_Base(x, z, x_chunk, z_chunk, gen_base2, voronoi_gen_base2));

				// Underwater ground
				currheight = Math.max(currheight, gen_Ground(x, z, x_chunk, z_chunk, gen_ground));

				// Some shiny hills
				currheight = Math.max(currheight, gen_Hills(x, z, x_chunk, z_chunk, currheight, gen_hills));

				// Some mountains
				currheight = Math.max(currheight, gen_Mountains(x, z, x_chunk, z_chunk, currheight, voronoi_gen_mountains));

				// Some highlands
				currheight = Math.max(currheight, gen_Highlands(x, z, x_chunk, z_chunk, currheight, gen_highland));

				// ############################## Heightmap end
				// ##############################

				// ############################## Build the chunk
				// ##############################

				// Apply Heightmap
				applyHeightMap(x, z, result, currheight);

				// Build bedrock floor
				genFloor(x, z, result);

				// Add a layer of grass and dirt & blank mountains
				gen_TopLayer(x, z, result, currheight);

				// Put the water in
				gen_Water(x, z, result);

				// ############################## Build the chunk end
				// ##############################

				// ############################## Set the biome
				// ##############################

				biomes.setBiome(x, z, Biome.FOREST);

				// ############################## Set the biome end
				// ##############################
			}
		}
		return result;
	}

	/**
	 * Writes the Value from the heightmap to the Chunk byte array
	 *
	 * @param x
	 * @param z
	 * @param chunk_data
	 * @param chunk_heightmap
	 */
	private void applyHeightMap(int x, int z, byte[][] chunk_data, int currheight) {
		for (int y = 0; y <= currheight; y++) {
			setMaterialAt(chunk_data, x, y, z, Material.STONE);
		}
	}

	/**
	 * Generates the bedrock floor
	 *
	 * @param x
	 * @param z
	 * @param chunk_data
	 */
	private void genFloor(int x, int z, byte[][] chunk_data) {
		for (int y = 0; y < 5; y++) {
			if (y < 3) { // build solid bedrock floor
				setMaterialAt(chunk_data, x, y, z, Material.BEDROCK);
			} else { // build 2 block height mix floor
				int rnd = new Random().nextInt(100);
				if (rnd < 40) {
					setMaterialAt(chunk_data, x, y, z, Material.BEDROCK);
				} else {
					setMaterialAt(chunk_data, x, y, z, Material.STONE);
				}
			}
		}
	}

	/**
	 * Generates some changes to the Continental Areas
	 *
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
		double noise = gen.noise((x + xChunk * 16) / 250.0f, (z + zChunk * 16) / 250.0f, 0.6, 0.6) * 25;
		return (int) (34 + noise);
	}

	/**
	 * Generates the Base-Hills
	 *
	 * @param x
	 * @param z
	 * @param xChunk
	 * @param zChunk
	 * @param current_height
	 * @param gen
	 * @return generated height
	 */
	private int gen_Hills(int x, int z, int xChunk, int zChunk, int current_height, SimplexOctaveGenerator gen) {
		double noise = gen.noise((x + xChunk * 16) / 250.0f, (z + zChunk * 16) / 250.0f, 0.6, 0.6) * 10;
		return (int) (current_height - 2 + noise);
	}

	/**
	 * Generates the Ocean ground
	 *
	 * @param x
	 * @param z
	 * @param xChunk
	 * @param zChunk
	 * @param gen
	 * @return generated height
	 */
	private int gen_Ground(int x, int z, int xChunk, int zChunk, SimplexOctaveGenerator gen) {
		gen.setScale(1 / 128.0);
		double noise = gen.noise(x + xChunk * 16, z + zChunk * 16, 0.01, 0.5) * 20;
		int limit = (int) (34 + noise);
		return limit;
	}

	/**
	 * Generates the higher Hills
	 *
	 * @param x
	 * @param z
	 * @param xChunk
	 * @param zChunk
	 * @param current_height
	 * @param noisegen
	 * @return generated height
	 */
	private int gen_Mountains(int x, int z, int xChunk, int zChunk, int current_height, Voronoi noisegen) {
		double noise = noisegen.get((x + xChunk * 16) / 250.0f, (z + zChunk * 16) / 250.0f) * 100;
		int limit = (int) (current_height + noise);
		if (limit < 30) {
			return 0;
		}
		return limit;
	}

	/**
	 * Generates the Continental-base
	 *
	 * @param x
	 * @param z
	 * @param xChunk
	 * @param zChunk
	 * @param gen
	 * @param noisegen
	 * @return
	 */
	private int gen_Base(int x, int z, int xChunk, int zChunk, SimplexOctaveGenerator gen, Voronoi noisegen) {
		double noise_raw1 = gen.noise((x + xChunk * 16) / 1200.0f, (z + zChunk * 16) / 1200.0f, 0.5, 0.5) * 600;
		double noise_raw2 = noisegen.get((x + xChunk * 16) / 800.0f, (z + zChunk * 16) / 800.0f) * 500;
		double noise = noise_raw1 * 0.5 + noise_raw2 * 0.5;
		double limit = 29 + noise;
		if (limit > 55) {
			limit = 55;
		}
		return (int) limit;
	}

	/**
	 * Puts a Dirt/Grass Layer over the Chunk
	 *
	 * @param x
	 * @param z
	 * @param chunk_data
	 * @param height
	 */
	private void gen_TopLayer(int x, int z, byte[][] chunk_data, int height) {
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
		} else {
			int soil_depth = rnd.nextInt(4);
			if (grass) {
				setMaterialAt(chunk_data, x, height, z, Material.GRASS);
			} else {
				setMaterialAt(chunk_data, x, height, z, Material.DIRT);
			}
			for (int y = height - 1; y >= height - soil_depth; y--) {
				setMaterialAt(chunk_data, x, y, z, Material.DIRT);
			}
		}
	}

	/**
	 * Fills the Oceans with water
	 *
	 * @param x
	 * @param z
	 * @param chunk_data
	 */
	private void gen_Water(int x, int z, byte[][] chunk_data) {
		int y = 48;
		while (y > 29) {
			if (getMaterialAt(chunk_data, x, y, z) == Material.AIR) {
				setMaterialAt(chunk_data, x, y, z, Material.STATIONARY_WATER);
			}
			y--;
		}
	}

	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		return populators;
	}

	/**
	 * Sets the Noise generators to use the specified seed
	 *
	 * @param seed
	 */
	public void changeSeed(Long seed) {
		gen_highland = new SimplexOctaveGenerator(new Random(seed), 16);
		gen_base1 = new SimplexOctaveGenerator(new Random(seed), 16);
		gen_base2 = new SimplexOctaveGenerator(new Random(seed), 16);
		gen_hills = new SimplexOctaveGenerator(new Random(seed), 4);
		gen_ground = new SimplexOctaveGenerator(new Random(seed), 16);

		voronoi_gen_base1 = new Voronoi(64, true, seed, 16, DistanceMetric.Squared, 4);
		voronoi_gen_base2 = new Voronoi(64, true, seed, 16, DistanceMetric.Quadratic, 4);
		voronoi_gen_mountains = new Voronoi(64, true, seed, 16, DistanceMetric.Squared, 4);
	}

	/**
	 * Checks if the Seed that is currently used by the Noise generators is the
	 * same as the given seed. If not {@link Generator.changeSeed()} is called.
	 *
	 * @param worldSeed
	 */
	private void checkSeed(Long worldSeed) {
		if (worldSeed != usedSeed) {
			changeSeed(worldSeed);
			usedSeed = worldSeed;
		}
	}

}
