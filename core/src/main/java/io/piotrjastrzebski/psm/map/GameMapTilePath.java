package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.math.Vector2;

public class GameMapTilePath extends DefaultGraphPath<GameMapTile> implements SmoothableGraphPath<GameMapTile, Vector2> {

    private final static Vector2 tmp = new Vector2();

    @Override
    public Vector2 getNodePosition (int index) {
        GameMapTile node = nodes.get(index);
        return tmp.set(node.x, node.y);
    }

    @Override
    public void swapNodes (int index1, int index2) {
        // x.swap(index1, index2);
        // y.swap(index1, index2);
        nodes.set(index1, nodes.get(index2));
    }

    @Override
    public void truncatePath (int newLength) {
        nodes.truncate(newLength);
    }

}
