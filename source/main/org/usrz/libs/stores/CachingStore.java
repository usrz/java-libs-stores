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

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.usrz.libs.logging.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 * A {@link Store} capable of <em>caching</em> documents in the wrapped
 * {@link Cache}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <D> The type of {@link Document}s stored by this {@link Store}.
 */
public class CachingStore<D extends Document> extends AbstractStoreWrapper<D> {

    private static final Log log = new Log();
    private final Cache<String, D> cache;

    public CachingStore(Store<D> store, Cache<String, D> cache) {
        super(store);
        this.cache = Objects.requireNonNull(cache, "Null cache");
    }

    @Override
    public D find(String id) {
        Throwable cause;
        try {
            return cache.get(id, () -> {
                final D document = super.find(id);
                if (document != null) log.debug("Caching document %s on find", document.id());
                return document;
            });
        } catch (ExecutionException | UncheckedExecutionException exception) {
            cause = exception.getCause();
            if (cause == null) cause = exception;
        } catch (InvalidCacheLoadException exception) {
            return null;
        }

        if (cause instanceof RuntimeException) throw (RuntimeException) cause;
        throw new UncheckedExecutionException(cause.getClass().getSimpleName() + " caught fetching document \"" + id + "\"", cause);
    }

    @Override
    public D store(D object) {
        final D document = super.store(object);
        if (document != null) {
            log.debug("Caching document %s on store", document.id());
            cache.put(document.id(), document);
        }
        return document;
    }

    @Override
    public boolean delete(String id) {
        cache.invalidate(id);
        return super.delete(id);
    }

}
