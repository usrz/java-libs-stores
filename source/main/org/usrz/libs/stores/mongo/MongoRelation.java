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

import java.util.UUID;

import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.DocumentIterator;
import org.usrz.libs.stores.Relation;
import org.usrz.libs.stores.Store;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

public class MongoRelation<L extends Document, R extends Document>
implements Relation<L, R> {

    private static final String ID = "_id";
    private static final String L = "l";
    private static final String R = "r";

    private final DBCollection collection;
    private final Store<L> storeL;
    private final Store<R> storeR;

    protected MongoRelation(DBCollection collection, Store<L> storeL, Store<R> storeR) {
        this.storeL = storeL;
        this.storeR = storeR;
        this.collection = collection;

        collection.ensureIndex(new BasicDBObject(L, 1), new BasicDBObject("unique", false).append("sparse", false));
        collection.ensureIndex(new BasicDBObject(R, 1), new BasicDBObject("unique", false).append("sparse", false));
        collection.ensureIndex(new BasicDBObject(L, 1).append(R, 1), new BasicDBObject("unique", true).append("sparse", false));

    }

    /* ====================================================================== */

    private BasicDBObject object(L l, R r) {
        final UUID uuidL = l.getUUID();
        final UUID uuidR = r.getUUID();
        return new BasicDBObject(ID,
                new UUID(uuidL.getMostSignificantBits()  ^ uuidR.getMostSignificantBits(),
                         uuidL.getLeastSignificantBits() ^ uuidR.getLeastSignificantBits()));
    }

    @Override
    public void associate(L l, R r) {
        final UUID uuidL = l.getUUID();
        final UUID uuidR = r.getUUID();
        collection.save(object(l, r)
                       .append(L, uuidL)
                       .append(R, uuidR));
    }

    @Override
    public void dissociate(L l, R r) {
        collection.remove(object(l, r));
    }

    @Override
    public boolean isAssociated(L l, R r) {
        return collection.findOne(object(l, r)) != null;
    }

    @Override
    public DocumentIterator<L> findL(R r) {
        final DBCursor cursor = this.collection.find(new BasicDBObject(R, r.getUUID()), new BasicDBObject(L, 1).append(ID, 0));
        return new ResultsIterator<L>(cursor, storeL, L);
    }

    @Override
    public DocumentIterator<R> findR(L l) {
        final DBCursor cursor = this.collection.find(new BasicDBObject(L, l.getUUID()), new BasicDBObject(R, 1).append(ID, 0));
        return new ResultsIterator<R>(cursor, storeR, R);
    }

    /* ====================================================================== */

    private static final class ResultsIterator<D extends Document>
    extends DocumentIterator<D> {

        private final DBCursor cursor;
        private final Store<D> store;
        private final String key;

        private ResultsIterator(DBCursor cursor, Store<D> store, String key) {
            this.cursor = cursor;
            this.store = store;
            this.key = key;
        }

        @Override
        public boolean hasNext() {
            return cursor.hasNext();
        }

        @Override
        public UUID nextUUID() {
            return (UUID) cursor.next().get(key);
        }

        @Override
        public D next() {
            return store.find(nextUUID());
        }

    }
}
