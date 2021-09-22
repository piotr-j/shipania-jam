package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class ShipEntity extends BaseEntity {
    protected static final String TAG = ShipEntity.class.getSimpleName();

    // input
    protected float moveForward;
    protected float moveRight;
    protected float rotateRight;
    protected float deadzone = .1f;

    // handling
    float forwardImpulse = 5;
    float rightImpulse = 2;
    float rotateImpulse = 4;

    protected Vector2 tmp = new Vector2();

    public ShipEntity (Body body) {
        super(body);

        forwardImpulse = 10;
        rightImpulse = 5;
        rotateImpulse = 30;
    }

    @Override
    public void fixed () {
        // do we allow strafe? perhaps different strength based on direction
        tmp.set(moveForward * forwardImpulse, moveRight * rightImpulse);
        if (!tmp.isZero()) {
            tmp.rotateRad(body.getAngle());
            body.applyForceToCenter(tmp.x, tmp.y, true);
        }
        if (rotateRight != 0) {
            body.applyTorque(-rotateRight * rotateImpulse, true);
        }
        super.fixed();
    }

    @Override
    public void update (float dt, float alpha) {
        super.update(dt, alpha);

        // how do we do this in cleaner way?
        Controller current = Controllers.getCurrent();
        if (current != null) {
            ControllerMapping mapping = current.getMapping();
            moveForward(-deadzone(current.getAxis(mapping.axisLeftY)));
            moveRight(-deadzone(current.getAxis(mapping.axisLeftX)));
            rotateRight(deadzone(current.getAxis(mapping.axisRightX)));
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
    }

    @Override
    public void drawDebug (ShapeDrawer drawer) {
        super.drawDebug(drawer);
        float x = current.x();
        float y = current.y();
        float angle = current.angle();
        tmp.set(1, 0).rotateRad(current.angle());
        float fx = tmp.x;
        float fy = tmp.y;

        drawer.setColor(Color.GOLDENROD);
        tmp.set(0, .4f).rotateRad(angle);
        drawer.line(x + tmp.x, y + tmp.y, x + tmp.x + fx * .8f, y + tmp.y + fy * .8f, .2f);
        tmp.set(0, -.4f).rotateRad(angle);
        drawer.line(x + tmp.x, y + tmp.y, x + tmp.x + fx * .8f, y + tmp.y + fy * .8f, .2f);

        drawer.setColor(Color.GOLD);
        drawer.filledCircle(x, y, .5f);

        tmp.set(0, .2f).rotateRad(angle);
        drawer.setColor(Color.ROYAL);
        drawer.filledTriangle(
            x + tmp.x - fx * .2f, y + tmp.y - fy * .2f,
            x - tmp.x - fx * .2f, y - tmp.y - fy * .2f,
            x + fx * .3f, y + fy * .3f);
    }

    private float deadzone (float axis) {
        if (axis > deadzone) return MathUtils.map(deadzone, 1, 0, 1, axis);
        if (axis < -deadzone) return MathUtils.map(-deadzone, -1, 0, -1, axis);;
        return 0;
    }

    protected void moveForward (float forward) {
        this.moveForward = forward;
    }

    protected void moveRight (float right) {
        this.moveRight = right;
    }

    protected void rotateRight (float rotateRight) {
        this.rotateRight = rotateRight;
    }
}
