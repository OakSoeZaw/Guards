package com.oak.Guards;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	OrthographicCamera camera;
	TiledMap map;
	OrthogonalTiledMapRenderer mapRenderer;
	TiledMapTileLayer tileLayer;

	@Override
	public void create(){
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, w, h);  // this will show half of the map
		camera.position.set(320, 320, 0);
		camera.update();

		map = new TmxMapLoader().load("Map/Map.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map);
		tileLayer = (TiledMapTileLayer) map.getLayers().get("collision");
		int cols = tileLayer.getWidth();
		int rows = tileLayer.getHeight();

		int mapWidth = map.getProperties().get("width", Integer.class);
		int mapHeight = map.getProperties().get("height", Integer.class);
		int tileWidth = map.getProperties().get("tilewidth", Integer.class);
		int tileHeight = map.getProperties().get("tileheight", Integer.class);

		System.out.println("Map tiles: " + mapWidth + "*" + mapHeight);
		System.out.println("Tiles size: " + tileWidth + "*" + tileHeight);
		System.out.println("Map tiles: " + mapWidth * tileWidth + "*" + mapHeight* tileHeight);
		
		Tile.init(rows, cols);

		for(int row = 0; row < rows; row++){
			for(int col = 0; col < cols; col++){
				int key = row * cols + col;
				boolean blocked = tileLayer.getCell(col, row) != null;
				Tile.type[key] = blocked ? (byte) 1 : (byte) 0;
			}
		}
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
