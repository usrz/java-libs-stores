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

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Query;
import org.usrz.libs.utils.concurrent.Acceptor;
import org.usrz.libs.utils.concurrent.NotifyingFuture;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;

public abstract class MongoQuery<D extends Document> implements Query<D> {

    protected final BasicDBObject query;

    protected MongoQuery(BasicDBObject query) {
        this.query = Objects.requireNonNull(query, "Null DBObject");
    }

    @Override
    public Operator and(String key) {
        return new Operator(Objects.requireNonNull(key, "Null key"));
    }

    @Override
    public abstract NotifyingFuture<?> documentsAsync(Acceptor<D> acceptor);

    /* ====================================================================== */

    public class Operator implements Query.Operator<D> {

        private final String field;

        private Operator(String field) {
            this.field = field;
        }

        @Override
        public MongoQuery<D> is(Object value) {
            query.append(field, value);
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> isNot(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.NE, value));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> gt(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.GT, value));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> gte(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.GTE, value));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> lt(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.LT, value));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> lte(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.LTE, value));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> in(Collection<?> collection) {
            final BasicDBList list = new BasicDBList();
            list.addAll(collection);
            query.append(field, new BasicDBObject(QueryOperators.IN, list));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> notIn(Collection<?> collection) {
            final BasicDBList list = new BasicDBList();
            list.addAll(collection);
            query.append(field, new BasicDBObject(QueryOperators.NIN, list));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> mod(int divisor, int reminder) {
            final BasicDBList list = new BasicDBList();
            list.add(divisor);
            list.add(reminder);
            query.append(field, new BasicDBObject(QueryOperators.MOD, list));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> matches(Pattern pattern) {
            query.append(field, pattern);
            return MongoQuery.this;
        }
    }
}
