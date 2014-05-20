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

import static org.usrz.libs.stores.annotations.Indexes.Option.SPARSE;
import static org.usrz.libs.stores.annotations.Indexes.Option.UNIQUE;
import static org.usrz.libs.stores.annotations.Indexes.Type.ASCENDING;
import static org.usrz.libs.stores.annotations.Indexes.Type.DESCENDING;
import static org.usrz.libs.stores.annotations.Indexes.Type.HASHED;

import java.io.IOException;
import java.util.List;

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
import org.usrz.libs.stores.annotations.Index;
import org.usrz.libs.stores.annotations.Index.Key;
import org.usrz.libs.stores.annotations.Indexed;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.inject.Guice;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;

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

    private void testIndexes(Store<?> store) {
        final String collection = store.getCollection();
        final List<DBObject> indexes = db.getCollection(collection).getIndexInfo();

        for (DBObject index: indexes) log.info("Found index %s", index);

        assertEquals(indexes.size(), 5);
        assertTrue(indexes.contains(new BasicDBObject("v", 1)
                                              .append("name", "_id_")
                                              .append("ns", "test." + collection)
                                              .append("key", new BasicDBObject("_id", 1))));

        assertTrue(indexes.contains(new BasicDBObject("v", 1)
                                              .append("name", "type_1")
                                              .append("unique", true)
                                              .append("expireAfterSeconds", 86400)
                                              .append("ns", "test." + collection)
                                              .append("key", new BasicDBObject("foo", 1).append("bar", -1))));

        assertTrue(indexes.contains(new BasicDBObject("v", 1)
                                              .append("name", "type_2")
                                              .append("sparse", true)
                                              .append("ns", "test." + collection)
                                              .append("key", new BasicDBObject("baz", "hashed"))));

        assertTrue(indexes.contains(new BasicDBObject("v", 1)
                                              .append("name", "hello")
                                              .append("expireAfterSeconds", 3600)
                                              .append("ns", "test." + collection)
                                              .append("key", new BasicDBObject("value_1", 1))));

        assertTrue(indexes.contains(new BasicDBObject("v", 1)
                                              .append("name", "value_2_hashed")
                                              .append("ns", "test." + collection)
                                              .append("key", new BasicDBObject("value_2", "hashed"))));
    }

    /* ====================================================================== */

    @Test
    public void testAbstractsAnnotations()
    throws Exception {
        assertEquals(abstractsStore.getCollection(), abstractsCollection);
        testIndexes(abstractsStore);
    }

    @Test
    public void testInterfacesAnnotations()
    throws Exception {
        assertEquals(interfacesStore.getCollection(), interfacesCollection);
        testIndexes(interfacesStore);
    }

    /* ====================================================================== */

    @Collection(abstractsCollection)
    @Index(name="type_1",
           options=UNIQUE,
           expiresAfter="1 day",
           keys={@Key(field="foo", type=ASCENDING),
                 @Key(field="bar", type=DESCENDING)})
    @Index(name="type_2",
           options=SPARSE,
           keys={@Key(field="baz", type=HASHED)})
    public static abstract class AbstractBean extends AbstractDocument {

        @JsonCreator
        protected AbstractBean(@Id String id) {
            super(id);
        }

        @Indexed(name="hello", expiresAfter="1 hour")
        public abstract String getValue1();

        public abstract void setValue1(String value);

        public abstract String getValue2();

        @Indexed(type=HASHED)
        public abstract void setValue2(String value);

    }

    /* ====================================================================== */


    @Collection(interfacesCollection)
    @Index(name="type_1",
           options=UNIQUE,
           expiresAfter="1 day",
           keys={@Key(field="foo", type=ASCENDING),
                 @Key(field="bar", type=DESCENDING)})
    @Index(name="type_2",
           options=SPARSE,
           keys={@Key(field="baz", type=HASHED)})
    public static interface InterfaceBean extends Document {

        @Indexed(name="hello", expiresAfter="1 hour")
        public abstract String getValue1();

        public abstract void setValue1(String value);

        public abstract String getValue2();

        @Indexed(type=HASHED)
        public abstract void setValue2(String value);

    }
}
