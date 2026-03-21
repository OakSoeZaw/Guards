package com.oak.Guards;

import java.util.*;

public class Dijkstra{
	public enum Status {RUNNING, FOUND, NOTFOUND};
	
	public Status status = Status.RUNNING;
	private int cols;
	private int rows;
	private int startKey;
	private int endKey;
	
	private int[] dist;
	private int[] prev;
	private boolean[] settled;
	private final PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));


	public Dijkstra(int rows, int cols,int start, int end){
		this.rows = rows;
		this.cols = cols;
		this.startKey = start;
		this.endKey = end;
		
		dist = new int[rows*cols];
		prev = new int[rows*cols];
		settled = new boolean[rows*cols];

		Arrays.fill(dist, Integer.MAX_VALUE);
		Arrays.fill(prev, -1);

		dist[startKey] = 0;
		pq.offer(new int[]{ 0, startKey});
	}

	public void step(){
		if(status != Status.RUNNING) return;
		if(pq.isEmpty()) { status = Status.NOTFOUND; return;}

		int[] top = pq.poll();
		int u = top[0], v = top[1];

		if(settled[v]) return;
		settled[v] = true;

		if(v == endKey) {status = Status.FOUND; return;}
		int vRow = v / cols;
		int vCol = v % cols;

		int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
		for(int[] dir: dirs){
			int newVRow = vRow + dir[0];
			int newVCol = vCol + dir[1];
			int newVKey = newVRow * cols + newVCol;
			if(!inBound(newVRow, newVCol)) continue;
			if(!Tile.passable(newVKey)) continue;

			int newDist = dist[v] + Tile.getWeight(newVKey);
			if(newDist< dist[newVKey]){
				dist[newVKey] = newDist;
				prev[newVKey] = v;
				pq.offer(new int[]{ newDist, newVKey});
			}
		}
	}
	public List<Integer> getPath(){
		List<Integer> path = new ArrayList<>();
		if(status != Status.FOUND) return path;
		for(int current = endKey; current != -1; current = prev[current]){
			path.add(current);
		}
		Collections.reverse(path);
		return path;
	}

	public boolean inBound(int row, int col){
		return row >=0 && row < rows && col >=0 && col < cols;
	}
	
}
