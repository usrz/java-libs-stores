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

import com.google.common.util.concurrent.Futures;

/**
 * An abstract implementation of the {@link Store} interface.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <D> The type of {@link Document}s stored by this {@link Store}.
 */
public abstract class AbstractStore<D extends Document> implements Store<D> {

    @Override
    public final D create() {
        return create((initializer) -> {});
    }

    @Override
    public final D find(Id id) {
        return Futures.getUnchecked(findAsync(id));
    }

    @Override
    public final D store(D object) {
        return Futures.getUnchecked(storeAsync(object));
    }

    @Override
    public final boolean delete(Id id) {
        return Futures.getUnchecked(deleteAsync(id));
    }

    @Override
    public final Query.Operator<D> query(String field) {
        return this.query().and(field);
    }

}
