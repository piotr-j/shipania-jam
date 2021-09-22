package io.piotrjastrzebski.psm.map;

public enum GameMapTileType {
    VOID, // outside of bounds
    EMPTY, // empty space inside traversable bounds
    WALL, // wall that blocks movement
}
