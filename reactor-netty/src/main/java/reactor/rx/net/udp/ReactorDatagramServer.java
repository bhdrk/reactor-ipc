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

package reactor.rx.net.udp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

import reactor.core.publisher.Mono;
import reactor.io.ipc.RemoteFluxHandler;
import reactor.io.netty.ReactivePeer;
import reactor.io.netty.udp.DatagramServer;
import reactor.rx.net.ChannelFlux;
import reactor.rx.net.ReactorPeer;

/**
 * @author Stephane Maldini
 * @since 2.5
 */
public class ReactorDatagramServer<IN, OUT> extends ReactorPeer<IN, OUT, DatagramServer<IN, OUT>> {

	/**
	 *
	 * @param peer
	 * @param <IN>
	 * @param <OUT>
	 * @return
	 */
	public static <IN, OUT> ReactorDatagramServer<IN, OUT> create(
			DatagramServer<IN, OUT> peer) {
		return new ReactorDatagramServer<IN, OUT>(peer);
	}

	/**
	 * Start this {@literal ReactorPeer}.
	 * @return a {@link Mono<Void>} that will be complete when the {@link
	 * ReactivePeer} is started
	 */
	public Mono<Void> start(RemoteFluxHandler<IN, OUT, ChannelFlux<IN, OUT>> handler) {
		return peer.start(
				ChannelFlux.wrap(handler, peer.getDefaultTimer(), peer.getDefaultPrefetchSize())
		);
	}

	protected ReactorDatagramServer(DatagramServer<IN, OUT> datagramServer) {
		super(datagramServer);
	}

	/**
	 *
	 * @return
	 */
	public InetSocketAddress getListenAddress() {
		return peer.getListenAddress();
	}

	/**
	 *
	 * @return
	 */
	public NetworkInterface getMulticastInterface() {
		return peer.getMulticastInterface();
	}


	/**
	 * Join a multicast group.
	 *
	 * @param multicastAddress multicast address of the group to join
	 * @param iface            interface to use for multicast
	 * @return a {@link Mono} that will be complete when the group has been joined
	 */
	public final Mono<Void> join(InetAddress multicastAddress, NetworkInterface iface){
		return peer.join(multicastAddress, iface);
	}

	/**
	 * Join a multicast group.
	 *
	 * @param multicastAddress multicast address of the group to join
	 * @return a {@link Mono} that will be complete when the group has been joined
	 */
	public final Mono<Void> join(InetAddress multicastAddress) {
		return join(multicastAddress, null);
	}

	/**
	 * Leave a multicast group.
	 *
	 * @param multicastAddress multicast address of the group to leave
	 * @param iface            interface to use for multicast
	 * @return a {@link Mono} that will be complete when the group has been left
	 */
	public final Mono<Void> leave(InetAddress multicastAddress, NetworkInterface iface){
		return peer.leave(multicastAddress, iface);
	}

	/**
	 * Leave a multicast group.
	 *
	 * @param multicastAddress multicast address of the group to leave
	 * @return a {@link Mono} that will be complete when the group has been left
	 */
	public final Mono<Void> leave(InetAddress multicastAddress) {
		return leave(multicastAddress, null);
	}
}
