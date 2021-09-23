package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.physics.box2d.Body;
import io.piotrjastrzebski.psm.entities.WallEntity;

public class GameMapTile {
    public final int x;
    public final int y;
    public GameMapTileType type = GameMapTileType.VOID;
    public WallEntity entity;

    public GameMapTile (int x, int y) {
        this.x = x;
        this.y = y;
    }
}
