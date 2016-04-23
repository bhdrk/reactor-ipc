/*
 * Copyright (c) 2011-2016 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.aeron.publisher;

import java.util.concurrent.atomic.AtomicBoolean;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.aeron.Context;
import reactor.aeron.subscriber.AeronServer;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.util.Logger;
import reactor.io.buffer.Buffer;

/**
 * A processor which publishes into and subscribes to data from Aeron.<br>
 * For more information about Aeron go to
 * <a href="https://github.com/real-logic/Aeron">Aeron Project Home</a>
 *
 * <p>The processor honours behaviours of both {@link AeronServer} and {@link AeronClient}
 * please refer to the corresponding documentation for configuration and implementation details.
 *
 * <p>The processor created via {@link #create(Context)} methods respects the Reactive Stream contract
 * and must not be signalled concurrently on any onXXXX methods.
 * <br/>Nonetheless Reactor allows creating of a processor which can be used by
 * publishers from different threads. In this case the processor should be
 * created via {@link #share(Context)} methods.
 *
 * @author Anatoly Kadyshev
 * @since 2.5
 */
public final class AeronProcessor extends FluxProcessor<Buffer, Buffer> {

	private static final Logger logger = Logger.getLogger(AeronProcessor.class);

	/**
	 * Reactive Publisher part of the processor - signals sender
	 */
	private final AeronClient publisher;

	/**
	 * Reactive Subscriber part of the processor - signals receiver
	 */
	private final AeronServer server;

	//FIXME: Rethink
	private volatile Subscriber<? super Buffer> delegateSubscriber;

	private final AtomicBoolean alive = new AtomicBoolean(true);

	private final Runnable onTerminateTask = new Runnable() {
		@Override
		public void run() {
			if (server.isTerminated() && publisher.isTerminated()) {
				logger.info("processor shutdown");
			}
		}
	};

	/**
	 * Creates a new processor using the context
	 *
	 * @param context configuration of the processor
	 */
	AeronProcessor(Context context, boolean multiPublishers) {
		this.server = new AeronServer(
				context,
				multiPublishers,
				this::shutdown,
				onTerminateTask);

		this.publisher = new AeronClient(context);

		logger.info("processor initialized");
	}

	public static AeronProcessor create(Context context) {
		context.validate();
		return new AeronProcessor(context, false);
	}

	public static AeronProcessor share(Context context) {
		context.validate();
		return new AeronProcessor(context, true);
	}

	@Override
	public void onSubscribe(Subscription s) {
		super.onSubscribe(s);

		//FIXME: Rethink
		server.start(channel -> {
			channel.send(new Publisher<Buffer>() {
				@Override
				public void subscribe(Subscriber<? super Buffer> subscriber) {
					delegateSubscriber = subscriber;
					subscriber.onSubscribe(s);
				}
			});

			return Mono.never();
		});
	}

	@Override
	public void subscribe(Subscriber<? super Buffer> subscriber) {
		publisher.start(request -> {
			request.receive().subscribe(subscriber);

			//FIXME: Rethink
			return Mono.never();
		});
	}

	/**
	 * Publishes Next signal containing <code>buffer</code> into Aeron.
	 *
	 * @param buffer buffer to be published
	 */
	@Override
	public void onNext(Buffer buffer) {
		delegateSubscriber.onNext(buffer);
	}

	@Override
	public void onError(Throwable t) {
		delegateSubscriber.onError(t);
	}

	@Override
	public void onComplete() {
		delegateSubscriber.onComplete();
	}

	public void shutdown() {
		if (alive.compareAndSet(true, false)) {
			server.shutdown();
			publisher.shutdown();
		}
	}

	public boolean alive() {
		return alive.get();
	}

	public boolean isTerminated() {
		return server.isTerminated() && publisher.isTerminated();
	}

}