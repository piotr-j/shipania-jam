package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.psm.entities.WallEntity;

public class GameMapTileConnection implements Connection<GameMapTile> {
    public GameMapTile from;
    public GameMapTile to;
    public float cost = 1;

    public GameMapTileConnection (GameMapTile from, GameMapTile to, float cost) {
        this.from = from;
        this.to = to;
        this.cost = cost;
    }

    @Override
    public float getCost () {
        return cost;
    }

    @Override
    public GameMapTile getFromNode () {
        return from;
    }

    @Override
    public GameMapTile getToNode () {
        return to;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameMapTileConnection that = (GameMapTileConnection)o;

        if (!from.equals(that.from)) return false;
        return to.equals(that.to);
    }

    @Override
    public int hashCode () {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        return result;
    }

    @Override
    public String toString () {
        return "GameMapTileConnection{" + "from=(" + from.x + ", " + from.y + "), to=(" + to.x + ", " + to.y + "), cost=" + cost + '}';
    }
}
