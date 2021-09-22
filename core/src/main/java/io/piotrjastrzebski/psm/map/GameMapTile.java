package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.physics.box2d.Body;

public class GameMapTile {
    public final int x;
    public final int y;
    public GameMapTileType type = GameMapTileType.VOID;
    public boolean breakable;
    public int health = 0;
    // if it needs one, eg wall or trigger of some sort
    public Body body;

    public GameMapTile (int x, int y) {
        this.x = x;
        this.y = y;
    }
}
