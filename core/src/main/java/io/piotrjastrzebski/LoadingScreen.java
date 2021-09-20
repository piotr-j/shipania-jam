package io.piotrjastrzebski;

public class LoadingScreen extends BaseScreen {
    public LoadingScreen (SMVApp app) {
        super(app);
    }

    @Override
    public void render (float delta) {
        super.render(delta);

        // todo some ui?
        app.assets.update();
    }
}
