package io.piotrjastrzebski.psm.entities;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

public class Transform {
    public float x;
    public float y;
    // radians
    public float rot;

    public void set (float x, float y, float rot) {
        this.x = x;
        this.y = y;
        this.rot = rot;
    }

    public void set (Transform other) {
        x = other.x;
        y = other.y;
        rot = other.rot;
    }

    public static void interpolate (Transform from, Transform to, float alpha, Transform out) {
        out.x = Interpolation.linear.apply(from.x, to.x, alpha);
        out.y = Interpolation.linear.apply(from.y, to.y, alpha);
        out.rot = MathUtils.lerpAngle(from.rot, to.rot, alpha);
    }
}
