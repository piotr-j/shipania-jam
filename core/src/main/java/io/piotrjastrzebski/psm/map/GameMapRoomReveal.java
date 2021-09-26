package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.physics.box2d.Contact;
import io.piotrjastrzebski.psm.Events;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.entities.BaseEntity;
import io.piotrjastrzebski.psm.entities.PlayerShipEntity;

public class GameMapRoomReveal extends GameMapRoom implements Telegraph {
    boolean revealed = false;
    public int roomId = -1;

    public GameMapRoomReveal (GameWorld world, int x, int y, int width, int height) {
        super(world, x, y, width, height);
        init();
    }

    public GameMapRoomReveal (GameWorld world, Polygon polygon) {
        super(world, polygon);
        init();
    }

    private void init () {
        Events.register(this, Events.GAME_RESTARTED);
    }

    @Override
    public void hit (BaseEntity other, Contact contact) {
        super.hit(other, contact);
        if (revealed) return;
        if (!(other instanceof PlayerShipEntity)) {
            return;
        }
        reveal();
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
        world.activateEntities(roomId, true);
    }

    @Override
    public boolean handleMessage (Telegram msg) {
        switch (msg.message) {
        case Events.GAME_RESTARTED: {
            if (roomId >= 0) {
                world.activateEntities(roomId, revealed);
            }
        } break;
        }
        return false;
    }
}
