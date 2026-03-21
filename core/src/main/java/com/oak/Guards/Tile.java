package com.oak.Guards;

import java.util.*;

public class Tile {
	private static final byte Passable = 0;
	private static final byte notPassable = 1;

	public static byte[] weight;
	public static byte[] type;

	public static void init(int rows, int cols){
		int total = rows * cols;
		weight = new byte[total];
		type = new byte[total];
		Arrays.fill(weight, (byte) 1);
	}
	
	public static boolean passable(int key){
		return type[key] != 1;
	}

	public static byte  getWeight(int key){
		return weight[key];
	}
}
