package com.oak.Guards;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Gdx;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	OrthographicCamera camera;
	TiledMap map;
	OrthogonalTiledMapRenderer mapRenderer;

	@Override
	public void create(){
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 640, 640);  // this will show half of the map
		camera.position.set(320, 320, 0);
		camera.update();

		map = new TmxMapLoader().load("Map/Map.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map);
	}
	@Override
	public void render(){
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);


		camera.update();
		mapRenderer.setView(camera);
		mapRenderer.render();
	}

	@Override
	public void resize(int width, int height){
	}

	@Override
	public void dispose(){
		map.dispose();
		mapRenderer.dispose();
	}
}
