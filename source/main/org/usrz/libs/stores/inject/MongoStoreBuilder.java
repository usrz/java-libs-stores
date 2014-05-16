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

import static org.usrz.libs.utils.Check.notNull;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.utils.caches.Cache;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.mongodb.DBCollection;

public class MongoStoreBuilder<D extends Document> {

    private final MongoCollectionProvider collection;
    private final MongoBeanClassProvider<D> bean;
    private final TypeLiteral<D> type;
    private final Binder binder;

    protected MongoStoreBuilder(Binder binder, Annotation unique, TypeLiteral<D> type, String collection) {
        this.type = notNull(type, "Null type literal");
        this.binder = notNull(binder, "Null binder");
        binder.skipSources(this.getClass());

        /* Start creating and binding our collection (annotated by collection name) */
        this.collection = new MongoCollectionProvider(collection, unique);
        binder.bind(DBCollection.class)
              .annotatedWith(Names.named(collection))
              .toProvider(this.collection);

        /* Create our bean Class<D> provider */
        @SuppressWarnings("unchecked")
        final TypeLiteral<Class<D>> beanType = (TypeLiteral<Class<D>>) TypeLiteral.get(Types.newParameterizedType(Class.class, type.getType()));
        this.bean = new MongoBeanClassProvider<D>(type.getRawType(), unique);
        binder.bind(beanType).toProvider(this.bean);

        /* Finally bind our Store<D> provider */
        @SuppressWarnings("unchecked")
        final TypeLiteral<Store<D>> storeType = (TypeLiteral<Store<D>>) TypeLiteral.get(Types.newParameterizedType(Store.class, type.getType()));
        binder.bind(storeType).toProvider(new MongoStoreProvider<D>(unique, type, collection));
    }

    public MongoIndexBuilder createIndex() {
        return collection.requireIndex();
    }

    public MongoStoreBuilder<D> withIndex(Consumer<MongoIndexBuilder> consumer) {
        consumer.accept(createIndex());
        return this;
    }

    public MongoStoreBuilder<D> withBeanDetails(Class<?> type, Class<?>... interfaces) {
        this.bean.setBeanDetails(type, interfaces);
        return this;
    }

    public MongoStoreBuilder<D> withCache(Cache<String, D> cache) {
        @SuppressWarnings("unchecked")
        final TypeLiteral<Cache<String, D>> cacheType = (TypeLiteral<Cache<String, D>>)
                TypeLiteral.get(Types.newParameterizedType(Cache.class, String.class, type.getType()));
        binder.bind(cacheType).toInstance(cache);
        return this;
    }

}
