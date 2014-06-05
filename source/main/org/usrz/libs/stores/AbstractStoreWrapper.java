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

import java.lang.reflect.Type;

/**
 * A <em>Wrapper</em> around another {@link Store}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <D> The type of {@link Document}s stored by this {@link Store}.
 */
public class AbstractStoreWrapper<D extends Document> implements Store<D> {

    /** The original {@link Store} wrapped by this instance. */
    protected final Store<D> store;

    /**
     * Create a new instance wrapping the specified {@link Store}.
     */
    public AbstractStoreWrapper(Store<D> store) {
        this.store = notNull(store, "Null store");
    }

    @Override
    public Type getDocumentType() {
        return store.getDocumentType();
    }

    @Override
    public Class<D> getDocumentClass() {
        return store.getDocumentClass();
    }

    @Override
    public String getCollection() {
        return store.getCollection();
    }

    @Override
    public D find(String id) {
        return store.find(id);
    }

    @Override
    public D store(D object) {
        return store.store(object);
    }

    @Override
    public boolean delete(String id) {
        return store.delete(id);
    }

    @Override
    public Query<D> query() {
        return store.query();
    }

}
