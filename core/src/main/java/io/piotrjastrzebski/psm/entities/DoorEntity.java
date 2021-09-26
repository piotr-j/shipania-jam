package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import io.piotrjastrzebski.psm.GameWorld;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class DoorEntity extends BaseEntity {
    Rectangle bounds;
    Fixture fixture;
    int doorId;

    public DoorEntity (int doorId, GameWorld world, float x, float y, float width, float height) {
        super(world, x + width/2, y + height/2, 0);
        this.doorId = doorId;
        bounds = new Rectangle(x, y, width, height);
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(width/2, height/2);
        fixture = body.createFixture(sensorShape, 1);
        sensorShape.dispose();

        fixture.setSensor(true);
    }


    @Override
    protected Body createBody (float x, float y, float angle) {
        BodyDef def = new BodyDef();
        def.position.set(x, y);
        def.angle = angle;
        def.type = BodyDef.BodyType.StaticBody;

        return world.box2d().createBody(def);
    }

    public void open() {
        fixture.setSensor(true);
    }

    public void close () {
        fixture.setSensor(false);
    }

    @Override
    public void drawDebug (ShapeDrawer drawer) {
        super.drawDebug(drawer);
        if (!fixture.isSensor()) {
            drawer.setColor(Color.ROYAL);
            drawer.filledRectangle(bounds);
        }
    }

    public int doorId () {
        return doorId;
    }
}
