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

import static org.usrz.libs.utils.Check.notNull;

import java.io.IOException;
import java.util.function.Consumer;

import org.usrz.libs.stores.AbstractStore;
import org.usrz.libs.stores.Cursor;
import org.usrz.libs.stores.Defaults;
import org.usrz.libs.stores.Defaults.Initializer;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Query;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.utils.RandomString;

import com.fasterxml.jackson.databind.InjectableValues;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;


public class MongoStore<D extends Document> extends AbstractStore<D> {

    private final DBCollection collection;
    private final BSONObjectMapper mapper;
    private final Injector injector;
    private final Class<D> type;
    private final Consumer<Initializer> creator;
    private final Consumer<Initializer> updater;

    public MongoStore(BSONObjectMapper mapper,
                      Injector injector,
                      DBCollection collection,
                      Class<D> type) {
        creator = Defaults.Finder.find(type, injector, true);
        updater = Defaults.Finder.find(type, injector, false);
        this.collection = collection;
        this.injector = injector;
        this.mapper = mapper;
        this.type = type;
    }

    @Override
    public Class<D> getType() {
        return type;
    }

    @Override
    public D create(Consumer<Initializer> consumer) {

        /*
         * Build our consumer, composing what's been given to us, what's
         * been specified in the type annotation, and something reading
         * our object
         */
        final String id = RandomString.get(32);
        if (consumer != null) {
            injector.injectMembers(consumer);
            return convert(id(id), consumer.andThen(creator));
        } else {
            return convert(id(id), creator);
        }
    }

    @Override
    public D find(String id) {
        return convert(collection.findOne(id(id)), updater);
    }

    @Override
    public D store(D object) {
        try {
             collection.save(mapper.writeValueAsBson(object));
             return object;
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
                return new MongoCursor<D>(collection.find(query), (o) -> convert(o, updater));
            }
        };
    }

    /* ====================================================================== */

    private BasicDBObject id(String id) {
        return new BasicDBObject("_id", notNull(id, "Null ID"));
    }

    /* ====================================================================== */

    private D convert(DBObject object, Consumer<Initializer> consumer) {
        if (object == null) return null;

        consumer = consumer.andThen((initializer) -> {
            object.keySet().forEach((key) -> {
                final Object value = object.get(key);
                initializer.property(key, value);
            });
        });

        try {
            /* Create an initializer and build our BSON + injectables */
            final BSONInitializer initializer = new BSONInitializer();
            consumer.accept(initializer);

            System.err.println("CREATING WITH " + initializer.bson);


            /* Map the constructed BSON to the object */
            final BasicDBObject bson = initializer.bson;
            final InjectableValues injectables = initializer.inject;
            final D document = mapper.readValue(bson, injectables, type);

            /* Just in case of some @Inject ... */
            injector.injectMembers(document);

            /* Done... */
            return document;
        } catch (IOException exception) {
            throw new MongoException("Exception mapping BSON to " + type.getName(), exception);
        }
    }

    /* ====================================================================== */

    private final class BSONInitializer implements Initializer {

        final BasicDBObject bson = new BasicDBObject();
        final InjectableValues.Std inject = new InjectableValues.Std();

        @Override
        public Initializer property(String name, Object value) {
            bson.put(name, value);
            return this;
        }

        @Override
        public Initializer inject(String name, Key<?> key) {
            inject.addValue(notNull(name, "Null name"), injector.getInstance(key));
            return this;
        }

        @Override
        public Initializer injectInstance(String name, Object instance) {
            inject.addValue(notNull(name, "Null name"), instance);
            return this;
        }
    }
}
