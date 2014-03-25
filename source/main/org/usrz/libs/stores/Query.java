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
import java.util.Iterator;
import java.util.regex.Pattern;

import org.usrz.libs.utils.concurrent.Acceptor;
import org.usrz.libs.utils.concurrent.NotifyingFuture;
import org.usrz.libs.utils.concurrent.QueuedIterator;

public interface Query<D extends Document> {

    public Operator<D> and(String key);

    public NotifyingFuture<?> documentsAsync(Acceptor<D> acceptor);

    default Iterator<D> documents() {
        final QueuedIterator<D> iterator = new QueuedIterator<>();
        this.documentsAsync(iterator);
        return iterator;
    }

    /* ====================================================================== */

    public interface Operator<D extends Document> {

        public Query<D> is(Object value);

        public Query<D> isNot(Object value);

        public Query<D> gt(Object value);

        public Query<D> gte(Object value);

        public Query<D> lt(Object value);

        public Query<D> lte(Object value);

        public Query<D> in(Collection<?> collection);

        public Query<D> notIn(Collection<?> collection);

        public Query<D> mod(int divisor, int reminder);

        public Query<D> matches(Pattern pattern);

    }
}
