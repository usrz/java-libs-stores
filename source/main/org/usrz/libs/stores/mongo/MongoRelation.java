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

import org.usrz.libs.stores.AbstractRelation;
import org.usrz.libs.stores.Cursor;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

public class MongoRelation<L extends Document, R extends Document>
extends AbstractRelation<L, R> {

    private static final String L = "l";
    private static final String R = "r";

    private final DBCollection collection;
    private final Store<L> storeL;
    private final Store<R> storeR;

    public MongoRelation(DBCollection collection, Store<L> storeL, Store<R> storeR) {
        this.storeL = storeL;
        this.storeR = storeR;
        this.collection = collection;

        collection.ensureIndex(new BasicDBObject(L, 1), new BasicDBObject("unique", false).append("sparse", false));
        collection.ensureIndex(new BasicDBObject(R, 1), new BasicDBObject("unique", false).append("sparse", false));
        collection.ensureIndex(new BasicDBObject(L, 1).append(R, 1), new BasicDBObject("unique", true).append("sparse", false));

    }

    /* ====================================================================== */

    private BasicDBObject object(L l, R r) {
        return new BasicDBObject()
                         .append(L, l.getId())
                         .append(R, r.getId());
    }

    /* ====================================================================== */

    @Override
    public void associate(L l, R r) {
        final BasicDBObject object = object(l, r);
        collection.update(object, object, true, false);
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
    public Cursor<L> findL(R r) {
        /* Build our query on "R" returning only "L" */
        final BasicDBObject query = new BasicDBObject(R, r.getId().toString());
        final BasicDBObject fields = new BasicDBObject(L, 1);

        /* Get our DB cursor and iterate over it */
        return new MongoCursor<L>(collection.find(query, fields),
                (o) -> storeL.find((String) o.get(L)));
    }

    @Override
    public Cursor<R> findR(L l) {
        /* Build our query on "L" returning only "R" */
        final BasicDBObject query = new BasicDBObject(L, l.getId().toString());
        final BasicDBObject fields = new BasicDBObject(R, 1);

        /* Get our DB cursor and iterate over it */
        return new MongoCursor<R>(collection.find(query, fields),
                (o) -> storeR.find((String) o.get(R)));
    }
}
