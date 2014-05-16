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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.JsonConfigurations;
import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.AbstractDocument;
import org.usrz.libs.stores.Id;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.RandomString;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.TypeLiteral;
import com.mongodb.DB;



public class InjectionTest extends AbstractTest {

    private static final Map<Object, Object> MAP = Collections.emptyMap();
    private static final List<Object> LIST = Collections.emptyList();
    private static final Set<Object> SET = Collections.emptySet();

    private static final String collection = RandomString.get(16);
    private static final Log log = new Log();

    @Inject private Store<MyBean> store;
    @Inject private DB db;

    @BeforeClass
    public void initialize()
    throws Exception {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector(MongoBuilder.apply((builder) -> {

            Binder binder = builder.binder();
            binder.bind(new TypeLiteral<Map<Object, Object>>(){}).toInstance(MAP);
            binder.bind(new TypeLiteral<List<Object>>(){}).toInstance(LIST);
            binder.bind(new TypeLiteral<Set<Object>>(){}).toInstance(SET);

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

        final MyBean bean = store.create();

        assertNotNull(bean, "Null bean");
        assertNotNull(bean.fieldInjection, "Null injection in field");
        assertNotNull(bean.setterInjection, "Null injection in setter");
        assertNotNull(bean.constructorInjection, "Null injection in constructor");

    }

    public abstract static class MyBean extends AbstractDocument {

        @Inject private Map<Object, Object> fieldInjection;
        private final Set<Object> constructorInjection;
        private List<Object> setterInjection;

        @JsonCreator
        protected MyBean(@Id String id,
                         @JacksonInject Set<Object> set) {
            super(id);
            constructorInjection = set;
        }

        @Inject
        public void setList(List<Object> list) {
            setterInjection = list;
        }
    }
}
