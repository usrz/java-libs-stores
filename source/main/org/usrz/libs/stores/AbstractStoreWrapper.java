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

import static org.usrz.libs.utils.Check.notNull;

import java.util.function.Consumer;

import org.usrz.libs.stores.Defaults.Initializer;
import org.usrz.libs.utils.concurrent.NotifyingFuture;

/**
 * A <em>Wrapper</em> around another {@link Store}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <D> The type of {@link Document}s stored by this {@link Store}.
 */
public class AbstractStoreWrapper<D extends Document> extends AbstractStore<D> {

    /** The original {@link Store} wrapped by this instance. */
    protected final Store<D> store;

    /**
     * Create a new instance wrapping the specified {@link Store}.
     */
    public AbstractStoreWrapper(Store<D> store) {
        this.store = notNull(store, "Null store");
    }

    @Override
    public Class<D> getType() {
        return store.getType();
    }

    @Override
    public D create(Consumer<Initializer> consumer) {
        return store.create(consumer);
    }

    @Override
    public NotifyingFuture<D> findAsync(Id id) {
        return store.findAsync(id);
    }

    @Override
    public NotifyingFuture<D> storeAsync(D object) {
        return store.storeAsync(object);
    }

    @Override
    public NotifyingFuture<Boolean> deleteAsync(Id id) {
        return store.deleteAsync(id);
    }

    @Override
    public Query<D> query() {
        return store.query();
    }

}
