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

package reactor.io.netty.config;

import java.net.InetSocketAddress;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.core.scheduler.TimedScheduler;
import reactor.io.netty.common.Peer;

/**
 * @author Stephane Maldini
 */
public class HttpClientOptions extends ClientOptions {

	public static HttpClientOptions create(){
		return new HttpClientOptions();
	}

	public static HttpClientOptions to(String host){
		return to(host, Peer.DEFAULT_PORT);
	}

	public static HttpClientOptions to(String host, int port){
		return create().connect(host, port);
	}

	HttpClientOptions(){

	}


	/**
	 * The host and port to which this client should connect.
	 *
	 * @param host The host to connect to.
	 * @param port The port to connect to.
	 * @return {@literal this}
	 */
	@Override
	public HttpClientOptions connect(@Nonnull String host, int port) {
		return connect(new InetSocketAddress(host, port));
	}

	/**
	 * The address to which this client should connect.
	 *
	 * @param connectAddress The address to connect to.
	 * @return {@literal this}
	 */
	@Override
	public HttpClientOptions connect(@Nonnull InetSocketAddress connectAddress) {
		return connect(() -> connectAddress);
	}

	/**
	 * The address to which this client should connect.
	 *
	 * @param connectAddress The address to connect to.
	 * @return {@literal this}
	 */
	@Override
	public HttpClientOptions connect(@Nonnull Supplier<? extends InetSocketAddress> connectAddress) {
		super.connect(connectAddress);
		return this;
	}

	/**
	 * The host and port to which this client should connect.
	 *
	 * @param host The host to connect to.
	 * @param port The port to connect to.
	 *
	 * @return {@literal this}
	 */
	public ClientOptions proxy(@Nonnull String host, int port) {
		return proxy(new InetSocketAddress(host, port));
	}

	/**
	 * The host and port to which this client should connect.
	 *
	 * @param host The host to connect to.
	 * @param port The port to connect to.
	 *
	 * @return {@literal this}
	 */
	public ClientOptions proxy(@Nonnull String host,
			int port,
			@Nullable String username,
			@Nullable Function<? extends String, ? extends String> password) {
		return proxy(new InetSocketAddress(host, port), username, password);
	}

	/**
	 * The address to which this client should connect.
	 *
	 * @param connectAddress The address to connect to.
	 *
	 * @return {@literal this}
	 */
	public ClientOptions proxy(@Nonnull InetSocketAddress connectAddress) {
		return proxy(() -> connectAddress, null, null);
	}

	/**
	 * The address to which this client should connect.
	 *
	 * @param connectAddress The address to connect to.
	 *
	 * @return {@literal this}
	 */
	public ClientOptions proxy(@Nonnull InetSocketAddress connectAddress,
			@Nullable String username,
			@Nullable Function<? extends String, ? extends String> password) {
		return proxy(() -> connectAddress, username, password);
	}

	/**
	 * The address to which this client should connect.
	 *
	 * @param connectAddress The address to connect to.
	 *
	 * @return {@literal this}
	 */
	public ClientOptions proxy(@Nonnull Proxy type,
			@Nonnull Supplier<? extends InetSocketAddress> connectAddress) {
		return proxy(connectAddress, null, null);
	}

	/**
	 * The address to which this client should connect.
	 *
	 * @param connectAddress The address to connect to.
	 *
	 * @return {@literal this}
	 */
	public ClientOptions proxy(@Nonnull Supplier<? extends InetSocketAddress> connectAddress,
			@Nullable String username,
			@Nullable Function<? extends String, ? extends String> password) {
		proxy(Proxy.HTTP, connectAddress, username, password);
		return this;
	}

	/**
	 * Return the eventual remote host
	 * @return the eventual remote host
	 */
	@Override
	public InetSocketAddress remoteAddress() {
		return super.remoteAddress();
	}

	/**
	 *
	 * @return this {@link HttpClientOptions}
	 */
	@Override
	public HttpClientOptions sslSupport() {
		ssl(SslContextBuilder.forClient());
		return this;
	}

	/**
	 *
	 * @return immutable {@link HttpClientOptions}
	 */
	public HttpClientOptions toImmutable() {
		return new ImmutableHttpClientOptions(this);
	}

	final static class ImmutableHttpClientOptions extends HttpClientOptions {

		final HttpClientOptions options;

		ImmutableHttpClientOptions(HttpClientOptions options) {
			this.options = options;
			if(options.ssl() != null){
					super.ssl(options.ssl());
			}
		}

		@Override
		public HttpClientOptions toImmutable() {
			return this;
		}

		@Override
		public InetSocketAddress remoteAddress() {
			return options.remoteAddress();
		}

		@Override
		public EventLoopGroup eventLoopGroup() {
			return options.eventLoopGroup();
		}

		@Override
		public boolean isManaged() {
			return options.isManaged();
		}

		@Override
		public boolean keepAlive() {
			return options.keepAlive();
		}

		@Override
		public int linger() {
			return options.linger();
		}

		@Override
		public Consumer<ChannelPipeline> pipelineConfigurer() {
			return options.pipelineConfigurer();
		}

		@Override
		public long prefetch() {
			return options.prefetch();
		}

		@Override
		public int rcvbuf() {
			return options.rcvbuf();
		}

		@Override
		public int sndbuf() {
			return options.sndbuf();
		}

		@Override
		public boolean tcpNoDelay() {
			return options.tcpNoDelay();
		}

		@Override
		public int timeout() {
			return options.timeout();
		}

		@Override
		public TimedScheduler timer() {
			return options.timer();
		}

		@Override
		public HttpClientOptions connect(@Nonnull String host, int port) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions connect(@Nonnull InetSocketAddress connectAddress) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions eventLoopGroup(EventLoopGroup eventLoopGroup) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions keepAlive(boolean keepAlive) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ClientOptions proxy(@Nonnull Proxy type,
				@Nonnull Supplier<? extends InetSocketAddress> connectAddress,
				@Nullable String username,
				@Nullable Function<? extends String, ? extends String> password) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions linger(int linger) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions managed(boolean managed) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions pipelineConfigurer(Consumer<ChannelPipeline> pipelineConfigurer) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions prefetch(long prefetch) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions rcvbuf(int rcvbuf) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions ssl(SslContextBuilder sslOptions) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions sslConfigurer(Consumer<? super SslContextBuilder> consumer) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions sndbuf(int sndbuf) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions tcpNoDelay(boolean tcpNoDelay) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions timeout(int timeout) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public HttpClientOptions timer(TimedScheduler timer) {
			throw new UnsupportedOperationException("Immutable Options");
		}
	}

}
