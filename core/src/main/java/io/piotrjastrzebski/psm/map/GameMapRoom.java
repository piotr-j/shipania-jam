package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.entities.BaseEntity;
import io.piotrjastrzebski.psm.entities.SensorEntity;

public class GameMapRoom implements SensorEntity.SensorListener {
    GameWorld world;
    Rectangle bounds;
    Array<GameMapTile> tiles;
    SensorEntity sensor;

    public GameMapRoom (GameWorld world, int x, int y, int width, int height) {
        this.world = world;
        bounds = new Rectangle(x, y, width, height);
        tiles = new Array<>();
        sensor = new SensorEntity(world, x, y, width, height);
        sensor.listener = this;
        world.addEntity(sensor);
    }

    @Override
    public void hit (BaseEntity other, Contact contact) {

    }
}
