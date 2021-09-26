package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.entities.BaseEntity;
import io.piotrjastrzebski.psm.entities.SensorEntity;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GameMapRoom implements SensorEntity.SensorListener {
    GameWorld world;
    Rectangle bounds;
    Polygon polygon;
    Array<GameMapTile> tiles;
    SensorEntity sensor;

    public GameMapRoom (GameWorld world, int x, int y, int width, int height) {
        this.world = world;
        bounds = new Rectangle(x, y, width, height);
        polygon = new Polygon(new float[]{x, y, x, y + height, x + width, y + height, x, y, x + width, y + height, x + width, y});
        tiles = new Array<>();
        sensor = new SensorEntity(world, x, y, width, height);
        sensor.listener = this;
        world.addEntity(sensor);
    }

    public GameMapRoom (GameWorld world, Polygon polygon) {
        this.world = world;
        bounds = polygon.getBoundingRectangle();
        this.polygon = polygon;
        tiles = new Array<>();
        sensor = new SensorEntity(world, bounds.x, bounds.y, polygon);
        sensor.listener = this;
        world.addEntity(sensor);
    }

    @Override
    public void hit (BaseEntity other, Contact contact) {

    }

    public void debugDraw (ShapeDrawer drawer) {
        if (true) return;
        drawer.setColor(Color.ORANGE);
        drawer.rectangle(bounds, .1f);
        drawer.setColor(Color.YELLOW);
        if (polygon != null) {
            drawer.polygon(polygon, .1f);
        }
    }
}
