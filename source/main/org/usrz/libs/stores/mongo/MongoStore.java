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
package org.usrz.libs.stores.mongo;

import static org.usrz.libs.stores.annotations.Id.ID;
import static org.usrz.libs.stores.annotations.LastModified.LAST_MODIFIED;
import static org.usrz.libs.utils.Check.notNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.function.Consumer;

import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.AbstractStore;
import org.usrz.libs.stores.Cursor;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Query;
import org.usrz.libs.stores.annotations.Defaults;
import org.usrz.libs.stores.annotations.Defaults.Initializer;
import org.usrz.libs.stores.annotations.Indexed;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.utils.Strings;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.inject.Injector;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;


public class MongoStore<D extends Document> extends AbstractStore<D> {

    private static final Log log = new Log();

    private final DBCollection collection;
    private final BSONObjectMapper mapper;
    private final Injector injector;
    private final Type type;
    private final Class<D> rawType;
    private final Consumer<Initializer> creator;
    private final boolean lastModified;

    public MongoStore(BSONObjectMapper mapper,
                      Injector injector,
                      DBCollection collection,
                      Type type,
                      Class<D> rawType) {
        this.collection = notNull(collection, "Null collection");
        this.injector = notNull(injector, "Null injector");
        this.mapper = notNull(mapper, "Null mapper");
        this.type = notNull(type, "Null type");
        this.rawType = notNull(rawType, "Null raw type");

        /* Figure out our defaults creator */
        creator = Defaults.Finder.find(rawType, injector);

        /* Figure out possible indexes from the bean description */
        boolean lastModified = false;
        final JavaType javaType = SimpleType.construct(rawType);
        final SerializationConfig config = mapper.getSerializationConfig();
        final BeanDescription description = config.getClassIntrospector().forSerialization(config, javaType, null);
        for (BeanPropertyDefinition property: description.findProperties()) {

            /* Check if we have some "_last_modified" property */
            if (property.getName().equals(LAST_MODIFIED)) {
                if (property.couldSerialize())
                    log.warn("Type %s defines accessor for \"%s\" property in class %s",
                             type.getTypeName(), LAST_MODIFIED, rawType.getName());
                lastModified |= property.couldDeserialize();
            }
            ensureIndex(property);
        }

        /* Remember if we can deserialize the "_last_modified_at" field */
        this.lastModified = lastModified;
    }

    private void ensureIndex(BeanPropertyDefinition property) {
        final String name = property.getName();
        final AnnotatedMember accessor = property.getAccessor();
        final AnnotatedMember mutator = property.getMutator();

        /* Jackson copies annotations from mutator to accessor, too */
        final AnnotatedMember member = accessor != null ? accessor : mutator;

        final Indexed annotation = member.getAnnotation(Indexed.class);
        if (annotation == null) return;
        new MongoIndex().withAnnotation(name, annotation).ensureIndex(collection);
    }

    @Override
    public Type getDocumentType() {
        return type;
    }

    @Override
    public Class<D> getDocumentClass() {
        return rawType;
    }

    @Override
    public String getCollection() {
        return collection.getName();
    }

    @Override
    public D create(Consumer<Initializer> consumer) {

        /*
         * Build our consumer, composing what's been given to us, what's
         * been specified in the type annotation, and something reading
         * our object
         */
        final String id = Strings.random(32);
        if (consumer != null) {
            injector.injectMembers(consumer);
            return convert(id(id), consumer.andThen(creator));
        } else {
            return convert(id(id), creator);
        }
    }

    @Override
    public D find(String id) {
        return convert(collection.findOne(id(id)));
    }

    @Override
    public D store(D object) {
        try {
            final BasicDBObject bson = mapper.writeValueAsBson(object);
            bson.put(LAST_MODIFIED, new Date());
            log.debug("Saving %s in collection \"%s\"", bson, collection);
            collection.save(bson);
            return convert(bson);
        } catch (IOException exception) {
            throw new MongoException("Unable to map object to BSON", exception);
        }

    }

    @Override
    public boolean delete(String id) {
        return collection.remove(id(id)).getN() != 0;
    }

    /* ====================================================================== */

    @Override
    public Query<D> query() {
        return new MongoQuery<D>(new BasicDBObject()) {

            @Override
            public Cursor<D> documents() {
                return new MongoCursor<D>(collection.find(query), (o) -> convert(o));
            }
        };
    }

    /* ====================================================================== */

    private BasicDBObject id(String id) {
        return new BasicDBObject(ID, notNull(id, "Null ID"));
    }

    /* ====================================================================== */

    private D convert(DBObject object) {
        return this.convert(object, null);
    }

    private D convert(DBObject object, Consumer<Initializer> consumer) {
        if (object == null) return null;

        final Consumer<Initializer> copier = (initializer) ->
                object.keySet().forEach((key) ->
                        initializer.property(key, object.get(key)));

        consumer = consumer == null ? copier : consumer.andThen(copier);

        try {
            /* Create an initializer and build our BSON + injectables */
            final BSONInitializer initializer = new BSONInitializer();
            consumer.accept(initializer);

            /* Map the constructed BSON to the object */
            final BasicDBObject bson = initializer.bson;
            if (!lastModified) bson.remove(LAST_MODIFIED);
            return mapper.readValue(bson, rawType);
        } catch (IOException exception) {
            throw new MongoException("Exception mapping BSON to " + rawType.getName(), exception);
        }
    }

    /* ====================================================================== */

    private final class BSONInitializer implements Initializer {

        final BasicDBObject bson = new BasicDBObject();

        @Override
        public Initializer property(String name, Object value) {
            bson.put(name, value);
            return this;
        }

    }
}
