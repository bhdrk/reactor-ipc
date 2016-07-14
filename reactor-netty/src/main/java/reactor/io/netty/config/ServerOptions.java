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
import java.net.NetworkInterface;
import java.net.ProtocolFamily;
import java.security.cert.CertificateException;
import java.util.function.Consumer;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import reactor.core.scheduler.TimedScheduler;
import reactor.util.Exceptions;
import reactor.io.netty.common.Peer;

/**
 * Encapsulates configuration options for server sockets.
 *
 * @author Stephane Maldini
 */
public class ServerOptions extends NettyOptions<ServerOptions> {

	/**
	 *
	 * @return
	 */
	public static ServerOptions create() {
		return new ServerOptions();
	}

	/**
	 *
	 * @return
	 */
	public static ServerOptions on(int port) {
		return on(Peer.DEFAULT_BIND_ADDRESS, port);
	}

	/**
	 *
	 * @return
	 */
	public static ServerOptions on(String address, int port) {
		return new ServerOptions().listen(address, port);
	}

	protected InetSocketAddress listenAddress;
	private   NetworkInterface  multicastInterface;
	private int            backlog        = 1000;
	private boolean        reuseAddr      = true;
	private ProtocolFamily protocolFamily = null;

	ServerOptions(){

	}

	/**
	 * Returns the configured pending connection backlog for the socket.
	 *
	 * @return The configured connection backlog size
	 */
	public int backlog() {
		return backlog;
	}

	/**
	 * Configures the size of the pending connection backlog for the socket.
	 *
	 * @param backlog The size of the backlog
	 * @return {@code this}
	 */
	public ServerOptions backlog(int backlog) {
		this.backlog = backlog;
		return this;
	}

	/**
	 * The port on which this server should listen, assuming it should bind to all available addresses.
	 *
	 * @param port The port to listen on.
	 * @return {@literal this}
	 */
	public ServerOptions listen(int port) {
		return listen(new InetSocketAddress(port));
	}

	/**
	 * The host and port on which this server should listen.
	 *
	 * @param host The host to bind to.
	 * @param port The port to listen on.
	 * @return {@literal this}
	 */
	public ServerOptions listen(String host, int port) {
		if (null == host) {
			host = "localhost";
		}
		return listen(new InetSocketAddress(host, port));
	}

	/**
	 * The {@link InetSocketAddress} on which this server should listen.
	 *
	 * @param listenAddress the listen address
	 * @return {@literal this}
	 */
	public ServerOptions listen(InetSocketAddress listenAddress) {
		this.listenAddress = listenAddress;
		return this;
	}

	/**
	 * Return the listening {@link InetSocketAddress}
	 * @return the listening address
	 */
	public InetSocketAddress listenAddress(){
		return this.listenAddress;
	}

	/**
	 * Set the interface to use for multicast.
	 *
	 * @param iface the {@link NetworkInterface} to use for multicast.
	 * @return {@literal this}
	 */
	public ServerOptions multicastInterface(NetworkInterface iface) {
		this.multicastInterface = iface;
		return this;
	}

	/**
	 * Return the multicast {@link NetworkInterface}
	 * @return the multicast {@link NetworkInterface
	 */
	public NetworkInterface multicastInterface() {
		return this.multicastInterface;
	}

	/**
	 * Returns the configured version family for the socket.
	 *
	 * @return the configured version family for the socket
	 */
	public ProtocolFamily protocolFamily() {
		return protocolFamily;
	}

	/**
	 * Configures the version family for the socket.
	 *
	 * @param protocolFamily the version family for the socket, or null for the system default family
	 * @return {@code this}
	 */
	public ServerOptions protocolFamily(ProtocolFamily protocolFamily) {
		this.protocolFamily = protocolFamily;
		return this;
	}

	/**
	 * Returns a boolean indicating whether or not {@code SO_REUSEADDR} is enabled
	 *
	 * @return {@code true} if {@code SO_REUSEADDR} is enabled, {@code false} if it is not
	 */
	public boolean reuseAddr() {
		return reuseAddr;
	}

	/**
	 * Enables or disables {@code SO_REUSEADDR}.
	 *
	 * @param reuseAddr {@code true} to enable {@code SO_REUSEADDR}, {@code false} to disable it
	 * @return {@code this}
	 */
	public ServerOptions reuseAddr(boolean reuseAddr) {
		this.reuseAddr = reuseAddr;
		return this;
	}

	/**
	 * Enable SSL service with a self-signed certificate
	 *
	 * @return {@code this}
	 */
	public ServerOptions sslSelfSigned() {
		SelfSignedCertificate ssc;
		try {
			ssc = new SelfSignedCertificate();
		}
		catch (CertificateException e) {
			throw Exceptions.bubble(e);
		}
		return ssl(SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()));
	}

	/**
	 * @return Immutable {@link ServerOptions}
	 */
	public ServerOptions toImmutable(){
		return new ImmutableServerOptions(this);
	}

	final static class ImmutableServerOptions extends ServerOptions {
		final ServerOptions options;

		ImmutableServerOptions(ServerOptions options) {
			this.options = options;
			if(options.ssl() != null){
				super.ssl(options.ssl());
			}
		}

		@Override
		public InetSocketAddress listenAddress() {
			return options.listenAddress();
		}

		@Override
		public int backlog() {
			return options.backlog();
		}

		@Override
		public NetworkInterface multicastInterface() {
			return options.multicastInterface();
		}

		@Override
		public ProtocolFamily protocolFamily() {
			return options.protocolFamily();
		}

		@Override
		public boolean reuseAddr() {
			return options.reuseAddr();
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
		public ServerOptions backlog(int backlog) {
			return super.backlog(backlog);
		}

		@Override
		public ServerOptions listen(int port) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions listen(String host, int port) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions listen(InetSocketAddress listenAddress) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions multicastInterface(NetworkInterface iface) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions protocolFamily(ProtocolFamily protocolFamily) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions reuseAddr(boolean reuseAddr) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions eventLoopGroup(EventLoopGroup eventLoopGroup) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions keepAlive(boolean keepAlive) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions linger(int linger) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions managed(boolean managed) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions pipelineConfigurer(Consumer<ChannelPipeline> pipelineConfigurer) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions prefetch(long prefetch) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions rcvbuf(int rcvbuf) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions ssl(SslContextBuilder sslOptions) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions sslConfigurer(Consumer<? super SslContextBuilder> consumer) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions toImmutable() {
			return this;
		}

		@Override
		public ServerOptions sndbuf(int sndbuf) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions tcpNoDelay(boolean tcpNoDelay) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions timeout(int timeout) {
			throw new UnsupportedOperationException("Immutable Options");
		}

		@Override
		public ServerOptions timer(TimedScheduler timer) {
			throw new UnsupportedOperationException("Immutable Options");
		}
	}

}
