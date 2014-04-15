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
import org.usrz.libs.stores.Defaults;
import org.usrz.libs.stores.Defaults.Initializer;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Id;
import org.usrz.libs.stores.Query;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.utils.concurrent.Acceptor;
import org.usrz.libs.utils.concurrent.NotifyingFuture;
import org.usrz.libs.utils.concurrent.SimpleExecutor;

import com.fasterxml.jackson.databind.InjectableValues;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;


public class MongoStore<D extends Document> extends AbstractStore<D> {

    private final SimpleExecutor executor;
    private final DBCollection collection;
    private final BSONObjectMapper mapper;
    private final Injector injector;
    private final Class<D> type;
    private final Consumer<Initializer> defaults;

    public MongoStore(SimpleExecutor executor,
                      BSONObjectMapper mapper,
                      Injector injector,
                      DBCollection collection,
                      Class<D> type) {
        this.defaults = injector.getInstance(Defaults.Finder.find(type));
        this.collection = collection;
        this.executor = executor;
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
        return convert(id(new Id()), consumer);
    }

    @Override
    public NotifyingFuture<D> findAsync(Id id) {
        return executor.call(() -> {
            return convert(collection.findOne(id(id)), null);
        });
    }

    @Override
    public NotifyingFuture<D> storeAsync(D object) {
        return executor.call(() -> {
            final BasicDBObject bson = mapper.writeValueAsBson(object);
            if (bson.containsField("id")) {
                bson.put("_id", bson.remove("id"));
            }
            collection.save(bson);
            return object;
        });
    }

    @Override
    public NotifyingFuture<Boolean> deleteAsync(Id id) {
        return executor.call(() -> (collection.remove(id(id)).getN() != 0));
    }

    /* ====================================================================== */

    @Override
    public Query<D> query() {
        return new MongoQuery<D>(new BasicDBObject()) {

            @Override
            public NotifyingFuture<?> documentsAsync(Acceptor<D> acceptor) {
                return executor.run(() -> {
                    try {
                        /* Normalize alias "id" -> "_id" */
                        if (query.containsKey((Object)"id")) {
                            query.put("_id", query.remove("id"));
                        }

                        /* Get our DB cursor and iterate over it */
                        final DBCursor cursor = collection.find(query);
                        cursor.forEach((object) -> acceptor.accept(convert(object, null)));
                        acceptor.completed();
                    } catch (Throwable throwable) {
                        acceptor.failed(throwable);
                    }
                });
            }
        };
    }

    /* ====================================================================== */

    private BasicDBObject id(Id id) {
        return new BasicDBObject("_id", notNull(id, "Null ID").toString());
    }

    /* ====================================================================== */

    private D convert(DBObject object, Consumer<Initializer> consumer) {
        if (object == null) return null;

        /*
         * Build our consumer, composing what's been given to us, what's
         * been specified in the type annotation, and something reading
         * our object
         */
        if (consumer != null) {
            injector.injectMembers(consumer);
            consumer = consumer.andThen(defaults);
        } else {
            consumer = defaults;
        }

        consumer = consumer.andThen((initializer) -> {
            object.keySet().forEach((key) -> {
                final Object value = object.get(key);
                if ("_id".equals(key)) key = "id";
                initializer.property(key, value);
            });
        });

        try {
            /* Create an initializer and build our BSON + injectables */
            final BSONInitializer initializer = new BSONInitializer();
            consumer.accept(initializer);

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

    }
}
