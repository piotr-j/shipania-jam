package io.piotrjastrzebski.psm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;

public class GameScreen extends BaseScreen {
    protected static final String TAG = GameScreen.class.getSimpleName();

    protected final GameWorld world;

    public GameScreen (SMApp app) {
        super(app);

        world = new GameWorld(app, this);

        for (Controller controller : Controllers.getControllers()) {
            Gdx.app.log(TAG, controller.getName());
        }

//        Table table = new Table();
//        table.add(new Label("Hi to the game you person!", skin)).row();
//        TextButton button = new TextButton("GO!", skin);
//        table.add(button);
//        table.pack();
//
//        button.addListener(new ChangeListener() {
//            @Override
//            public void changed (ChangeEvent event, Actor actor) {
//                go();
//            }
//        });
//        stage.addActor(table);
    }

    @Override
    public void render (float delta) {
        super.render(delta);

        gameViewport.apply();
        batch.setProjectionMatrix(gameViewport.getCamera().combined);
        world.render(delta);

        uiViewport.apply();
        stage.act(delta);
        stage.draw();
    }
}
