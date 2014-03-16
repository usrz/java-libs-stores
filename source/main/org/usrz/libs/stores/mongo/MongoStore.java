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

import org.usrz.libs.stores.DocumentIterator;
import org.usrz.libs.stores.Query;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.bson.BSONObjectMapper;

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

    protected MongoStore(BSONObjectMapper mapper, Injector injector, DBCollection collection, Class<D> type) {
        this.collection = collection;
        this.injector = injector;
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
        return convert(object);
    }

    @Override
    public D find(UUID uuid) {
        return convert(collection.findOne(new BasicDBObject("_id", uuid)));
    }

    @Override
    public D store(D object) {
        try {
            collection.save(mapper.writeValueAsBson(object));
            return object;
        } catch (IOException exception) {
            throw new MongoException("Exception mapping " + type.getName() + " to BSON", exception);
        }
    }

    /* ====================================================================== */

    @Override
    public Query<D> query() {
        return new Query<D>() {

            @Override
            public DocumentIterator<D> results() {
                /* Normalize alias "uuid" -> "_id" */
                if (query.containsKey((Object)"uuid")) {
                    query.put("_id", query.remove("uuid"));
                }
                final DBCursor cursor = collection.find(query);
                return new DocumentIterator<D>() {
                    @Override public boolean hasNext() { return cursor.hasNext(); }
                    @Override public D next() { return convert(cursor.next()); }
                };
            }

            @Override
            public D first() {
                /* Normalize alias "uuid" -> "_id" */
                if (query.containsKey((Object)"uuid")) {
                    query.put("_id", query.remove("uuid"));
                }
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
            final D document = mapper.readValue(object, type);
            injector.injectMembers(document);
            return document;
        } catch (IOException exception) {
            throw new MongoException("Exception mapping BSON to " + type.getName(), exception);
        }
    }
}
