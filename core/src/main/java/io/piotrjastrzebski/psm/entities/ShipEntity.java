package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.psm.GameWorld;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.Date;

public class ShipEntity extends MovableEntity {
    protected static final String TAG = ShipEntity.class.getSimpleName();

    // input
    protected float moveForward;
    protected float moveRight;
    protected float rotateRight;
    protected float deadzone = .1f;

    // handling
    float forwardImpulse = 5;
    float rightImpulse = 2;
    float rotateImpulse = 4;

    // we kinda want Attack or something that can track this crap?
    boolean firePrimary;
    // 3 times per second
    int firePrimaryCooldown = GameWorld.WORLD_STEPS_PER_SECOND / 6;
    int firePrimaryDelay = 0;

    boolean fireSecondary;
    // 1 times per second
    int fireSecondaryCooldown = GameWorld.WORLD_STEPS_PER_SECOND / 1;
    int fireSecondaryDelay = 0;

    public ShipEntity (GameWorld world, float x, float y, float angle) {
        super(world, x, y, angle);

        health(100);

        forwardImpulse = 10;
        rightImpulse = 5;
        rotateImpulse = 30;
    }

    @Override
    protected Body createBody (float x, float y, float angle) {

        BodyDef def = new BodyDef();
        def.position.set(x, y);
        def.angle = angle;
        def.type = BodyDef.BodyType.DynamicBody;
        def.angularDamping = 20;
        def.linearDamping = 1;

        Body body = world.box2d().createBody(def);

        CircleShape shape = new CircleShape();
        shape.setRadius(.5f);
        Fixture fixture = body.createFixture(shape, 1);
        fixture.setFriction(0);
        fixture.setRestitution(.3f);
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
    public void fixed () {
        // do we allow strafe? perhaps different strength based on direction
        tmp.set(moveForward * forwardImpulse, moveRight * rightImpulse);
        if (!tmp.isZero()) {
            tmp.rotateRad(body.getAngle());
            body.applyForceToCenter(tmp.x, tmp.y, true);
        }
        if (rotateRight != 0) {
            body.applyTorque(-rotateRight * rotateImpulse, true);
        }
        super.fixed();

        // priority? do we allow to fire both at same time?
        // use some sort of resource for secondary?
        if (firePrimaryDelay > 0) {
            firePrimaryDelay --;
        }
        if (firePrimary && firePrimaryDelay <= 0) {
            firePrimaryDelay = firePrimaryCooldown;
            firePrimary();
        }
        if (fireSecondaryDelay > 0) {
            fireSecondaryDelay --;
        }
        if (fireSecondary && fireSecondaryDelay <= 0) {
            fireSecondaryDelay = fireSecondaryCooldown;
            fireSecondary();
        }
    }

    private void firePrimary () {
        // small fast
        Gdx.app.log(TAG, "prim!");

        float x = target.x();
        float y = target.y();
        float angle = target.angle();
        tmp.set(1, 0).rotateRad(angle);
        float fx = x + tmp.x * 1.5f;
        float fy = y + tmp.y * 1.5f;

        // need to pool this crap at some point
        // friendly fire?
        ProjectileEntity entity = new ProjectileEntity(world, fx, fy, angle);
        // inherit our velocity?
        Vector2 lv = body.getLinearVelocity();
        float pvx = tmp.x * 20 + lv.x;
        float pvy = tmp.y * 20 + lv.y;
        entity.body.setLinearVelocity(pvx, pvy);

        for (Fixture fixture : entity.body.getFixtureList()) {
            Filter filter = fixture.getFilterData();

            fixture.setFilterData(filter);
        }

        world.addEntity(entity);


    }

    private void fireSecondary () {
        // large slow, charge? hold fire for a bit to charge up, dealing more damage or whatever
        Gdx.app.log(TAG, "sec!");
    }

    @Override
    public void update (float dt, float alpha) {
        super.update(dt, alpha);

        firePrimary = fireSecondary = false;

    }

    @Override
    public void drawDebug (ShapeDrawer drawer) {
        super.drawDebug(drawer);
        float x = current.x();
        float y = current.y();
        float angle = current.angle();
        tmp.set(1, 0).rotateRad(current.angle());
        float fx = tmp.x;
        float fy = tmp.y;

        drawer.setColor(Color.GOLDENROD);
        tmp.set(0, .4f).rotateRad(angle);
        drawer.line(x + tmp.x, y + tmp.y, x + tmp.x + fx * .8f, y + tmp.y + fy * .8f, .2f);
        tmp.set(0, -.4f).rotateRad(angle);
        drawer.line(x + tmp.x, y + tmp.y, x + tmp.x + fx * .8f, y + tmp.y + fy * .8f, .2f);

        drawer.setColor(Color.GOLD);
        drawer.filledCircle(x, y, .5f);

        tmp.set(0, .2f).rotateRad(angle);
        drawer.setColor(Color.ROYAL);
        drawer.filledTriangle(
            x + tmp.x - fx * .2f, y + tmp.y - fy * .2f,
            x - tmp.x - fx * .2f, y - tmp.y - fy * .2f,
            x + fx * .3f, y + fy * .3f);
    }

    protected void moveForward (float forward) {
        this.moveForward = forward;
    }

    protected void moveRight (float right) {
        this.moveRight = right;
    }

    protected void rotateRight (float rotateRight) {
        this.rotateRight = rotateRight;
    }
}
