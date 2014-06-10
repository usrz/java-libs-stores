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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.JsonConfigurations;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.Strings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.mongodb.DB;

public class InjectionTest extends AbstractTest {

    private static final Map<Object, Object> MAP = Collections.emptyMap();
    private static final List<Object> LIST = Collections.emptyList();
    private static final Set<Object> SET = Collections.emptySet();

    private static final String collection = Strings.random(16);

    @Inject private Store<MyBean> store;
    @Inject private DB db;

    @BeforeClass
    public void initialize()
    throws Exception {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        final Injector injector = Guice.createInjector((binder) -> {
                new MongoBuilder(binder).configure(configurations.strip("mongo"))
                                        .store(MyBean.class, collection);
                binder.bind(new TypeLiteral<Map<Object, Object>>(){}).toInstance(MAP);
                binder.bind(new TypeLiteral<List<Object>>(){}).toInstance(LIST);
                binder.bind(new TypeLiteral<Set<Object>>(){}).toInstance(SET);
            });

        injector.getAllBindings().entrySet().forEach((entry) -> {
            log.info("Binding for key \"%s\"", entry.getKey());
        });

        injector.injectMembers(this);
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

        final MyBean bean = store.store(new MyBean());

        assertNotNull(bean, "Null bean");
        assertSame(bean.fieldInjection, MAP, "Null injection in field");
        assertSame(bean.setterInjection, LIST, "Null injection in setter");
        assertSame(bean.constructorInjection, SET, "Null injection in constructor");

    }

    @RequiredArgsConstructor
    public static class MyBean extends Document {

        @Getter
        private final String value;

        @Inject private Map<Object, Object> fieldInjection;
        @Inject private final Set<Object> constructorInjection;
        private List<Object> setterInjection;

        public MyBean() {
            value = null;
            constructorInjection = null;
        }

        @Inject @JsonIgnore
        public void setList(List<Object> list) {
            setterInjection = list;
        }
    }
}
