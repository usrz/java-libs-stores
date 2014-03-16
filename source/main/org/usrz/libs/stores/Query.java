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
package org.usrz.libs.stores;

import java.util.Collection;
import java.util.regex.Pattern;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;

public abstract class Query<D extends Document> {

    protected final BasicDBObject query = new BasicDBObject();

    protected Query() {
        /* Nothing to do */
    }

    public Operator and(String key) {
        if (key == null) throw new NullPointerException("Null key");
        return new Operator(key);
    }

    public abstract DocumentIterator<D> results();

    public abstract D first();

    /* ====================================================================== */

    public class Operator {

        private final String field;

        private Operator(String field) {
            this.field = field;
        }

        public Query<D> is(Object value) {
            query.append(field, value);
            return Query.this;
        }

        public Query<D> isNot(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.NE, value));
            return Query.this;
        }

        public Query<D> gt(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.GT, value));
            return Query.this;
        }

        public Query<D> gte(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.GTE, value));
            return Query.this;
        }

        public Query<D> lt(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.LT, value));
            return Query.this;
        }

        public Query<D> lte(Object value) {
            query.append(field, new BasicDBObject(QueryOperators.LTE, value));
            return Query.this;
        }

        public Query<D> in(Collection<?> collection) {
            final BasicDBList list = new BasicDBList();
            list.addAll(collection);
            query.append(field, new BasicDBObject(QueryOperators.IN, list));
            return Query.this;
        }

        public Query<D> notIn(Collection<?> collection) {
            final BasicDBList list = new BasicDBList();
            list.addAll(collection);
            query.append(field, new BasicDBObject(QueryOperators.NIN, list));
            return Query.this;
        }

        public Query<D> mod(int divisor, int reminder) {
            final BasicDBList list = new BasicDBList();
            list.add(divisor);
            list.add(reminder);
            query.append(field, new BasicDBObject(QueryOperators.MOD, list));
            return Query.this;
        }

        public Query<D> matches(Pattern pattern) {
            query.append(field, pattern);
            return Query.this;
        }

    }
}
