package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.utils.Utils;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class PlayerShipEntity extends ShipEntity {
    public PlayerShipEntity (GameWorld world, float x, float y, float angle) {
        super(world, x, y, angle);

        health(150);
    }

    @Override
    public void update (float dt, float alpha) {
        super.update(dt, alpha);
        // how do we do this in cleaner way?
        Controller current = Controllers.getCurrent();
        if (current != null) {
            ControllerMapping mapping = current.getMapping();
            moveForward(-Utils.deadzone(current.getAxis(mapping.axisLeftY), deadzone));
            moveRight(-Utils.deadzone(current.getAxis(mapping.axisLeftX), deadzone));
            rotateRight(Utils.deadzone(current.getAxis(mapping.axisRightX), deadzone));
            firePrimary |= current.getButton(mapping.buttonR1);
            // wonder if this works on gwt :/
            // ControllerAxis.TRIGGERLEFT = 4
            // ControllerAxis.TRIGGERRIGHT = 5
            fireSecondary |= current.getAxis(5) > .5f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveForward(1);
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveForward(-1);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveRight(1);
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveRight(-1);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            rotateRight(-1);
        } else if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            rotateRight(1);
        }
        firePrimary |= Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        fireSecondary |= Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
    }

    @Override
    public void drawDebug (ShapeDrawer drawer) {
        super.drawDebug(drawer);

        drawer.setColor(Color.TEAL);
        drawer.circle(current.x(), current.y(), .49f, .1f);

    }
}
