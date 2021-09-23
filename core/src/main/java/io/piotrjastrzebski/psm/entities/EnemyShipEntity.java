package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.graphics.Color;
import io.piotrjastrzebski.psm.GameWorld;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class EnemyShipEntity extends ShipEntity {
    public EnemyShipEntity (GameWorld world, float x, float y, float angle) {
        super(world, x, y, angle);
    }


    @Override
    public void drawDebug (ShapeDrawer drawer) {
        super.drawDebug(drawer);

        drawer.setColor(Color.RED);
        drawer.circle(current.x(), current.y(), .49f, .1f);

    }
}
