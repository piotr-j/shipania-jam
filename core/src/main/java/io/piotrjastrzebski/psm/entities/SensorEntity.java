package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.physics.box2d.*;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.map.GameMapRoom;
import io.piotrjastrzebski.psm.map.GameMapTile;

public class SensorEntity extends BaseEntity {
    public SensorListener listener;

    public SensorEntity (GameWorld world, float x, float y, float width, float height) {
        super(world, x + width/2, y + height/2, 0);

        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(width/2, height/2);
        Fixture fixture = body.createFixture(sensorShape, 1);
        fixture.setSensor(true);
        sensorShape.dispose();

        // TODO only player?
        Filter filterData = fixture.getFilterData();
        //filterData.categoryBits
        fixture.setFilterData(filterData);
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
        if (listener != null) listener.hit(other, contact);
    }

    public interface SensorListener {
        void hit (BaseEntity other, Contact contact);
    }
}
