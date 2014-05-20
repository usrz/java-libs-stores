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
import java.util.Date;
import java.util.regex.Pattern;

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
import org.usrz.libs.stores.annotations.LastModified;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.Strings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Guice;
import com.mongodb.DB;

public class StoreTest extends AbstractTest {

    private final String abstractsCollection = Strings.random(16);
    private final String interfacesCollection = Strings.random(16);

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

        /* Check we get the proper types */
        assertEquals(abstractsStore.getDocumentType(), AbstractBean.class);
        assertEquals(interfacesStore.getDocumentType(), InterfaceBean.class);

        /* Constructed beans name pattern */
        final Pattern abstractsPattern = Pattern.compile(AbstractBean.class.getName().replaceAll("\\.", "\\\\.").replaceAll("\\$", "\\\\\\$") + "_[0-9A-Fa-f]+");
        assertTrue(abstractsPattern.matcher(abstractsStore.getDocumentClass().getName()).matches(), "Class name \"" + abstractsStore.getDocumentClass().getName() + "\" not matching \"" + abstractsPattern.pattern() + "\"");
        final Pattern interfacesPattern = Pattern.compile(AbstractDocument.class.getName().replaceAll("\\.", "\\\\.") + "_[0-9A-Fa-f]+");
        assertTrue(interfacesPattern.matcher(interfacesStore.getDocumentClass().getName()).matches(), "Class name \"" + interfacesStore.getDocumentClass().getName() + "\" not matching \"" + interfacesPattern.pattern() + "\"");

    }

    /* ====================================================================== */

    @Test
    public void testAbstracts()
    throws Exception {

        final String value = Strings.random(16);

        AbstractBean bean = abstractsStore.create();

        assertNull(bean.getValue());
        bean.setValue(value);

        assertEquals(bean.getValue(), value);
        assertNull(bean.getLastModified());

        bean = abstractsStore.store(bean);
        assertNotNull(bean.getLastModified());
        final Date date1 = bean.getLastModified();

        bean = abstractsStore.find(bean.getId());
        assertNotNull(bean);
        assertEquals(bean.getValue(), value);
        assertEquals(bean.getLastModified(), date1);

        Thread.sleep(100); // make sure last modified date changes

        bean = abstractsStore.store(bean);
        assertNotEquals(bean.getLastModified(), date1);
        final Date date2 = bean.getLastModified();

        bean = abstractsStore.find(bean.getId());
        assertEquals(bean.getLastModified(), date2);
    }

    public static abstract class AbstractBean extends AbstractDocument {

        private final Date lastModified;

        @JsonCreator
        protected AbstractBean(@Id String id,
                               @LastModified Date modified) {
            super(id);
            lastModified = modified;
        }

        public abstract String getValue();

        public abstract void setValue(String value);

        @LastModified
        @JsonProperty(value="_last_modified_at", required=true) // "force" property to be visible, should be ignored
        public Date getLastModified() {
            return lastModified;
        }

    }

    /* ====================================================================== */

    @Test
    public void testInterfaces()
    throws Exception {

        final String value = Strings.random(16);

        InterfaceBean bean = interfacesStore.create();

        assertNull(bean.getValue());
        bean.setValue(value);
        assertEquals(bean.getValue(), value);
        assertNull(bean.getLastModified());

        bean = interfacesStore.store(bean);
        assertNotNull(bean.getLastModified());
        final Date date1 = bean.getLastModified();

        bean = interfacesStore.find(bean.getId());
        assertNotNull(bean);
        assertEquals(bean.getValue(), value);
        assertEquals(bean.getLastModified(), date1);

        Thread.sleep(100); // make sure last modified date changes

        bean = interfacesStore.store(bean);
        assertNotEquals(bean.getLastModified(), date1);
        final Date date2 = bean.getLastModified();

        bean = interfacesStore.find(bean.getId());
        assertEquals(bean.getLastModified(), date2);

    }

    public static interface InterfaceBean extends Document {

        public String getValue();

        public void setValue(String value);

        @LastModified
        @JsonProperty(value="_last_modified_at", required=true) // "force" property to be visible, should be ignored
        public Date getLastModified();

        @LastModified
        public void setLastModified(Date date);

    }
}
