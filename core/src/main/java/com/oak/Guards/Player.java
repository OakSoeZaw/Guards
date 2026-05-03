package com.oak.Guards;

import java.util.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Player{

	public Vector2 position;
	public Vector2 velocity;
	public Vector2 orientation;

	private static final float MAX_ACCELERATION = 200f;

	private static final int TILE_SIZE = 16;
	private static final float SPEED = 80f;

	private Animation<TextureRegion> idleAnimation;
	private Animation<TextureRegion> runAnimation;
	
	private float stateTime = 0f;
	public boolean moving = false;
	private boolean facingLeft = false;

	public Player(float x, float y, Texture idleTexture, Texture runTexture){
		this.position = new Vector2(x, y);
		this.velocity = new Vector2(0, 0);
		this.orientation = new Vector2(1, 0);

		TextureRegion[][] idleFrames = TextureRegion.split(idleTexture, 192, 192);
		TextureRegion[][] runFrames = TextureRegion.split(runTexture, 192, 192);

		idleAnimation = new Animation<>(0.15f, idleFrames[0]);
		runAnimation = new Animation<>(0.1f, runFrames[0]);
	}

	public void update(float delta){
		stateTime+= delta;
		moving = false;

		Vector2 input = new Vector2(0, 0);


		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
			input.x -= 1;
		}if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
			input.x += 1;
		}if(Gdx.input.isKeyPressed(Input.Keys.UP)){
			input.y += 1;
		}if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
			input.y -= 1;
		}

		if(input.len() > 0){
			input.nor().scl(SPEED);
		}
		moving = velocity.len() > 0.01f;
		if(moving) facingLeft = velocity.x < 0;

		Vector2 steering = new Vector2(input).sub(velocity);
		if(steering.len() > MAX_ACCELERATION) steering.nor().scl(MAX_ACCELERATION);

		velocity.add(steering.x * delta, steering.y *delta);
		if(velocity.len() > SPEED ) velocity.nor().scl(SPEED);

		float newX = position.x + velocity.x * delta;
		float newY = position.y + velocity.y * delta;

		int colX = (int) ((newX + ( velocity.x > 0 ? TILE_SIZE/2 : -TILE_SIZE/2)) / TILE_SIZE);
		int rowX = (int) (position.y / TILE_SIZE);
		if(Tile.passable(rowX * Tile.cols + colX)) position.x = newX;
		else velocity.x = 0;

		int colY = (int) (position.x / TILE_SIZE);
		int rowY = (int) ((newY + (velocity.y > 0 ? TILE_SIZE/2 : -TILE_SIZE/2)) / TILE_SIZE);
		if(Tile.passable(rowY * Tile.cols + colY)) position.y = newY;
		else velocity.y = 0;

		if(velocity.len() > 0.01f) orientation.set(velocity).nor();

		position.x = Math.max(0, Math.min(position.x, Tile.cols * TILE_SIZE));
		position.y = Math.max(0, Math.min(position.y, Tile.rows * TILE_SIZE));

	}
	public void draw(SpriteBatch batch){
		Animation<TextureRegion> current;
		if(moving){
			current = runAnimation;
		}else{
			current = idleAnimation;
		}

		batch.begin();
		TextureRegion frame = current.getKeyFrame(stateTime, true);
		if(facingLeft){
			batch.draw(frame, position.x + 32f, position.y - 32f, -64f, 64f);
		}else{
			batch.draw(frame, position.x - 32f, position.y - 32f, 64f, 64f);
		}
		batch.end();
	}
	public boolean isCaught(float guardX, float guardY){
		return position.dst(guardX, guardY) <= 32f;
	}
}
