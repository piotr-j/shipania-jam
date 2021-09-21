package io.piotrjastrzebski.psm;

public class LoadingScreen extends BaseScreen {
    public LoadingScreen (SMApp app) {
        super(app);
    }

    @Override
    public void render (float delta) {
        super.render(delta);

        // todo some ui?
        app.assets.update();
    }
}
