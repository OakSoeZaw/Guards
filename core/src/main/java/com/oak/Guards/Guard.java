package com.oak.Guards;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.*;

public class Guard{
	public enum State { PATROL, CHASE, RETURN }
	public State state = State.PATROL;

	public float x, y;

	private float homeX, homeY;
	
	private static final float SCALE = 1f;

	private Animation<TextureRegion> idleAnimation;
	private Animation<TextureRegion> runAnimation;

	private float stateTime = 0f;

	//pov
	private float sightRange = 100f * SCALE;
	private static final float FOV = 120f; 

	private float angle = 0f;
	private float minAngle = -135f;
	private float maxAngle = 135f;
	private float turnSpeed = 45f;

	// still thinking if i still need patrolSpeed
	private float patrolSpeed = 10f * SCALE;
	private float chaseSpeed = 65f * SCALE;
	private float returnSpeed = 50f * SCALE;

	private List<Integer> returnPath;
	private int pathIndex;
	private boolean facingLeft;
	

	private static final int TILE_SIZE =(int) ( 16 * SCALE);

	public Guard(float x, float y, Texture idleTexture, Texture runTexture){
		this.x = x;
		this.y = y;
		this.homeX = x;
		this.homeY = y;
		
		TextureRegion[][] idleFrames = TextureRegion.split(idleTexture, 192, 192);
		TextureRegion[][] runFrames = TextureRegion.split(runTexture, 192, 192);

		idleAnimation = new Animation<>(0.15f, idleFrames[0]);
		runAnimation = new Animation<>(0.1f, runFrames[0]);

	}
	
	public void update(float delta, float playerX, float playerY){
		stateTime += delta;
		switch(state){
			case PATROL:
				angle += delta * turnSpeed;
				if(angle >= maxAngle){
					angle = maxAngle;
					turnSpeed = -turnSpeed;
				}else if(angle <= minAngle){
					angle = minAngle;
					turnSpeed = -turnSpeed;
				}
				if(canSeePlayer(playerX, playerY)) state = State.CHASE;
				break;

			case CHASE:
				if(!canSeePlayer(playerX, playerY)){
					startReturn();
				}else{
					angle = (float) Math.toDegrees(Math.atan2(playerY -y, playerX - x));
					float dist = (float) Math.hypot(playerX-x, playerY -y);
					x+= ((playerX-x) / dist) * chaseSpeed * delta;
					y += ((playerY - y) / dist) * chaseSpeed * delta;
				}
				break;
			case RETURN:
				if(returnPath == null || returnPath.isEmpty()){
					state = State.PATROL;
					break;
				}

				if(canSeePlayer(playerX, playerY)){
					state = State.CHASE;
					break;
				}
				int currKey = returnPath.get(pathIndex);
				int currRow = currKey / Tile.cols;
				int currCol = currKey % Tile.cols;
					
				int targetX = currCol * TILE_SIZE + TILE_SIZE / 2;
				int targetY = currRow * TILE_SIZE + TILE_SIZE / 2;

				float dist = (float) Math.hypot(x - targetX, y - targetY);
				x += ((targetX - x) / dist) * returnSpeed * delta;
				y += ((targetY - y ) / dist) * returnSpeed * delta;
				if ( dist < 2f){
					pathIndex ++;
					if( pathIndex >= returnPath.size()){
						state = State.PATROL;
					}
				}
				break;
		}
	}

	public boolean canSeePlayer(float playerX, float playerY){
		float distance = (float) Math.hypot(playerX - x, playerY - y);
		if(distance > sightRange){
			return false;
		}
		float angleToPlayer = (float) Math.toDegrees(Math.atan2(playerY - y, playerX -x));
		float angleDiff = angleToPlayer - angle;
		float normalizedDiff = (((angleDiff + 180) % 360 +360) % 360) - 180;
		if(Math.abs(normalizedDiff) > FOV /2){
			return false;
		}
		// checking if there is sth blocking the view 
		int steps = (int) (distance / TILE_SIZE);
		
		for(int i = 1; i< steps; i++){
			float t = (float) i/steps;

			float checkX = x + (playerX - x) * t;
			float checkY = y + (playerY - y) * t;
			
			int col = (int)(checkX / TILE_SIZE);
			int row = (int) (checkY/ TILE_SIZE);
			int key = row * Tile.cols + col;

			if(!Tile.passable(key)) return false;
		}

		return true;
	}

	public void startReturn(){
		int startRow = (int) (y / TILE_SIZE);
		int startCol = (int) (x / TILE_SIZE);
		int startKey = startRow * Tile.cols + startCol;
		int endRow = (int) (homeY / TILE_SIZE);
		int endCol = (int) (homeX / TILE_SIZE);
		int endKey = endRow * Tile.cols + endCol;
		Dijkstra dijkstra = new Dijkstra(Tile.rows, Tile.cols, startKey, endKey);
		
		while(dijkstra.status != Dijkstra.Status.FOUND) dijkstra.step();
		if( dijkstra.status == Dijkstra.Status.FOUND){
			returnPath = dijkstra.getPath();
			pathIndex = 0;
			state = State.RETURN;
		}else{
			state = State.PATROL;
		}
	}

	public void draw(ShapeRenderer sr, SpriteBatch batch){
		sr.begin(ShapeRenderer.ShapeType.Line);
		sr.setColor(Color.RED);

		sr.line(x,y, x + sightRange * (float) Math.cos(Math.toRadians(angle - FOV /2)), y + sightRange * (float) Math.sin(Math.toRadians(angle - FOV /2)));
		sr.line(x,y, x + sightRange * (float) Math.cos(Math.toRadians(angle + FOV /2)), y + sightRange * (float) Math.sin(Math.toRadians(angle + FOV /2)));
		sr.arc(x,y, sightRange, angle - FOV /2, FOV);
		sr.end();
		
		batch.begin();
		Animation<TextureRegion> current;
		switch(state){
			case PATROL:  current = idleAnimation; break;
    		case CHASE:   current = runAnimation;  break;
    		case RETURN:  current = runAnimation;  break;
    		default:      current = idleAnimation; break;
		}

		TextureRegion frame = current .getKeyFrame(stateTime, true);
		facingLeft =  Math.cos(Math.toRadians(angle)) < 0;
		if(facingLeft){
			batch.draw(frame, x + 32f, y - 32f, -64f, 64f);
		}else{
			batch.draw(frame, x - 32f, y - 32f, 64f, 64f);
		}
		batch.end();
	}


}
