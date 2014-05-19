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

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.JsonConfigurations;
import org.usrz.libs.stores.AbstractDocument;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.Stores;
import org.usrz.libs.stores.annotations.Id;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.RandomString;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Guice;
import com.mongodb.DB;

public class StoreTest extends AbstractTest {

    private final String abstractsCollection = RandomString.get(16);
    private final String interfacesCollection = RandomString.get(16);

    @BeforeClass
    public void prepare()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector(MongoBuilder.apply((builder) -> {
                builder.configure(configurations.strip("mongo"));
                builder.store(AbstractBean.class, abstractsCollection);
                builder.store(InterfaceBean.class, interfacesCollection);
        })).injectMembers(this);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup()
    throws IOException {
        if (db != null) try {
            db.getCollection(abstractsCollection).drop();
        } finally {
            db.getCollection(interfacesCollection).drop();
        }
    }

    /* ====================================================================== */

    @Inject
    private Stores stores;
    @Inject
    private Store<AbstractBean> abstractsStore;
    @Inject
    private Store<InterfaceBean> interfacesStore;
    @Inject
    private DB db;

    /* ====================================================================== */

    @Test(priority=-1)
    public void testStores() {
        assertNotNull(stores.getStore(AbstractBean.class));
        assertNotNull(stores.getStore(InterfaceBean.class));
        assertNotNull(stores.getStore(abstractsCollection));
        assertNotNull(stores.getStore(interfacesCollection));

        assertSame(stores.getStore(AbstractBean.class), abstractsStore);
        assertSame(stores.getStore(InterfaceBean.class), interfacesStore);
        assertSame(stores.getStore(abstractsCollection), abstractsStore);
        assertSame(stores.getStore(interfacesCollection), interfacesStore);
    }

    /* ====================================================================== */

    @Test
    public void testAbstracts()
    throws Exception {

        final String value = RandomString.get(16);

        AbstractBean bean = abstractsStore.create();

        assertNull(bean.getValue());
        bean.setValue(value);
        assertEquals(bean.getValue(), value);

        abstractsStore.store(bean);

        AbstractBean bean2 = abstractsStore.find(bean.getId());
        assertNotNull(bean2);
        assertEquals(bean.getValue(), value);

    }

    public static abstract class AbstractBean extends AbstractDocument {

        @JsonCreator
        protected AbstractBean(@Id String id) {
            super(id);
        }

        @JsonProperty("value")
        public abstract String getValue();

        @JsonProperty("value")
        public abstract void setValue(String value);

    }

    /* ====================================================================== */

    @Test
    public void testInterfaces()
    throws Exception {

        final String value = RandomString.get(16);

        InterfaceBean bean = interfacesStore.create();

        assertNull(bean.getValue());
        bean.setValue(value);
        assertEquals(bean.getValue(), value);

        interfacesStore.store(bean);

        InterfaceBean bean2 = interfacesStore.find(bean.getId());
        assertNotNull(bean2);
        assertEquals(bean.getValue(), value);

    }

    public static interface InterfaceBean extends Document {

        @JsonProperty("value")
        public abstract String getValue();

        @JsonProperty("value")
        public abstract void setValue(String value);

    }
}
