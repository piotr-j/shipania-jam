package io.piotrjastrzebski.psm;

import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.AlphaAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import io.piotrjastrzebski.psm.entities.PlayerShipEntity;

public class Hud extends Table implements Telegraph {
    final SMApp app;
    final Skin skin;

    Table hpContainer;
    Label hpLabel;
    Slider hpSlider;


    Table dmgContainer;
    Label dmgLabel;

    public Hud (SMApp app, GameScreen gameScreen) {
        this.app = app;
        skin = app.assets.skin;
        setFillParent(true);

        {
            hpContainer = new Table();
            hpContainer.setFillParent(true);

            Table inner = new Table();
            hpSlider = new Slider(0, 1, .01f, false, skin);
            hpLabel = new Label("0/0", skin);
            hpLabel.setFontScale(2);
            hpLabel.setColor(Color.LIME);
            inner.add(hpLabel).padRight(10);
            inner.add(hpSlider);

            hpContainer.add(inner).expand().top().left().pad(40);
        }
        {
            dmgContainer = new Table();
            dmgContainer.setFillParent(true);

            dmgLabel = new Label("15", skin);
            dmgLabel.setFontScale(2);
            dmgLabel.setColor(Color.SCARLET);
            dmgContainer.add(dmgLabel).expand().top().right().pad(40);
        }


        Events.register(this, Events.PLAYER_SPAWNED);
        Events.register(this, Events.PLAYER_KILLED);
        Events.register(this, Events.PLAYER_HP_CHANGED);
    }

    @Override
    public boolean handleMessage (Telegram msg) {
        switch (msg.message) {
        case Events.PLAYER_SPAWNED: {
            PlayerShipEntity player = (PlayerShipEntity)msg.extraInfo;
            update(player);
            showHud();
        } break;
        case Events.PLAYER_KILLED: {
            showDeath();
            hideHud();
        } break;
        case Events.PLAYER_HP_CHANGED: {
            PlayerShipEntity player = (PlayerShipEntity)msg.extraInfo;
            update(player);
        } break;
        }
        return false;
    }

    private void update (PlayerShipEntity player) {
        float hp = player.health();
        float mhp = player.maxHealth();
        hpLabel.setText("HP: "+player.health() + "/" + player.maxHealth());
        hpSlider.setValue(hp/mhp);

        dmgLabel.setText("DMG: " + player.damage());
    }

    private void showHud () {
        addActor(hpContainer);
        addActor(dmgContainer);
    }

    private void hideHud () {
        hpContainer.remove();
        dmgContainer.remove();
    }

    private void showDeath () {
        Table container = new Table();
        container.setTransform(true);

        Label label = new Label("YOU DIED!", skin);

        label.pack();
        container.add(label);
        container.setPosition(getWidth()/2, getHeight()/2, Align.center);
        container.setScale(4);
        container.addAction(Actions.sequence(
            Actions.scaleTo(6, 6, 1.5f, Interpolation.sine),
            Actions.removeActor()
        ));
        addActor(container);
    }
}
