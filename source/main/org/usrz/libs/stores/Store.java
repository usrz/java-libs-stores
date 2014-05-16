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

import java.util.function.Consumer;

import org.usrz.libs.stores.Defaults.Initializer;

/**
 * A {@link Store} stores {@link Document} instances.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <D> The type of {@link Document}s stored by this {@link Store}.
 */
public interface Store<D extends Document> {

    /**
     * Return the concrete {@link Class type} used by this {@link Store}.
     */
    public Class<D> getType();

    /**
     * Create a new {@link Document} instance.
     */
    public default D create() {
        return create((initializer) -> {});
    }

    /**
     * Create a new {@link Document} instance, using the specified
     * {@link Consumer} to initialize its default.
     * <p>
     * If the {@link Document} class is also annotated with the {@link Defaults}
     * annotation, bot {@link Consumer}s will be invoked in order, first the
     * one from the annotation, then the one specified here.
     */
    public D create(Consumer<Initializer> consumer);

    /**
     * Find the {@link Document} associated with the specified {@link Id}.
     */
    public D find(Id id);

    /**
     * Asynchronously find the {@link Document} associated with the specified
     * {@link Id}.
     */
    //public NotifyingFuture<D> findAsync(Id id);

    /**
     * Store the specified {@link Document}.
     */
    public D store(D object);

    /**
     * Asynchronously store the specified {@link Document}.
     */
    //public NotifyingFuture<D> storeAsync(D object);

    /**
     * Delete the {@link Document} associated with the specified {@link Id}.
     */
    public boolean delete(Id id);

    /**
     * Asynchronously delete the {@link Document} associated with the specified
     * {@link Id}.
     */
    //public NotifyingFuture<Boolean> deleteAsync(Id id);

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

}
