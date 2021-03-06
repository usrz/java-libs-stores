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

import static org.usrz.libs.stores.Query.Field.LAST_MODIFIED_AT;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.Strings;

import com.google.inject.Guice;
import com.mongodb.DB;

public class LastModifiedTest extends AbstractTest {

    private final String collection = Strings.random(16);

    @BeforeClass
    public void prepare()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector((binder) -> new MongoBuilder(binder)
                .configure(configurations.strip("mongo"))
                .store(SimpleBean.class, collection)
            ).injectMembers(this);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup()
    throws IOException {
        if (db != null) db.getCollection(collection).drop();
    }

    /* ====================================================================== */

    @Inject
    private Store<SimpleBean> store;
    @Inject
    private DB db;

    /* ====================================================================== */

    @Test
    public void testLastModifiedAt()
    throws Exception {

        final String value = Strings.random(16);

        SimpleBean bean = new SimpleBean();

        assertNull(bean.getValue());
        bean.setValue(value);

        assertEquals(bean.getValue(), value);
        assertNull(bean.lastModifiedAt());
        assertNull(bean.id());

        bean = store.store(bean);

        assertNotNull(bean.lastModifiedAt());
        assertNotNull(bean.id());

        final Date date1 = bean.lastModifiedAt();

        bean = store.find(bean.id());
        assertNotNull(bean);
        assertEquals(bean.getValue(), value);
        assertEquals(bean.lastModifiedAt(), date1);

        Thread.sleep(100); // make sure last modified date changes

        bean = store.store(bean);
        assertNotEquals(bean.lastModifiedAt(), date1);
        final Date date2 = bean.lastModifiedAt();

        bean = store.find(bean.id());
        assertEquals(bean.lastModifiedAt(), date2);

        /* Query */
        final List<SimpleBean> list = store.query(LAST_MODIFIED_AT).gt(new Date(0)).list();
        assertNotNull(list, "Null results list");
        assertEquals(list.size(), 1, "Wrong number of results");
        assertNotNull(list.get(0), "Null result from query");
        assertEquals(list.get(0).id(), bean.id(), "Wrong result ID");
    }

    public static class SimpleBean extends Document {

        @Getter @Setter private String value;

    }

}
