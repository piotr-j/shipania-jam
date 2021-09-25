package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.map.GameMapTile;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class EnemyShipEntity extends ShipEntity {
    public EnemyShipEntity (GameWorld world, float x, float y, float angle) {
        super(world, x, y, angle);
    }

    GraphPath<GameMapTile> followPath;
    int nextTile = 0;
    int maxFollowPathLength = 30;
    float aggroRange = 15;
    float stopRange = 5;
    float attackRange = 8;

    float repathTimer;
    float fireTimer;
    float fireDuration = 1;
    float fireCooldown = 2;
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
        // simply move to player when within range
        PlayerShipEntity player = world.player();
        firePrimary = false;
        float dstToPlayer = Vector2.dst(x(), y(), player.x(), player.y());
        if (dstToPlayer <= aggroRange) {
            if (repathTimer > 0) {
                repathTimer -= dt;
            } else {
                repathTimer = .25f;
                world.map().findPathToPlayer(this);
            }
        }
        // dont follow path if its super long
        // could use LoS check :d
        if (dstToPlayer <= aggroRange && dstToPlayer >= stopRange && followPath != null && followPath.getCount() < maxFollowPathLength) {
            GameMapTile tile = followPath.get(nextTile);
            float dstToTile = Vector2.dst(x(), y(), tile.cx(), tile.cy());
            if (dstToTile < 1f) {
                if (nextTile + 1 < followPath.getCount()) {
                    nextTile++;
                }
            } else {
                lookAt.set(tile.cx() - x(), tile.cy() - y());
                moveForward(.3f);
            }
        } else {
            moveForward(0);
        }
        if (fireTimer > 0) fireTimer -= dt;

        if (dstToPlayer < attackRange) {
            lookAt.set(player.x() - x(), player.y() - y());
            if (fireTimer <= 0) fireTimer += fireCooldown + fireDuration;

            if (fireTimer >= fireCooldown) {
                firePrimary = true;
            }
        }
    }

    public void path (GraphPath<GameMapTile> resultPath) {
        followPath = resultPath;
        nextTile = 0;
    }

    @Override
    public void drawDebug (ShapeDrawer drawer) {
        super.drawDebug(drawer);

        drawer.setColor(Color.RED);
        drawer.circle(current.x(), current.y(), .49f, .1f);

        if (!(followPath != null && followPath.getCount() > 0 && followPath.getCount() < maxFollowPathLength)) return;
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
