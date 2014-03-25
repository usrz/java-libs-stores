/* ========================================================================== *
 * Copyright 2014 USRZ.com and Pier Paolo Fumagalli                           *
 * -------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *  http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 * ========================================================================== */
package org.usrz.libs.stores.bson;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleSerializers;

public class BSONIdentitySerializer<T> extends JsonSerializer<T> {

    private static final ConcurrentHashMap<Class<?>, BSONIdentitySerializer<?>> INSTANCES = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> BSONIdentitySerializer<T> create(Class<T> type) {
        if (INSTANCES.containsKey(type)) return (BSONIdentitySerializer<T>) INSTANCES.get(type);
        final BSONIdentitySerializer<T> serializer = new BSONIdentitySerializer<T>(type);
        final BSONIdentitySerializer<?> instance = INSTANCES.putIfAbsent(type, serializer);
        return instance == null ? serializer : (BSONIdentitySerializer<T>) instance;
    }

    public static <T> void register(SimpleSerializers serializers, Class<T> type) {
        final BSONIdentitySerializer<T> serializer = create(type);
        serializers.addSerializer(type, serializer);
    }

    /* ====================================================================== */

    private final Class<T> type;

    private BSONIdentitySerializer(Class<T> type) {
        this.type = Objects.requireNonNull(type, "Null type");
    }

    @Override
    public void serialize(final T object,
                          final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider)
    throws IOException {
        jsonGenerator.writeObject(object);
    }

    @Override
    public Class<T> handledType() {
        return type;
    }

}

