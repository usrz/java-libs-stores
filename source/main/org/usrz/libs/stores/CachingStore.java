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

import static org.usrz.libs.utils.concurrent.Immediate.immediate;

import java.util.Objects;
import java.util.UUID;

import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.caches.Cache;
import org.usrz.libs.utils.concurrent.NotifyingFuture;
import org.usrz.libs.utils.concurrent.SimpleExecutor;

public class CachingStore<D extends Document> extends AbstractStore<D> {

    private static final Log log = new Log();
    private final SimpleExecutor executor;
    private final Store<D> store;
    private final Cache<UUID, D> cache;

    public CachingStore(SimpleExecutor executor, Store<D> store, Cache<UUID, D> cache) {
        this.executor = Objects.requireNonNull(executor, "Null executor");
        this.store = Objects.requireNonNull(store, "Null store");
        this.cache = Objects.requireNonNull(cache, "Null cache");
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
    public NotifyingFuture<D> findAsync(UUID uuid) {
        return executor.delegate(() -> {
            final D cached = cache.get(uuid);
            if (cached != null) return immediate(cached);
            return store.findAsync(uuid).withConsumer((future) -> {
                try {
                    final D document = future.get();
                    if (document == null) return;
                    log.debug("Caching document %s on fetch", document.getUUID());
                    cache.put(document.getUUID(), document);
                } catch (Exception exception) {
                    log.warn(exception, "Exception caching document");
                }
            });
        });
    }

    @Override
    public NotifyingFuture<D> storeAsync(D object) {
        return store.storeAsync(object).withConsumer((future) -> {
            try {
                final D document = future.get();
                if (document == null) return;
                log.debug("Caching document %s on store", document.getUUID());
                cache.put(document.getUUID(), document);
            } catch (Exception exception) {
                log.warn(exception, "Exception caching document");
            }

        });
    }

    @Override
    public Query<D> query() {
        return store.query();
    }

}
