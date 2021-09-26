package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Array;
import io.piotrjastrzebski.psm.Events;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.entities.BaseEntity;
import io.piotrjastrzebski.psm.entities.DoorEntity;
import io.piotrjastrzebski.psm.entities.EnemyShipEntity;
import io.piotrjastrzebski.psm.entities.PlayerShipEntity;

public class GameMapRoomChallenge extends GameMapRoom implements Telegraph {
    protected static final String TAG = GameMapRoomChallenge.class.getSimpleName();

    int doorId;
    int enemyId;

    int aliveEnemies = 0;

    public GameMapRoomChallenge (GameWorld world, int x, int y, int width, int height) {
        super(world, x, y, width, height);
    }

    public GameMapRoomChallenge (GameWorld world, Polygon polygon) {
        super(world, polygon);
        Events.register(this, Events.ENTITY_SPAWNED);
        Events.register(this, Events.ENTITY_KILLED);
        Events.register(this, Events.GAME_RESTARTING);
    }

    @Override
    public void hit (BaseEntity other, Contact contact) {
        super.hit(other, contact);
        if (!(other instanceof PlayerShipEntity)) {
            return;
        }
        Gdx.app.log(TAG, "player entered :o");
        world.closeDoors(doorId);
    }

    @Override
    public boolean handleMessage (Telegram msg) {
        switch (msg.message) {
        case Events.ENTITY_KILLED: {
            if (msg.extraInfo instanceof EnemyShipEntity) {
                EnemyShipEntity ese = (EnemyShipEntity)msg.extraInfo;
                if (ese.mapId == enemyId) {
                    aliveEnemies--;
                    Gdx.app.log(TAG, "room enemy died! " + aliveEnemies);
                    if (aliveEnemies <= 0) {
                        world.openDoors(doorId);
                    }
                }
            }
        } break;
        case Events.ENTITY_SPAWNED: {
            if (msg.extraInfo instanceof EnemyShipEntity) {
                EnemyShipEntity ese = (EnemyShipEntity)msg.extraInfo;
                if (ese.mapId == enemyId) {
                    Gdx.app.log(TAG, "room enemy spawnd! " + aliveEnemies);
                    aliveEnemies ++;
                }
            }
        } break;
        case Events.GAME_RESTARTING: {
            Gdx.app.log(TAG, "restart! " + aliveEnemies);
            aliveEnemies = 0;
            world.openDoors(doorId);
        } break;
        }
        return false;
    }
}
