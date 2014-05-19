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

import java.lang.reflect.Type;

import com.google.inject.TypeLiteral;

public interface Stores {

    public default <D extends Document> Store<D> getStore(Class<D> type) {
        return getStore(TypeLiteral.get(type));
    }

    @SuppressWarnings("unchecked")
    public default Store<?> getStore(Type type) {
        return getStore((TypeLiteral<? extends Document>) TypeLiteral.get(type));
    }

    public <D extends Document> Store<D> getStore(TypeLiteral<D> type);

    public Store<? extends Document> getStore(String collection);

}
