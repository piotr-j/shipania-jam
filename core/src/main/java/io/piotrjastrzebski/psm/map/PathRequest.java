package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.ai.pfa.PathFinderRequest;
import com.badlogic.gdx.ai.pfa.PathSmoother;
import com.badlogic.gdx.ai.pfa.PathSmootherRequest;
import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import io.piotrjastrzebski.psm.entities.EnemyShipEntity;
import io.piotrjastrzebski.psm.entities.PlayerShipEntity;

class PathRequest extends PathFinderRequest<GameMapTile> implements Pool.Poolable {
    PathSmoother<GameMapTile, Vector2> pathSmoother;

    PathSmootherRequest<GameMapTile, Vector2> pathSmootherRequest;
    boolean smoothFinished;

    EnemyShipEntity sender;
    PlayerShipEntity target;

    public PathRequest () {
        this.resultPath = new GameMapTilePath();
        pathSmootherRequest = new PathSmootherRequest<>();
    }

    @Override
    public boolean initializeSearch (long timeToRun) {
        resultPath.clear();
        pathSmootherRequest.refresh((SmoothableGraphPath<GameMapTile, Vector2>)resultPath);
        smoothFinished = false;
        return true;
    }

    @Override
    public boolean finalizeSearch (long timeToRun) {
        if (pathFound  && !smoothFinished) {
            smoothFinished = pathSmoother.smoothPath(pathSmootherRequest, timeToRun);
            return smoothFinished;
        }
        return true;
    }

    @Override
    public void reset () {
        this.startNode = null;
        this.endNode = null;
        this.heuristic = null;
        this.client = null;
    }
}
