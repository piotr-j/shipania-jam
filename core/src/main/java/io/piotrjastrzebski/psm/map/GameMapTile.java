package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.psm.entities.WallEntity;

public class GameMapTile {
    public final int index;
    public final int x;
    public final int y;
    public GameMapTileType type = GameMapTileType.VOID;
    public WallEntity entity;


    // graph
    public Array<Connection<GameMapTile>> connections = new Array<>();

    public GameMapTile (int index, int x, int y) {
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public void add (GameMapTileConnection connection) {
        if (!connections.contains(connection, false)) {
            connections.add(connection);
        }
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameMapTile that = (GameMapTile)o;

        return index == that.index;
    }

    @Override
    public int hashCode () {
        return index;
    }

    @Override
    public String toString () {
        return "GameMapTile{" + "x=" + x + ", y=" + y + ", type=" + type + ", connections=" + connections + '}';
    }

    public float cx () {
        return x + .5f;
    }

    public float cy () {
        return y + .5f;
    }
}
