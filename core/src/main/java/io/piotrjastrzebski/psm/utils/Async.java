package io.piotrjastrzebski.psm.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

    public static void update (float dt) {
        instance.process(dt);
    }

    Array<PendingRunnable> pendingRunnables = new Array<>();
    private void process (float dt) {
        Array.ArrayIterator<PendingRunnable> it = pendingRunnables.iterator();
        while (it.hasNext()) {
            PendingRunnable next = it.next();
            next.remainingDelay -= dt;
            if (next.remainingDelay <= 0) {
                next.runnable.run();
                it.remove();
            }
        }
    }

    public void dispose () {
        executor.shutdownNow();
    }

    public static void ui (Runnable runnable) {
        Gdx.app.postRunnable(wrap(runnable, "ui"));
    }

    public static void ui (Runnable runnable, float delay) {
        Runnable wrapped = wrap(runnable, "ui");
        instance.runLater(() -> Gdx.app.postRunnable(wrapped), delay);
    }

    private void runLater (Runnable runnable, float delay) {
        pendingRunnables.add(new PendingRunnable(runnable, delay));
    }

    static class PendingRunnable {
        Runnable runnable;
        float remainingDelay;

        public PendingRunnable (Runnable runnable, float delay) {
            this.runnable = runnable;
            this.remainingDelay = delay;
        }
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
