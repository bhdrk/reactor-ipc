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

import java.util.function.Consumer;
import java.util.function.Function;

import reactor.ipc.buffer.Buffer;

/**
 * {@code Codec} for decoding data into length-field-based {@link Frame Frames}.
 * @author Jon Brisbin
 * @author Stephane Maldini
 */
public class FrameCodec extends BufferCodec<Frame, Frame> {

	public enum LengthField {
		SHORT, INT, LONG
	}

	private final LengthField lengthField;
	private final int         prefixLength;
	private final int         minRequiredLen;

	public FrameCodec(int prefixLength, LengthField lengthField) {
		this.prefixLength = prefixLength;
		this.lengthField = lengthField;
		this.minRequiredLen = lengthFieldLength(lengthField) + prefixLength;
	}

	@Override
	public Function<Buffer, Frame> decoder(final Consumer<Frame> next) {
		return new DefaultInvokeOrReturnFunction<Void>(next) {
			@Override
			public Frame apply(Buffer buffer) {
				if (next != null) {
					while (buffer.remaining() > minRequiredLen) {
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
		if(buffer.remaining() < minRequiredLen){
			return -1;
		}

		int limit = buffer.remaining();
		int length = readLen(buffer.duplicate());

		limit = limit - prefixLength < length ? -1 : (buffer.position() + length + minRequiredLen - prefixLength);

		return limit;
	}

	@Override
	protected Frame decodeNext(Buffer buffer, Object context) {
		if (buffer.remaining() > minRequiredLen) {
			int pos = buffer.position();
			int limit = buffer.limit();

			Buffer.View prefix = readPrefix(buffer);
			if (null == prefix) {
				// insufficient data
				buffer.limit(limit);
				buffer.position(pos);
				return null;
			}

			Buffer.View data = readData(prefix, buffer);
			if (null == data) {
				// insufficient data
				buffer.limit(limit);
				buffer.position(pos);
				return null;
			}

			Buffer prefixBuff = new Buffer(prefixLength, true).append(prefix.get())
			                                                  .flip();
			Buffer dataBuff =
					new Buffer(data.getEnd() - data.getStart(), true).append(data.get())
					                                                 .flip();

			buffer.position(data.getEnd());
			buffer.limit(limit);

			return new Frame(prefixBuff, dataBuff);
		}
		else {
			return null;
		}
	}

	private Buffer.View readPrefix(Buffer buffer) {
		if (buffer.remaining() < prefixLength) {
			return null;
		}

		int pos = buffer.position();
		Buffer.View prefix = buffer.createView(pos, pos + prefixLength);
		buffer.position(pos + prefixLength);

		return prefix;
	}

	private Buffer.View readData(Buffer.View prefix, Buffer buffer) {
		int pos = buffer.position();
		int limit = buffer.limit();

		int len = readLen(prefix.get());
		if (len == -1 || buffer.remaining() < len) {
			buffer.limit(limit);
			buffer.position(pos);
			return null;
		}

		pos = buffer.position();
		Buffer.View data = buffer.createView(pos, pos + len);
		buffer.position(pos + len);

		return data;
	}

	private int readLen(Buffer buffer) {
		switch (lengthField) {
			case SHORT:
				if (buffer.remaining() >= 2) {
					return buffer.readShort();
				}
				break;
			case INT:
				if (buffer.remaining() >= 4) {
					return buffer.readInt();
				}
				break;
			case LONG:
				if (buffer.remaining() >= 8) {
					return (int) buffer.readLong();
				}
				break;
		}

		return -1;
	}

	@Override
	public Buffer apply(Frame frame) {
		return null;
	}

	private static int lengthFieldLength(LengthField lf) {
		switch (lf) {
			case SHORT:
				return 2;
			case INT:
				return 4;
			default:
				return 8;
		}
	}

}
