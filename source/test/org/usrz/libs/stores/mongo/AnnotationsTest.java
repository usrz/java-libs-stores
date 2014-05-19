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
import org.usrz.libs.stores.annotations.Collection;
import org.usrz.libs.stores.annotations.Id;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Guice;
import com.mongodb.DB;

public class AnnotationsTest extends AbstractTest {

    private static final String abstractsCollection = "abstracts_test";
    private static final String interfacesCollection = "interfaces_test";

    @BeforeClass
    public void prepare()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector(MongoBuilder.apply((builder) -> {
                builder.configure(configurations.strip("mongo"));
                builder.store(AbstractBean.class);
                builder.store(InterfaceBean.class);
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
    private Store<AbstractBean> abstractsStore;
    @Inject
    private Store<InterfaceBean> interfacesStore;
    @Inject
    private DB db;

    /* ====================================================================== */

    @Test
    public void testAnnotations()
    throws Exception {
        assertEquals(abstractsStore.getCollection(), abstractsCollection);
        assertEquals(interfacesStore.getCollection(), interfacesCollection);
    }

    /* ====================================================================== */

    @Collection(abstractsCollection)
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

    @Collection(interfacesCollection)
    public static interface InterfaceBean extends Document {

        @JsonProperty("value")
        public abstract String getValue();

        @JsonProperty("value")
        public abstract void setValue(String value);

    }
}
