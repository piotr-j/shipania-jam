package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.physics.box2d.*;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.map.GameMapTile;

public class WallEntity extends BaseEntity {
    public GameMapTile tile;

    public WallEntity (GameWorld world, float x, float y) {
        super(world, x, y, 0);
    }

    @Override
    protected Body createBody (float x, float y, float angle) {
        BodyDef def = new BodyDef();
        def.position.set(x, y);
        def.angle = angle;
        def.type = BodyDef.BodyType.StaticBody;

        Body body = world.box2d().createBody(def);

        PolygonShape wallShape = new PolygonShape();
        wallShape.setAsBox(.5f, .5f);
        Fixture fixture = body.createFixture(wallShape, 1);
        fixture.setFriction(0);
        fixture.setRestitution(.3f);
        wallShape.dispose();

        Filter filterData = fixture.getFilterData();
        //filterData.categoryBits
        fixture.setFilterData(filterData);

        return body;
    }

    @Override
    public void hit (BaseEntity other, Contact contact) {

    }
}
