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

import org.usrz.libs.stores.annotations.Defaults;
import org.usrz.libs.stores.annotations.Defaults.Initializer;
import org.usrz.libs.stores.annotations.Id;


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
     * Return the underlying collection name associated with the {@link Store}.
     */
    public String getCollection();

    /**
     * Create a new {@link Document} instance.
     * <p>
     * If the {@link Document} class is annotated with the {@link Defaults}
     * annotation, its {@link Consumer}s will be invoked.
     */
    public default D create() {
        return create((initializer) -> {});
    }

    /**
     * Create a new {@link Document} instance, using the specified
     * {@link Consumer} to initialize its defaults.
     * <p>
     * If the {@link Document} class is also annotated with the {@link Defaults}
     * annotation, both {@link Consumer}s will be invoked in order, first the
     * one from the annotation, then the one specified here.
     */
    public D create(Consumer<Initializer> consumer);

    /**
     * Find the {@link Document} associated with the specified {@link Id}.
     */
    public D find(String id);

    /**
     * Create and store a new {@link Document}.
     *
     * @see #create()
     * @see #store()
     */
    default D storeNew() {
        return store(create());
    }

    /**
     * Create and store a new {@link Document} initialized with the specified
     * {@link Consumer}.
     *
     * @see #create(Consumer)
     * @see #store()
     */
    default D storeNew(Consumer<Initializer> consumer) {
        return store(create(consumer));
    }

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

}
