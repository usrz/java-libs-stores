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
import java.util.UUID;
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
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.RandomString;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.mongodb.DB;

public class StoreCreationTest extends AbstractTest {

    private static final String id = UUID.randomUUID().toString();
    private static final String collection = RandomString.get(16);
    private static final Log log = new Log();

    @Inject private DB db;
    @Inject private Store<MyBean> store;
    @Inject private Map<String, Integer> map;

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

        final MyBean bean = store.create();
        assertNotNull(bean, "Null bean created");

        assertEquals(bean.getSensibleDefault(), "a sensible default");
        assertNotEquals(bean.getId(), id);
        assertNull(bean.nullable);
        assertSame(bean.map, map);

        bean.setSensibleDefault("we override the default");

        store.store(bean);

        final MyBean bean2 = store.find(bean.getId());
        assertNotNull(bean2, "Null bean created");

        assertEquals(bean2.getSensibleDefault(), "we override the default");
        assertEquals(bean2.getId(), bean.getId());
        assertNull(bean.nullable);
        assertSame(bean.map, map);

    }

    @Test
    public void testDefaultsOverride()
    throws Exception {
        assertNotNull(store, "Null store");

        /* Use an object, so we can see it's injected */
        final MyBean bean = store.create(new Consumer<Initializer>() {

            @Inject Injector injector;

            @Override
            public void accept(Initializer initializer) {
                if (injector == null) throw new IllegalStateException("Not injected");
                initializer.property("nullable", "not a null string");
            }
        });

        assertNotNull(bean, "Null bean created");

        assertEquals(bean.getSensibleDefault(), "a sensible default");
        assertNotEquals(bean.getId(), id);
        assertEquals(bean.nullable, "not a null string");
        assertSame(bean.map, map);

        bean.setSensibleDefault("we override the default");

        store.store(bean);

        final MyBean bean2 = store.find(bean.getId());
        assertNotNull(bean2, "Null bean created");

        assertEquals(bean2.getSensibleDefault(), "we override the default");
        assertEquals(bean2.getId(), bean.getId());
        assertEquals(bean.nullable, "not a null string");
        assertSame(bean.map, map);

    }

    @Defaults(MyInitializer.class)
    public static abstract class MyBean extends AbstractDocument {

        private String sensible;
        private final String nullable;
        private final Map<String, Integer> map;

        @JsonCreator
        public MyBean(@JsonProperty("id") String id,
                      @JsonProperty("sensible") String sensible,
                      @JsonProperty("nullable") String nullable,
                      @JacksonInject("map") Map<String, Integer> map) {
            super(id);
            this.sensible = sensible;
            this.nullable = nullable;
            this.map = map;
        }

        @JsonProperty("sensible")
        public String getSensibleDefault() {
            return sensible;
        }

        @JsonProperty("nullable")
        public String getNullableDefault() {
            return nullable;
        }

        @JsonIgnore
        public void setSensibleDefault(String sensible) {
            this.sensible = sensible;
        }
    }

    public static class MyInitializer implements Consumer<Initializer> {

        @Inject Injector injector;

        @Override
        public void accept(Initializer initializer) {
            if (injector == null) throw new IllegalStateException("Not injected");
            initializer.property("sensible", "a sensible default")
                       .property("id", id) // this should be overridden!
                       .inject("map", new TypeLiteral<Map<String, Integer>>(){});
        }

    }
}
