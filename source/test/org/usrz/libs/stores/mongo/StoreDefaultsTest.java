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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.JsonConfigurations;
import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.AbstractDocument;
import org.usrz.libs.stores.Defaults;
import org.usrz.libs.stores.Defaults.Initializer;
import org.usrz.libs.stores.Id;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.RandomString;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Guice;
import com.google.inject.TypeLiteral;
import com.mongodb.DB;

public class StoreDefaultsTest extends AbstractTest {

    private static final String collection = RandomString.get(16);
    private static final Log log = new Log();

    @Inject private DB db;
    @Inject private Store<MyBean> store;

    @BeforeClass
    public void initialize()
    throws Exception {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));
        Guice.createInjector(MongoBuilder.apply((builder) -> {
            builder.configure(configurations.strip("mongo"));
            builder.store(MyBean.class, collection);
            builder.binder().bind(new TypeLiteral<Map<String, Integer>>(){}).toInstance(new HashMap<>());
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
    public void testDefaults()
    throws Exception {
        assertNotNull(store, "Null store");

        final MyBean bean = store.create((i) -> i.property("extra", "something extra"));
        assertNotNull(bean, "Null bean created");

        assertEquals(bean.init, "initialized");
        assertEquals(bean.state, "created");
        assertEquals(bean.value, null);
        assertEquals(bean.extra, "something extra");

        bean.value = "the value to store";

        final MyBean stored = store.store(bean);
        assertNotNull(stored, "Null bean stored");

        assertEquals(stored.init, "initialized");
        assertEquals(stored.state, "created");
        assertEquals(stored.value, "the value to store");
        assertEquals(stored.extra, "something extra");

        final MyBean fetched = store.find(stored.getId());
        assertNotNull(fetched, "Null bean fetched");

        assertEquals(fetched.init, "initialized");
        assertEquals(fetched.state, "updated");
        assertEquals(fetched.value, "the value to store");
        assertEquals(fetched.extra, null);

    }

    @Defaults(value=MyInitializer.class, create=MyCreator.class, update=MyUpdater.class)
    public static abstract class MyBean extends AbstractDocument {

        @JsonIgnore private final String init;
        @JsonIgnore private final String state;
        @JsonIgnore private final String extra;
        @JsonIgnore private String value;

        @JsonCreator
        public MyBean(@Id String id,
                      @JsonProperty("init") String init,
                      @JsonProperty("state") String state,
                      @JsonProperty("extra") String extra,
                      @JsonProperty("value") String value) {
            super(id);
            this.init = init;
            this.state = state;
            this.value = value;
            this.extra = extra;
        }

        @JsonProperty("value")
        public String getSensibleDefault() {
            return value;
        }

    }

    public static class MyInitializer implements Consumer<Initializer> {

        @Override
        public void accept(Initializer initializer) {
            initializer.property("init", "initialized");
            initializer.property("state", "initialized"); // this will be overridden!
        }

    }

    public static class MyCreator implements Consumer<Initializer> {

        @Override
        public void accept(Initializer initializer) {
            initializer.property("state", "created");
        }

    }

    public static class MyUpdater implements Consumer<Initializer> {

        @Override
        public void accept(Initializer initializer) {
            initializer.property("state", "updated");
        }

    }
}
