package io.piotrjastrzebski.psm.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

public class Transform {
    protected float x;
    protected float y;
    // radians
    protected float angle;

    public void set (float x, float y, float angle) {
        this.x = x;
        this.y = y;
        this.angle = Utils.sanitizeAngle(angle);
    }

    public void set (Transform other) {
        x = other.x;
        y = other.y;
        angle = other.angle;
    }

    public float x () {
        return x;
    }

    public float y () {
        return y;
    }

    public float angle () {
        return angle;
    }
}
