package io.piotrjastrzebski.psm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** First screen of the application. Displayed after the application is created. */
public abstract class BaseScreen implements Screen, InputProcessor {
	protected final SMApp app;
	protected final Assets assets;
	protected final Skin skin;
	protected final ExtendViewport gameViewport;
	protected final ExtendViewport uiViewport;
	protected final Stage stage;
	protected final TwoColorPolygonBatch batch;
	protected final ShapeRenderer renderer;
	protected final ShapeDrawer drawer;

	public BaseScreen (SMApp app) {
		this.app = app;
		assets = app.assets;
		gameViewport = new ExtendViewport(SMApp.WIDTH * SMApp.INV_SCALE, SMApp.HEIGHT * SMApp.INV_SCALE);
		uiViewport = new ExtendViewport(SMApp.WIDTH, SMApp.HEIGHT);
		batch = app.batch;
		renderer = app.renderer;

		stage = new Stage(uiViewport, batch);

		// null in loading screen!
		drawer = app.drawer;
		skin = app.assets.skin;

		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, this));
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(Color.GRAY);
	}

	@Override
	public void resize(int width, int height) {
		gameViewport.update(width, height);
		uiViewport.update(width, height);
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean keyDown (int keycode) {
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped (char character) {
		return false;
	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled (float amountX, float amountY) {
		return false;
	}
}