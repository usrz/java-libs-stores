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
package org.usrz.libs.stores.inject;

import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.Stores;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

@Singleton
public class MongoGuiceStores implements Stores {

    private final ConcurrentHashMap<String, Store<?>> cacheByCollection = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TypeLiteral<?>, Store<?>> cacheByType = new ConcurrentHashMap<>();

    @Inject
    private MongoGuiceStores(Injector injector) {
        injector.getAllBindings().keySet().forEach((key) -> {
            if ((key.getTypeLiteral().getRawType().equals(Store.class)) &&
                (key.getAnnotation() == null)) {

                /* Non annotated store key */
                final Store<?> store = (Store<?>) injector.getInstance(key);
                final TypeLiteral<?> literal = TypeLiteral.get(store.getDocumentType());
                final String collection = store.getCollection();
                cacheByCollection.put(collection, store);
                cacheByType.put(literal, store);
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends Document> Store<D> getStore(final TypeLiteral<D> type) {
        return (Store<D>) cacheByType.get(type);
    }

    @Override
    public Store<?> getStore(String collection) {
        return cacheByCollection.get(collection);
    }

}
