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

import java.util.function.Consumer;

import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Relation;
import org.usrz.libs.stores.Stores;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.utils.inject.ConfiguringBindingBuilder;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongoBuilder
extends ConfiguringBindingBuilder<MongoBuilder>
implements MongoBindingBuilder {

    public MongoBuilder(Binder binder) {
        super(binder, MongoConfigurations.class);

        /* Shared instances across all stores (not skipping this source) */
        binder.bind(BSONObjectMapper.class).asEagerSingleton();
        binder.bind(Stores.class).asEagerSingleton();

        /* Client and DB binding */
        binder.bind(MongoClient.class).toProvider(MongoClientProvider.class).asEagerSingleton();
        binder.bind(DB.class).toProvider(MongoDatabaseProvider.class).asEagerSingleton();
    }

    /* ====================================================================== */

    @Override
    public <D extends Document> MongoStoreBuilder<D> store(TypeLiteral<D> type, String collection) {
        return new MongoStoreBuilder<D>(this, binder(), type, collection);
    }

    @Override
    public <D extends Document> MongoBuilder store(TypeLiteral<D> type, String collection, Consumer<MongoStoreBuilder<D>> consumer) {
        consumer.accept(store(type, collection));
        return this;
    }

    @Override
    public <L extends Document, R extends Document> MongoBuilder relate(TypeLiteral<L> left, TypeLiteral<R> right, String collection) {

        /* Start creating and binding our collection (annotated by collection name) */
        MongoCollectionProvider provider = new MongoCollectionProvider(collection);
        binder().bind(DBCollection.class)
                .annotatedWith(Names.named(collection))
                .toProvider(provider);

        /* Bind our relation */
        @SuppressWarnings("unchecked")
        final TypeLiteral<Relation<L, R>> type = (TypeLiteral<Relation<L, R>>)
                TypeLiteral.get(Types.newParameterizedType(Relation.class, left.getType(), right.getType()));
        binder().bind(type).toProvider(new MongoRelationProvider<L, R>(left, right, collection));

        /* Return ourselves */
        return this;
    }

}
