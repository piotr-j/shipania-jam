package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.utils.Transform;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class BaseEntity {
    public final static short CATEGORY_WALL = 1 << 0;
    public final static short CATEGORY_SENSOR = 1 << 1;
    public final static short CATEGORY_PLAYER = 1 << 2;
    public final static short CATEGORY_ENEMY = 1 << 3;
    public final static short CATEGORY_PROJECTILE_PLAYER = 1 << 4;
    public final static short CATEGORY_PROJECTILE_ENEMY = 1 << 5;

    protected final int id;
    protected final GameWorld world;
    protected static Vector2 tmp = new Vector2();
    protected Body body;
    protected Transform current = new Transform();
    // unbreakable if -1
    protected int maxHealth = -1;
    protected int health = -1;

    protected boolean pendingRemoval;

    public BaseEntity (GameWorld world, float x, float y, float angle) {
        this.world = world;
        id = world.nextEntityId();

        body = createBody(x, y, angle);
        body.setUserData(this);
        Vector2 position = body.getPosition();
        current.set(position.x, position.y, body.getAngle());
    }

    protected abstract Body createBody (float x, float y, float angle);

    public void fixed () {

    }

    public void update (float dt, float alpha) {

    }

    public void draw (TwoColorPolygonBatch batch) {

    }

    public void drawDebug (ShapeDrawer drawer) {

    }

    public boolean shouldBeRemoved () {
        return pendingRemoval;
    }

    public void kill () {
        pendingRemoval = true;
    }

    public Body body () {
        return body;
    }

    public float x () {
        return current.x();
    }

    public float y () {
        return current.y();
    }

    public float angle () {
        return current.angle();
    }

    public void hit (BaseEntity other, Contact contact) {

    }

    public void health (int health) {
        this.health = maxHealth = health;
    }

    public int health () {
        return health;
    }

    public int maxHealth () {
        return maxHealth;
    }

    public void changeHealth (int amount) {
        if (health == -1) return;
        health = MathUtils.clamp(health + amount, 0, maxHealth);
        if (health <=0) {
            kill();
        }
    }

    public boolean isAlive () {
        return !pendingRemoval;
    }

    public boolean isValid () {
        return body != null;
    }

    public void destroy (World world) {
        if (body != null) {
            world.destroyBody(body);
            body = null;
        }
    }
}
