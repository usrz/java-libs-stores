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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.annotations.Index;
import org.usrz.libs.stores.mongo.MongoIndex;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.mongodb.DBCollection;

public class MongoStoreBuilder<D extends Document>
implements MongoBindingBuilder {

    private final MongoCollectionProvider collection;
    private final MongoStoreProvider<D> provider;
    private final MongoBuilder builder;
    private final TypeLiteral<D> type;
    private final Binder binder;

    protected MongoStoreBuilder(MongoBuilder builder, Binder binder, TypeLiteral<D> type, String collection) {
        this.builder = notNull(builder, "Null builder");
        this.type = notNull(type, "Null type literal");
        this.binder = notNull(binder, "Null binder").skipSources(getClass());

        /* Start creating and binding our collection (annotated by collection name) */
        this.collection = new MongoCollectionProvider(collection);
        binder.bind(DBCollection.class)
              .annotatedWith(Names.named(collection))
              .toProvider(this.collection);

        /* Bind our Store<D> provider */
        @SuppressWarnings("unchecked")
        final TypeLiteral<Store<D>> storeType = (TypeLiteral<Store<D>>) TypeLiteral.get(Types.newParameterizedType(Store.class, type.getType()));
        binder.bind(storeType).toProvider(provider = new MongoStoreProvider<D>(type, collection));

        /* Process @Index annotations */
        processIndexAnnotations(discoverIndexAnnotations(type.getRawType()));
    }

    /* ====================================================================== */

    private Set<Index> discoverIndexAnnotations(Class<?> type) {
        if (type == null) return Collections.emptySet();

        final Set<Index> annotations = new HashSet<>();
        for (Index index : type.getAnnotationsByType(Index.class)) {
            annotations.add(index);
        }
        for (Class<?> interfaceClass : type.getInterfaces()) {
            annotations.addAll(discoverIndexAnnotations(interfaceClass));
        }
        annotations.addAll(discoverIndexAnnotations(type.getSuperclass()));
        return annotations;
    }

    private void processIndexAnnotations(Collection<Index> annotations) {
        for (Index index: annotations) {
            this.withIndex((builder) -> ((MongoIndex) builder).withAnnotation(index));
        }
    }

    /* ====================================================================== */

    public MongoStoreBuilder<D> withValidation() {
        return this.withValidation(true);
    }

    public MongoStoreBuilder<D> withValidation(boolean validation) {
        provider.validation = validation;
        return this;
    }

    /* ====================================================================== */

    public MongoIndexBuilder createIndex() {
        return collection.requireIndex();
    }

    public MongoStoreBuilder<D> withIndex(Consumer<MongoIndexBuilder> consumer) {
        consumer.accept(createIndex());
        return this;
    }

    public MongoStoreBuilder<D> withCache(String cacheSpec) {
        return this.withCache(CacheBuilder.from(cacheSpec).build());
    }

    public MongoStoreBuilder<D> withCache(Cache<String, D> cache) {
        @SuppressWarnings("unchecked")
        final TypeLiteral<Cache<String, D>> cacheType = (TypeLiteral<Cache<String, D>>)
                TypeLiteral.get(Types.newParameterizedType(Cache.class, String.class, type.getType()));
        binder.bind(cacheType).toInstance(cache);
        return this;
    }

    /* ====================================================================== */

    @Override
    public <X extends Document> MongoStoreBuilder<X> store(TypeLiteral<X> type, String collection) {
        return builder.store(type, collection);
    }

    @Override
    public <X extends Document> MongoBuilder store(TypeLiteral<X> type, String collection, Consumer<MongoStoreBuilder<X>> consumer) {
        return builder.store(type, collection, consumer);
    }

    @Override
    public <L extends Document, R extends Document> MongoBuilder relate(TypeLiteral<L> left, TypeLiteral<R> right, String collection) {
        return builder.relate(left, right, collection);
    }

}
