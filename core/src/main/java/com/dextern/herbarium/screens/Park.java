package com.dextern.herbarium.screens;

import static com.badlogic.gdx.net.HttpRequestBuilder.json;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.dextern.herbarium.Herb;

public class Park implements Screen, InputProcessor {
    private final Game game;
    private OrthographicCamera camera;
    private TiledMap map;
    private TiledMapRenderer renderer;
    private static final float SCALE_FACTOR=1.5f;
    private int tileWidth, mapWidth, mapHeight;
    private float speed;
    private float moveX, moveY;
    private SpriteBatch batch;
    private float stateTime;
    private float tileWidthA;
    private Texture sprite;
    private MapLayer boundsLayer;
    private MapObjects boundObjects;
    private BitmapFont font;
    private Herb selectedHerb;
    private Herb[] herbsData;

    private float initialTouchX, initialTouchY;
    private int activePointerCount;

    public Park(Game game) {
        this.game=game;
    }

    @Override
    public void show() {
        Json json = new Json();

        // Load the JSON file from the assets folder
        FileHandle file = Gdx.files.internal("data/herbsData.json");

        // Parse the JSON file into an array of GameData objects
        herbsData = json.fromJson(Herb[].class, file);


        font=new BitmapFont();
        map=new TmxMapLoader().load("tilemaps/TILEMAP/isaiah658's-Pixel-Pack/tilemap.tmx");
        boundsLayer=map.getLayers().get("obj");
        boundObjects=boundsLayer.getObjects();
        tileWidth=map.getProperties().get("tilewidth", Integer.class);
        tileWidthA=tileWidth*SCALE_FACTOR;
        mapWidth=map.getProperties().get("width", Integer.class);
        mapHeight=map.getProperties().get("height", Integer.class);

        int oX= (int) ((mapWidth*tileWidth*SCALE_FACTOR)/2);
        int oY= (int) ((mapHeight*tileWidth*SCALE_FACTOR)/2);
        speed=10*tileWidthA;

        camera=new OrthographicCamera(Gdx.graphics.getWidth()/SCALE_FACTOR, Gdx.graphics.getHeight()/SCALE_FACTOR);
        camera.position.set(oX, 0, 0);
        camera.update();
        renderer=new OrthogonalTiledMapRenderer(map, SCALE_FACTOR);

        sprite = new Texture("sp.png");
        batch=new SpriteBatch();
        stateTime=0f;


        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float newCameraX = camera.position.x + moveX * delta;
        float newCameraY = camera.position.y + moveY * delta;
        float mapWidthInPixels = mapWidth * tileWidthA;
        float mapHeightInPixels = mapHeight * tileWidthA;


        // Clamp the new camera position within the map boundaries
        newCameraX = MathUtils.clamp(newCameraX, 0, mapWidthInPixels-tileWidth);
        newCameraY = MathUtils.clamp(newCameraY, 0, mapHeightInPixels-tileWidth);
        camera.position.set(newCameraX, newCameraY, 0);

        boolean drawText=false;

        renderer.setView(camera);
        camera.update();
        renderer.render();

        stateTime+=delta;
        batch.setProjectionMatrix(camera.projection);
        batch.begin();
        batch.draw(sprite, 0, 0);
        Rectangle characterBounds = new Rectangle(
            (newCameraX - 32) / SCALE_FACTOR,  // Reverse scaling for X position
            (newCameraY - 32) / SCALE_FACTOR,  // Reverse scaling for Y position
            16 / SCALE_FACTOR,                // Reverse scaling for width
            16 / SCALE_FACTOR                 // Reverse scaling for height
        );

        selectedHerb=null;

        for(MapObject object:boundObjects) {
            if(object instanceof RectangleMapObject) {
                Rectangle rectObject=((RectangleMapObject) object).getRectangle();
                if(rectObject.overlaps(characterBounds)) {
                    try {
                        selectedHerb = findGameDataByName(herbsData, object.getName());
                        font.draw(batch, selectedHerb.EnglishName, 0, 0);
                    } catch (Exception e) {
                        System.out.println("Error 404: "+object.getName());
                    }
                }
            }
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth=width/SCALE_FACTOR;
        camera.viewportHeight=height/SCALE_FACTOR;
        camera.update();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        switch(keycode) {
            case Keys.UP:
                moveY=speed;
                break;
            case Keys.DOWN:
                moveY=-speed;
                break;
            case Keys.LEFT:
                moveX=-speed;
                break;
            case Keys.RIGHT:
                moveX=speed;
                break;
            case Keys.ENTER:
                if(selectedHerb!=null || true) {
                    game.setScreen(new ModelDisplayer(game, this, selectedHerb));
                }
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Keys.UP:
            case Keys.DOWN:
                moveY=0;
                break;
            case Keys.LEFT:
            case Keys.RIGHT:
                moveX=0;
                break;
        }
        return true;
    }

    public Herb findGameDataByName(Herb[] gameDataArray, String playerName) {
        for (Herb gameData : gameDataArray) {
            if (gameData.EnglishName.equalsIgnoreCase(playerName)) {
                return gameData;
            }
        }
        return null; // Return null if the player is not found
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Calculate how much the touch has moved
        float deltaX = initialTouchX - screenX;
        float deltaY = initialTouchY - screenY;

        // Update camera movement based on touch drag
        moveX = -deltaX * SCALE_FACTOR;
        moveY = deltaY * SCALE_FACTOR;

        // Update the camera position
        camera.position.x += moveX;
        camera.position.y += moveY;
        camera.update();

        // Reset initial touch position for the next drag event
        initialTouchX = screenX;
        initialTouchY = screenY;

        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Record initial touch position
        initialTouchX = screenX;
        initialTouchY = screenY;
        activePointerCount++;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (activePointerCount == 2 && selectedHerb != null) {
            game.setScreen(new ModelDisplayer(game, this, selectedHerb));
        }
        activePointerCount = 0;  // Reset the count after fingers are lifted
        return true;
    }


    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
