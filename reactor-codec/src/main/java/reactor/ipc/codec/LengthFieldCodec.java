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

package reactor.ipc.codec;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Function;

import reactor.ipc.buffer.Buffer;

/**
 * A codec that uses a length-field at the start of each chunk to denote the chunk's size.
 * During decoding the delegate is used to process each chunk. During encoding the delegate
 * is used to encode each piece of output into a buffer. The buffer is then output, with its
 * length prepended.
 *
 * @param <IN>  The type that will be produced by decoding
 * @param <OUT> The type that will be consumed by encoding
 * @author Jon Brisbin
 * @author Stephane Maldini
 */
public class LengthFieldCodec<IN, OUT> extends BufferCodec<IN, OUT> {

	private final int                    lengthFieldLength;
	private final Codec<Buffer, IN, OUT> delegate;

	/**
	 * Create a length-field codec that reads the first integer as the length of the
	 * remaining message.
	 *
	 * @param delegate The delegate {@link Codec}.
	 */
	public LengthFieldCodec(Codec<Buffer, IN, OUT> delegate) {
		this(4, delegate);
	}

	/**
	 * Create a length-field codec that reads the first short, integer, or long as the
	 * length of the remaining message, and prepends a short, integer, long to its output.
	 *
	 * @param lengthFieldLength The size of the length field. Valid values are 4 (int) or 8 (long).
	 * @param delegate          The delegate {@link Codec}.
	 */
	public LengthFieldCodec(int lengthFieldLength, Codec<Buffer, IN, OUT> delegate) {
		if(lengthFieldLength != 2 && lengthFieldLength != 4 && lengthFieldLength != 8) {
			throw new IllegalArgumentException("lengthFieldLength should be 2 (short), 4 (int), or 8 (long).");
		}
		this.lengthFieldLength = lengthFieldLength;
		this.delegate = delegate;
	}

	@Override
	public Function<Buffer, IN> decoder(final Consumer<IN> next) {
		return new DefaultInvokeOrReturnFunction<Object>(next, delegate.decoderContextProvider.get()){
			@Override
			public IN apply(Buffer buffer) {
				if(next != null){
					while(buffer.remaining() > lengthFieldLength){
						next.accept(super.apply(buffer));
					}
					return null;
				}
				return super.apply(buffer);
			}
		};
	}

	@Override
	protected int canDecodeNext(Buffer buffer, Object context) {
		if(buffer.remaining() < lengthFieldLength){
			return -1;
		}

		int limit = buffer.remaining();
		int length = readLen(buffer.duplicate());

		limit = limit - lengthFieldLength < length ? -1 : (buffer.position() + length + lengthFieldLength);

		return limit;
	}

	@Override
	protected IN decodeNext(Buffer buffer, Object context) {
		if (buffer.remaining() > lengthFieldLength) {
			int expectedLen = readLen(buffer);
			if (expectedLen < 0 || expectedLen > buffer.remaining()) {
				// This Buffer doesn't contain a full frame of data
				// reset to the start and bail out
				buffer.rewind(lengthFieldLength);
				return null;
			}
			// We have at least a full frame of data

			// save the position and limit so we can reset it later
			int pos = buffer.position();
			int limit = buffer.limit();

			// create a view of the frame
			Buffer.View v = buffer.createView(pos, pos + expectedLen);
			// call the delegate decoder with the full frame
			IN in = delegate.decodeNext(v.get());
			// reset the limit
			buffer.byteBuffer().limit(limit);
			if (buffer.position() == pos) {
				// the pointer hasn't advanced, advance it
				buffer.skip(expectedLen);
			}
			if (null != in) {
				// no Consumer was invoked, return this data
				return in;
			}
		}

		return null;
	}

	private int readLen(Buffer buffer) {
		if (lengthFieldLength == 4) {
			return buffer.readInt();
		} else if (lengthFieldLength == 2) {
			return buffer.readShort();
		} else {
			return (int) buffer.readLong();
		}
	}

	@Override
	public Buffer apply(OUT out) {
		if (null == out) {
			return null;
		}

		Buffer encoded = delegate.apply(out);
		if (null != encoded && encoded.remaining() > 0) {
			int len = encoded.remaining();
			ByteBuffer bb = null;
			if (lengthFieldLength == 4) {
				bb = ByteBuffer.allocate(len + 4);
				bb.putInt(len);
			} else if (lengthFieldLength == 2) {
				bb = ByteBuffer.allocate(len + 2);
				bb.putShort((short) len);
			} else if (lengthFieldLength == 8) {
				bb = ByteBuffer.allocate(len + 8);
				bb.putLong((long) len);
			}
			if (null != bb) {
				bb.put(encoded.byteBuffer()).flip();
				return new Buffer(bb);
			}
		}
		return encoded;
	}

}
