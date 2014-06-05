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
import org.usrz.libs.stores.annotations.Collection;

import com.google.inject.TypeLiteral;

public interface MongoBindingBuilder  {

    default <D extends Document> MongoStoreBuilder<D> store(Class<D> type) {
        return store(TypeLiteral.get(type));
    }

    default <D extends Document> MongoStoreBuilder<D> store(TypeLiteral<D> type) {
        final Collection collection = type.getRawType().getAnnotation(Collection.class);
        if (collection != null) return this.store(type, collection.value());
        throw new IllegalArgumentException("Type " + type.getClass().getName() + " does not specify an @Collection annotation");
    }

    default <D extends Document> MongoStoreBuilder<D> store(Class<D> type, String collection) {
        return store(TypeLiteral.get(type), collection);
    }

    public <D extends Document> MongoStoreBuilder<D> store(TypeLiteral<D> type, String collection);

    /* ---------------------------------------------------------------------- */

    default <D extends Document> MongoBuilder store(Class<D> type, Consumer<MongoStoreBuilder<D>> consumer) {
        return store(TypeLiteral.get(type), consumer);
    }

    default <D extends Document> MongoBuilder store(TypeLiteral<D> type, Consumer<MongoStoreBuilder<D>> consumer) {
        final Collection collection = type.getRawType().getAnnotation(Collection.class);
        if (collection != null) return store(type, collection.value(), consumer);
        throw new IllegalArgumentException("Type " + type.getClass().getName() + " does not specify an @Collection annotation");
    }

    default <D extends Document> MongoBuilder store(Class<D> type, String collection, Consumer<MongoStoreBuilder<D>> consumer) {
        return store(TypeLiteral.get(type), collection, consumer);
    }

    public <D extends Document> MongoBuilder store(TypeLiteral<D> type, String collection, Consumer<MongoStoreBuilder<D>> consumer);

    /* ====================================================================== */

    default <L extends Document, R extends Document> MongoBuilder relate(Class<L> left, Class<R> right, String collection) {
        return this.relate(TypeLiteral.get(left), TypeLiteral.get(right), collection);
    }

    public <L extends Document, R extends Document> MongoBuilder relate(TypeLiteral<L> left, TypeLiteral<R> right, String collection);

}
