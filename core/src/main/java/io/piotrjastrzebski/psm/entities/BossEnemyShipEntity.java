package io.piotrjastrzebski.psm.entities;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.map.GameMapTile;
import org.omg.CORBA.MARSHAL;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class BossEnemyShipEntity extends EnemyShipEntity {

    public BossEnemyShipEntity (GameWorld world, float x, float y, float angle) {
        super(world, x, y, angle);


        firePrimaryCooldown = GameWorld.WORLD_STEPS_PER_SECOND / 8;
        maxAngularVelocity = 90 * MathUtils.degRad;

        primaryDamage = 20;

        aggroRange = 20;
        stopRange = 8;
        attackRange = 12;


        forwardImpulse = 300;
        rightImpulse = 100;


        fireDuration = 2;
        fireCooldown = 2f;

        primaryVelocity = 5;
        primaryAliveTime = 10;

        health(5000);
    }

    @Override
    protected void createShipFixtures () {
        CircleShape shape = new CircleShape();
        shape.setRadius(2f);
        Fixture fixture = body.createFixture(shape, 1);
        fixture.setFriction(0);
        fixture.setRestitution(.3f);

        Filter filterData = fixture.getFilterData();
        filterData.categoryBits = CATEGORY_ENEMY;
        filterData.maskBits = CATEGORY_WALL | CATEGORY_PLAYER | CATEGORY_PROJECTILE_PLAYER;
        fixture.setFilterData(filterData);


        shape.dispose();

        // make it simpler to deal with, basically cube data
        MassData massData = body.getMassData();
        massData.mass = 100;
        massData.I = 0.16666667f;
        body.setMassData(massData);

        PointLight light = new PointLight(world.rays(), 64);
        light.setSoft(true);
        light.setPosition(x(), y());
        light.setContactFilter(CATEGORY_LIGHT, (short)0, (short)(CATEGORY_WALL | CATEGORY_PLAYER));
        light.setDistance(10);
        light.setColor(Color.SCARLET);
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

        Body body = world.box2d().createBody(def);



        return body;
    }



    protected void firePrimary () {
        // small fast
        float hp = health/(float)maxHealth;
        float angle = target.angle();
        if (hp < .5f) {
            float t = ((fireTimer - fireCooldown)/fireDuration - .5f) * 2;
            angle += t * 15 * MathUtils.degRad;
        }
        firePrimary(angle - 15 * MathUtils.degRad, 2.5f);
        firePrimary(angle, 2.5f);
        firePrimary(angle + 15 * MathUtils.degRad, 2.5f);
    }

    @Override
    public void update (float dt, float alpha) {
        super.update(dt, alpha);
    }



    @Override
    public void drawDebug (ShapeDrawer drawer) {
        super.drawDebug(drawer);

        drawer.setColor(Color.ORANGE);
        drawer.filledCircle(current.x(), current.y(), 2);

        drawer.setColor(Color.RED);
        drawer.circle(current.x(), current.y(), 2f, .1f);


        float hp = health/(float)maxHealth;
        float angle = target.angle();
        if (hp < .5f && firePrimary) {
            float t = ((fireTimer - fireCooldown)/fireDuration - .5f) * 2;
            angle += t * 15 * MathUtils.degRad;

        }
        drawTurret(drawer, angle - 15 * MathUtils.degRad);
        drawTurret(drawer, angle);
        drawTurret(drawer, angle + 15 * MathUtils.degRad);

        float x = current.x();
        float y = current.y();
//        float angle = current.angle();
        tmp.set(1, 0).rotateRad(angle);
        float fx = tmp.x;
        float fy = tmp.y;
//
//        drawer.setColor(Color.GOLDENROD);
//        tmp.set(0, .4f).rotateRad(angle);
//        drawer.line(x + tmp.x, y + tmp.y, x + tmp.x + fx * .8f, y + tmp.y + fy * .8f, .2f);
//        tmp.set(0, -.4f).rotateRad(angle);
//        drawer.line(x + tmp.x, y + tmp.y, x + tmp.x + fx * .8f, y + tmp.y + fy * .8f, .2f);

        tmp.set(0, .2f).rotateRad(angle);
        drawer.setColor(Color.ROYAL);
        drawer.filledTriangle(
            x + tmp.x - fx * .2f, y + tmp.y - fy * .2f,
            x - tmp.x - fx * .2f, y - tmp.y - fy * .2f,
            x + fx * .3f, y + fy * .3f);
    }

    private void drawTurret (ShapeDrawer drawer, float angle) {
        tmp.set(1, 0).rotateRad(angle);
        drawer.setColor(Color.BLACK);
        drawer.line(x() + tmp.x * 1f, y() + tmp.y * 1f, x() + tmp.x * 2.5f, y() + tmp.y * 2.5f, .3f);
    }

    @Override
    public void changeHealth (int amount) {
        if (!hasAggro) return;
        super.changeHealth(amount);
    }
}
