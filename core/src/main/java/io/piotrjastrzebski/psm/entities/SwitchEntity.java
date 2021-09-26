package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.psm.Events;
import io.piotrjastrzebski.psm.GameWorld;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class SwitchEntity extends SensorEntity implements Telegraph {
    Rectangle bounds;
    Fixture fixture;
    public int doorId;
    boolean open;
    Array<DoorEntity> doors = new Array<>();

    public SwitchEntity (GameWorld world, float x, float y, float width, float height) {
        super(world, x + width/2, y + height/2, 1, 1);
        bounds = new Rectangle(x, y, width, height);
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(width/2, height/2);
        fixture = body.createFixture(sensorShape, 1);
        sensorShape.dispose();

        fixture.setSensor(true);

        Events.register(this, Events.GAME_RESTARTING);
    }


    @Override
    protected Body createBody (float x, float y, float angle) {
        BodyDef def = new BodyDef();
        def.position.set(x, y);
        def.angle = angle;
        def.type = BodyDef.BodyType.StaticBody;

        return world.box2d().createBody(def);
    }

    @Override
    public void hit (BaseEntity other, Contact contact) {
        super.hit(other, contact);
        if (other instanceof PlayerShipEntity) {
            if (!open) {
                open = true;
                world.openDoors(doorId);
            }
        }
    }

    @Override
    public void drawDebug (ShapeDrawer drawer) {
        super.drawDebug(drawer);
        if (open) {
            drawer.setColor(Color.GREEN);
        } else {
            drawer.setColor(Color.RED);
            for (DoorEntity door : doors) {
                drawer.line(x(), y(), door.x(), door.y(), .1f);
            }
        }
        drawer.filledCircle(x(), y(), .35f);


    }

    @Override
    public boolean handleMessage (Telegram msg) {
        switch (msg.message) {
        case Events.GAME_RESTARTING: {
            world.findDoors(doorId, doors);
        } break;
        }
        return false;
    }
}
