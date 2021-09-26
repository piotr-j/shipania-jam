package io.piotrjastrzebski.psm.entities;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.piotrjastrzebski.psm.GameWorld;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class ProjectileEntity extends MovableEntity {
    protected static final String TAG = ProjectileEntity.class.getSimpleName();
    public int damage = 15;
    public float alive = -1;
    public boolean players;

    public ProjectileEntity (GameWorld world, float x, float y, float angle, boolean players) {
        super(world, x, y, angle);
        this.players = players;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(.25f, .05f);
        // we want higher density so they push target a bit
        Fixture fixture = body.createFixture(shape, 3);
        fixture.setFriction(0);
        fixture.setRestitution(.3f);
//        fixture.setSensor(true);

        PointLight light = new PointLight(world.rays(), 8);
        light.setSoft(false);
        light.setXray(true);
        light.setPosition(x(), y());
        light.setDistance(.7f);
        light.setContactFilter(CATEGORY_LIGHT, (short)0, CATEGORY_WALL);
        lights.add(light);

        Filter filterData = fixture.getFilterData();
        if (players) {
            light.setColor(Color.CHARTREUSE);
            filterData.categoryBits = CATEGORY_PROJECTILE_PLAYER;
            filterData.maskBits = CATEGORY_WALL | CATEGORY_ENEMY;
        } else {
            light.setColor(Color.SCARLET);
            filterData.categoryBits = CATEGORY_PROJECTILE_ENEMY;
            filterData.maskBits = CATEGORY_WALL | CATEGORY_PLAYER;
        }
        light.getColor().a = .75f;
        fixture.setFilterData(filterData);
        shape.dispose();

    }

    @Override
    protected Body createBody (float x, float y, float angle) {

        BodyDef def = new BodyDef();
        def.position.set(x, y);
        def.angle = angle;
        // kinematic?
        def.type = BodyDef.BodyType.DynamicBody;
        def.angularDamping = 0;
        def.linearDamping = 0;
        def.bullet = true;

        Body body = world.box2d().createBody(def);

        return body;
    }


    @Override
    public void update (float dt, float alpha) {
        super.update(dt, alpha);

        if (alive > 0) {
            alive -= dt;
            if (alive < 0) kill();
        }
    }

    @Override
    public void drawDebug (ShapeDrawer drawer) {
        super.drawDebug(drawer);
        float x = current.x();
        float y = current.y();
        tmp.set(1, 0).rotateRad(current.angle());
        float fx = tmp.x * .25f;
        float fy = tmp.y * .25f;


        if (players) {
            drawer.setColor(Color.CHARTREUSE);
        } else {
            drawer.setColor(Color.SCARLET);
        }
        drawer.line(x - fx, y - fy, x + fx, y + fy, .1f);

//        drawer.setColor(Color.GOLDENROD);
//        tmp.set(0, .4f).rotateRad(angle);
//        drawer.line(x + tmp.x, y + tmp.y, x + tmp.x + fx * .8f, y + tmp.y + fy * .8f, .2f);
//        tmp.set(0, -.4f).rotateRad(angle);
//        drawer.line(x + tmp.x, y + tmp.y, x + tmp.x + fx * .8f, y + tmp.y + fy * .8f, .2f);
//
//        drawer.setColor(Color.GOLD);
//        drawer.filledCircle(x, y, .5f);
//
//        tmp.set(0, .2f).rotateRad(angle);
//        drawer.setColor(Color.ROYAL);
//        drawer.filledTriangle(
//            x + tmp.x - fx * .2f, y + tmp.y - fy * .2f,
//            x - tmp.x - fx * .2f, y - tmp.y - fy * .2f,
//            x + fx * .3f, y + fy * .3f);
    }

    @Override
    public void hit (BaseEntity other, Contact contact) {
        super.hit(other, contact);
        // isHittable?
        if (other instanceof SensorEntity) return;
        if (other instanceof DoorEntity) {
            DoorEntity door = (DoorEntity)other;
            if (door.isOpen()) {
                return;
            }
        }
        other.changeHealth(-damage);
        // optional? based on weapon?
        kill();
    }
}
