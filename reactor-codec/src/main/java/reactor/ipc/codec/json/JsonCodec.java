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

package reactor.ipc.codec.json;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import reactor.ipc.buffer.Buffer;
import reactor.ipc.buffer.StringBuffer;
import reactor.ipc.codec.BufferCodec;
import reactor.ipc.codec.Codec;

/**
 * A codec for decoding JSON into Java objects and encoding Java objects into JSON.
 * @param <IN> The type to decode JSON into
 * @param <OUT> The type to encode into JSON
 * @author Jon Brisbin
 */
public class JsonCodec<IN, OUT> extends BufferCodec<IN, OUT> {

	private final Class<IN>    inputType;
	private final ObjectMapper mapper;

	/**
	 * Creates a new {@code JsonCodec} that will create instances of {@code inputType}
	 * when decoding.
	 * @param inputType The type to create when decoding.
	 */
	public JsonCodec(Class<IN> inputType) {
		this(inputType, null);
	}

	/**
	 * Creates a new {@code JsonCodec} that will create instances of {@code inputType}
	 * when decoding. The {@code customModule} will be registered with the underlying
	 * {@link ObjectMapper}.
	 * @param inputType The type to create when decoding.
	 * @param customModule The module to register with the underlying ObjectMapper
	 */
	@SuppressWarnings("unchecked")
	public JsonCodec(Class<IN> inputType, Module customModule) {
		this(inputType, customModule, Codec.DEFAULT_DELIMITER);
	}

	/**
	 * Creates a new {@code JsonCodec} that will create instances of {@code inputType}
	 * when decoding. The {@code customModule} will be registered with the underlying
	 * {@link ObjectMapper}.
	 * @param inputType The type to create when decoding.
	 * @param customModule The module to register with the underlying ObjectMapper
	 * @param delimiter A nullable delimiting byte for batch decoding
	 */
	@SuppressWarnings("unchecked")
	public JsonCodec(Class<IN> inputType, Module customModule, Byte delimiter) {
		super(delimiter);
		this.inputType = Objects.requireNonNull(inputType, "inputType must not be null");

		this.mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		if (null != customModule) {
			this.mapper.registerModule(customModule);
		}
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected IN decodeNext(Buffer buffer, Object context) {
		try {
			if (JsonNode.class.isAssignableFrom(inputType)) {
				return (IN) mapper.readTree(buffer.inputStream());
			}
			else {
				return mapper.readValue(buffer.inputStream(), inputType);
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	static final private byte HEAD_DELIMITER = (byte) '{';
	static final private byte HEAD_DELIMITER_2 = (byte) '[';
	static final private byte TAIL_DELIMITER = (byte) '}';
	static final private byte TAIL_DELIMITER_2 = (byte) ']';

	@Override
	protected int canDecodeNext(Buffer buffer, Object context) {
		int countHead = 0;
		int countHead2 = 0;
		int countTail = 0;
		int countTail2 = 0;
		int pos = 0;
		for (byte b : buffer.duplicate()) {
			pos++;
			if (b == HEAD_DELIMITER) {
				countHead++;
			}
			else if (b == TAIL_DELIMITER) {
				countTail++;
				if (countTail == countHead && countTail2 == countHead2) {
					return buffer.position() + pos;
				}
			}
			else if (b == HEAD_DELIMITER_2){
				countHead2++;
			}
			else if (b == TAIL_DELIMITER_2){
				countTail2++;
				if (countTail == countHead && countTail2 == countHead2) {
					return buffer.position() + pos;
				}
			}
		}
		return -1;
	}

	@Override
	public Buffer apply(OUT out) {
		try {
			return StringBuffer.wrap(mapper.writeValueAsBytes(out));
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}

}
