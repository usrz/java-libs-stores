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

import javax.inject.Inject;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Guice;
import com.mongodb.DB;

public class MongoStoreTest extends AbstractTest {

    private final String referencingCollection = UUID.randomUUID().toString();
    private final String referencedCollection = UUID.randomUUID().toString();

    @BeforeClass
    public void prepare()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector(MongoBuilder.apply((builder) -> {
                builder.configure(configurations.strip("mongo"));
                builder.store(ReferencingBean.class, referencingCollection);
                builder.store(ReferencedBean.class, referencedCollection);
        })).injectMembers(this);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup()
    throws IOException {
        if (db != null) try {
            db.getCollection(referencingCollection).drop();
        } finally {
            db.getCollection(referencedCollection).drop();
        }
    }

    /* ====================================================================== */

    @Inject
    private Store<ReferencedBean> referencedStore;
    @Inject
    private Store<ReferencingBean> referencingStore;
    @Inject
    private DB db;

    /* ====================================================================== */

    @Test
    public void testReferences()
    throws Exception {

        ReferencedBean referencedBean = referencedStore.create();
        referencedStore.store(referencedBean);
        ReferencedBean referencedBean2 = referencedStore.find(referencedBean.getUUID());

        assertNotNull(referencedBean2);
        assertNotSame(referencedBean2, referencedBean);
        assertEquals(referencedBean2.getUUID(), referencedBean.getUUID());

        ReferencingBean referencingBean = referencingStore.create().withReferenced(referencedBean2);
        referencingStore.store(referencingBean);

        assertNotNull(referencingBean.bean);
        assertNotNull(referencingBean.getReferenceUUID());

        ReferencingBean referencingBean2 = referencingStore.find(referencingBean.getUUID());
        assertNotNull(referencingBean2);
        assertNull(referencingBean2.bean);
        assertNotNull(referencingBean2.getReferenceUUID());
        assertNotNull(referencingBean2.getReferenced());

    }

    /* ====================================================================== */

    public static abstract class ReferencingBean extends MongoDocument {

        private Store<ReferencedBean> store;
        private ReferencedBean bean;

        @JsonIgnore
        public ReferencingBean withReferenced(ReferencedBean bean) {
            setReferenceUUID(bean.getUUID());
            this.bean = bean;
            return this;
        }

        @JsonIgnore
        public ReferencedBean getReferenced() {
            if (bean != null) return bean;
            return bean = store.find(getReferenceUUID());
        }

        @JsonProperty("reference_uuid")
        public abstract UUID getReferenceUUID();

        @JsonProperty("reference_uuid")
        public abstract void setReferenceUUID(UUID uuid);

        @Inject
        public void injectStore(Store<ReferencedBean> store) {
            this.store = store;
        }

    }

    /* ====================================================================== */

    public static interface ReferencedBean extends Document {

        /* Marker */

    }

}
