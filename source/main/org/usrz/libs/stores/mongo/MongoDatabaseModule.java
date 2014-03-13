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

import java.lang.reflect.ParameterizedType;

import javassist.ClassPool;

import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.utils.beans.BeanBuilder;
import org.usrz.libs.utils.beans.MapperBuilder;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import com.mongodb.DB;

public abstract class MongoDatabaseModule implements Module {

    private final ThreadLocal<Binder> binder = new ThreadLocal<Binder>();

    protected MongoDatabaseModule() {
        /* Nothing to do */
    }

    @Override
    public final void configure(Binder binder) {
        binder.bind(ClassPool.class).toInstance(ClassPool.getDefault());
        binder.bind(BeanBuilder.class);
        binder.bind(MapperBuilder.class);

        binder.bind(DB.class).toProvider(MongoDatabaseProvider.class);
        binder.bind(BSONObjectMapper.class).asEagerSingleton();

        try {
            this.binder.set(binder);
            this.configure();
        } finally {
            this.binder.remove();
        }
    }

    public abstract void configure();

    public <D extends Document> CollectionBindingBuilder<D> bind(Class<D> type) {
        return new CollectionBindingBuilder<D>(checkType(type));
    }

    public <D extends Document> Class<D> checkType(Class<D> type) {
        if (type.isInterface()) return type;
        if (MongoDocument.class.isAssignableFrom(type)) return type;
        throw new IllegalArgumentException("Class " + type.getName() +
                                           " must be assignable from " +
                                           MongoDocument.class.getName());
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

        public BeanCustomizerBindingBuilder<D> toCollection(String collection) {
            if (collection == null) throw new NullPointerException("Null collection");

            /* Create our mongo store provider */
            final MongoStoreProvider<D> provider = new MongoStoreProvider<D>(collection).withBeanConstructionParameters(storedType);

            /* Bind the provider and return a bean customizer */
            binder.get().bind(literal).toProvider(provider);
            binder.get().requestInjection(binder);
            return new BeanCustomizerBindingBuilder<D>(provider);
        }
    }

    /* ====================================================================== */

    public class BeanCustomizerBindingBuilder<D extends Document> {

        private final MongoStoreProvider<D> provider;

        private BeanCustomizerBindingBuilder(MongoStoreProvider<D> provider) {
            this.provider = provider;
        }

        public void withBean(Class<?> abstractClassOrInterface, Class<?>... interfaces) {
            provider.withBeanConstructionParameters(abstractClassOrInterface, interfaces);
        }

    }

}
