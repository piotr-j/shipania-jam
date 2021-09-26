package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.pfa.*;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.ai.sched.LoadBalancingScheduler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import io.piotrjastrzebski.psm.Events;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.SMApp;
import io.piotrjastrzebski.psm.entities.*;
import io.piotrjastrzebski.psm.utils.Utils;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GameMap implements IndexedGraph<GameMapTile>, Telegraph {
    protected static final String TAG = GameMap.class.getSimpleName();


    protected final TiledMap map;
    protected final TiledMapTileLayer background;
    protected final float backgroundParallaxX;
    protected final float backgroundParallaxY;
    protected final TiledMapTileLayer walls;
    protected final TiledMapTileLayer foreground;
    protected final GameWorld world;
    protected final GameMapTile[][] gameTiles;
    protected final OrthogonalTiledMapRenderer mapRenderer;
    protected final int mapWidth;
    protected final int mapHeight;



    protected final Array<GameMapRoom> rooms = new Array<>();

    protected final IndexedAStarPathFinder<GameMapTile> pathFinder;
    protected final LoadBalancingScheduler scheduler;
    protected final PathSmoother<GameMapTile, Vector2> pathSmoother;

    // need list
    private Vector2 playerSpawn = new Vector2();

    public GameMap (SMApp app, GameWorld world) {

        map = app.assets().get("maps/test.tmx", TiledMap.class);
        this.world = world;

        mapRenderer = new OrthogonalTiledMapRenderer(map, SMApp.INV_SCALE, app.batch());

        MapLayers layers = map.getLayers();
        background = (TiledMapTileLayer)layers.get("background");
        backgroundParallaxX = background.getProperties().get("parallax-x", 1f, Float.class);
        backgroundParallaxY = background.getProperties().get("parallax-y", 1f, Float.class);
        walls = (TiledMapTileLayer)layers.get("walls");
        foreground = (TiledMapTileLayer)layers.get("foreground");

        mapWidth = walls.getWidth();
        mapHeight = walls.getHeight();
        gameTiles = new GameMapTile[mapWidth][mapHeight];

        processMap();


        pathFinder = new IndexedAStarPathFinder<>(this, true);

        PathFinderQueue<GameMapTile> pathFinderQueue = new PathFinderQueue<>(pathFinder);
        Events.register(pathFinderQueue, Events.PF_REQUEST);

        scheduler = new LoadBalancingScheduler(100);
        scheduler.add(pathFinderQueue, 1, 0);

        pathSmoother = new PathSmoother<GameMapTile, Vector2>(new GameMapPathSmoother(this));

//        MessageManager.getInstance().addListener(pathFinderQueue, PF_REQUEST);

        Events.register(msg -> {
            switch (msg.message) {
            case Events.ENTITY_KILLED: {
                BaseEntity entity = (BaseEntity)msg.extraInfo;
                if (entity instanceof WallEntity) {
                    WallEntity we = (WallEntity)entity;
                    destroyWallTile(we.tile);
                }
            } break;
            }
            return false;
        }, Events.ENTITY_KILLED);
    }

    private void destroyWallTile (GameMapTile tile) {
        tile.type = GameMapTileType.EMPTY;
        tile.entity = null;
        TiledMapTileLayer.Cell cell = walls.getCell(tile.x, tile.y);
        cell.setTile(null);

        addConnections(tile, true);
    }

    private void processMap () {
        // default spawn point just in case
        playerSpawn.set(20, 85);

        foreground.setOpacity(1);

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                TiledMapTileLayer.Cell cell = walls.getCell(x, y);
                if (cell == null) continue;
                TiledMapTile tile = cell.getTile();
                if (tile == null) continue;
                GameMapTile gt = new GameMapTile(x + y * mapWidth, x, y);
                gameTiles[x][y] = gt;
                MapProperties props = tile.getProperties();
                String type = props.get("type", null, String.class);

                if ("wall".equals(type)) {
                    gt.type = GameMapTileType.WALL;
                    gt.entity = new WallEntity(world, x + .5f, y + .5f);
                    gt.entity.tile = gt;
                    world.addEntity(gt.entity);
                } else if ("void".equals(type)) {
                    gt.type = GameMapTileType.VOID;
                } else if ("empty".equals(type)) {
                    gt.type = GameMapTileType.EMPTY;
                } else if (type != null) {
                    Gdx.app.error(TAG, "unknown tile type: " + type + " (" + x + "," + y + ")");
                }
                int health = props.get("health", 0, Integer.class);

                // laser,rocket,bump ?
                String breakable = props.get("breakable", null, String.class);
                if (health > 0) {
                    gt.entity.health(health);
                }
            }
        }

//        for (int x = 0; x < mapWidth; x++) {
//            for (int y = 0; y < mapHeight; y++) {
//                GameMapTile tile = gameTiles[x][y];
//                if (tile == null || tile.type != GameMapTileType.WALL) continue;
//                tile.entity = new WallEntity(world, tile.x + .5f, tile.y + .5f);
//            }
//        }

        MapLayers layers = map.getLayers();
        MapLayer helpers = layers.get("helpers");
        for (MapObject object : helpers.getObjects()) {
            if (object instanceof RectangleMapObject) {
                RectangleMapObject mo = (RectangleMapObject)object;
                Rectangle rect = mo.getRectangle();
                int x = MathUtils.floor(rect.x);
                int y = MathUtils.floor(rect.y);
                int width = MathUtils.ceil(rect.x + rect.width) - x;
                int height = MathUtils.ceil(rect.y + rect.height) - y;
                MapProperties props = mo.getProperties();
                String type = props.get("type", null, String.class);
                if ("room-reveal".equals(type)) {
                    // add room
                    GameMapRoomReveal room = new GameMapRoomReveal(world, x, y, width, height);
                    room.roomId = props.get("room-id", -1, Integer.class);
                    rooms.add(room);
                } else if ("room-challenge".equals(type)) {
                    GameMapRoomChallenge room = new GameMapRoomChallenge(world, x, y, width, height);
                    room.doorId = props.get("door-id", 0, Integer.class);
                    room.enemyId = props.get("enemy-id", 0, Integer.class);;
                    rooms.add(room);
                } else if ("door".equals(type)) {
                    int id = props.get("door-id", 0, Integer.class);
                    boolean locked = props.get("locked", false, Boolean.class);
                    world.addDoor(id, x,y ,width, height, locked);
                } else if ("switch".equals(type)) {
                    int doorId = props.get("door-id", 0, Integer.class);
                    world.addSwitch(doorId, x, y);
                } else {
                    Gdx.app.log(TAG, "unknown type: " + type);
                }
                //
                continue;
            }
            if (object instanceof TiledMapTileMapObject) {
                TiledMapTileMapObject mo = (TiledMapTileMapObject)object;
                float cx = MathUtils.round(mo.getX()) + .5f;
                float cy = MathUtils.round(mo.getY()) + .5f;
                MapProperties props = mo.getProperties();
                int playerSpawnId = props.get("playerSpawn", -1, Integer.class);
                if (playerSpawnId >= 0) {
                    // add spawns to some array
                    playerSpawn.set(cx, cy);
                }
                String enemyType = props.get("enemy-type", null, String.class);
                int enemyTier = props.get("enemy-tier", 1, Integer.class);
                int enemyId = props.get("enemy-id", 0, Integer.class);
                if (enemyType != null) {
                    world.addEnemySpawn(enemyId, cx, cy, enemyType, enemyTier);
                    continue;
                }

                String buffType = props.get("buff-type", null, String.class);
                int buffTier = props.get("buff-tier", 1, Integer.class);
                int buffId = props.get("buff-id", -1, Integer.class);
                if (buffType != null) {
                    world.spawnBuff(buffId, cx, cy, buffType, buffTier);
                    continue;
                }

                continue;
            }
            if (object instanceof PolygonMapObject) {
                PolygonMapObject mo = (PolygonMapObject)object;
                Polygon polygon = mo.getPolygon();
                MapProperties props = mo.getProperties();
                String type = props.get("type", null, String.class);
                if ("room-reveal".equals(type)) {
                    // add room
                    GameMapRoomReveal room = new GameMapRoomReveal(world, new Polygon(polygon.getTransformedVertices()));
                    room.roomId = props.get("room-id", -1, Integer.class);
                    rooms.add(room);
                } else if ("room-challenge".equals(type)) {
                    GameMapRoomChallenge room = new GameMapRoomChallenge(world, new Polygon(polygon.getTransformedVertices()));
                    room.doorId = props.get("door-id", 0, Integer.class);
                    room.enemyId = props.get("enemy-id", 0, Integer.class);;
                    rooms.add(room);
                }
                continue;
            }
            Gdx.app.log(TAG, "other object: " + object);
        }

        findEmptyTiles((int)playerSpawn.x, (int)playerSpawn.y);
        addConnections();
    }

    private void findEmptyTiles (int x, int y) {
        Queue<GridPoint2> tiles = new Queue<>();
        tiles.addLast(new GridPoint2(x, y));
        ObjectSet<GridPoint2> visited = new ObjectSet<>();
        while (tiles.size > 0) {
            GridPoint2 gp = tiles.removeFirst();
            visited.add(gp);
            if (gp.x < 0 || gp.y < 0 || gp.x >= mapWidth || gp.y >= mapHeight) continue;
            GameMapTile tile = gameTiles[gp.x][gp.y];
            if (tile == null) {
                tile = new GameMapTile(gp.x + gp.y * mapWidth, gp.x, gp.y);
                tile.type = GameMapTileType.EMPTY;
                gameTiles[tile.x][tile.y] = tile;
            } else if (tile.type == GameMapTileType.WALL && tile.entity.health() == -1) {
                // dont skip breakable walls
                continue;
            } else if (tile.type == GameMapTileType.EMPTY) {
                // already visited
                continue;
            }
            {
                GridPoint2 ngp = new GridPoint2(tile.x + 1, tile.y);
                if (!visited.contains(ngp)) tiles.addLast(ngp);
            }
            {
                GridPoint2 ngp = new GridPoint2(tile.x - 1, tile.y);
                if (!visited.contains(ngp)) tiles.addLast(ngp);
            }
            {
                GridPoint2 ngp = new GridPoint2(tile.x, tile.y + 1);
                if (!visited.contains(ngp)) tiles.addLast(ngp);
            }
            {
                GridPoint2 ngp = new GridPoint2(tile.x, tile.y - 1);
                if (!visited.contains(ngp)) tiles.addLast(ngp);
            }
        }
    }

    private void addConnections () {
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                GameMapTile tile = tileAt(x, y);
                if (tile == null || tile.type != GameMapTileType.EMPTY) continue;
                addConnections(tile, false);
            }
        }
    }

    private void addConnections (GameMapTile from, boolean bidirectional) {
        int x = from.x;
        int y = from.y;
        for (int ox = -1; ox <= 1; ox++) {
            for (int oy = -1; oy <= 1; oy++) {
                if (ox == 0 && oy == 0) continue;
                GameMapTile other = tileAt(x + ox, y + oy);
                if (other == null || other.type != GameMapTileType.EMPTY) continue;
                if (ox != 0 && oy != 0) {
                    // check corners
                    if (!isWalkable(x + ox, y)) continue;
                    if (!isWalkable(x, y + oy)) continue;
                }
                float cost = Vector2.dst(x, y, x + ox, y + oy);
                from.add(new GameMapTileConnection(from, other, cost));
                from.add(new GameMapTileConnection(from, other, cost));
                if (bidirectional) {
                    other.add(new GameMapTileConnection(other, from, cost));
                    other.add(new GameMapTileConnection(other, from, cost));
                    addConnections(other, false);
                }
            }
        }
    }

    private boolean isWalkable (int x, int y) {
        GameMapTile tile = tileAt(x, y);
        return tile != null && tile.type == GameMapTileType.EMPTY;
    }

    private void removeConnections (GameMapTile from) {

    }

    public void renderBackground (OrthographicCamera camera) {
        mapRenderer.setView(camera);
        // we can use offset for parallax
        // ideally we would match behavior in tiled, so it is easier to preview
        // or something close at least
        float cx = camera.position.x;
        float cy = camera.position.y;
        float ox = cx - cx * backgroundParallaxX;
        float oy = cy - cy * backgroundParallaxX;
        background.setOffsetX(ox * SMApp.SCALE);
        background.setOffsetY(oy * SMApp.SCALE);
        mapRenderer.renderTileLayer(background);
        mapRenderer.renderTileLayer(walls);
    }

    public void renderForeground (OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.renderTileLayer(foreground);
    }

    private Vector2 v2 = new Vector2();
    public void renderDebug (OrthographicCamera camera, ShapeDrawer drawer) {
        if (false) {
            for (int x = 0; x < mapWidth; x++) {
                for (int y = 0; y < mapHeight; y++) {
                    if (!Utils.visible(camera, x + .5f, y + .5f)) continue;
                    GameMapTile tile = gameTiles[x][y];
                    drawer.setColor(Color.WHITE);
                    if (tile != null) {
                        switch (tile.type) {
                        case WALL:
                            drawer.setColor(Color.FIREBRICK);
                            if (tile.entity.health() > 0) {
                                drawer.setColor(Color.GREEN);
                            }
                            break;
                        case EMPTY:
                            drawer.setColor(Color.NAVY);
                            break;
                        case VOID:
                            drawer.setColor(Color.WHITE);
                            break;
                        default:
                            drawer.setColor(Color.MAGENTA);
                        }
                    }
                    drawer.filledRectangle(x + .45f, y + .45f, .1f, .1f);
                }
            }
        }

        if (true) {
            for (GameMapRoom room : rooms) {
                room.debugDraw(drawer);
            }
        }
        if (false) {
            for (int x = 0; x < mapWidth; x++) {
                for (int y = 0; y < mapHeight; y++) {
                    if (!Utils.visible(camera, x + .5f, y + .5f)) continue;
                    GameMapTile tile = gameTiles[x][y];
                    if (tile == null) continue;
                    drawer.setColor(1, 1, 1, .1f);
                    drawer.rectangle(x, y, 1, 1, .05f);
                    for (Connection<GameMapTile> c : tile.connections) {
                        GameMapTileConnection connection = (GameMapTileConnection)c;
                        if (connection.cost <= 1) {
                            drawer.setColor(0, 1, 0, .7f);
                        } else {
                            drawer.setColor(1, 1, 0, .7f);
                        }
                        v2.set(connection.to.x + .5f, connection.to.y + .5f).sub(tile.x + .5f, tile.y + .5f).nor().scl(.35f);
                        drawer.line(tile.x + .5f, tile.y + .5f, tile.x + .5f + v2.x, tile.y + .5f + v2.y, .05f);
                    }
                }
            }
        }
    }

    public void update (float dt) {
        scheduler.run(TimeUtils.millisToNanos(3));
    }

    public Vector2 playerSpawn () {
        return playerSpawn;
    }

    private GameMapTile tileAt (float x, float y) {
        return tileAt(MathUtils.floor(x), MathUtils.floor(y));
    }

    public GameMapTile tileAt (int x, int y) {
        if (x < 0 || y < 0 || x >= mapWidth || y >= mapHeight) return null;
        return gameTiles[x][y];
    }

    public void revealTile (int x, int y) {
        TiledMapTileLayer.Cell cell = foreground.getCell(x, y);
        if (cell == null) return;
        // TODO fade of sort, some sort of animated tile? cant tint stuff on per tile basis it seams
        cell.setTile(null);
    }


    Heuristic<GameMapTile> heuristic = (node, endNode) -> Vector2.dst(node.x, node.y, endNode.x, endNode.y);
    public void findPathToPlayer (EnemyShipEntity entity) {
        PathRequest pfRequest = new PathRequest();
        pfRequest.pathSmoother = pathSmoother;
        pfRequest.sender = entity;
        pfRequest.target = world.player();
        pfRequest.startNode = tileAt(entity.x(), entity.y());
        pfRequest.endNode = tileAt(world.player().x(), world.player().y());
        pfRequest.heuristic = heuristic;
        pfRequest.responseMessageCode = Events.PF_RESPONSE;
        Events.send(this, Events.PF_REQUEST, pfRequest);
    }

    @Override
    public boolean handleMessage (Telegram msg) {
        switch (msg.message) {
        case Events.PF_RESPONSE: // PathFinderQueue will call us directly, no need to register for this message
            PathRequest pfr = (PathRequest)msg.extraInfo;
            if (pfr.pathFound) {
                //Gdx.app.log(TAG, "found path!");
                pfr.sender.path(pfr.resultPath);
            } else {
                Gdx.app.debug(TAG, "not found path!");
            }
            break;
        }
        return false;
    }

    @Override
    public int getIndex (GameMapTile node) {
        return node.index;
    }

    @Override
    public int getNodeCount () {
        return mapWidth * mapHeight;
    }

    @Override
    public Array<Connection<GameMapTile>> getConnections (GameMapTile fromNode) {
        return fromNode.connections;
    }

}
