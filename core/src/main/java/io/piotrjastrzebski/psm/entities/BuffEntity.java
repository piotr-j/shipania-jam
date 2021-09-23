package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.Contact;
import io.piotrjastrzebski.psm.GameWorld;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class BuffEntity extends SensorEntity {
    protected static final String TAG = BuffEntity.class.getSimpleName();
    public int extraHealth;
    public int healHealth;

    public BuffEntity (GameWorld world, float x, float y, float width, float height) {
        super(world, x, y, width, height);
    }

    @Override
    public void hit (BaseEntity other, Contact contact) {
        super.hit(other, contact);
        if (other instanceof PlayerShipEntity) {
            other.maxHealth += extraHealth;
            other.changeHealth(healHealth);
            Gdx.app.log(TAG, "Buff player hp +" + extraHealth + ", heal +" + healHealth);
            kill();
        }
    }

    @Override
    public void drawDebug (ShapeDrawer drawer) {
        super.drawDebug(drawer);
        drawer.setColor(Color.GREEN);
        drawer.filledCircle(current.x(), current.y(), .4f);
    }
}
