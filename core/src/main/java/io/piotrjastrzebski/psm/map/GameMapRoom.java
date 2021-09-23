package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.entities.BaseEntity;
import io.piotrjastrzebski.psm.entities.SensorEntity;
import io.piotrjastrzebski.psm.entities.ShipEntity;

public class GameMapRoom implements SensorEntity.SensorListener {
    GameWorld world;
    boolean revealed = false;
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

    private void reveal () {
        if (revealed) return;
        revealed = true;
        GameMap map = world.map();
        int sx = (int)bounds.x;
        int sy = (int)bounds.y;
        int width = (int)bounds.width;
        int height = (int)bounds.height;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                GameMapTile tile = map.tileAt(sx + x, sy + y);
                if (tile == null) continue;
                switch (tile.type) {
                case VOID: break;
                case EMPTY:
                case WALL:
                    map.revealTile(sx + x, sy + y);
                    break;
                }
            }
        }
    }

    @Override
    public void hit (BaseEntity other, Contact contact) {
        if (revealed) return;
        if (!(other instanceof ShipEntity)) {
            return;
        }

        ShipEntity shipEntity = (ShipEntity)other;
        reveal();
    }
}
