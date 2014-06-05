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

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.JsonConfigurations;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.Stores;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.Strings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Guice;
import com.mongodb.DB;

public class StoreTest extends AbstractTest {

    private final String normalBeanCollection = Strings.random(16);
    private final String lombokBeanCollection = Strings.random(16);

    @BeforeClass
    public void prepare()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector((binder) ->
                new MongoBuilder(binder)
                        .configure(configurations.strip("mongo"))
                        .store(NormalBean.class, normalBeanCollection)
                        .store(LombokBean.class, lombokBeanCollection)
            ).injectMembers(this);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup()
    throws IOException {
        if (db != null) try {
            db.getCollection(normalBeanCollection).drop();
        } finally {
            db.getCollection(lombokBeanCollection).drop();
        }
    }

    /* ====================================================================== */

    @Inject
    private Stores stores;
    @Inject
    private Store<NormalBean> normalBeanStore;
    @Inject
    private Store<LombokBean> lombokBeanStore;
    @Inject
    private DB db;

    /* ====================================================================== */

    @Test(priority=-1)
    public void testStores() {
        assertNotNull(stores.getStore(NormalBean.class));
        assertNotNull(stores.getStore(LombokBean.class));
        assertNotNull(stores.getStore(normalBeanCollection));
        assertNotNull(stores.getStore(lombokBeanCollection));

        assertSame(stores.getStore(NormalBean.class), normalBeanStore);
        assertSame(stores.getStore(LombokBean.class), lombokBeanStore);
        assertSame(stores.getStore(normalBeanCollection), normalBeanStore);
        assertSame(stores.getStore(lombokBeanCollection), lombokBeanStore);

        /* Check we get the proper types */
        assertEquals(normalBeanStore.getDocumentType(), NormalBean.class);
        assertEquals(lombokBeanStore.getDocumentType(), LombokBean.class);

    }

    /* ====================================================================== */

    @Test
    public void testNormalBean()
    throws Exception {

        final String value = Strings.random(16);

        NormalBean bean = new NormalBean(value);

        assertNull(bean.id());
        assertNull(bean.lastModifiedAt());

        bean = normalBeanStore.store(bean);
        assertNotNull(bean.lastModifiedAt());
        final Date date1 = bean.lastModifiedAt();

        bean = normalBeanStore.find(bean.id());
        assertNotNull(bean);
        assertEquals(bean.getValue(), value);
        assertEquals(bean.lastModifiedAt(), date1);

        Thread.sleep(100); // make sure last modified date changes

        bean = normalBeanStore.store(bean);
        assertNotEquals(bean.lastModifiedAt(), date1);
        final Date date2 = bean.lastModifiedAt();

        bean = normalBeanStore.find(bean.id());
        assertEquals(bean.lastModifiedAt(), date2);
    }

    public static class NormalBean extends Document {

        private String value;

        @JsonCreator
        protected NormalBean(@JsonProperty("value") String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    /* ====================================================================== */

    @Test
    public void testLombokBean()
    throws Exception {

        final String value = Strings.random(16);

        LombokBean bean = new LombokBean();

        assertNull(bean.getValue());
        bean.setValue(value);
        assertEquals(bean.getValue(), value);
        assertNull(bean.lastModifiedAt());

        bean = lombokBeanStore.store(bean);
        assertNotNull(bean.lastModifiedAt());
        final Date date1 = bean.lastModifiedAt();

        bean = lombokBeanStore.find(bean.id());
        assertNotNull(bean);
        assertEquals(bean.getValue(), value);
        assertEquals(bean.lastModifiedAt(), date1);

        Thread.sleep(100); // make sure last modified date changes

        bean = lombokBeanStore.store(bean);
        assertNotEquals(bean.lastModifiedAt(), date1);
        final Date date2 = bean.lastModifiedAt();

        bean = lombokBeanStore.find(bean.id());
        assertEquals(bean.lastModifiedAt(), date2);

    }

    public static class LombokBean extends Document {

        @Getter @Setter
        private String value;

    }
}
