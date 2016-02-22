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

import reactor.aeron.Context;
import reactor.aeron.utils.AeronTestUtils;

/**
 * @author Anatoly Kadyshev
 */
public class AeronProcessorUnicastVerificationTest extends AeronProcessorCommonVerificationTest {

	private final String SENDER_CHANNEL = AeronTestUtils.availableLocalhostChannel();

	private final String RECEIVER_CHANNEL = AeronTestUtils.availableLocalhostChannel();

	@Override
	protected Context createContext(int streamId) {
		return new Context()
				.name("processor")
				.autoCancel(true)
				.streamId(streamId)
				.errorStreamId(streamId + 1)
				.serviceRequestStreamId(streamId + 2)
				.senderChannel(SENDER_CHANNEL)
				.receiverChannel(RECEIVER_CHANNEL)
				.publicationRetryMillis(500)
				.ringBufferSize(1024);
	}

}
