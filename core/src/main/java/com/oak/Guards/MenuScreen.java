package com.oak.Guards;

import java.util.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class MenuScreen implements Screen {
	private Game game;
	private BitmapFont font;
	private SpriteBatch batch;

	public MenuScreen(Game game){
		this.game = game;
		font = new BitmapFont();
		batch = new SpriteBatch();
	}

	@Override
	public void render(float delta){
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		font.draw(batch, "Guards Game", 200f, 300f);
		font.draw(batch, "Press Enter to Start", 160f, 200f);
		batch.end();

		if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
			game.setScreen( new GameScreen(game));
		}
	}

	@Override
	public void dispose(){
		font.dispose();
		batch.dispose();
	}

	@Override public void show(){}
	@Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

}
