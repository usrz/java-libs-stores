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

import java.util.UUID;

import org.usrz.libs.utils.concurrent.NotifyingFuture;

public class AbstractStoreWrapper<D extends Document> extends AbstractStore<D> {

    protected final Store<D> store;

    public AbstractStoreWrapper(Store<D> store) {
        this.store = notNull(store, "Null store");
    }

    @Override
    public String getName() {
        return store.getName();
    }

    @Override
    public Class<D> getType() {
        return store.getType();
    }

    @Override
    public D create() {
        return store.create();
    }

    @Override
    public NotifyingFuture<D> findAsync(UUID uuid) {
        return store.findAsync(uuid);
    }

    @Override
    public NotifyingFuture<D> storeAsync(D object) {
        return store.storeAsync(object);
    }

    @Override
    public Query<D> query() {
        return store.query();
    }

}
