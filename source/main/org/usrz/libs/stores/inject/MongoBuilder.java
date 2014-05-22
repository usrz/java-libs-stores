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

import java.util.function.Consumer;

import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Relation;
import org.usrz.libs.stores.Stores;
import org.usrz.libs.stores.annotations.Collection;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.utils.beans.BeansModule;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongoBuilder {

    private final Binder binder;

    public MongoBuilder(Binder binder) {
        this.binder = notNull(binder).skipSources(getClass());

        /* Shared instances across all stores (not skipping this source) */
        binder.bind(BSONObjectMapper.class).asEagerSingleton();
        binder.bind(Stores.class).asEagerSingleton();
        binder.install(BeansModule.BEANS_MODULE);
    }

    /* ====================================================================== */

    public static Module apply(Consumer<MongoBuilder> consumer) {
        return (binder) -> consumer.accept(new MongoBuilder(binder));
    }

    /* ====================================================================== */

    public Binder binder() {
        return binder;
    }

    /* ====================================================================== */

    public void configure(Configurations configurations) {
        binder.bind(MongoClient.class).toProvider(new MongoClientProvider().with(configurations)).asEagerSingleton();
        binder.bind(DB.class).toProvider(new MongoDatabaseProvider().with(configurations)).asEagerSingleton();
    }

    /* ====================================================================== */

    public <D extends Document> MongoStoreBuilder<D> store(Class<D> type) {
        return store(TypeLiteral.get(type));
    }

    public <D extends Document> MongoStoreBuilder<D> store(TypeLiteral<D> type) {
        final Collection collection = type.getRawType().getAnnotation(Collection.class);
        if (collection != null) return this.store(type, collection.value());
        throw new IllegalArgumentException("Type " + type.getClass().getName() + " does not specify an @Collection annotation");
    }

    public <D extends Document> MongoStoreBuilder<D> store(Class<D> type, String collection) {
        return store(TypeLiteral.get(type), collection);
    }

    public <D extends Document> MongoStoreBuilder<D> store(TypeLiteral<D> type, String collection) {
        return new MongoStoreBuilder<D>(binder, type, collection);
    }

    /* ---------------------------------------------------------------------- */

    public <D extends Document> void store(Class<D> type, Consumer<MongoStoreBuilder<D>> consumer) {
        consumer.accept(store(type));
    }

    public <D extends Document> void store(TypeLiteral<D> type, Consumer<MongoStoreBuilder<D>> consumer) {
        consumer.accept(store(type));
    }

    public <D extends Document> void store(Class<D> type, String collection, Consumer<MongoStoreBuilder<D>> consumer) {
        consumer.accept(store(type, collection));
    }

    public <D extends Document> void store(TypeLiteral<D> type, String collection, Consumer<MongoStoreBuilder<D>> consumer) {
        consumer.accept(store(type, collection));
    }

    /* ====================================================================== */

    public <L extends Document, R extends Document> void relate(Class<L> left, Class<R> right, String collection) {
        this.relate(TypeLiteral.get(left), TypeLiteral.get(right), collection);
    }

    public <L extends Document, R extends Document> void relate(TypeLiteral<L> left, TypeLiteral<R> right, String collection) {

        /* Start creating and binding our collection (annotated by collection name) */
        MongoCollectionProvider provider = new MongoCollectionProvider(collection);
        binder.bind(DBCollection.class)
              .annotatedWith(Names.named(collection))
              .toProvider(provider);

        /* Bind our relation */
        @SuppressWarnings("unchecked")
        final TypeLiteral<Relation<L, R>> type = (TypeLiteral<Relation<L, R>>)
                TypeLiteral.get(Types.newParameterizedType(Relation.class, left.getType(), right.getType()));
        binder.bind(type).toProvider(new MongoRelationProvider<L, R>(left, right, collection));
    }
}
