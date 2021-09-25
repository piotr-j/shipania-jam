package io.piotrjastrzebski.psm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import io.piotrjastrzebski.psm.entities.*;
import io.piotrjastrzebski.psm.map.GameMap;
import io.piotrjastrzebski.psm.utils.Async;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GameWorld implements Telegraph {
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

    protected final Array<EnemySpawn> enemySpawns = new Array<>();

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
//        spawnPlayer();
//        spawnEnemies();
        Events.register(this, Events.GAME_RESTART_REQUEST, Events.PLAYER_KILLED);
    }


    PlayerShipEntity player;
    private void spawnPlayer () {
        PlayerShipEntity prevPlayer = player;

        float angle = 90 * MathUtils.degRad;
        if (prevPlayer != null && prevPlayer.isAlive()) {
            playerSpawn.set(prevPlayer.x(), prevPlayer.y());
            angle = prevPlayer.angle();
        }

        player = new PlayerShipEntity(this, playerSpawn.x, playerSpawn.y, angle);

        if (prevPlayer != null && prevPlayer.isAlive()) {
            Body prevBody = prevPlayer.body();
            Body body = player.body();
            body.setLinearVelocity(prevBody.getLinearVelocity());
            body.setAngularVelocity(prevBody.getAngularVelocity());
        }

        if (prevPlayer != null) {
            // keep unlocks
            player.health(prevPlayer.maxHealth());
        }

        entities.add(player);
        Events.send(Events.PLAYER_SPAWNED, player);
    }

    public void addEnemySpawn (float cx, float cy, String type, String tier) {
        enemySpawns.add(new EnemySpawn(cx, cy, type, tier));
    }

    private BaseEntity spawnEnemy (float cx, float cy, String type, String tier) {
        EnemyShipEntity entity = new EnemyShipEntity(this, cx, cy, MathUtils.random(MathUtils.PI2));
        entity.health(50);
        entities.add(entity);
        return entity;
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

        if (player != null) {
            camera.position.x = player.x();
            camera.position.y = player.y();
        } else {
            camera.position.x = playerSpawn.x;
            camera.position.y = playerSpawn.y;
        }
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
                if (next instanceof PlayerShipEntity) {
                    Events.send(Events.PLAYER_KILLED, next);
                }
                next.destroy(world);
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

    @Override
    public boolean handleMessage (Telegram msg) {
        switch (msg.message) {
        case Events.GAME_RESTART_REQUEST: {
            if (player == null) {
                spawnPlayer();
                spawnEnemies();
            }
        } break;
        case Events.PLAYER_KILLED: {
            Async.ui(() -> {
                spawnPlayer();
                spawnEnemies();
            }, 2);
        } break;
        }
        return false;
    }

    private void spawnEnemies () {
        // make sure all are gone fist
        for (EnemySpawn spawn : enemySpawns) {
            destroyEntity(spawn.entity);
        }
        for (EnemySpawn spawn : enemySpawns) {
            spawn.entity = spawnEnemy(spawn.x, spawn.y, spawn.type, spawn.tier);
        }
    }

    private void destroyEntity (BaseEntity entity) {
        if (entity == null || !entity.isValid()) return;
        entities.removeValue(entity, true);
        entity.destroy(world);
    }

    static class EnemySpawn {
        final float x;
        final float y;
        final String type;
        final String tier;
        BaseEntity entity;

        public EnemySpawn (float x, float y, String type, String tier) {

            this.x = x;
            this.y = y;
            this.type = type;
            this.tier = tier;
        }
    }
}
