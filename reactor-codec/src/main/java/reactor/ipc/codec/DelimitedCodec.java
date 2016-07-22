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
 * An implementation of {@link Codec} that decodes by splitting a {@link Buffer} into segments
 * based on a delimiter and encodes by appending its delimiter to each piece of output.
 * During decoding the delegate is used to process each segment. During encoding the delegate
 * is used to create a buffer for each piece of output to which the delimiter is then appended.
 *
 * @param <IN>  The type that will be produced by decoding
 * @param <OUT> The type that will be consumed by encoding
 * @author Jon Brisbin
 * @author Stephane Maldini
 */
public class DelimitedCodec<IN, OUT> extends BufferCodec<IN, OUT> {

	private final Codec<Buffer, IN, OUT> delegate;
	private final boolean                stripDelimiter;

	/**
	 * Create a line-feed-delimited codec, using the given {@code Codec} as a delegate.
	 *
	 * @param delegate The delegate {@link Codec}.
	 */
	public DelimitedCodec(Codec<Buffer, IN, OUT> delegate) {
		this((byte) 10, true, delegate);

	}

	/**
	 * Create a line-feed-delimited codec, using the given {@code Codec} as a delegate.
	 *
	 * @param stripDelimiter Flag to indicate whether the delimiter should be stripped from the
	 *                       chunk or not during decoding.
	 * @param delegate       The delegate {@link Codec}.
	 */
	public DelimitedCodec(boolean stripDelimiter, Codec<Buffer, IN, OUT> delegate) {
		this((byte) 10, stripDelimiter, delegate);

	}

	/**
	 * Create a delimited codec using the given delimiter and using the given {@code Codec}
	 * as a delegate.
	 *
	 * @param delimiter      The delimiter to use.
	 * @param stripDelimiter Flag to indicate whether the delimiter should be stripped from the
	 *                       chunk or not during decoding.
	 * @param delegate       The delegate {@link Codec}.
	 */
	public DelimitedCodec(byte delimiter, boolean stripDelimiter, Codec<Buffer, IN, OUT> delegate) {
		super(delimiter, delegate.decoderContextProvider);
		this.stripDelimiter = stripDelimiter;
		this.delegate = delegate;
	}

	@Override
	public Function<Buffer, IN> decoder(Consumer<IN> next) {
		return new BufferInvokeOrReturnFunction<>(next, delegate.decoderContextProvider.get());
	}



	@Override
	protected IN decodeNext(Buffer buffer, Object context) {
		Buffer b = buffer;
		if(delimiter != null){
			int end = buffer.indexOf(delimiter);
			if(end != - 1) {
				b = buffer.duplicate().limit(stripDelimiter ? end - 1 : end);
				buffer.skip((stripDelimiter ? Math.min(buffer.limit(), end) : end) - buffer.position());
			}
		}
		return delegate.decodeNext(b, context);
	}

	@Override
	public Buffer apply(OUT out) {
		Buffer encoded = delegate.apply(out);
		if (null != encoded && encoded.remaining() > 0) {
			return encoded.newBuffer().append(encoded).append(delimiter).flip();
		}
		return null;
	}

}
