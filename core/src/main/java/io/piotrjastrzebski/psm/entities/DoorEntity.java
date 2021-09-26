package io.piotrjastrzebski.psm.entities;

import box2dLight.Light;
import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
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

        Filter filterData = fixture.getFilterData();
        filterData.categoryBits = CATEGORY_WALL;
        filterData.maskBits = CATEGORY_PLAYER | CATEGORY_ENEMY | CATEGORY_PROJECTILE_PLAYER | CATEGORY_PROJECTILE_ENEMY;
        filterData.set(filterData);

        fixture.setSensor(true);

        float ox = width>height?1:0;
        float oy = width<height?1:0;
        int count = width>height?MathUtils.ceil(width):MathUtils.ceil(height);
        for (int i = -count/2 + 1; i < count/2; i++) {
            PointLight light = new PointLight(world.rays(), 8);
            light.setSoft(true);
            light.setPosition(x()  + ox * i, y() + oy * i);
            light.setContactFilter(CATEGORY_LIGHT, (short)0, (short)(CATEGORY_WALL | CATEGORY_PLAYER));
            light.setDistance(2);
            light.setColor(Color.SKY);
            light.getColor().a = .75f;
            light.setActive(false);
            lights.add(light);
        }

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
        for (Light light : lights) {
            light.setActive(false);
        }

    }

    public void close () {
        fixture.setSensor(false);
        for (Light light : lights) {
            light.setActive(true);
        }
    }

    public boolean isOpen () {
        return fixture.isSensor();
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
