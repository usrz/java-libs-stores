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

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.bson.BSONObject;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.mongodb.BasicDBObject;

public class BSONObjectMapper extends ObjectMapper {

    public static final Version VERSION = new Version(1, 0, 0, null, "org.usrz.libs", "mongodb");

    public BSONObjectMapper() {
        this(null);
    }

    public BSONObjectMapper(ObjectMapper mapper) {
        /* Copy base configurations (or default if null) */
        super(mapper == null ? new ObjectMapper() : mapper);

        /* Always use underscores in names */
        setPropertyNamingStrategy(CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        /*
         * We can't use a module here, as they seem to be shared across all
         * mappers (or, somehow, even after a "copy()" their addition causes
         * the behavior of the parent to be modified. Do it manually...
         */
        final SimpleSerializers serializers = new SimpleSerializers();
        final SimpleDeserializers deserializers = new SimpleDeserializers();
        for (Class<?> type: BSONIdentityMappings.handledTypes()) {
            BSONIdentitySerializer.register(serializers, type);
            BSONIdentityDeserializer.register(deserializers, type);
        }

        _serializerFactory = _serializerFactory
                .withAdditionalSerializers(serializers);
        _serializerProvider = _serializerProvider.createInstance(
                getSerializationConfig(), _serializerFactory);

        _deserializationContext = _deserializationContext.with(
                _deserializationContext.getFactory()
                        .withAdditionalDeserializers(deserializers));
    }

    /* ====================================================================== */

    public <T> T readValue(BSONObject object, Class<T> type)
    throws JsonProcessingException, IOException {
        return readBSON(reader(type), object, type);
    }

    public <T> T readValueWithView(BSONObject object, Class<T> type, Class<?> view)
    throws JsonProcessingException, IOException {
        Objects.requireNonNull(view, "Null view");
        return readBSON(readerWithView(view), object, type);
    }

    private <T> T readBSON(ObjectReader reader, BSONObject object, Class<T> type)
    throws JsonProcessingException, IOException {
        return reader.readValue(new BSONParser(this, object), type);
    }

    /* ====================================================================== */

    public BasicDBObject writeValueAsBson(Object object)
    throws JsonGenerationException, JsonMappingException, IOException {
        return writeBson(writer(), object);
    }

    public BasicDBObject writeValueAsBsonWithView(Object object, Class<?> view)
    throws JsonGenerationException, JsonMappingException, IOException {
        Objects.requireNonNull(view, "Null view");
        return writeBson(writerWithView(view), object);
    }

    private BasicDBObject writeBson(ObjectWriter writer, Object object)
    throws JsonGenerationException, JsonMappingException, IOException {
        final BSONGenerator generator = new BSONGenerator(this);
        writer.writeValue(generator, object);
        final BSONObject bson = generator.getOutputTarget();
        if (bson instanceof BasicDBObject) return (BasicDBObject) bson;
        final Map<?, ?> map = bson instanceof Map ? (Map<?, ?>) bson : bson.toMap();
        return new BasicDBObject(map);
    }

    /* ====================================================================== */

    @Override
    public BSONObjectMapper copy() {
        return new BSONObjectMapper(this);
    }
}
