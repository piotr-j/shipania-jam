package io.piotrjastrzebski.psm.entities;

import box2dLight.Light;
import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.Contact;
import io.piotrjastrzebski.psm.GameWorld;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class BuffEntity extends SensorEntity {
    protected static final String TAG = BuffEntity.class.getSimpleName();
    public int extraHealth;
    public int healHealth;
    public int extraDamage;
    public int tier = 1;

    public BuffEntity (GameWorld world, float x, float y, float width, float height) {
        super(world, x, y, width, height);

        PointLight light = new PointLight(world.rays(), 8);
        light.setXray(true);
        light.setSoft(true);
        light.setPosition(x(), y());
        light.setContactFilter(CATEGORY_LIGHT, (short)0, (short)(CATEGORY_WALL));
        light.setDistance(1.5f);
        light.setColor(Color.GREEN);
        light.getColor().a = .75f;
        light.setActive(true);
        lights.add(light);
    }

    @Override
    public void hit (BaseEntity other, Contact contact) {
        super.hit(other, contact);
        if (other instanceof PlayerShipEntity) {
            ShipEntity entity = (ShipEntity)other;
            entity.primaryDamage += extraDamage;
            entity.maxHealth += extraHealth;
            entity.changeHealth(healHealth);
            //Gdx.app.log(TAG, "Buff player hp +" + extraHealth + ", heal +" + healHealth + ", dmg +" + extraDamage);
            kill();
        }
    }

    @Override
    public void drawDebug (ShapeDrawer drawer) {
        if (!active) return;
        super.drawDebug(drawer);
        if (extraDamage > 0) {
            drawer.setColor(Color.SCARLET);
            for (Light light : lights) {
                light.setColor(Color.SCARLET);
            }
        } else if (extraHealth > 0) {
            drawer.setColor(Color.LIME);
            for (Light light : lights) {
                light.setColor(Color.LIME);
            }
        }
        drawer.filledCircle(x(), y(), .3f + tier * .2f);
        drawer.setColor(Color.BLACK);
        drawer.line(x() - .3f, y(), x() + .3f, y(), .1f);
        drawer.line(x(), y() - .3f, x(), y() + .3f, .1f);
    }

    @Override
    public void active (boolean active) {
        super.active(active);
        if (!active) return;
        if (extraDamage > 0) {
            for (Light light : lights) {
                light.setColor(Color.SCARLET);
                light.getColor().a = .75f;
            }
        } else if (extraHealth > 0) {
            for (Light light : lights) {
                light.setColor(Color.LIME);
                light.getColor().a = .75f;
            }
        }
    }
}
