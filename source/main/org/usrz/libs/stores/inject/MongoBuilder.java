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

import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Relation;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.utils.Injections;
import org.usrz.libs.utils.beans.BeanBuilder;
import org.usrz.libs.utils.concurrent.SimpleExecutor;
import org.usrz.libs.utils.concurrent.SimpleExecutorProvider;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongoBuilder {

    private final Annotation unique = Injections.unique();
    private final Binder binder;

    public MongoBuilder(Binder binder) {
        this.binder = notNull(binder);
        binder.skipSources(this.getClass());

        /* Basic bindings, all uniquely annotated */
        this.binder.bind(MongoClient.class).toProvider(new MongoClientProvider().with(unique)).asEagerSingleton();
        this.binder.bind(DB.class).toProvider(new MongoDatabaseProvider().with(unique)).asEagerSingleton();

        /* Shared instances across all stores */
        this.binder.bind(BSONObjectMapper.class).annotatedWith(unique).toInstance(new BSONObjectMapper());
        this.binder.bind(BeanBuilder.class).annotatedWith(unique).toInstance(new BeanBuilder());
    }

    /* ====================================================================== */

    public static Module apply(Consumer<MongoBuilder> consumer) {
        return (binder) -> consumer.accept(new MongoBuilder(binder));
    }

    /* ====================================================================== */

    public void configure(Configurations configurations) {
        /* Bind our configurations */
        binder.bind(Configurations.class).annotatedWith(unique).toInstance(configurations);

        /* If we have executor configurations, inject them */
        final Configurations executor = configurations.extract("executor");
        if (!executor.isEmpty()) withExecutor(executor);
    }

    /* ====================================================================== */

    public void withExecutor(Configurations configurations) {
        /* Bind our simple executor with its configurations */
        binder.bind(SimpleExecutor.class)
              .annotatedWith(unique)
              .toProvider(new SimpleExecutorProvider().with(configurations));
    }

    /* ====================================================================== */

    public <D extends Document> MongoStoreBuilder<D> store(Class<D> type, String collection) {
        return store(TypeLiteral.get(type), collection);
    }

    public <D extends Document> MongoStoreBuilder<D> store(TypeLiteral<D> type, String collection) {
        return new MongoStoreBuilder<D>(binder, unique, type, collection);
    }

    /* ---------------------------------------------------------------------- */

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
        MongoCollectionProvider provider = new MongoCollectionProvider(collection, unique);
        binder.bind(DBCollection.class)
              .annotatedWith(Names.named(collection))
              .toProvider(provider);

        /* Bind our relation */
        @SuppressWarnings("unchecked")
        final TypeLiteral<Relation<L, R>> type = (TypeLiteral<Relation<L, R>>)
                TypeLiteral.get(Types.newParameterizedType(Relation.class, left.getType(), right.getType()));
        binder.bind(type).toProvider(new MongoRelationProvider<L, R>(unique, left, right, collection));
    }
}
