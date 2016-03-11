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

package reactor.rx.net.http;

import reactor.io.ipc.ChannelFlux;
import reactor.io.ipc.ChannelFluxHandler;
import reactor.io.netty.ReactiveClient;
import reactor.io.netty.ReactivePeer;

/**
 * A {@link ChannelFlux} callback that is attached on {@link ReactivePeer} or {@link ReactiveClient} initialization
 * and receives
 * all connected {@link ChannelFlux}. The {@link #apply} implementation must return a Publisher to complete or error
 * in order to close the {@link ChannelFlux}.
 *
 * @param <IN>  the type of the received data
 * @param <OUT> the type of replied data
 * @author Stephane Maldini
 * @since  2.5
 */
public interface ReactorHttpHandler<IN, OUT>
		extends ChannelFluxHandler<IN, OUT, HttpChannelFlux<IN, OUT>> {
}
