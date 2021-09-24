package io.piotrjastrzebski.psm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import io.piotrjastrzebski.psm.entities.*;
import io.piotrjastrzebski.psm.map.GameMap;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GameWorld {
    protected static final String TAG = GameWorld.class.getSimpleName();

    public final static int WORLD_STEPS_PER_SECOND = 120;
    public final static float WORLD_STEP_TIME = 1f/ WORLD_STEPS_PER_SECOND;

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
                Body bodyA = contact.getFixtureA().getBody();
                Body bodyB = contact.getFixtureB().getBody();
                Object dataA = bodyA.getUserData();
                Object dataB = bodyB.getUserData();
                // everything should have one of those
                if (dataA instanceof BaseEntity && dataB instanceof BaseEntity) {
                    // do we want projectiles to affect bodies? push them back on hit. conditional?
                    BaseEntity eA = (BaseEntity)dataA;
                    BaseEntity eB = (BaseEntity)dataB;
                    eA.hit(eB, contact);
                    eB.hit(eA, contact);
                }
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

        map = new GameMap(app, this);
        playerSpawn.set(map.playerSpawn());
        createPlayer();
    }


    PlayerShipEntity player;
    private void createPlayer () {
        PlayerShipEntity prevPlayer = player;

        float angle = 90 * MathUtils.degRad;
        if (prevPlayer != null) {
            playerSpawn.set(prevPlayer.x(), prevPlayer.y());
            angle = prevPlayer.angle();
        }

        player = new PlayerShipEntity(this, playerSpawn.x, playerSpawn.y, angle);

        if (prevPlayer != null) {
            Body prevBody = prevPlayer.body();
            Body body = player.body();
            body.setLinearVelocity(prevBody.getLinearVelocity());
            body.setAngularVelocity(prevBody.getAngularVelocity());
        }

        entities.add(player);
    }

    public void spawnEnemy (float cx, float cy, String type, String tier) {
        EnemyShipEntity entity = new EnemyShipEntity(this, cx, cy, MathUtils.random(MathUtils.PI2));
        entity.health(50);
        entities.add(entity);
    }

    public void spawnBuff (float cx, float cy, String type, String tier) {
        BuffEntity entity = new BuffEntity(this, cx, cy, .75f, .75f);
        entity.extraHealth = 25;
        entities.add(entity);
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

        for (BaseEntity entity : entities) {
            entity.drawDebug(drawer);
        }
        if (true) map.renderDebug(camera, drawer);
        map.renderForeground(camera);
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
                Events.send(Events.ENTITY_KILLED, next);
                world.destroyBody(next.body());
            }
        }
    }

    private void variableUpdate (float dt, float alpha) {
        //Gdx.app.log(TAG, "Variable update");
        for (BaseEntity entity : entities) {
            entity.update(dt, alpha);
        }
        map.update(dt);
    }

    public World box2d () {
        return world;
    }

    public void addEntity (BaseEntity entity) {
        entities.add(entity);
    }

    private int ids = 0;
    public int nextEntityId () {
        return ++ids;
    }

    public GameMap map () {
        return map;
    }

    public OrthographicCamera camera () {
        return camera;
    }

    public PlayerShipEntity player () {
        return player;
    }
}
