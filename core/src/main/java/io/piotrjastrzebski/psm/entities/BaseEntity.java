package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import io.piotrjastrzebski.psm.utils.Transform;
import io.piotrjastrzebski.psm.utils.Utils;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class BaseEntity {
    protected Body body;

    protected Transform start = new Transform();
    protected Transform target = new Transform();
    protected Transform current = new Transform();

    protected boolean pendingRemoval;

    public BaseEntity (Body body) {
        this.body = body;
        Vector2 position = body.getPosition();
        start.set(position.x, position.y, body.getAngle());
        target.set(start);
        current.set(start);

    }

    public void fixed () {
        Vector2 position = body.getPosition();
        target.set(position.x, position.y, body.getAngle());
        start.set(current);
    }

    public void update (float dt, float alpha) {
        Utils.interpolate(start, target, alpha, current);
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
}
