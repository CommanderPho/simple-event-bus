package com.chalcodes.event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Executor;

/**
 * Creates instances of {@link SimpleEventBus}.
 *
 * @author Kevin Krumwiede
 */
public class SimpleBusFactory<T> extends AbstractBusFactory<T> {
	public SimpleBusFactory(@Nonnull final Executor executor,
							@Nullable final EventBus<Exception> exceptionBus,
							@Nonnull final ReceiverSetFactory<T> receiverSetFactory) {
		super(executor, exceptionBus, receiverSetFactory);
	}

	public SimpleBusFactory(@Nonnull final Executor executor,
							@Nullable final EventBus<Exception> exceptionBus) {
		this(executor, exceptionBus, ReceiverSetFactories.<T>hashSetFactory());
	}

	@Nonnull
	@Override
	public EventBus<T> newBus() {
		return new SimpleEventBus<T>(mExecutor, mExceptionBus, mReceiverSetFactory);
	}
}
