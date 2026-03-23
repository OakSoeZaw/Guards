package com.oak.Guards;

import java.util.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Player{
	public float x, y;

	private static final int TILE_SIZE = 16;
	private static final float SPEED = 70f;

	private Animation<TextureRegion> idleAnimation;
	private Animation<TextureRegion> runAnimation;
	
	private float stateTime = 0f;
	public boolean moving = false;
	private boolean facingLeft = false;

	public Player(float x, float y, Texture idleTexture, Texture runTexture){
		this.x = x;
		this.y = y;

		TextureRegion[][] idleFrames = TextureRegion.split(idleTexture, 192, 192);
		TextureRegion[][] runFrames = TextureRegion.split(runTexture, 192, 192);

		idleAnimation = new Animation<>(0.15f, idleFrames[0]);
		runAnimation = new Animation<>(0.1f, runFrames[0]);
	}

	public void update(float delta){
		stateTime+= delta;
		moving = false;
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
			facingLeft = true;
			float newX = x - SPEED * delta;
			int col = (int) ((newX - TILE_SIZE/2) / TILE_SIZE);
			int row = (int) (y  / TILE_SIZE);
			int key = row * Tile.cols + col;
			if(Tile.passable(key)){
				x = newX;
				moving = true;
			}
		}if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
			facingLeft = false;
			float newX = x + SPEED * delta;
			int col = (int) ((newX + TILE_SIZE/2) / TILE_SIZE);
			int row = (int) (y / TILE_SIZE);
			int key = row * Tile.cols + col;
			if(Tile.passable(key)){
				x = newX;
				moving = true;
			}
		}if(Gdx.input.isKeyPressed(Input.Keys.UP)){
			float newY = y + SPEED * delta;
			int col = (int) (x / TILE_SIZE);
			int row = (int) ((newY + TILE_SIZE /2) / TILE_SIZE);
			int key = row * Tile.cols + col;
			if(Tile.passable(key)){
				y = newY;
				moving = true;
			}
		}if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
			float newY = y - SPEED * delta;
			int col = (int) (x / TILE_SIZE);
			int row = (int) ((newY - TILE_SIZE /2) / TILE_SIZE);
			int key = row * Tile.cols + col;
			if(Tile.passable(key)){
				y = newY;
				moving = true;
			}
		}

		x = Math.max(0, Math.min(x, Tile.cols * TILE_SIZE));
		y = Math.max(0, Math.min(y, Tile.rows * TILE_SIZE));

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
			batch.draw(frame, x + 32f, y - 32f, -64f, 64f);
		}else{
			batch.draw(frame, x - 32f, y - 32f, 64f, 64f);
		}
		batch.end();
	}
	public boolean isCaught(float guardX, float guardY){
		float distance = (float) Math.hypot(guardX -x, guardY -y);
		if(distance <= 32f){
			return true;
		}
		return false;
	}
}
