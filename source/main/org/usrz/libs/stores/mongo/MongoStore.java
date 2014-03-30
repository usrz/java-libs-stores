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

import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

import org.usrz.libs.stores.AbstractStore;
import org.usrz.libs.stores.Query;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.utils.concurrent.Acceptor;
import org.usrz.libs.utils.concurrent.NotifyingFuture;
import org.usrz.libs.utils.concurrent.SimpleExecutor;

import com.google.inject.Injector;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public class MongoStore<D extends MongoDocument> extends AbstractStore<D> {

    private final SimpleExecutor executor;
    private final DBCollection collection;
    private final BSONObjectMapper mapper;
    private final Function<D, D> creator;
    private final Injector injector;
    private final Class<D> type;

    protected MongoStore(SimpleExecutor executor, BSONObjectMapper mapper, Injector injector, DBCollection collection, Function<D, D> creator, Class<D> type) {
        this.collection = collection;
        this.executor = executor;
        this.injector = injector;
        this.creator = creator;
        this.mapper = mapper;
        this.type = type;
        mapper.addMixInAnnotations(type, MongoDocumentMixIn.class);
    }

    @Override
    public String getName() {
        return collection.getName();
    }

    @Override
    public Class<D> getType() {
        return type;
    }

    @Override
    public D create() {
        final BasicDBObject object = new BasicDBObject();
        object.put("_id", UUID.randomUUID());
        return creator.apply(convert(object));
    }

    @Override
    public NotifyingFuture<D> findAsync(UUID uuid) {
        return executor.call(() -> {
            return convert(collection.findOne(new BasicDBObject("_id", uuid)));
        });
    }

    @Override
    public NotifyingFuture<D> storeAsync(D object) {
        return executor.call(() -> {
            collection.save(mapper.writeValueAsBson(object));
            return object;
        });
    }

    /* ====================================================================== */

    @Override
    public Query<D> query() {
        return new MongoQuery<D>(new BasicDBObject()) {

            @Override
            public NotifyingFuture<?> documentsAsync(Acceptor<D> acceptor) {
                return executor.run(() -> {
                    try {
                        /* Normalize alias "uuid" -> "_id" */
                        if (query.containsKey((Object)"uuid")) {
                            query.put("_id", query.remove("uuid"));
                        }

                        /* Get our DB cursor and iterate over it */
                        final DBCursor cursor = collection.find(query);
                        cursor.forEach((object) -> acceptor.accept(convert(object)));
                        acceptor.completed();
                    } catch (Throwable throwable) {
                        acceptor.failed(throwable);
                    }
                });
            }
        };
    }

    /* ====================================================================== */

    private D convert(DBObject object) {
        if (object == null) return null;

        try {
            final D document = mapper.readValue(object, type);
            injector.injectMembers(document);
            return document;
        } catch (IOException exception) {
            throw new MongoException("Exception mapping BSON to " + type.getName(), exception);
        }
    }
}
