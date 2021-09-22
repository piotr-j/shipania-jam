package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.piotrjastrzebski.psm.SMApp;
import io.piotrjastrzebski.psm.utils.Utils;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GameMap {
    protected static final String TAG = GameMap.class.getSimpleName();

    protected final TiledMap map;
    protected final TiledMapTileLayer background;
    protected final float backgroundParallaxX;
    protected final float backgroundParallaxY;
    protected final TiledMapTileLayer walls;
    protected final TiledMapTileLayer foreground;
    protected final World world;
    protected final GameMapTile[][] gameTiles;
    protected final OrthogonalTiledMapRenderer mapRenderer;
    protected final int mapWidth;
    protected final int mapHeight;

    // need list
    private Vector2 playerSpawn = new Vector2();

    public GameMap (SMApp app, World world) {

        map = app.assets().get("maps/test.tmx", TiledMap.class);
        this.world = world;

        mapRenderer = new OrthogonalTiledMapRenderer(map, SMApp.INV_SCALE, app.batch());

        MapLayers layers = map.getLayers();
        background = (TiledMapTileLayer)layers.get("background");
        backgroundParallaxX = background.getProperties().get("parallax-x", 1f, Float.class);
        backgroundParallaxY = background.getProperties().get("parallax-y", 1f, Float.class);
        walls = (TiledMapTileLayer)layers.get("walls");
        foreground = (TiledMapTileLayer)layers.get("foreground");

        mapWidth = walls.getWidth();
        mapHeight = walls.getHeight();
        gameTiles = new GameMapTile[mapWidth][mapHeight];

        processMap();
    }

    private void processMap () {
        // default spawn point just in case
        playerSpawn.set(20, 85);

        foreground.setOpacity(1);

        for (int x = 0; x < walls.getWidth(); x++) {
            for (int y = 0; y < walls.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = walls.getCell(x, y);
                if (cell == null) continue;
                TiledMapTile tile = cell.getTile();
                if (tile == null) continue;
                GameMapTile gt = new GameMapTile(x, y);
                gameTiles[x][y] = gt;
                MapProperties props = tile.getProperties();
                String type = props.get("type", null, String.class);

                if ("wall".equals(type)) {
                    gt.type = GameMapTileType.WALL;
                } else if ("void".equals(type)) {
                    gt.type = GameMapTileType.VOID;
                } else if ("empty".equals(type)) {
                    gt.type = GameMapTileType.EMPTY;
                } else if (type != null) {
                    Gdx.app.error(TAG, "unknown tile type: " + type + " (" + x + "," + y + ")");
                }
                int health = props.get("health", 0, Integer.class);

                // laser,rocket,bump ?
                String breakable = props.get("breakable", null, String.class);
                if (health > 0) {
                    gt.health = health;
                    gt.breakable = true;
                }
            }
        }

        PolygonShape wallShape = new PolygonShape();
        wallShape.setAsBox(.5f, .5f);
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                GameMapTile tile = gameTiles[x][y];
                if (tile == null || tile.type != GameMapTileType.WALL) continue;
                createWall(tile, wallShape);
            }
        }
        wallShape.dispose();

        MapLayers layers = map.getLayers();
        MapLayer helpers = layers.get("helpers");
        for (MapObject object : helpers.getObjects()) {
            if (object instanceof RectangleMapObject) {
                RectangleMapObject mo = (RectangleMapObject)object;
                String type = mo.getProperties().get("type", null, String.class);
                if ("room".equals(type)) {
                    // add room
                }
                //
                continue;
            }
            if (object instanceof TiledMapTileMapObject) {
                TiledMapTileMapObject mo = (TiledMapTileMapObject)object;
                int playerSpawnId = mo.getProperties().get("playerSpawn", -1, Integer.class);
                if (playerSpawnId >= 0) {
                    // add spawns to some array
                    playerSpawn.set(MathUtils.round(mo.getX()) + .5f, MathUtils.round(mo.getY()) + .5f);
                }
                continue;
            }
            Gdx.app.log(TAG, "other object: " + object);
        }
    }

    private void createWall (GameMapTile tile, PolygonShape wallShape) {
        BodyDef def = new BodyDef();
        def.position.set(tile.x + .5f, tile.y + .5f);
        def.type = BodyDef.BodyType.StaticBody;

        Body body = world.createBody(def);
        Fixture fixture = body.createFixture(wallShape, 1);
        fixture.setFriction(0);
        fixture.setRestitution(.3f);
        tile.body = body;
        body.setUserData(tile);
        Filter filterData = fixture.getFilterData();
        //filterData.categoryBits
        fixture.setFilterData(filterData);

        if (tile.health <= 0) {
            return;
        }

        // set custom collision filter?
    }

    public void renderBackground (OrthographicCamera camera) {
        mapRenderer.setView(camera);
        // we can use offset for parallax
        // ideally we would match behavior in tiled, so it is easier to preview
        // or something close at least
        float cx = camera.position.x;
        float cy = camera.position.y;
        float ox = cx - cx * backgroundParallaxX;
        float oy = cy - cy * backgroundParallaxX;
        background.setOffsetX(ox * SMApp.SCALE);
        background.setOffsetY(oy * SMApp.SCALE);
        mapRenderer.renderTileLayer(background);
        mapRenderer.renderTileLayer(walls);
    }

    public void renderForeground (OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.renderTileLayer(foreground);
    }

    public void renderDebug (OrthographicCamera camera, ShapeDrawer drawer) {
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (!Utils.visible(camera, x + .5f, y + .5f)) continue;
                GameMapTile tile = gameTiles[x][y];
                drawer.setColor(Color.BLACK);
                if (tile != null) {
                    switch (tile.type) {
                    case WALL:
                        drawer.setColor(Color.FIREBRICK);
                        if (tile.breakable) {
                            drawer.setColor(Color.GREEN);
                        }
                        break;
                    case EMPTY:
                        drawer.setColor(Color.NAVY);
                        break;
                    case VOID:
                        drawer.setColor(Color.BLACK);
                        break;
                    default:
                        drawer.setColor(Color.MAGENTA);
                    }
                }
                drawer.filledRectangle(x + .45f, y + .45f, .1f, .1f);
            }
        }
    }

    public Vector2 playerSpawn () {
        return playerSpawn;
    }
}
