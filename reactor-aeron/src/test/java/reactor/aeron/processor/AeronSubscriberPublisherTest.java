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
package reactor.aeron.processor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import reactor.io.buffer.Buffer;
import reactor.io.net.tcp.support.SocketUtils;
import reactor.rx.Streams;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * @author Anatoly Kadyshev
 */
public class AeronSubscriberPublisherTest {

	private static final int TIMEOUT_SECS = 5;

	private ThreadSnapshot threadSnapshot;

	@Before
	public void doSetup() {
		threadSnapshot = new ThreadSnapshot().take();

		AeronTestUtils.setAeronEnvProps();
	}

	@After
	public void doTearDown() throws InterruptedException {
		assertTrue(threadSnapshot.takeAndCompare(new String[] {"hash", "global"},
				TimeUnit.SECONDS.toMillis(TIMEOUT_SECS)));
	}

	@Test
	public void testNextSignalIsReceivedByPublisher() throws InterruptedException {
		final int senderPort = SocketUtils.findAvailableTcpPort();
		final int receiverPort = SocketUtils.findAvailableTcpPort();

		AeronSubscriber subscriber = AeronSubscriber.create(new Context()
				.name("subscriber")
				.senderPort(senderPort)
				.receiverChannel("udp://127.0.0.1:" + receiverPort));

		Streams.just(
				Buffer.wrap("One"),
				Buffer.wrap("Two"),
				Buffer.wrap("Three"))
				.subscribe(subscriber);

		AeronPublisher publisher = AeronPublisher.create(new Context()
				.name("publisher")
				.senderChannel("udp://127.0.0.1:" + senderPort)
				.receiverPort(receiverPort));

		TestSubscriber clientSubscriber = TestSubscriber.createWithTimeoutSecs(1);
		publisher.subscribe(clientSubscriber);
		clientSubscriber.requestUnlimited();

		clientSubscriber.assertNextSignals("One", "Two", "Three");

		subscriber.shutdown();
		publisher.shutdown();
	}

}