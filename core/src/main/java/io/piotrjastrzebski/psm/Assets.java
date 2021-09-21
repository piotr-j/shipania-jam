package io.piotrjastrzebski.psm;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Assets {
    protected final AssetManager manager;

    protected Skin skin;

    public Assets () {

        InternalFileHandleResolver resolver = new InternalFileHandleResolver();

        manager = new AssetManager(resolver);
        manager.load("ui/uiskin.json", Skin.class);
    }

    public boolean update () {
        boolean update = manager.update();
        if (update) {
            finishLoading();
        }
        return update;
    }

    private void finishLoading () {
        skin = manager.get("ui/uiskin.json", Skin.class);

        Events.send(Events.ASSETS_LOADED);
    }

    public void dispose () {
        manager.dispose();
    }
}
