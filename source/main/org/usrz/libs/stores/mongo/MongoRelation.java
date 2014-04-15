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
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Id;
import org.usrz.libs.stores.Store;
import org.usrz.libs.utils.concurrent.Acceptor;
import org.usrz.libs.utils.concurrent.NotifyingFuture;
import org.usrz.libs.utils.concurrent.SimpleExecutor;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

public class MongoRelation<L extends Document, R extends Document>
extends AbstractRelation<L, R> {

    private static final String ID = "_id";
    private static final String L = "l";
    private static final String R = "r";

    private final SimpleExecutor executor;
    private final DBCollection collection;
    private final Store<L> storeL;
    private final Store<R> storeR;

    public MongoRelation(SimpleExecutor executor, DBCollection collection, Store<L> storeL, Store<R> storeR) {
        this.storeL = storeL;
        this.storeR = storeR;
        this.executor = executor;
        this.collection = collection;

        collection.ensureIndex(new BasicDBObject(L, 1), new BasicDBObject("unique", false).append("sparse", false));
        collection.ensureIndex(new BasicDBObject(R, 1), new BasicDBObject("unique", false).append("sparse", false));
        collection.ensureIndex(new BasicDBObject(L, 1).append(R, 1), new BasicDBObject("unique", true).append("sparse", false));

    }

    /* ====================================================================== */

    private BasicDBObject object(L l, R r) {
        return object(l, r, false);
    }

    private BasicDBObject object(L l, R r, boolean full) {
        final Id idL = l.getId();
        final Id idR = r.getId();
        final BasicDBObject object = new BasicDBObject(ID, idL.xor(idR).toString());
        if (full) object.append(L, idL.toString()).append(R, idR.toString());
        return object;
    }

    /* ====================================================================== */

    @Override
    public NotifyingFuture<?> associateAsync(L l, R r) {
        return this.executor.run(() -> collection.save(object(l, r, true)));
    }

    @Override
    public NotifyingFuture<?> dissociateAsync(L l, R r) {
        return this.executor.run(() -> collection.remove(object(l, r)));
    }

    @Override
    public NotifyingFuture<Boolean> isAssociatedAsync(L l, R r) {
        return this.executor.call(() -> collection.findOne(object(l, r)) != null);
    }

    @Override
    public NotifyingFuture<?> findAsyncL(R r, Acceptor<L> acceptor) {
        return executor.run(() -> {
            try {
                /* Build our query on "R" returning only "L" */
                final BasicDBObject query = new BasicDBObject(R, r.getId().toString());
                final BasicDBObject fields = new BasicDBObject(L, 1).append(ID, 0);

                /* Get our DB cursor and iterate over it */
                final DBCursor cursor = collection.find(query, fields);
                cursor.forEach((object) -> {
                    /* Find *SYNCHRONOUSLY* (we want results in order) */
                    final L document = storeL.find(new Id((String) object.get(L)));
                    if (document != null) acceptor.accept(document);
                });
                acceptor.completed();
            } catch (Throwable throwable) {
                acceptor.failed(throwable);
            }
        });
    }

    @Override
    public NotifyingFuture<?> findAsyncR(L l, Acceptor<R> acceptor) {
        return executor.run(() -> {
            try {
                /* Build our query on "L" returning only "R" */
                final BasicDBObject query = new BasicDBObject(L, l.getId().toString());
                final BasicDBObject fields = new BasicDBObject(R, 1).append(ID, 0);

                /* Get our DB cursor and iterate over it */
                final DBCursor cursor = collection.find(query, fields);
                cursor.forEach((object) -> {
                    /* Find *SYNCHRONOUSLY* (we want results in order) */
                    final R document = storeR.find(new Id((String) object.get(R)));
                    if (document != null) acceptor.accept(document);
                });
                acceptor.completed();
            } catch (Throwable throwable) {
                acceptor.failed(throwable);
            }
        });
    }

}
