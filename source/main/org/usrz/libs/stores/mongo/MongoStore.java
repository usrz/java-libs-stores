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
import java.util.Iterator;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.usrz.libs.stores.Query;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.stores.bson.BSONParser;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public class MongoStore<D extends MongoDocument> implements Store<D> {

    private final DBCollection collection;
    private final BSONObjectMapper mapper;
    private final Injector injector;
    private final Class<D> type;

    @Inject
    public MongoStore(BSONObjectMapper mapper, Injector injector, DBCollection collection, Class<D> type) {
        this.collection = collection;
        this.injector = injector;
        this.mapper = mapper;
        this.type = type;

        collection.ensureIndex(new BasicDBObject("uuid", 1), "_uuid_", true);
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
        object.put("_id", new ObjectId());
        object.put("uuid", UUID.randomUUID());
        return convert(object);
    }

    @Override
    public D get(UUID uuid) {
        return convert(collection.findOne(new BasicDBObject("uuid", uuid)));
    }

    @Override
    public void put(D object) {
        try {
            collection.save(mapper.writeValueAsBson(object));
        } catch (IOException exception) {
            throw new MongoException("Exception mapping " + type.getName() + " to BSON", exception);
        }
    }

    /* ====================================================================== */

    @Override
    public Query<D> query() {
        return new Query<D>() {

            @Override
            public Iterator<D> results() {
                final DBCursor cursor = collection.find(query);
                return new Iterator<D>() {
                    @Override public boolean hasNext() { return cursor.hasNext(); }
                    @Override public D next() { return convert(cursor.next()); }
                    @Override public void remove() { throw new UnsupportedOperationException(); }
                };
            }

            @Override
            public D first() {
                return convert(collection.findOne(query));
            }

        };
    }

    @Override
    public Query<D>.Operator query(String field) {
        return this.query().and(field);
    }

    /* ====================================================================== */

    private D convert(DBObject object) {
        if (object == null) return null;

        try {
            final D document = mapper.readValue(new BSONParser(mapper, object), type);
            injector.injectMembers(document);
            return document;
        } catch (IOException exception) {
            throw new MongoException("Exception mapping BSON to " + type.getName(), exception);
        }
    }
}
