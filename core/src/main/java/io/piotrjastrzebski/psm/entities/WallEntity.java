package io.piotrjastrzebski.psm.entities;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
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
        filterData.categoryBits = CATEGORY_WALL;
        filterData.maskBits = CATEGORY_PLAYER | CATEGORY_ENEMY | CATEGORY_PROJECTILE_PLAYER | CATEGORY_PROJECTILE_ENEMY | CATEGORY_LIGHT;
        fixture.setFilterData(filterData);

        return body;
    }

    @Override
    public void hit (BaseEntity other, Contact contact) {

    }

    @Override
    public void health (int health) {
        super.health(health);

        PointLight light = new PointLight(world.rays(), 8);
        light.setSoft(true);
        light.setPosition(x(), y());
        light.setContactFilter(CATEGORY_LIGHT, (short)0, (short)(CATEGORY_PLAYER));
        light.setDistance(1.2f);
        light.setColor(.1f, 0, .6f, 0.45f);
        lights.add(light);
    }
}
