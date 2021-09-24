package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import io.piotrjastrzebski.psm.GameWorld;
import io.piotrjastrzebski.psm.utils.Utils;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class PlayerShipEntity extends ShipEntity {
    protected static final String TAG = PlayerShipEntity.class.getSimpleName();

    protected float deadzone = .1f;

    public PlayerShipEntity (GameWorld world, float x, float y, float angle) {
        super(world, x, y, angle);

        health(150);
    }

    Vector3 v3 = new Vector3();
    @Override
    public void update (float dt, float alpha) {
        super.update(dt, alpha);
        //Gdx.app.log(TAG, "fps: " + Gdx.graphics.getFramesPerSecond());
        lookAt.setZero();
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
            float aimX = Utils.deadzone(current.getAxis(mapping.axisRightX), deadzone);
            float aimY = Utils.deadzone(current.getAxis(mapping.axisRightY), deadzone);
            if (aimX != 0 || aimY != 0) {
                lookAt.set(aimX, -aimY).nor();
            }
        }
        // could use some sort of priority :d
        // last used input type or something
        if (lookAt.isZero()) {
            world.camera().unproject(v3.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            lookAt.set(v3.x, v3.y).sub(x(), y()).nor();
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
