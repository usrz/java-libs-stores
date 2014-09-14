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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.usrz.libs.stores.Query.Field;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * A {@link Store} stores {@link Document} instances.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <D> The type of {@link Document}s stored by this {@link Store}.
 */
public interface Store<D extends Document> {

    /**
     * Return the {@link Type} stored by this instance.
     * <p>
     * This might not be the actual {@linkplain #getDocumentClass() document
     * class}, as a definition for a {@link Store} might take an interface,
     * and can be <em>{@linkplain ParameterizedType parameterized}.</em>.
     */
    public Type getDocumentType();

    /**
     * Return the concrete {@linkplain Class class} used by this {@link Store}.
     */
    public Class<D> getDocumentClass();

    /**
     * Return the underlying collection name associated with the {@link Store}.
     */
    public String getCollection();

    /**
     * Find the {@link Document} associated with the specified {@link Id}.
     */
    public D find(String id);

    /**
     * Store the specified {@link Document}.
     */
    public D store(D object);

    /**
     * Delete the {@link Document} associated with the specified {@link Id}.
     */
    public boolean delete(String id);

    /**
     * Return a {@link Query} instance capable of searching {@link Document}s
     * stored by this {@link Store}.
     */
    public Query<D> query();

    /**
     * Return a {@link Query} instance capable of searching {@link Document}s
     * stored by this {@link Store} conveniently specifying the base
     * <em>field</em> to search for.
     * <p>
     * This is equivalent to calling {@code this.query().and(field)}.
     */
    public default Query.Operator<D> query(String field) {
        return this.query().and(field);
    }

    /**
     * Return a {@link Query} instance capable of searching {@link Document}s
     * stored by this {@link Store} conveniently specifying the base
     * <em>field</em> to search for.
     * <p>
     * This is equivalent to calling {@code this.query().and(field)}.
     */
    public default Query.Operator<D> query(Field field) {
        return this.query().and(field);
    }

    /**
     * Return a {@link Query} instance capable of searching {@link Document}s
     * stored by this {@link Store} conveniently specifying the base
     * <em>field</em> to search for.
     * <p>
     * This is equivalent to calling {@code this.query().and(field).is(value)}.
     */
    public default Query<D> query(String field, Object value) {
        return this.query().and(field).is(value);
    }

    /**
     * Return a {@link Query} instance capable of searching {@link Document}s
     * stored by this {@link Store} conveniently specifying the base
     * <em>field</em> to search for.
     * <p>
     * This is equivalent to calling {@code this.query().and(field).is(value)}.
     */
    public default Query<D> query(Field field, Object value) {
        return this.query().and(field).is(value);
    }

}
