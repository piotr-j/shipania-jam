package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.map.GameMapTile;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class EnemyShipEntity extends ShipEntity {
    public EnemyShipEntity (GameWorld world, float x, float y, float angle) {
        super(world, x, y, angle);
    }

    GraphPath<GameMapTile> followPath;
    float timer;
    @Override
    public void update (float dt, float alpha) {
        super.update(dt, alpha);

        // so we kinda want to attack the player
        // pathfinding will likely be needed, as straight path to player would likely fail
        // can we add a sensor fixture to body to detect player?
        // stuff to do:
        // detect nearby player, sensor?
        // find a path to player location
        //      need to build proper graph, no point for hierarchical
        //      we have GameMapTile in all relevant places, we can use them
        //      once we have that, make them seek player by following the path
        //      ideally we want to use same api as player, eg by setting steering left/right/rotate
        //

        if (timer > 0) {
            timer -= dt;
            return;
        }
        timer = 1;
        world.map().findPathToPlayer(this);
    }

    public void path (GraphPath<GameMapTile> resultPath) {
        followPath = resultPath;
    }

    @Override
    public void drawDebug (ShapeDrawer drawer) {
        super.drawDebug(drawer);

        drawer.setColor(Color.RED);
        drawer.circle(current.x(), current.y(), .49f, .1f);

        if (followPath == null || followPath.getCount() == 0) return;
        drawer.setColor(Color.YELLOW);
        GameMapTile first = followPath.get(0);
        drawer.line(x(), y(), first.cx(), first.cy(), .05f);

        drawer.setColor(Color.ORANGE);
        for (int i = 0, n = followPath.getCount() - 1; i < n; i++) {
            GameMapTile from = followPath.get(i);
            GameMapTile to = followPath.get(i + 1);

            drawer.line(from.cx(), from.cy(), to.cx(), to.cy(), .05f);
        }
    }
}
