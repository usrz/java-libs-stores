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

import java.util.UUID;

import com.google.common.util.concurrent.Futures;

public abstract class AbstractStore<D extends Document> implements Store<D> {

    @Override
    public final D find(UUID uuid) {
        return Futures.getUnchecked(findAsync(uuid));
    }

    @Override
    public final D store(D object) {
        return Futures.getUnchecked(storeAsync(object));
    }

    @Override
    public final boolean delete(UUID uuid) {
        return Futures.getUnchecked(deleteAsync(uuid));
    }

    @Override
    public final Query.Operator<D> query(String field) {
        return this.query().and(field);
    }

}
