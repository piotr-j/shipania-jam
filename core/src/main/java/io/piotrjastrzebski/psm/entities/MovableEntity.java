package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.utils.Transform;
import io.piotrjastrzebski.psm.utils.Utils;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class MovableEntity extends BaseEntity {
    protected Transform start = new Transform();
    protected Transform target = new Transform();

    public MovableEntity (GameWorld world, float x, float y, float angle) {
        super(world, x, y, angle);

        start.set(current);
        target.set(start);
    }

    public void fixed () {
        Vector2 position = body.getPosition();
        target.set(position.x, position.y, body.getAngle());
        start.set(current);
    }

    public void update (float dt, float alpha) {
        Utils.interpolate(start, target, alpha, current);
    }
}
