package com.codegy.aerlink.connection.characteristic;

import android.util.Log;

import java.util.Queue;

/**
 * Created by Guiye on 12/10/16.
 */

public class CharacteristicSubscriberThread extends Thread {

    private static final String LOG_TAG = CharacteristicSubscriberThread.class.getSimpleName();

    public enum State {
        Disconnected,
        Connecting,
        Discovering,
        Subscribing,
        Ready
    }

    private CharacteristicSubscriber subscriber;
    private Queue<CharacteristicIdentifier> subscribeRequests;
    private boolean run = true;
    private State state = State.Connecting;

    private final Object lock = new Object();


    public CharacteristicSubscriberThread(CharacteristicSubscriber subscriber) {
        this.subscriber = subscriber;
    }


    @Override
    public void run() {
        while (run) {
            try {
                synchronized (lock) {
                    switch (state) {
                        case Disconnected:
                            subscriber.onConnectionFailed();
                            lock.wait();
                            break;
                        case Connecting:
                            state = State.Disconnected;
                            lock.wait(5000);
                            break;
                        case Discovering:
                            state = State.Disconnected;
                            lock.wait(2000);
                            break;
                        case Subscribing:
                            Log.d(LOG_TAG, "Subscribe Requests: " + (subscribeRequests == null ? -1 :subscribeRequests.size()));

                            CharacteristicIdentifier characteristic = subscribeRequests.peek();
                            if (characteristic != null) {
                                subscriber.subscribeCharacteristic(characteristic);

                                state = State.Disconnected;
                                lock.wait(2000);

                                break;
                            }
                            else {
                                state = State.Ready;
                            }
                        case Ready:
                            run = false;
                            subscriber.onConnectionReady();
                            break;
                    }
                }
            }
            catch (InterruptedException ignored) {}
        }
    }

    /**
     * Kill the thread, discards all characteristics in queue
     */
    public void kill() {
        run = false;

        interrupt();
    }

    public void reset() {
        state = State.Connecting;
        subscribeRequests = null;

        interrupt();
    }

    public void setConnecting() {
        synchronized(lock) {
            state = State.Connecting;

            lock.notify();
        }
    }

    public void setDiscovering() {
        synchronized(lock) {
            state = State.Discovering;

            lock.notify();
        }
    }

    public void setSubscribeRequests(Queue<CharacteristicIdentifier> subscribeRequests) {
        synchronized(lock) {
            this.subscribeRequests = subscribeRequests;
            state = State.Subscribing;

            lock.notify();
        }
    }

    /**
     * Remove current characteristic and keep going with the next one
     */
    public void remove() {
        synchronized(lock) {
            Log.d(LOG_TAG, "Characteristic subscribed");

            subscribeRequests.poll();

            if (subscribeRequests.size() > 0) {
                state = State.Subscribing;
            }
            else {
                state = State.Ready;
            }

            lock.notify();
        }
    }

}
