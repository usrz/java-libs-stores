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

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.utils.beans.BeanBuilder;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public class MongoStoreProvider<D extends Document> implements Provider<Store<D>> {

    private final String collection;
    private final Set<Class<?>> interfaces = new HashSet<>();
    private Class<?> abstractClass;
    private Store<D> store;

    protected MongoStoreProvider(String collection) {
        this.collection = collection;
    }

    protected MongoStoreProvider<D> withBeanConstructionParameters(Class<?> abstractType, Class<?>... interfaces) {

        /* We *MUST* have at least one type */
        if (abstractType == null) throw new NullPointerException("Null abstract type");

        /* Clear our interfaces */
        this.interfaces.clear();

        /* Determine what to do (abstract class or interface? */
        if (abstractType.isInterface()) {
            this.abstractClass = MongoDocument.class;
            this.interfaces.add(abstractType);
        } else {
            this.abstractClass = abstractType;
        }

        /* Add all the remaining interfaces */
        if (interfaces != null) this.interfaces.addAll(Arrays.asList(interfaces));

        /* Done */
        return this;
    }

    @Inject
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void injectAll(Injector injector, BSONObjectMapper mapper, DB db, BeanBuilder builder) {

        /* Create the actual type (if needed) or just the one specified */
        final Class<MongoDocument> type;
        if (Modifier.isAbstract(abstractClass.getModifiers()) || (!interfaces.isEmpty())) {
            type = builder.newClass(abstractClass, interfaces.toArray(new Class<?>[interfaces.size()]));
        } else {
            type = (Class<MongoDocument>) abstractClass;
        }

        /* Get a hold on our DB collection */
        final DBCollection collection = db.getCollection(this.collection);

        /* Create our store */
        this.store = new MongoStore(mapper, injector, collection, type);

    }

    @Override
    public Store<D> get() {
        return store;
    }
}
