package io.piotrjastrzebski.psm;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets {
    protected final AssetManager manager;

    protected Skin skin;

    public Assets () {

        InternalFileHandleResolver resolver = new InternalFileHandleResolver();

        manager = new AssetManager(resolver);
        manager.load("ui/uiskin.json", Skin.class);
        manager.setLoader(TiledMap.class, new TmxMapLoader());
        {
            TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
            parameters.convertObjectToTileSpace = true;
            parameters.textureMinFilter = Texture.TextureFilter.Linear;
            parameters.textureMagFilter = Texture.TextureFilter.Linear;
            manager.load("maps/test.tmx", TiledMap.class, parameters);
        }
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

    public <T> T get (String fileName, Class<T> type) {
        return manager.get(fileName, type);
    }
}
