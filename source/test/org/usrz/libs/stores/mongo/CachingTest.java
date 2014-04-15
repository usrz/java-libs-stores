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

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.JsonConfigurations;
import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Id;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.RandomString;
import org.usrz.libs.utils.caches.Cache;
import org.usrz.libs.utils.caches.SimpleCache;

import com.google.inject.Guice;
import com.mongodb.DB;

public class CachingTest extends AbstractTest {

    private static final String collection = RandomString.get(16);
    private static final Log log = new Log();

    @Inject private Store<MyBean> store;
    @Inject private Cache<Id, MyBean> cache;
    @Inject private DB db;

    @BeforeClass
    public void initialize()
    throws Exception {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));
        final Cache<Id, MyBean> cache = new SimpleCache<>();

        Guice.createInjector(MongoBuilder.apply((builder) -> {
            builder.configure(configurations.strip("mongo"));
            builder.store(MyBean.class, collection)
                   .withCache(cache);
        })).injectMembers(this);
    }

    @AfterClass(alwaysRun=true)
    public void destroy()
    throws Exception {
        if (db != null) {
            log.info("Dropping collection %s.%s", db.getName(), collection);
            db.getCollection(collection).drop();
        }
    }

    @Test
    public void testInjection()
    throws Exception {
        assertNotNull(store, "Null store");

        final MyBean bean = store.create();
        assertNotNull(bean, "Null bean created");
        assertNull(cache.fetch(bean.getId()), "Cached on creation");

        store.store(bean);
        Thread.sleep(100); // cache on store is asynchronous...
        assertNotNull(cache.fetch(bean.getId()), "Not cached on store");

        cache.invalidate(bean.getId());
        assertNull(cache.fetch(bean.getId()), "Cache not invalidated");

        final MyBean bean2 = store.find(bean.getId());
        assertNotNull(bean2, "Stored bean not found");
        assertNotNull(cache.fetch(bean2.getId()), "Not cached on find");

    }

    public interface MyBean extends Document {}
}
