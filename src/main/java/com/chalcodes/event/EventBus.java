package com.chalcodes.event;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

/**
 * A type-safe event bus.
 *
 * @param <T> the event type
 * @author Kevin Krumwiede
 */
public class EventBus<T> {
    private final Executor mExecutor;
    private final EventBus<Exception> mExceptionBus;
    private final boolean mSticky;
    private final boolean mNullAllowed;
    private final Set<EventReceiver<T>> mReceivers = new CopyOnWriteArraySet<EventReceiver<T>>();
    private boolean mHasSticky;
    private T mStickyEvent;

    /**
     * Creates a new event bus.  The executor should be single-threaded, and
     * all methods except {@link #broadcast(Object)} should be called in the
     * executor thread.  If an exception bus is provided, any exception thrown
     * by a receiver will be broadcast on it.  If the bus is sticky, it will
     * retain the last event broadcast and dispatch it to any subsequently
     * registered receiver.
     *
     * @param executor the broadcast executor
     * @param exceptionBus the exception bus; may be null
     * @param sticky true if this bus should be sticky; otherwise false
     * @param nullAllowed true if this bus should allow null events; otherwise
     *                    false
     * @throws NullPointerException if executor is null
     */
    public EventBus(final Executor executor, final EventBus<Exception> exceptionBus,
                    final boolean sticky, final boolean nullAllowed) {
        if(executor == null) {
            throw new NullPointerException();
        }
        mExecutor = executor;
        mExceptionBus = exceptionBus;
        mSticky = sticky;
        mNullAllowed = nullAllowed;
    }

    /**
     * Gets the exception bus for this bus.
     *
     * @return the exception bus
     */
    public EventBus<Exception> getExceptionBus() {
        return mExceptionBus;
    }

    /**
     * Immediately registers a receiver.  The receiver will receive any
     * pending event dispatch.  If the bus is sticky, the receiver will
     * receive any sticky event.
     *
     * @param receiver the receiver to register
     */
    public void register(final EventReceiver<T> receiver) {
        if(receiver == null) {
            throw new NullPointerException();
        }
        if(mReceivers.add(receiver) && mHasSticky) {
            mExecutor.execute(new Runnable() {
                final T sticky = mStickyEvent;
                @Override
                public void run() {
                    if(mReceivers.contains(receiver)) {
                        dispatch(receiver, sticky);
                    }
                }
            });
        }
    }

    /**
     * Immediately unregisters a receiver.  The receiver will not receive any
     * pending event dispatch.
     *
     * @param receiver the receiver to unregister
     */
    public void unregister(final EventReceiver<T> receiver) {
        if(receiver == null) {
            throw new NullPointerException();
        }
        mReceivers.remove(receiver);
    }

    /**
     * Asynchronously broadcasts an event.  If this bus is sticky, the event
     * does not becomes stuck until the broadcast executor dispatches it.
     *
     * @param event the event to broadcast
     * @throws NullPointerException if event is null and this bus does not
     * allow null events
     */
    public void broadcast(final T event) {
        if(event == null && !mNullAllowed) {
            throw new NullPointerException();
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for(final EventReceiver<T> receiver : mReceivers) {
                    dispatch(receiver, event);
                }
                if(mSticky) {
                    mHasSticky = true;
                    mStickyEvent = event;
                }
            }
        });
    }

    /**
     * Asynchronously clears the sticky event.
     */
    public void clearSticky() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mHasSticky = false;
                mStickyEvent = null;
            }
        });
    }

    /**
     * Tests whether this bus has a sticky event.
     *
     * @return true if this bus has a sticky event; otherwise false
     */
    public boolean hasSticky() {
        return mHasSticky;
    }

    /**
     * Gets the sticky event.  Only meaningful if {@link #hasSticky()} returns
     * true.
     *
     * @return the sticky event, or null
     */
    public T getSticky() {
        return mStickyEvent;
    }

    private void dispatch(final EventReceiver<T> receiver, final T event) {
        try {
            receiver.onEvent(this, event);
        } catch(Exception e) {
            mReceivers.remove(receiver);
            if (mExceptionBus != null) {
                mExceptionBus.broadcast(e);
            }
        }
    }

}
