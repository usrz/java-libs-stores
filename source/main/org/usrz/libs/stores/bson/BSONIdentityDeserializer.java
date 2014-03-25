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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;

public class BSONIdentityDeserializer<T> extends JsonDeserializer<T> {

    private static final ConcurrentHashMap<Class<?>, BSONIdentityDeserializer<?>> INSTANCES = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> BSONIdentityDeserializer<T> create(Class<T> type) {
        if (INSTANCES.containsKey(type)) return (BSONIdentityDeserializer<T>) INSTANCES.get(type);
        final BSONIdentityDeserializer<T> serializer = new BSONIdentityDeserializer<T>(type);
        final BSONIdentityDeserializer<?> instance = INSTANCES.putIfAbsent(type, serializer);
        return instance == null ? serializer : (BSONIdentityDeserializer<T>) instance;
    }

    public static <T> void register(SimpleDeserializers deserializers, Class<T> type) {
        final BSONIdentityDeserializer<T> deserializer = create(type);
        deserializers.addDeserializer(type, deserializer);
    }

    /* ====================================================================== */

    private final Class<T> type;

    private BSONIdentityDeserializer(Class<T> type) {
        this.type = Objects.requireNonNull(type, "Null type");
    }

    @Override
    public Class<T> handledType() {
        return type;
    }

    @Override @SuppressWarnings("unchecked")
    public T deserialize(JsonParser jsonParser, DeserializationContext ctxt)
    throws IOException, JsonProcessingException {
        final Object object = jsonParser.getEmbeddedObject();
        if (object == null) return null;

        /* Normal casting */
        if (this.type.isInstance(object)) return type.cast(object);

        /* Primitives casting */
        if (this.type.isPrimitive()) return (T) object;

        /* Fail */
        throw new JsonParseException("Unable to cast " + object.getClass().getName() + " to " + type.getName(), jsonParser.getCurrentLocation());
    }

}

