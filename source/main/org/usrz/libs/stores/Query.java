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

/**
 * An extremely simple query interface for {@link Document}s.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <D> The type of {@link Document}s queried by this.
 */
public interface Query<D extends Document> {

    /**
     * Continue this {@link Query} by <em>and</em>-ing another field search.
     */
    public Operator<D> and(String field);

    /**
     * Search the {@link Document}s matching this {@link Query}.
     */
    default Iterator<D> documents() {
        final QueuedIterator<D> iterator = new QueuedIterator<>();
        this.documentsAsync(iterator);
        return iterator;
    }

    /**
     * Asynchronously search the {@link Document}s matching this {@link Query}.
     */
    public NotifyingFuture<?> documentsAsync(Acceptor<D> acceptor);

    /* ====================================================================== */

    /**
     * An operator for matching <em>field</em> values in a {@link Query}
     *
     * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
     * @param <D> The type of {@link Document}s queried by this.
     */
    public interface Operator<D extends Document> {

        /** The <em>equals</em> operator. */
        public Query<D> is(Object value);

        /** The <em>not-equals</em> operator. */
        public Query<D> isNot(Object value);

        /** The <em>greater-than</em> operator. */
        public Query<D> gt(Object value);

        /** The <em>greater-than-or-equals</em> operator. */
        public Query<D> gte(Object value);

        /** The <em>less-than</em> operator. */
        public Query<D> lt(Object value);

        /** The <em>less-than-or-equals</em> operator. */
        public Query<D> lte(Object value);

        /** Matches any value present in the {@link Collection}. */
        public Query<D> in(Collection<?> collection);

        /** Matches any value <em>not</em> present in the {@link Collection}. */
        public Query<D> notIn(Collection<?> collection);

        /** The <em>modulo</em> operator. */
        public Query<D> mod(int divisor, int reminder);

        /** Matches any string value according to the given {@link Pattern}. */
        public Query<D> matches(Pattern pattern);

    }
}
