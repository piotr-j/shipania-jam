package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.utils.Transform;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class BaseEntity {
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

    public void changeHealth (int amount) {
        if (health == -1) return;
        health = MathUtils.clamp(health + amount, 0, maxHealth);
        if (health <=0) {
            kill();
        }
    }
}
