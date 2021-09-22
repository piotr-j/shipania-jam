package io.piotrjastrzebski.psm;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SMApp extends Game {
	protected static final String TAG = SMApp.class.getSimpleName();

	public final static int WIDTH = 1280;
	public final static int HEIGHT = 720;
	public final static float SCALE = 40;
	public final static float INV_SCALE = 1/SCALE;

	Assets assets;
	TwoColorPolygonBatch batch;
	ShapeRenderer renderer;
	ShapeDrawer drawer;

	@Override
	public void create() {
		assets = new Assets();
		batch = new TwoColorPolygonBatch();
		renderer = new ShapeRenderer();

		// load assets and stuff
		setScreen(new LoadingScreen(this));

		// once it is done, show game/menu screen
		Events.register(msg -> {
			Gdx.app.log(TAG, "Assets loaded...");
			drawer = new ShapeDrawer(batch, assets.skin.getRegion("white"));
			setScreen(new GameScreen(SMApp.this));
			return false;
		}, Events.ASSETS_LOADED);
	}

	@Override
	public void render () {
		float dt = MathUtils.clamp(Gdx.graphics.getDeltaTime(), 1/90f, 1/20f);
		if (screen != null) screen.render(dt);
		batch.setColor(Color.WHITE);
		Events.update(dt);
	}

	@Override
	public void dispose () {
		super.dispose();
		assets.dispose();
	}

	public Assets assets () {
		return assets;
	}

	public TwoColorPolygonBatch batch () {
		return batch;
	}

	public ShapeDrawer drawer () {
		return drawer;
	}

	public ShapeRenderer renderer () {
		return renderer;
	}
}