/*
 * Copyright (c) 2011-2014 Pivotal Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package reactor.io.net;

import java.net.InetSocketAddress;

import org.reactivestreams.Publisher;
import reactor.Flux;
import reactor.Mono;
import reactor.io.buffer.Buffer;

/**
 * {@code Channel} is a virtual connection that often matches with a Socket or a Channel (e.g. Netty).
 * Implementations handle interacting inbound (received data) and errors by subscribing to {@link #input()}.
 * <p>
 * Writing and "flushing" is controlled by sinking 1 or more {@link #writeWith(Publisher)}
 * that will forward data to outbound.
 * When a drained Publisher completes or error, the channel will automatically "flush" its pending writes.
 *
 * @author Stephane Maldini
 * @since 2.5
 */
public interface ReactiveChannel<IN, OUT>  {

	/**
	 * Get the address of the remote peer.
	 *
	 * @return the peer's address
	 */
	InetSocketAddress remoteAddress();

	/**
	 * Send data to the peer, listen for any error on write and close on terminal signal (complete|error).
	 * If more than one publisher is attached (multiple calls to writeWith()) completion occurs after all publishers
	 * complete.
	 *
	 * @param dataStream the dataStream publishing OUT items to write on this channel
	 * @return A Publisher to signal successful sequence write (e.g. after "flush") or any error during write
	 */
	Mono<Void> writeWith(Publisher<? extends OUT> dataStream);

	/**
	 * Send bytes to the peer, listen for any error on write and close on terminal signal (complete|error).
	 * If more than one publisher is attached (multiple calls to writeWith()) completion occurs after all publishers
	 * complete.
	 *
	 * @param dataStream the dataStream publishing Buffer items to write on this channel
	 * @return A Publisher to signal successful sequence write (e.g. after "flush") or any error during write
	 */
	Mono<Void> writeBufferWith(Publisher<? extends Buffer> dataStream);

	/**
	 * Get the input publisher (request body or incoming tcp traffic for instance)
	 *
	 * @return A Publisher to signal reads and stop reading when un-requested.
	 */
	Publisher<IN> input();

	/**
	 * Assign event handlers to certain channel lifecycle events.
	 *
	 * @return ConsumerSpec to build the events handlers
	 */
	ConsumerSpec on();

	/**
	 * @return The underlying IO runtime connection reference (Netty Channel for instance)
	 */
	Object delegate();

	/**
	 * Spec class for assigning multiple event handlers on a channel.
	 */
	interface ConsumerSpec {
		/**
		 * Assign a {@link Runnable} to be invoked when the channel is closed.
		 *
		 * @param onClose the close event handler
		 * @return {@literal this}
		 */
		ConsumerSpec close(Runnable onClose);

		/**
		 * Assign a {@link Runnable} to be invoked when reads have become idle for the given timeout.
		 *
		 * @param idleTimeout the idle timeout
		 * @param onReadIdle  the idle timeout handler
		 * @return {@literal this}
		 */
		ConsumerSpec readIdle(long idleTimeout, Runnable onReadIdle);

		/**
		 * Assign a {@link Runnable} to be invoked when writes have become idle for the given timeout.
		 *
		 * @param idleTimeout the idle timeout
		 * @param onWriteIdle the idle timeout handler
		 * @return {@literal this}
		 */
		ConsumerSpec writeIdle(long idleTimeout, Runnable onWriteIdle);
	}

}
