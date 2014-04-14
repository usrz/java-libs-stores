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

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.JsonConfigurations;
import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.DB;


public class InjectionTest extends AbstractTest {

    private static final String collection = UUID.randomUUID().toString();
    private static final Log log = new Log();

    @Inject private Store<MyBean> store;
    @Inject private DB db;
    @Inject private Injector injector;

    @BeforeClass
    public void initialize()
    throws Exception {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector(MongoBuilder.apply((builder) -> {
            builder.configure(configurations.strip("mongo"));
            builder.store(MyBean.class, collection);
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
    throws IOException {
        assertNotNull(store);

        final AtomicReference<Store<MyBean>> reference = new AtomicReference<>();
        injector.injectMembers(new Object() {
            @Inject
            public void setStore(Store<MyBean> store) {
                if (reference.compareAndSet(null, store)) return;
                throw new IllegalStateException("Injected multiple times");
            }
        });

        assertNotNull(reference.get());
        assertSame(store, reference.get());

    }

    public interface MyBean extends Document {
        public void setString(String string);
        public String getString();
    }
}
