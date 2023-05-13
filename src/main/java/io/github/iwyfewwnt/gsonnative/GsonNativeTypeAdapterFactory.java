/*
 * Copyright 2023 iwyfewwnt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.iwyfewwnt.gsonnative;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * A type adapter factory.
 *
 * <p>Adds support for the JVM native {@code #readResolve}
 * {@literal &} {@code #writeReplace} methods.
 *
 * <p><hr>
 * <pre>{@code
 *     new GsonBuilder()
 *             .registerTypeAdapterFactory(new GsonNativeTypeAdapterFactory())
 *             .create();
 * }</pre>
 * <hr>
 */
@SuppressWarnings({"unused", "unchecked"})
public final class GsonNativeTypeAdapterFactory implements TypeAdapterFactory {

	/**
	 * A {@code #writeReplace} method name.
	 */
	private static final String WRITE_REPLACE_METHOD_NAME = "writeReplace";

	/**
	 * A {@code #readResolve} method name.
	 */
	private static final String READ_RESOLVE_METHOD_NAME = "readResolve";

	/**
	 * Initialize a {@link GsonNativeTypeAdapterFactory} instance.
	 */
	public GsonNativeTypeAdapterFactory() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		Class<? super T> rawType = type.getRawType();

		Method writeReplaceMethod = null;
		Method readResolveMethod = null;

		try {
			writeReplaceMethod = rawType.getDeclaredMethod(WRITE_REPLACE_METHOD_NAME);
			writeReplaceMethod.setAccessible(true);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException ignored) {
		}

		try {
			readResolveMethod = rawType.getDeclaredMethod(READ_RESOLVE_METHOD_NAME);
			readResolveMethod.setAccessible(true);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException ignored) {
		}

		if (writeReplaceMethod == null
				&& readResolveMethod == null) {
			return null;
		}

		Method finalWriteReplaceMethod = writeReplaceMethod;
		Method finalReadResolveMethod = readResolveMethod;

		TypeAdapter<T> context = gson.getDelegateAdapter(this, type);

		return new TypeAdapter<T>() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void write(JsonWriter out, T value) throws IOException {
				if (finalWriteReplaceMethod != null && value != null) {
					try {
						value = (T) finalWriteReplaceMethod.invoke(value);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}

				context.write(out, value);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public T read(JsonReader in) throws IOException {
				T value = context.read(in);

				if (finalReadResolveMethod != null && value != null) {
					try {
						value = (T) finalReadResolveMethod.invoke(value);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}

				return value;
			}
		};
	}
}
