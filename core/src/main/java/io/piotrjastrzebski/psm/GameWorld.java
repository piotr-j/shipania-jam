package io.piotrjastrzebski.psm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import io.piotrjastrzebski.psm.entities.BaseEntity;
import io.piotrjastrzebski.psm.entities.ShipEntity;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GameWorld {
    protected static final String TAG = GameWorld.class.getSimpleName();

    private final static float WORLD_STEP_TIME = 1/120f;

    protected final SMApp app;
    protected final GameScreen gameScreen;
    protected final TwoColorPolygonBatch batch;
    protected final ShapeDrawer drawer;


    protected final World world;
    protected final Box2DDebugRenderer debugRenderer;

    protected final Array<BaseEntity> entities;

    public GameWorld (SMApp app, GameScreen gameScreen) {
        this.app = app;
        this.gameScreen = gameScreen;
        batch = app.batch;
        drawer = app.drawer;

        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        entities = new Array<>();

        createBounds();
        createPlayer();
    }

    Body groundBody;
    private void createBounds () {
        // simple bounds for testing
        float halfWidth = gameScreen.gameViewport.getWorldWidth() / 2f - 0.5f;
        float halfHeight = gameScreen.gameViewport.getWorldHeight() / 2f - 0.5f;
        ChainShape chainShape = new ChainShape();
        chainShape.createLoop(new float[] {-halfWidth, -halfHeight, halfWidth, -halfHeight,
            halfWidth, halfHeight, -halfWidth, halfHeight});
        BodyDef chainBodyDef = new BodyDef();
        chainBodyDef.type = BodyDef.BodyType.StaticBody;
        groundBody = world.createBody(chainBodyDef);
        groundBody.createFixture(chainShape, 0);
        chainShape.dispose();
    }

    ShipEntity player;
    private void createPlayer () {
        BodyDef def = new BodyDef();
        def.position.set(0, 0);
        def.angle = 0;
//        def.angle = MathUtils.PI;
        def.type = BodyDef.BodyType.DynamicBody;
        def.angularDamping = 1;
        def.linearDamping = 1;

        if (player != null) {
            Body oldBody = player.body();
            Vector2 position = oldBody.getPosition();
            def.position.set(position.x, position.y);
            def.angle = oldBody.getAngle();
        }

        Body body = world.createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(.5f);
        body.createFixture(shape, 1);
        shape.dispose();

        // make it simpler to deal with, basically cube data
        MassData massData = body.getMassData();
        massData.mass = 1;
        massData.I = 0.16666667f;
        body.setMassData(massData);

        if (player != null) {
            Body oldBody = player.body();
            body.setLinearVelocity(oldBody.getLinearVelocity());
            body.setAngularVelocity(oldBody.getAngularVelocity());
        }

        player = new ShipEntity(body);
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

        batch.enableBlending();
        batch.begin();
        for (BaseEntity entity : entities) {
            entity.draw(batch);
        }
        for (BaseEntity entity : entities) {
            entity.drawDebug(drawer);
        }
        batch.end();

        if (false) {
            debugRenderer.render(world, batch.getProjectionMatrix());
        }


        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
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
}
