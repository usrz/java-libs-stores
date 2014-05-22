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
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * A class capable of retrieving {@link Store}s either by <em>type</em> or
 * by <em>collection</em> name.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
@Singleton
public final class Stores {

    private final ConcurrentHashMap<String, Store<?>> cacheByCollection = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TypeLiteral<?>, Store<?>> cacheByType = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;
    private final Injector injector;

    @Inject
    private Stores(Injector injector) {
        this.injector = injector;
    }

    private synchronized void initialize() {
        if (initialized) return;
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

    /**
     * Return the {@link Store} associated with the given {@link Document} type.
     */
    public <D extends Document> Store<D> getStore(Class<D> type) {
        return getStore(TypeLiteral.get(type));
    }

    /**
     * Return the {@link Store} associated with the given {@link Document} type.
     */
    @SuppressWarnings("unchecked")
    public Store<?> getStore(Type type) {
        return getStore((TypeLiteral<? extends Document>) TypeLiteral.get(type));
    }

    /**
     * Return the {@link Store} associated with the given {@link Document} type.
     */
    @SuppressWarnings("unchecked")
    public <D extends Document> Store<D> getStore(final TypeLiteral<D> type) {
        if (!initialized) initialize();
        return (Store<D>) cacheByType.get(type);
    }

    /**
     * Return the {@link Store} associated with the given <em>collection</em>.
     */
    public Store<?> getStore(String collection) {
        if (!initialized) initialize();
        return cacheByCollection.get(collection);
    }

}
