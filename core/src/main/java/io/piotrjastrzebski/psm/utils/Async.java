package io.piotrjastrzebski.psm.utils;

import com.badlogic.gdx.Gdx;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Async {
    protected static final String TAG = Async.class.getSimpleName();

    protected volatile static Async instance;
    protected ScheduledExecutorService executor;

    public Async () {
        executor = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r, "Async");
            thread.setDaemon(true);
            return thread;
        });
        instance = this;
    }

    public void dispose () {
        executor.shutdownNow();
    }


    public static void background (Runnable runnable) {
        instance.executor.submit(wrap(runnable, "bg"));
    }

    public static void background (Runnable runnable, float delay) {
        int millis = (int) (delay * 1000);
        Runnable wrapped = wrap(runnable, "bg");
        instance.executor.schedule(wrapped, millis, TimeUnit.MILLISECONDS);
    }

    public static void ui (Runnable runnable) {
        Gdx.app.postRunnable(wrap(runnable, "ui"));
    }

    public static void ui (Runnable runnable, float delay) {
        int millis = (int) (delay * 1000);
        Runnable wrapped = wrap(runnable, "ui");
        instance.executor.schedule(() -> Gdx.app.postRunnable(wrapped), millis, TimeUnit.MILLISECONDS);
    }

    static Runnable wrap (Runnable runnable, String tag) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                Gdx.app.error(TAG, ";" + tag + "' Runnable failed with exception", ex);
            }
        };
    }
}
