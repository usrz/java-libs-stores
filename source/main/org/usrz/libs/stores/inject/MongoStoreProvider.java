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

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Provider;

import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.stores.mongo.MongoStore;
import org.usrz.libs.utils.Injections;
import org.usrz.libs.utils.concurrent.SimpleExecutor;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.mongodb.DBCollection;

public class MongoStoreProvider<D extends Document>
implements Provider<Store<D>> {

    private final Log log = new Log();
    private final Annotation annotation;
    private final TypeLiteral<Class<D>> bean;
    private final String collection;
    private Store<D> store;

    public MongoStoreProvider(Annotation annotation, TypeLiteral<Class<D>> bean, String collection) {
        this.annotation = annotation;
        this.collection = collection;
        this.bean = bean;
    }

    @Inject
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setup(Injector injector) {
        final SimpleExecutor executor = Injections.getInstance(injector, SimpleExecutor.class, annotation);
        final BSONObjectMapper mapper = Injections.getInstance(injector, BSONObjectMapper.class, annotation);
        final DBCollection collection = Injections.getInstance(injector, DBCollection.class, Names.named(this.collection));
        final Class<D> beanClass = Injections.getInstance(injector, Key.get(bean));

        // TODO caches + creators!

        this.store = new MongoStore(executor, mapper, injector, collection, beanClass);
        final Type type = ((ParameterizedType) bean.getType()).getActualTypeArguments()[0];
        log.info("Created Store<%s> in collection %s", type.getTypeName(), collection.getName());
    }

    @Override
    public Store<D> get() {
        if (store == null) throw new IllegalStateException("Not available");
        return store;
    }

}
