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
import org.usrz.libs.utils.Check;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBRef;
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
    public Operator and(Field field) {
        switch (Check.notNull(field, "Null field")) {
            case ID: return and(MongoStore.ID);
            case LAST_MODIFIED_AT: return and(MongoStore.LAST_MODIFIED_AT);
            default: throw new IllegalArgumentException("Unsupported field " + field);
        }
    }

    /* ====================================================================== */

    public class Operator implements Query.Operator<D> {

        private final String field;

        private Operator(String field) {
            this.field = field;
        }

        @Override
        public MongoQuery<D> is(Object value) {
            query.append(field, map(value));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> isNot(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.NE, map(value)));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> gt(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.GT, map(value)));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> gte(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.GTE, map(value)));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> lt(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.LT, map(value)));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> lte(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.LTE, map(value)));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> in(Collection<?> collection) {
            final BasicDBList list = new BasicDBList();
            collection.forEach((value) -> list.add(map(value)));
            query.append(field, new BasicDBObject(QueryOperators.IN, list));
            return MongoQuery.this;
        }

        @Override
        public MongoQuery<D> notIn(Collection<?> collection) {
            final BasicDBList list = new BasicDBList();
            collection.forEach((value) -> list.add(map(value)));
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

        /* ================================================================== */

        private Object map(Object object) {
            if (object == null) return null;
            if (object instanceof Document) {
                final Document document = (Document) object;
                final String id = document.id();
                final String collection = document.store().getCollection();
                return new DBRef(null, collection, id);
            }
            return object;
        }

    }
}
