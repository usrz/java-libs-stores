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

import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.CachingStore;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.stores.mongo.MongoStore;
import org.usrz.libs.utils.inject.InjectingProvider;
import org.usrz.libs.utils.inject.Injections;

import com.google.common.cache.Cache;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.mongodb.DBCollection;

public class MongoStoreProvider<D extends Document>
extends InjectingProvider<Store<D>> {

    private final Log log = new Log();
    private final TypeLiteral<D> type;
    private final String collection;

    public MongoStoreProvider(TypeLiteral<D> type, String collection) {
        this.collection = collection;
        this.type = type;;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Store<D> get(Injector injector) {
        final BSONObjectMapper mapper = injector.getInstance(BSONObjectMapper.class);
        final DBCollection collection = Injections.getInstance(injector, DBCollection.class, Names.named(this.collection));

        /* Create the basic store */
        Store store = new MongoStore(mapper, collection, type.getRawType(), type.getType());
        log.info("Created Store<%s> in collection \"%s\"", type, collection.getName());

        /* Caches */
        final TypeLiteral<Cache<String, D>> cacheType = (TypeLiteral<Cache<String, D>>) TypeLiteral.get(Types.newParameterizedType(Cache.class, String.class, type.getType()));
        final Cache<String, D> cache = Injections.getInstance(injector, Key.get(cacheType), true);
        if (cache != null) {
            store = new CachingStore<D>(store, cache);
            log.info("Enabling cache on Store<%s> with cache %s", type, cache);
        }

        return store;
    }

}
