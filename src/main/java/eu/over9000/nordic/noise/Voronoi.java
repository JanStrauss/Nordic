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
package eu.over9000.nordic.noise;

import java.util.Random;

/**
 * @author unknown
 */
public class Voronoi {

	private float[][][][][] grid;
	private Random r;
	private int density;
	private int size;
	private int zsize;
	private int dimensions;
	private boolean is2D;
	private DistanceMetric metric;
	private int level;

	public enum DistanceMetric {Linear, Squared, Manhattan, Quadratic, Chebyshev, Wiggly}

	public Voronoi(int size, boolean is2D, long seed, int density, DistanceMetric metric, int level) {
		zsize = (is2D ? 1 : size);
		dimensions = (is2D ? 2 : 3);
		grid = new float[size][size][zsize][density][dimensions];
		r = new Random(seed);
		this.size = size;
		this.density = density;
		this.metric = metric;
		this.level = level;
		this.is2D = is2D;
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				for (int k = 0; k < zsize; k++)
					for (int d = 0; d < density; d++)
						for (int e = 0; e < dimensions; e++)
							grid[i][j][k][d][e] = r.nextFloat();
	}

	private float distance(float[] a, int[] offset, float[] b) {
		float[] m = new float[dimensions];
		for (int i = 0; i < dimensions; i++)
			m[i] = b[i] - (a[i] + offset[i]);

		float d = 0.f;
		switch (metric) {
			case Linear:
				for (int i = 0; i < dimensions; i++)
					d += m[i] * m[i];
				return (float) Math.sqrt(d);
			case Squared: //Linear Squared (needs special care of gain) try .05f
				for (int i = 0; i < dimensions; i++)
					d += m[i] * m[i];
				return d;
			case Manhattan:
				for (int i = 0; i < dimensions; i++)
					d += Math.abs(m[i]);
				return d;
			case Chebyshev:
				for (int i = 0; i < dimensions; i++)
					d = Math.max(Math.abs(m[i]), d);
				return d;
			case Quadratic: //quadratic try .08f for gain
				for (int i = 0; i < dimensions; i++)
					for (int j = i; j < dimensions; j++)
						d += m[i] * m[j];
				return d;
			case Wiggly:
				for (int i = 0; i < dimensions; i++)
					d += Math.pow(Math.abs(m[i]), 15.f);
				return (float) Math.pow(d, 1.f / 15.f);
			default:
				return Float.POSITIVE_INFINITY;
		}
	}

	static final int order3D[][] = {
			{0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {0, 0, 1}, {-1, 0, 0}, {0, -1, 0}, {0, 0, -1},
			{1, 1, 0}, {1, 0, 1}, {0, 1, 1}, {-1, 1, 0}, {-1, 0, 1}, {0, -1, 1},
			{1, -1, 0}, {1, 0, -1}, {0, 1, -1}, {-1, -1, 0}, {-1, 0, -1}, {0, -1, -1},
			{1, 1, 1}, {-1, 1, 1}, {1, -1, 1}, {1, 1, -1}, {-1, -1, 1}, {-1, 1, -1},
			{1, -1, -1}, {-1, -1, -1}
	};
	static final int order2D[][] = {
			{0, 0}, {1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}
	};


	public float get(float xin, float yin, float zin) {
		if (is2D)
			throw new UnsupportedOperationException(
					"Cannot create 3D Voronoi basis when instantiated with is2D = true.");

		int[] cell = {fastfloor(xin), fastfloor(yin), fastfloor(zin)};
		float[] pos = {xin - cell[0], yin - cell[1], zin - cell[2]};
		for (int i = 0; i < 3; i++) cell[i] %= size;

		float distances[] = new float[level];
		for (int i = 0; i < level; i++) distances[i] = Float.MAX_VALUE;
		for (int i = 0; i < order3D.length; i++) {
			boolean possible = true;
			float farDist = distances[level - 1];
			if (farDist < Float.MAX_VALUE)
				for (int j = 0; j < 3; j++)
					if (order3D[i][j] < 0 && farDist < pos[j] ||
							order3D[i][j] > 0 && farDist < 1 - pos[j]) {
						possible = false;
						break;
					}
			if (!possible) continue;
			int cx = (order3D[i][0] + cell[0]) % size;
			int cy = (order3D[i][1] + cell[1]) % size;
			int cz = (order3D[i][2] + cell[2]) % size;
			for (int j = 0; j < density; j++) {
				float d = distance(grid[cx][cy][cz][j], order3D[i], pos);
				for (int k = 0; k < level; k++) {
					if (d < distances[k]) {
						for (int l = level - 1; l > k; l--)
							distances[l] = distances[l - 1];
						distances[k] = d;
						break;
					}
				}
			}
		}
		return distances[level - 1];
	}


	public float get(float xin, float yin) {
		if (!is2D)
			throw new UnsupportedOperationException(
					"Cannot create 2D Voronoi basis when instantiated with is2D = false.");

		int[] cell = {fastfloor(xin), fastfloor(yin)};
		float[] pos = {xin - cell[0], yin - cell[1]};
		for (int i = 0; i < 2; i++) cell[i] %= size;

		float distances[] = new float[level];
		for (int i = 0; i < level; i++) distances[i] = Float.MAX_VALUE;
		for (int i = 0; i < order2D.length; i++) {
			boolean possible = true;
			float farDist = distances[level - 1];
			if (farDist < Float.MAX_VALUE)
				for (int j = 0; j < dimensions; j++)
					if (order2D[i][j] < 0 && farDist < pos[j] ||
							order2D[i][j] > 0 && farDist < 1 - pos[j]) {
						possible = false;
						break;
					}
			if (!possible) continue;
			int cx = (order2D[i][0] + cell[0] + size) % size;
			int cy = (order2D[i][1] + cell[1] + size) % size;
			for (int j = 0; j < density; j++) {
				float d = distance(grid[cx][cy][0][j], order2D[i], pos);
				for (int k = 0; k < level; k++) {
					if (d < distances[k]) {
						for (int l = level - 1; l > k; l--)
							distances[l] = distances[l - 1];
						distances[k] = d;
						break;
					}
				}
			}
		}
		return distances[level - 1];
	}

	private int fastfloor(float x) {
		return x > 0 ? (int) x : (int) x - 1;
	}
}

