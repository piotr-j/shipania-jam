package io.piotrjastrzebski.psm.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

import static com.badlogic.gdx.math.MathUtils.PI2;

public class Utils {
    public static float sanitizeAngle (float angle) {
        float sanitized = ((angle + PI2) % PI2);
        if (sanitized < 0) sanitized += PI2;
        return sanitized;
    }

    public static void interpolate (Transform from, Transform to, float alpha, Transform out) {
        out.x = Interpolation.linear.apply(from.x, to.x, alpha);
        out.y = Interpolation.linear.apply(from.y, to.y, alpha);
        // angles should be already be sanitized at this point when we set them in Transform
        out.angle = MathUtils.lerpAngle(from.angle, to.angle, alpha);
    }

    public static boolean visible (OrthographicCamera camera, float x, float y) {
        return visible(camera, x, y, 1, 1);
    }

    public static boolean visible (OrthographicCamera camera, float x, float y, float width, float height) {
        float hw = camera.viewportWidth/2;
        float hh = camera.viewportHeight/2;
        if (x > camera.position.x + hw) return false;
        if (x < camera.position.x - hw) return false;
        if (y > camera.position.y + hh) return false;
        if (y < camera.position.y - hh) return false;
        return true;
    }

}
