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

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.mongo.MongoDatabaseModule;
import org.usrz.libs.stores.mongo.MongoDocument;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.configurations.Configurations;
import org.usrz.libs.utils.configurations.JsonConfigurations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;

public class MongoStoreTest extends AbstractTest {

    @BeforeTest
    public void prepare()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector(new AbstractModule() {
            @Override public void configure() {
                this.bind(Configurations.class).toInstance(configurations);
            }
        }, new MongoDatabaseModule() {
            @Override public void configure() {
                this.bind(ReferencingBean.class).toCollection("referencing");
                this.bind(ReferencedBean.class).toCollection("referenced");
            }
        }).injectMembers(this);
    }

    /* ====================================================================== */

    private Store<ReferencedBean> referencedStore;
    private Store<ReferencingBean> referencingStore;

    @Inject
    public void setReferencedStore(Store<ReferencedBean> referencedStore) {
        this.referencedStore = referencedStore;
    }

    @Inject
    public void setReferencingStore(Store<ReferencingBean> referencingStore) {
        this.referencingStore = referencingStore;
    }

    /* ====================================================================== */

    @Test(groups="local")
    public void testReferences()
    throws Exception {

        ReferencedBean referencedBean = referencedStore.create();
        referencedStore.put(referencedBean);
        ReferencedBean referencedBean2 = referencedStore.get(referencedBean.getUUID());

        assertNotNull(referencedBean2);
        assertNotSame(referencedBean2, referencedBean);
        assertEquals(referencedBean2.getUUID(), referencedBean.getUUID());

        ReferencingBean referencingBean = referencingStore.create().withReferenced(referencedBean2);
        referencingStore.put(referencingBean);

        assertNotNull(referencingBean.bean);
        assertNotNull(referencingBean.getReferenceUUID());

        ReferencingBean referencingBean2 = referencingStore.get(referencingBean.getUUID());
        assertNotNull(referencingBean2);
        assertNull(referencingBean2.bean);
        assertNotNull(referencingBean2.getReferenceUUID());
        assertNotNull(referencingBean2.getReferenced());

    }

    /* ====================================================================== */

    public static abstract class ReferencingBean extends MongoDocument {

        private Store<ReferencedBean> store;
        private ReferencedBean bean;

        public ReferencingBean() {
            super();
        }

        @JsonIgnore
        public ReferencingBean withReferenced(ReferencedBean bean) {
            setReferenceUUID(bean.getUUID());
            this.bean = bean;
            return this;
        }

        @JsonIgnore
        public ReferencedBean getReferenced() {
            if (bean != null) return bean;
            return bean = store.get(getReferenceUUID());
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
