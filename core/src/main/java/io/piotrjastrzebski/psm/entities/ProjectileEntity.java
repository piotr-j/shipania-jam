package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.*;
import io.piotrjastrzebski.psm.GameWorld;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class ProjectileEntity extends MovableEntity {
    protected static final String TAG = ProjectileEntity.class.getSimpleName();
    public int damage = 15;

    public ProjectileEntity (GameWorld world, float x, float y, float angle) {
        super(world, x, y, angle);
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

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(.25f, .05f);
        Fixture fixture = body.createFixture(shape, 1);
        fixture.setFriction(0);
        fixture.setRestitution(.3f);
//        fixture.setSensor(true);
        Filter filterData = fixture.getFilterData();

        fixture.setFilterData(filterData);
        shape.dispose();

        // make it simpler to deal with, basically cube data
        MassData massData = body.getMassData();
        massData.mass = 1;
        massData.I = 0.16666667f;
        body.setMassData(massData);

        return body;
    }


    @Override
    public void update (float dt, float alpha) {
        super.update(dt, alpha);

    }

    @Override
    public void drawDebug (ShapeDrawer drawer) {
        super.drawDebug(drawer);
        float x = current.x();
        float y = current.y();
        float angle = current.angle();
        tmp.set(1, 0).rotateRad(current.angle());
        float fx = tmp.x * .25f;
        float fy = tmp.y * .25f;


        drawer.setColor(Color.RED);
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
        other.changeHealth(-damage);
        // we might disable contact here if we dont want projectile hits to affect other
        kill();
    }
}
