package io.piotrjastrzebski.psm;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class GameScreen extends BaseScreen {
    protected static final String TAG = GameScreen.class.getSimpleName();

    public final GameWorld world;
    public final Hud hud;
    public final Dialog menu;

    public GameScreen (SMApp app) {
        super(app);

        world = new GameWorld(app, this);
        hud = new Hud(app, this);
        stage.addActor(hud);

        menu = new Dialog("Shipania!", skin);
        menu.getTitleTable().getCell(menu.getTitleLabel()).pad(10);
        Table content = menu.getContentTable();
        String text = "Welcome to this jam vidya!" +
            "\nYou are an {insert name here} on a a {planet name}!" +
            "\nDo the {thing} to {save the world or whatever}!" +
            "\n" +
            "\nPlay with mouse and keyboard or gamepad! No touch controls scrub!" +
            "\nWhen you die you and enemies will respawn" +
            "\nFound buffs still apply!" +
            "\nThere are no sounds, music or graphics. Budget run out :(" +
            "\nHave a {platitudes}!";
        Label label = new Label(text, skin);
        label.setWrap(true);
        content.add(label).width(500).pad(40).row();

        TextButton button = new TextButton("PLAY!", skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                Events.send(Events.GAME_RESTART_REQUEST);
                menu.hide();
            }
        });
        content.add(button).size(100, 50).pad(40);
    }

    @Override
    public void show () {
        super.show();
        menu.show(stage);
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

    @Override
    public void resize (int width, int height) {
        super.resize(width, height);
        if (world != null) world.resize();
    }
}
