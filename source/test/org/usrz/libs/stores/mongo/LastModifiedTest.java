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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.JsonConfigurations;
import org.usrz.libs.stores.AbstractDocument;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.annotations.Id;
import org.usrz.libs.stores.annotations.LastModified;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.Strings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.inject.Guice;
import com.mongodb.DB;

public class LastModifiedTest extends AbstractTest {

    private final String abstractsCollection = Strings.random(16);

    @BeforeClass
    public void prepare()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector(MongoBuilder.apply((builder) -> {
                builder.configure(configurations.strip("mongo"));
                builder.store(AbstractBean.class, abstractsCollection);
        })).injectMembers(this);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup()
    throws IOException {
        if (db != null) db.getCollection(abstractsCollection).drop();
    }

    /* ====================================================================== */

    @Inject
    private Store<AbstractBean> abstractsStore;
    @Inject
    private DB db;

    /* ====================================================================== */

    @Test
    public void testAbstracts()
    throws Exception {

        final String value = Strings.random(16);

        AbstractBean bean = abstractsStore.create();

        assertNull(bean.getValue());
        bean.setValue(value);

        assertEquals(bean.getValue(), value);
        assertNull(bean.lastModified());

        bean = abstractsStore.store(bean);
        assertNotNull(bean.lastModified());
        final Date date1 = bean.lastModified();

        bean = abstractsStore.find(bean.getId());
        assertNotNull(bean);
        assertEquals(bean.getValue(), value);
        assertEquals(bean.lastModified(), date1);

        Thread.sleep(100); // make sure last modified date changes

        bean = abstractsStore.store(bean);
        assertNotEquals(bean.lastModified(), date1);
        final Date date2 = bean.lastModified();

        bean = abstractsStore.find(bean.getId());
        assertEquals(bean.lastModified(), date2);
    }

    public static abstract class AbstractBean extends AbstractDocument {

        @LastModified private Date lastModified;

        @JsonCreator
        protected AbstractBean(@Id String id) {
            super(id);
        }

        public abstract String getValue();

        public abstract void setValue(String value);

        public Date lastModified() {
            return lastModified;
        }

    }

}
