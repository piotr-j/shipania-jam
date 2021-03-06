package io.piotrjastrzebski.psm.entities;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.utils.Utils;
import space.earlygrey.shapedrawer.ShapeDrawer;

import static com.badlogic.gdx.math.MathUtils.PI;
import static com.badlogic.gdx.math.MathUtils.PI2;

public class ShipEntity extends MovableEntity {
    protected static final String TAG = ShipEntity.class.getSimpleName();

    // input
    protected float moveForward;
    protected float moveRight;
    protected float rotateRight;

    // handling
    float forwardImpulse = 5;
    float rightImpulse = 2;
    float maxAngularVelocity = 180 * MathUtils.degRad;

    // we kinda want Attack or something that can track this crap?
    boolean firePrimary;
    // 3 times per second
    int firePrimaryCooldown = GameWorld.WORLD_STEPS_PER_SECOND / 6;
    int firePrimaryDelay = 0;
    int primaryDamage = 15;
    float primaryAliveTime = -1;
    float primaryVelocity = 5;

    boolean fireSecondary;
    // 1 times per second
    int fireSecondaryCooldown = GameWorld.WORLD_STEPS_PER_SECOND / 1;
    int fireSecondaryDelay = 0;

    boolean isPlayer = false;

    // direction
    Vector2 lookAt = new Vector2();

    public ShipEntity (GameWorld world, float x, float y, float angle, boolean isPlayer) {
        super(world, x, y, angle);
        this.isPlayer = isPlayer;
        health(100);


        forwardImpulse = 10;
        rightImpulse = 10;

        createShipFixtures();
    }

    protected void createShipFixtures () {
        CircleShape shape = new CircleShape();
        shape.setRadius(.5f);
        Fixture fixture = body.createFixture(shape, 1);
        fixture.setFriction(0);
        fixture.setRestitution(.3f);
        Filter filterData = fixture.getFilterData();
        if (isPlayer) {
            filterData.categoryBits = CATEGORY_PLAYER;
            filterData.maskBits = CATEGORY_WALL | CATEGORY_ENEMY | CATEGORY_PROJECTILE_ENEMY | CATEGORY_SENSOR;
        } else {
            filterData.categoryBits = CATEGORY_ENEMY;
            filterData.maskBits = CATEGORY_WALL | CATEGORY_PLAYER | CATEGORY_PROJECTILE_PLAYER;
        }

        fixture.setFilterData(filterData);
        shape.dispose();

        // make it simpler to deal with, basically cube data
        MassData massData = body.getMassData();
        massData.mass = 1;
        massData.I = 0.16666667f;
        body.setMassData(massData);

        PointLight light = new PointLight(world.rays(), 32);
        light.setSoft(true);
        light.setPosition(x(), y());
        if (isPlayer) {
            light.setContactFilter(CATEGORY_LIGHT, (short)0, (short)(CATEGORY_WALL | CATEGORY_ENEMY));
            light.setDistance(5);
            light.setColor(Color.GOLD);
        } else {
            light.setContactFilter(CATEGORY_LIGHT, (short)0, (short)(CATEGORY_WALL | CATEGORY_PLAYER));
            light.setDistance(4);
            light.setColor(Color.SCARLET);
        }
        light.getColor().a = .75f;
        lights.add(light);
    }

    @Override
    protected Body createBody (float x, float y, float angle) {

        BodyDef def = new BodyDef();
        def.position.set(x, y);
        def.angle = angle;
        def.type = BodyDef.BodyType.DynamicBody;
        def.angularDamping = 20;
        def.linearDamping = 1;
        return world.box2d().createBody(def);
    }

    @Override
    public void fixed () {
        // do we do this after super.fixed()?
        if (!lookAt.isZero()) {
            float angle = Utils.sanitizeAngle(body.getAngle());
            float targetAngle = Utils.sanitizeAngle(lookAt.angleRad());
            // this helps with body oscillating when reaching target
            float nextAngle = angle + body.getAngularVelocity() / GameWorld.WORLD_STEPS_PER_SECOND;
            // from MathUtils.lerpAngle, get shorter direction
            float delta = ((targetAngle - nextAngle + PI2 + PI) % PI2) - PI;
            float desiredAngularVelocity = delta * GameWorld.WORLD_STEPS_PER_SECOND;

            desiredAngularVelocity = Math.min(maxAngularVelocity, Math.max(-maxAngularVelocity, desiredAngularVelocity));

            float torque = body.getInertia() * desiredAngularVelocity / GameWorld.WORLD_STEP_TIME;
            body.applyTorque(torque, true);
        }
        // do we allow strafe? perhaps different strength based on direction
        tmp.set(moveForward, moveRight).limit2(1);
        if (!tmp.isZero()) {
            tmp.x *= forwardImpulse;
            tmp.y *= rightImpulse;
            tmp.rotateRad(Utils.sanitizeAngle(body.getAngle()));
            body.applyForceToCenter(tmp.x, tmp.y, true);
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

    protected void firePrimary () {
        // small fast
        float angle = target.angle();
        firePrimary(angle, 1.5f);
    }

    protected void firePrimary (float angle, float offset) {
        float x = target.x();
        float y = target.y();
        tmp.set(1, 0).rotateRad(angle);
        float fx = x + tmp.x * offset;
        float fy = y + tmp.y * offset;

        // need to pool this crap at some point
        // friendly fire?
        ProjectileEntity entity = new ProjectileEntity(world, fx, fy, angle, isPlayer);
        entity.damage = primaryDamage;
        entity.alive = primaryAliveTime;
        // inherit our velocity?
        Vector2 lv = body.getLinearVelocity();
        float pvx = tmp.x * primaryVelocity + lv.x;
        float pvy = tmp.y * primaryVelocity + lv.y;
        entity.body.setLinearVelocity(pvx, pvy);

        for (Fixture fixture : entity.body.getFixtureList()) {
            Filter filter = fixture.getFilterData();

            fixture.setFilterData(filter);
        }

        world.addEntity(entity);
    }

    private void fireSecondary () {
        // large slow, charge? hold fire for a bit to charge up, dealing more damage or whatever
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

        drawer.setColor(Color.CYAN);
        if (!lookAt.isZero() && false) {
            drawer.line(x, y, x + lookAt.x * 2, y + lookAt.y * 2, .1f);
        }
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

    public int damage () {
        return primaryDamage;
    }

    public void damage (int damage) {
        primaryDamage = damage;
    }
}
