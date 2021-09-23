package io.piotrjastrzebski.psm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import io.piotrjastrzebski.psm.entities.BaseEntity;
import io.piotrjastrzebski.psm.entities.ShipEntity;
import io.piotrjastrzebski.psm.map.GameMap;
import io.piotrjastrzebski.psm.map.GameMapTile;
import io.piotrjastrzebski.psm.map.GameMapTileType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GameWorld {
    protected static final String TAG = GameWorld.class.getSimpleName();

    private final static float WORLD_STEP_TIME = 1/120f;

    protected final SMApp app;
    protected final GameScreen gameScreen;
    protected final TwoColorPolygonBatch batch;
    protected final ShapeDrawer drawer;
    protected final OrthographicCamera camera;

    protected final World world;
    protected final Box2DDebugRenderer debugRenderer;

    protected final Array<BaseEntity> entities;
    protected final GameMap map;
    protected final Vector2 playerSpawn = new Vector2();

    public GameWorld (SMApp app, GameScreen gameScreen) {
        this.app = app;
        this.gameScreen = gameScreen;
        camera = (OrthographicCamera)gameScreen.gameViewport.getCamera();
        batch = app.batch;
        drawer = app.drawer;

        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact (Contact contact) {

            }

            @Override
            public void endContact (Contact contact) {

            }

            @Override
            public void preSolve (Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve (Contact contact, ContactImpulse impulse) {

            }
        });
        debugRenderer = new Box2DDebugRenderer();
        entities = new Array<>();

        map = new GameMap(app, world);
        playerSpawn.set(map.playerSpawn());
        createPlayer();
    }


    ShipEntity player;
    private void createPlayer () {
        ShipEntity prevPlayer = player;

        float angle = 90 * MathUtils.degRad;
        if (prevPlayer != null) {
            playerSpawn.set(prevPlayer.x(), prevPlayer.y());
            angle = prevPlayer.angle();
        }

        player = new ShipEntity(this, playerSpawn.x, playerSpawn.y, angle);

        if (prevPlayer != null) {
            Body prevBody = prevPlayer.body();
            Body body = player.body();
            body.setLinearVelocity(prevBody.getLinearVelocity());
            body.setAngularVelocity(prevBody.getAngularVelocity());
        }

        entities.add(player);
    }

    float accumulator;
    public void render (float dt) {
        accumulator += dt;

        while (WORLD_STEP_TIME < accumulator) {
            fixedUpdate();
            accumulator -= WORLD_STEP_TIME;
        }
        variableUpdate(dt, accumulator / WORLD_STEP_TIME);

        camera.position.x = player.x();
        camera.position.y = player.y();
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.enableBlending();
        batch.begin();

        map.renderBackground(camera);
        for (BaseEntity entity : entities) {
            entity.draw(batch);
        }
        map.renderForeground(camera);

        for (BaseEntity entity : entities) {
            entity.drawDebug(drawer);
        }
        if (true) map.renderDebug(camera, drawer);
        batch.end();

        if (false) {
            debugRenderer.render(world, batch.getProjectionMatrix());
        }


        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            Gdx.app.log(TAG, "Recreate player");
            if (player != null) player.kill();
            createPlayer();
        }
    }

    private void fixedUpdate () {
        //Gdx.app.log(TAG, "Fixed update");
        world.step(WORLD_STEP_TIME, 7, 3);
        Array.ArrayIterator<BaseEntity> it = entities.iterator();
        while (it.hasNext()) {
            BaseEntity next = it.next();
            next.fixed();
            if (next.shouldBeRemoved()) {
                it.remove();
                world.destroyBody(next.body());
            }
        }
    }

    private void variableUpdate (float dt, float alpha) {
        //Gdx.app.log(TAG, "Variable update");
        for (BaseEntity entity : entities) {
            entity.update(dt, alpha);
        }
    }

    public World box2d () {
        return world;
    }
}
