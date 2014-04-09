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

import java.util.UUID;

import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.JsonConfigurations;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.caches.Cache;
import org.usrz.libs.utils.caches.SimpleCache;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;



public class CachingTest extends AbstractTest {

    @Test
    public void testInjection()
    throws Exception {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));
        final Cache<UUID, MyBean> cache = new SimpleCache<>();

        final Injector injector = Guice.createInjector(MongoBuilder.apply((builder) -> {
            builder.configure(configurations.strip("mongo"));
            builder.store(MyBean.class, UUID.randomUUID().toString())
                   .withCache(cache);
        }));

        final Store<MyBean> store = injector.getInstance(Key.get(new TypeLiteral<Store<MyBean>>(){}));
        assertNotNull(store, "Null store");

        final MyBean bean = store.create();
        assertNotNull(bean, "Null bean created");
        assertNull(cache.fetch(bean.getUUID()), "Cached on creation");

        store.store(bean);
        Thread.sleep(100); // cache on store is asynchronous...
        assertNotNull(cache.fetch(bean.getUUID()), "Not cached on store");

        cache.invalidate(bean.getUUID());
        assertNull(cache.fetch(bean.getUUID()), "Cache not invalidated");

        final MyBean bean2 = store.find(bean.getUUID());
        assertNotNull(bean2, "Stored bean not found");
        assertNotNull(cache.fetch(bean2.getUUID()), "Not cached on find");

    }

    public interface MyBean extends Document {}
}
