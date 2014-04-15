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
import org.usrz.libs.utils.concurrent.NotifyingFuture;

public interface Store<D extends Document> {

    public String getName();

    public Class<D> getType();

    public D create();

    public D create(Consumer<Initializer> consumer);

    public D find(Id id);

    public NotifyingFuture<D> findAsync(Id id);

    public D store(D object);

    public NotifyingFuture<D> storeAsync(D object);

    public boolean delete(Id id);

    public NotifyingFuture<Boolean> deleteAsync(Id id);

    public Query<D> query();

    public Query.Operator<D> query(String field);

}
