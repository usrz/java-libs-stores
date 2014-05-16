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
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bson.BSONObject;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.module.guice.GuiceAnnotationIntrospector;
import com.fasterxml.jackson.module.guice.GuiceInjectableValues;
import com.google.inject.Injector;
import com.mongodb.BasicDBObject;

@Singleton
public class BSONObjectMapper extends ObjectMapper {

    public static final Version VERSION = new Version(1, 0, 0, null, "org.usrz.libs", "mongodb");

    private final Injector injector;

    @Inject
    private BSONObjectMapper(Injector injector) {
        super(new ObjectMapper());

        this.injector = injector;
        setInjectableValues(new GuiceInjectableValues(injector));

        final GuiceAnnotationIntrospector guiceIntrospector = new GuiceAnnotationIntrospector();
        final AnnotationIntrospector introspector = getSerializationConfig().getAnnotationIntrospector();
        setAnnotationIntrospectors(
            new AnnotationIntrospectorPair(guiceIntrospector, introspector),
            new AnnotationIntrospectorPair(guiceIntrospector, introspector)
        );

        /* Always use underscores in names */
        setPropertyNamingStrategy(BSONPropertyNamingStrategy.INSTANCE);

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

        /* Our deserialization context */
        _deserializationContext = _deserializationContext
                .with(_deserializationContext.getFactory().withAdditionalDeserializers(deserializers));
    }

    /* ====================================================================== */

    private final <T> T inject(T instance) {
        if (instance != null) injector.injectMembers(instance);
        return instance;
    }

    public <T> T readValue(BSONObject object, Class<T> type)
    throws JsonProcessingException, IOException {
        return inject(readBSON(reader(type), object, type));
    }

    public <T> T readValueWithView(BSONObject object, Class<T> type, Class<?> view)
    throws JsonProcessingException, IOException {
        Objects.requireNonNull(view, "Null view");
        return inject(readBSON(readerWithView(view), object, type));
    }

    private <T> T readBSON(ObjectReader reader, BSONObject object, Class<T> type)
    throws JsonProcessingException, IOException {
        return inject(reader.readValue(new BSONParser(this, object), type));
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

}
