package io.piotrjastrzebski.psm;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.msg.MessageDispatcher;
import com.badlogic.gdx.ai.msg.TelegramProvider;
import com.badlogic.gdx.ai.msg.Telegraph;

public class Events {
    private static final MessageDispatcher dispatcher = new MessageDispatcher();

    public static final int ASSETS_LOADED = 10000;
    public static final int ENTITY_KILLED = 10001;
    public final static int PF_REQUEST = 10005;
    public final static int PF_RESPONSE = 10006;



    public static void update (float delta) {
        // timepiece is used for keeping track of time in events
        GdxAI.getTimepiece().update(delta);
        dispatcher.update();
    }

    public static void dispose () {
        dispatcher.clear();
    }

    public static void register (Telegraph listener, int msg) {
        dispatcher.addListener(listener, msg);
    }

    public static void register (Telegraph listener, int... msgs) {
        for (int msg : msgs) {
            register(listener, msg);
        }
    }

    public static void unregister (Telegraph listener, int msg) {
        dispatcher.removeListener(listener, msg);
    }

    public static void unregister (Telegraph listener, int... msgs) {
        for (int msg : msgs) {
            unregister(listener, msg);
        }
    }

    public static void send (int msg) {
        dispatcher.dispatchMessage(msg);
    }

    public static void send (int msg, Object extraInfo) {
        dispatcher.dispatchMessage(msg, extraInfo);
    }

    public static void send (Telegraph sender, int msg) {
        dispatcher.dispatchMessage(sender, msg);
    }

    public static void send (Telegraph sender, int msg, Object extraInfo) {
        dispatcher.dispatchMessage(sender, msg, extraInfo);
    }

    public static void sendDelayed (float delay, int msg) {
        dispatcher.dispatchMessage(delay, msg);
    }

    public static void sendDelayed (float delay, int msg, Object extraInfo) {
        dispatcher.dispatchMessage(delay, msg, extraInfo);
    }


    public static void provide (TelegramProvider provider, int msg) {
        dispatcher.addProvider(provider, msg);
    }

    public static void provide (TelegramProvider provider, int... msgs) {
        dispatcher.addProviders(provider, msgs);
    }

}
