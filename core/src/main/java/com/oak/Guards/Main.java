package com.oak.Guards;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	OrthographicCamera camera;
	TiledMap map;
	OrthogonalTiledMapRenderer mapRenderer;
	TiledMapTileLayer tileLayer;
	
	SpriteBatch spriteBatch;
	ShapeRenderer shapeRenderer;

	Guard guard;
	Player player;

	Texture runGuard;
	Texture idleGuard;
	
	Texture runPlayer;
	Texture idlePlayer;
	@Override
	public void create(){
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 640, 640);  // this will show half of the map
		camera.position.set(320, 320, 0);
		camera.update();

		spriteBatch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		
		runGuard = new Texture("Warrior_Run.png");
		idleGuard = new Texture("Warrior_Idle.png");

		runPlayer = new Texture("Run.png");
		idlePlayer = new Texture("Idle.png");

		guard = new Guard(175f, 425f, idleGuard, runGuard);
		player = new Player(600f, 25f, idlePlayer, runPlayer);

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

		//testing

		for(int row = Tile.rows -1; row >=0; row --){
			for(int col = 0; col< Tile.cols; col ++){
				int key = row * Tile.cols + col;
				System.out.print(Tile.type[key] +" ");
			}
			System.out.println();
		}

	}
	@Override
	public void render(){
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
		
		float delta = Gdx.graphics.getDeltaTime();
		
		guard.update(delta, player.x, player.y);
		player.update(delta);

		camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();
		
		shapeRenderer.setProjectionMatrix(camera.combined);
		spriteBatch.setProjectionMatrix(camera.combined);
		guard.draw(shapeRenderer, spriteBatch);
		player.draw(spriteBatch);
		if(Gdx.input.isTouched()) {
    		Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
    		camera.unproject(touch);
    		System.out.println("world: " + touch.x + ", " + touch.y);
		}

		if(player.isCaught(guard.x, guard.y)){
			System.out.println("Game Over");
		}
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
