package com.oak.Guards;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.*;

public class Guard{
	public enum State { PATROL, CHASE, RETURN }
	public State state = State.PATROL;


	public Vector2 position;
	public Vector2 velocity;
	public Vector2 acceleration;
	public Vector2 orientation;

	private final float maxAcceleration;
	private final float maxVelocity;

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


	private List<Integer> returnPath;
	private int pathIndex;
	private boolean facingLeft;
	

	private static final int TILE_SIZE =(int) ( 16 * SCALE);

	public Guard(float x, float y, Texture idleTexture, Texture runTexture){
		this.position = new Vector2(x, y);
		this.velocity = new Vector2(0, 0);
		this.orientation = new Vector2(1, 0);
		this.acceleration = new Vector2(0 , 0);
		this.homeX = x;
		this.homeY = y;
		
		TextureRegion[][] idleFrames = TextureRegion.split(idleTexture, 192, 192);
		TextureRegion[][] runFrames = TextureRegion.split(runTexture, 192, 192);

		idleAnimation = new Animation<>(0.15f, idleFrames[0]);
		runAnimation = new Animation<>(0.1f, runFrames[0]);

		maxAcceleration = 150f;
		maxVelocity = 35f;
	}
	
	public void update(float delta, float playerX, float playerY, float playerVelX, float playerVelY){
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
					//Prediction of player position
					float dist = position.dst(playerX, playerY);
					float T = Math.min(dist / maxVelocity, 0.5f);
					float predictedX = playerX + playerVelX * T;
					float predictedY = playerY + playerVelY * T;

					Vector2 desired = new Vector2(predictedX - position.x, predictedY - position.y).nor().scl(maxVelocity);
					Vector2 steering = desired.sub(velocity);
					if(steering.len() > maxAcceleration) steering.nor().scl(maxAcceleration);
					acceleration.set(steering);
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

				float slowRadius = 20f;

				float dist = position.dst(targetX, targetY);
				float speed = (dist < slowRadius) ? maxVelocity * (dist / slowRadius) : maxVelocity;

				Vector2 desired = new Vector2(targetX - position.x, targetY - position.y).nor().scl(speed);
				Vector2 steering = desired.sub(velocity);
				if(steering.len() > maxAcceleration) steering.nor().scl(maxAcceleration);
				acceleration.set(steering);

				if(dist< 5f){
					pathIndex++;
					if(pathIndex >= returnPath.size()) {
						state = State.PATROL;
						velocity.setZero();
						acceleration.setZero();
					}
				}

				break;
		}

		velocity.add(acceleration.x*delta, acceleration.y*delta);
		if(velocity.len() > maxVelocity) velocity.nor().scl(maxVelocity);
		position.add(velocity.x * delta, velocity.y * delta);
		acceleration.setZero();

		position.x = Math.max(0, Math.min(position.x, Tile.cols * TILE_SIZE));
		position.y = Math.max(0, Math.min(position.y, Tile.rows * TILE_SIZE));

		if(velocity.len() > 0.01f){
			orientation.set(velocity).nor();
			angle = velocity.angleDeg();
		}
	}

	public boolean canSeePlayer(float playerX, float playerY){
		float distance = position.dst(playerX, playerY);
		if(distance > sightRange){
			return false;
		}

		float angleToPlayer = new Vector2(playerX - position.x, playerY - position.y).angleDeg();
		float angleDiff = angleToPlayer - angle;
		float normalizedDiff = (((angleDiff + 180) % 360 + 360) % 360) -180;
		if(Math.abs(normalizedDiff) > FOV /2) return false;

		Vector2 dir = new Vector2(playerX - position.x, playerY - position.y).nor().scl(TILE_SIZE);
		float checkX =position.x;
		float checkY = position.y;
		int steps = (int) (distance/ TILE_SIZE);
		
		for(int i = 1; i< steps; i++){
			checkX += dir.x;
			checkY += dir.y;
			int col = (int) (checkX / TILE_SIZE);
			int row = (int) (checkY / TILE_SIZE);
			if(!Tile.passable(row *Tile.cols + col)) return false;
		}

		return true;
	}

	public void startReturn(){
		int startRow = (int) (position.y / TILE_SIZE);
		int startCol = (int) (position.x / TILE_SIZE);
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

		sr.line(position.x,position.y, position.x + sightRange * (float) Math.cos(Math.toRadians(angle - FOV /2)), position.y + sightRange * (float) Math.sin(Math.toRadians(angle - FOV /2)));
		sr.line(position.x,position.y, position.x + sightRange * (float) Math.cos(Math.toRadians(angle + FOV /2)), position.y + sightRange * (float) Math.sin(Math.toRadians(angle + FOV /2)));
		sr.arc(position.x,position.y, sightRange, angle - FOV /2, FOV);
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
		if(state == State.PATROL){
    		facingLeft = Math.cos(Math.toRadians(angle)) < 0;
		} else {
			facingLeft = velocity.x < 0;
		}
		if(facingLeft){
			batch.draw(frame, position.x + 32f, position.y - 32f, -64f, 64f);
		}else{
			batch.draw(frame, position.x - 32f, position.y - 32f, 64f, 64f);
		}
		batch.end();
	}


}
