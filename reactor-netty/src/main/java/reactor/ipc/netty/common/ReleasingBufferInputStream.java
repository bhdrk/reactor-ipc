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
package reactor.ipc.netty.common;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

/**
 * @author Stephane Maldini
 */
final class ReleasingBufferInputStream extends ByteBufInputStream {

	final ByteBuf bb;

	volatile int closed;

	static final AtomicIntegerFieldUpdater<ReleasingBufferInputStream> CLOSE =
	AtomicIntegerFieldUpdater.newUpdater(ReleasingBufferInputStream.class, "closed");

	public ReleasingBufferInputStream(ByteBuf bb) {
		super(bb.retain());
		this.bb = bb;
	}

	@Override
	public void close() throws IOException {
		if(CLOSE.compareAndSet(this, 0, 1)) {
			super.close();
			bb.release();
		}
	}
}
