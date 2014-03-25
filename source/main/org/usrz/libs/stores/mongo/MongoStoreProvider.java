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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.CachingStore;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.utils.beans.BeanBuilder;
import org.usrz.libs.utils.caches.Cache;
import org.usrz.libs.utils.concurrent.SimpleExecutor;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public class MongoStoreProvider<D extends Document> implements Provider<Store<D>> {

    private static final Log log = new Log();

    private final String collection;
    private final Class<D> storedType;
    private final Map<BasicDBObject, BasicDBObject> indexes = new HashMap<>();
    private final Set<Class<?>> interfaces = new HashSet<>();
    private Class<?> abstractClass;
    private Key<Cache<UUID, D>> cacheKey;

    @Inject private Injector injector;
    @Inject private BSONObjectMapper mapper;
    @Inject private BeanBuilder beanBuilder;
    @Inject private DB db;

    protected MongoStoreProvider(String collection, Class<D> storedType) {
        this.collection = collection;
        this.withBeanConstructionParameters(storedType);
        this.storedType = storedType;
    }

    protected MongoStoreProvider<D> withBeanConstructionParameters(Class<?> abstractType, Class<?>... interfaces) {

        /* We *MUST* have at least one type */
        Objects.requireNonNull(abstractType, "Null abstract type");

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

    protected MongoStoreProvider<D> withIndexDefinition(boolean unique, boolean sparse, String... keys) {
        if ((keys == null) || (keys.length == 0)) throw new IllegalArgumentException("No keys to index");

        final BasicDBObject index = new BasicDBObject();
        for (String key: keys) {
            if ((key == null) || (key.isEmpty())) throw new IllegalArgumentException("Empty or null key");
            if (key.charAt(0) == '-') {
                index.put(key.substring(1), -1);
            } else if (key.charAt(0) == '+') {
                index.put(key.substring(1), 1);
            } else {
                index.put(key, 1);
            }
        }

        final BasicDBObject options = new BasicDBObject();
        options.put("unique", unique);
        options.put("sparse", sparse);

        indexes.put(index, options);

        return this;
    }

    protected MongoStoreProvider<D> withCacheKey(Key<Cache<UUID, D>> cacheKey) {
        this.cacheKey = Objects.requireNonNull(cacheKey, "Null key");
        return this;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Store<D> get() {
        if (injector == null) throw new ProvisionException(this.getClass().getSimpleName() + " not injected");

        try {
            /* Create the actual type (if needed) or just the one specified */
            final Class<MongoDocument> type;
            if (Modifier.isAbstract(abstractClass.getModifiers()) || (!interfaces.isEmpty())) {
                type = beanBuilder.newClass(abstractClass, interfaces.toArray(new Class<?>[interfaces.size()]));
            } else {
                type = (Class<MongoDocument>) abstractClass;
            }

            /* Let's notify what we're doing */
            log.info("Creating new MongoDB Store<%s> with collection: %s and type: %s", storedType.getSimpleName(), collection, type);

            /* Get a hold on our DB collection */
            final DBCollection collection = db.getCollection(this.collection);

            /* Indexes */
            for (Entry<BasicDBObject, BasicDBObject> entry: this.indexes.entrySet()) {
                log.debug("Ensuring index on collection %s: keys: %s, options: %s", collection, entry.getKey(), entry.getValue());
                collection.ensureIndex(entry.getKey(), entry.getValue());
            }

            /* Get our executor */
            final SimpleExecutor executor = injector.getInstance(SimpleExecutor.class);

            /* Create our store */
            final Store<D> store = new MongoStore(executor, mapper, injector, collection, type);
            if (cacheKey == null) return store;

            /* Caching store */
            final Cache<UUID, D> cache = injector.getInstance(cacheKey);
            return new CachingStore(executor, store, cache);

        } catch (Exception exception) {
            throw new ProvisionException(String.format("Unable to create MongoStore<%s> with collection %s", storedType.getSimpleName(), collection), exception);
        }

    }
}
