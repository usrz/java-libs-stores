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
package org.usrz.libs.stores;

import java.util.UUID;

import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.caches.Cache;

public class CachingStore<D extends Document> implements Store<D> {

    private static final Log log = new Log();
    private final Store<D> store;
    private final Cache<UUID, D> cache;

    public CachingStore(Store<D> store, Cache<UUID, D> cache) {
        if (store == null) throw new NullPointerException("Null store");
        if (cache == null) throw new NullPointerException("Null cache");
        this.store = store;
        this.cache = cache;
    }

    @Override
    public String getName() {
        return store.getName();
    }

    @Override
    public Class<D> getType() {
        return store.getType();
    }

    @Override
    public D create() {
        return store.create();
    }

    @Override
    public D find(UUID uuid) {
        final D cached = cache.get(uuid);
        if (cached != null) return cached;
        log.debug("Document %s not cached", uuid);
        final D document = store.find(uuid);
        if (document != null) {
            log.debug("Caching document %s on retrieval", uuid);
            cache.put(document.getUUID(), document);
        }
        return document;
    }

    @Override
    public D store(D object) {
        cache.invalidate(object.getUUID());
        final D document = store.store(object);
        if (document != null) {
            log.debug("Caching document %s on store", document.getUUID());
            cache.put(document.getUUID(), document);
        }
        return document;
    }

    @Override
    public Query<D> query() {
        return store.query();
    }

    @Override
    public Query<D>.Operator query(String field) {
        return store.query(field);
    }

}
