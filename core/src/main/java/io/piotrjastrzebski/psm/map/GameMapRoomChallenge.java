package io.piotrjastrzebski.psm.map;

import com.badlogic.gdx.physics.box2d.Contact;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.entities.BaseEntity;
import io.piotrjastrzebski.psm.entities.PlayerShipEntity;

public class GameMapRoomChallenge extends GameMapRoom {

    public GameMapRoomChallenge (GameWorld world, int x, int y, int width, int height) {
        super(world, x, y, width, height);
    }

    @Override
    public void hit (BaseEntity other, Contact contact) {
        super.hit(other, contact);
        if (!(other instanceof PlayerShipEntity)) {
            return;
        }
        // lock doors till enemies are dead
        // how do we do doors? some sort of barrier
        // waves?
    }

}
