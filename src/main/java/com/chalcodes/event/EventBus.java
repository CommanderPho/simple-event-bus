package com.chalcodes.event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A type-safe event bus.  Only {@link #broadcast(Object)} is required to be
 * thread-safe.
 *
 * @param <T> the event type
 * @author Kevin Krumwiede
 */
public interface EventBus<T> {
	/**
	 * Registers a receiver.  May throw {@link NullPointerException} if {@code
	 * receiver} is null.
	 *
	 * @param receiver the receiver to register
	 * @return true if the receiver was registered; otherwise false
	 */
	boolean register(@Nonnull EventReceiver<T> receiver);

	/**
	 * Unregisters a receiver.  The receiver is guaranteed not to receive any
	 * further callbacks until it is registered again.  May throw {@link
	 * NullPointerException} if {@code receiver} is null.
	 *
	 * @param receiver the receiver to unregister
	 * @return true if the receiver was unregistered; otherwise false
	 */
	boolean unregister(@Nonnull EventReceiver<T> receiver);

	/**
	 * Broadcasts an event to receivers that are registered at the time this
	 * method is called, excluding any that are unregistered before the
	 * dispatch occurs.  May throw {@link NullPointerException} if {@code
	 * event} is null and the implementation does not allow null events.
	 *
	 * @param event the event to broadcast
	 */
	void broadcast(@Nullable T event);

	/**
	 * Gets the bus on which this bus broadcasts exceptions thrown by its
	 * receivers.
	 *
	 * @return the exception bus; may be null
	 */
	@Nullable EventBus<Exception> getExceptionBus();
}
