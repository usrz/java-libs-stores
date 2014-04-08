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
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

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
    private final Binder binder;

    protected MongoStoreBuilder(Binder binder, Annotation unique, TypeLiteral<D> type, String collection) {
        this.binder = notNull(binder, "Null binder");
        binder.skipSources(this.getClass());

        /* Get our store and bean type */
        @SuppressWarnings("unchecked")
        final TypeLiteral<Store<D>> storeType = (TypeLiteral<Store<D>>) TypeLiteral.get(Types.newParameterizedType(Store.class, type.getType()));
        @SuppressWarnings("unchecked")
        final TypeLiteral<Class<D>> beanType = (TypeLiteral<Class<D>>) TypeLiteral.get(Types.newParameterizedType(Class.class, type.getType()));

        /* Start creating and binding our collection (annotated by collection name) */
        this.collection = new MongoCollectionProvider(collection, unique);
        binder.bind(DBCollection.class)
              .annotatedWith(Names.named(collection))
              .toProvider(this.collection);

        /* Create our bean class provider */
        this.bean = new MongoBeanClassProvider<D>(type.getRawType(), unique);
        binder.bind(beanType).toProvider(this.bean);

        binder.bind(storeType).toProvider(new MongoStoreProvider<D>(unique, beanType, collection));
    }

    public MongoIndexBuilder createIndex() {
        return collection.requireIndex();
    }

    public void createIndex(Consumer<MongoIndexBuilder> consumer) {
        consumer.accept(createIndex());
    }

    public void withBeanDetails(Class<?> type, Class<?>... interfaces) {
        this.bean.setBeanDetails(type, interfaces);
    }

    public void withCache(Cache<UUID, D> cache) {
        binder.bind(new TypeLiteral<Cache<UUID, D>>(){}).toInstance(cache);
    }

    public void withCreator(Function<D, ? extends D> function) {
        binder.bind(new TypeLiteral<Function<D, ? extends D>>(){}).toInstance(function);
    }
}
