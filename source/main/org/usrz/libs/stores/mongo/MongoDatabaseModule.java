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
package org.usrz.libs.stores.mongo;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.UUID;

import javassist.ClassPool;

import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Relation;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.utils.beans.BeanBuilder;
import org.usrz.libs.utils.beans.MapperBuilder;
import org.usrz.libs.utils.caches.Cache;
import org.usrz.libs.utils.configurations.Configurations;
import org.usrz.libs.utils.configurations.ConfigurationsModule;
import org.usrz.libs.utils.inject.ModuleSupport;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import com.mongodb.DB;

public abstract class MongoDatabaseModule extends ModuleSupport {

    private final Configurations configurations;

    protected MongoDatabaseModule(Configurations configurations) {
        if (configurations == null) throw new NullPointerException("Null configurations");
        this.configurations = configurations;
    }

    @Override
    public final void configure(Binder binder) {
        binder.install(new ConfigurationsModule() {

            @Override
            public void configure() {
                this.configure(MongoDatabaseProvider.class).with(configurations);
            }

        });

        binder.bind(ClassPool.class).toInstance(ClassPool.getDefault());
        binder.bind(BeanBuilder.class).asEagerSingleton();;
        binder.bind(MapperBuilder.class).asEagerSingleton();;

        binder.bind(DB.class).toProvider(MongoDatabaseProvider.class).asEagerSingleton();;
        binder.bind(BSONObjectMapper.class).asEagerSingleton();

        super.configure(binder);
    }

    @Override
    public abstract void configure();

    public <D extends Document> CollectionBindingBuilder<D> bind(Class<D> type) {
        return new CollectionBindingBuilder<D>(checkType(type));
    }

    public <L extends Document, R extends Document> RelationBindingBuilder<L, R> join(Class<L> typeL, Class<R> typeR) {
        if (typeL == null) throw new NullPointerException("Null type L");
        if (typeR == null) throw new NullPointerException("Null type R");
        return new RelationBindingBuilder<L, R>(typeL, typeR);
    }

    public <D extends Document> Class<D> checkType(Class<D> type) {
        if (type.isInterface()) return type;
        if (MongoDocument.class.isAssignableFrom(type)) return type;
        throw new IllegalArgumentException("Class " + type.getName() +
                                           " must be assignable from " +
                                           MongoDocument.class.getName());
    }

    /* ====================================================================== */

    public class RelationBindingBuilder<L extends Document, R extends Document> {

        private final TypeLiteral<Relation<L, R>> literal;
        private final Class<L> typeL;
        private final Class<R> typeR;

        @SuppressWarnings("unchecked")
        private RelationBindingBuilder(Class<L> typeL, Class<R> typeR) {
            final ParameterizedType storeType = Types.newParameterizedType(Relation.class, typeL, typeR);
            literal = (TypeLiteral<Relation<L, R>>) TypeLiteral.get(storeType);
            this.typeL = typeL;
            this.typeR = typeR;
        }

        public void toCollection(String collection) {
            if (collection == null) throw new NullPointerException("Null collection");

            /* Create our mongo store provider */
            final MongoRelationProvider<L, R> provider = new MongoRelationProvider<>(collection, typeL, typeR);

            /* Bind the provider and make sure it gets injected */
            binder().bind(literal).toProvider(provider).asEagerSingleton();
            binder().requestInjection(provider);
        }

    }

    /* ====================================================================== */

    public class CollectionBindingBuilder<D extends Document> {

        private final TypeLiteral<Store<D>> literal;
        private final Class<D> storedType;

        @SuppressWarnings("unchecked")
        private CollectionBindingBuilder(Class<D> type) {
            final ParameterizedType storeType = Types.newParameterizedType(Store.class, type);
            this.literal = (TypeLiteral<Store<D>>) TypeLiteral.get(storeType);
            this.storedType = type;
        }

        public StoreCustomizerBindingBuilder<D> toCollection(String collection) {
            if (collection == null) throw new NullPointerException("Null collection");

            /* Create our mongo store provider */
            final MongoStoreProvider<D> provider = new MongoStoreProvider<D>(collection, storedType);

            /* Bind the provider and return a bean customizer */
            binder().bind(literal).toProvider(provider).asEagerSingleton();
            binder().requestInjection(provider);
            return new StoreCustomizerBindingBuilder<D>(provider, storedType);
        }
    }

    /* ====================================================================== */

    public class StoreCustomizerBindingBuilder<D extends Document> {

        private final MongoStoreProvider<D> provider;
        private final Class<D> storedType;

        private StoreCustomizerBindingBuilder(MongoStoreProvider<D> provider, Class<D> storedType) {
            this.provider = provider;
            this.storedType = storedType;
        }

        public StoreCustomizerBindingBuilder<D> withBean(Class<?> abstractClassOrInterface, Class<?>... interfaces) {
            provider.withBeanConstructionParameters(abstractClassOrInterface, interfaces);
            return this;
        }

        public StoreCustomizerBindingBuilder<D> withCache() {
            return this.withCache((Annotation) null);
        }

        @SuppressWarnings("unchecked")
        public StoreCustomizerBindingBuilder<D> withCache(Annotation annotation) {
            final ParameterizedType cacheType = Types.newParameterizedType(Cache.class, UUID.class, storedType);
            final TypeLiteral<Cache<UUID, D>> literal = (TypeLiteral<Cache<UUID, D>>) TypeLiteral.get(cacheType);
            return withCache(annotation == null ? Key.get(literal) : Key.get(literal, annotation));
        }

        public StoreCustomizerBindingBuilder<D> withCache(TypeLiteral<Cache<UUID, D>> literal) {
            return this.withCache(Key.get(literal));
        }


        public StoreCustomizerBindingBuilder<D> withCache(Key<Cache<UUID, D>> key) {
            provider.withCacheKey(key);
            return this;
        }

        public StoreCustomizerBindingBuilder<D> withIndex(String... keys) {
            return withIndex(false, false, keys);
        }

        public StoreCustomizerBindingBuilder<D> withIndex(boolean unique, String... keys) {
            return withIndex(unique, false, keys);
        }

        public StoreCustomizerBindingBuilder<D> withIndex(boolean unique, boolean sparse, String... keys) {
            provider.withIndexDefinition(unique, sparse, keys);
            return this;
        }
    }

}
