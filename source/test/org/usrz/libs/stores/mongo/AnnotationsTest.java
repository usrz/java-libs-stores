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

import lombok.Getter;
import lombok.Setter;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.JsonConfigurations;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.annotations.Collection;
import org.usrz.libs.stores.annotations.Index;
import org.usrz.libs.stores.annotations.Index.Key;
import org.usrz.libs.stores.annotations.Indexed;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Guice;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;

public class AnnotationsTest extends AbstractTest {

    private static final String normalCollection = "normal_bean_test";
    private static final String lombokCollection = "lombok_bean_test";

    @BeforeClass
    public void prepare()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector((binder) ->
                new MongoBuilder(binder)
                        .configure(configurations.strip("mongo"))
                        .store(NormalBean.class)
                        .store(LombokBean.class)
            ).injectMembers(this);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup()
    throws IOException {
        if (db != null) try {
            db.getCollection(normalCollection).drop();
        } finally {
            db.getCollection(lombokCollection).drop();
        }
    }

    /* ====================================================================== */

    @Inject
    private Store<NormalBean> normalBeanStore;
    @Inject
    private Store<LombokBean> lombokBeanStore;
    @Inject
    private DB db;

    /* ====================================================================== */

    private void assertIndex(List<DBObject> indexes, BasicDBObject index) {
        assertTrue(indexes.contains(index), "Index not found: " + index);
    }

    private void testIndexes(Store<?> store) {
        final String collection = store.getCollection();
        final List<DBObject> indexes = db.getCollection(collection).getIndexInfo();

        for (DBObject index: indexes) log.info("Found index %s", index);

        assertEquals(indexes.size(), 5);
        assertIndex(indexes, new BasicDBObject("v", 1)
                                    .append("name", "_id_")
                                    .append("ns", "test." + collection)
                                    .append("key", new BasicDBObject("_id", 1)));

        assertIndex(indexes, new BasicDBObject("v", 1)
                                    .append("name", "type_1")
                                    .append("unique", true)
                                    .append("expireAfterSeconds", 86400)
                                    .append("ns", "test." + collection)
                                    .append("key", new BasicDBObject("foo", 1).append("bar", -1)));

        assertIndex(indexes, new BasicDBObject("v", 1)
                                    .append("name", "type_2")
                                    .append("sparse", true)
                                    .append("ns", "test." + collection)
                                    .append("key", new BasicDBObject("baz", "hashed")));

        assertIndex(indexes, new BasicDBObject("v", 1)
                                    .append("name", "hello")
                                    .append("expireAfterSeconds", 3600)
                                    .append("ns", "test." + collection)
                                    .append("key", new BasicDBObject("value_1", 1)));

        assertIndex(indexes, new BasicDBObject("v", 1)
                                    .append("name", "value_2_hashed")
                                    .append("ns", "test." + collection)
                                    .append("key", new BasicDBObject("value_2", "hashed")));
    }

    /* ====================================================================== */

    @Test
    public void testNormalBeanAnnotations()
    throws Exception {
        assertEquals(normalBeanStore.getCollection(), normalCollection);
        testIndexes(normalBeanStore);
    }

    @Test
    public void testLombokBeanAnnotations()
    throws Exception {
        assertEquals(lombokBeanStore.getCollection(), lombokCollection);
        testIndexes(lombokBeanStore);
    }

    /* ====================================================================== */

    @Collection(normalCollection)
    @Index(name="type_1",
           options=UNIQUE,
           expiresAfter="1 day",
           keys={@Key(field="foo", type=ASCENDING),
                 @Key(field="bar", type=DESCENDING)})
    @Index(name="type_2",
           options=SPARSE,
           keys={@Key(field="baz", type=HASHED)})
    public static class NormalBean extends Document {

        private String value1;
        private String value2;

        protected NormalBean() {
            /* Nothing to do */
        }

        @Indexed(name="hello", expiresAfter="1 hour")
        public String getValue1() {
            return value1;
        }

        public void setValue1(String value) {
            value1 = value;
        }

        public String getValue2() {
            return value2;
        }

        @Indexed(type=HASHED)
        public void setValue2(String value) {
            value2 = value;
        }

    }

    /* ====================================================================== */


    @Collection(lombokCollection)
    @Index(name="type_1",
           options=UNIQUE,
           expiresAfter="1 day",
           keys={@Key(field="foo", type=ASCENDING),
                 @Key(field="bar", type=DESCENDING)})
    @Index(name="type_2",
           options=SPARSE,
           keys={@Key(field="baz", type=HASHED)})
    public static class LombokBean extends Document {

        @Setter @Getter
        @Indexed(name="hello", expiresAfter="1 hour")
        @JsonProperty("value_1")
        private String value1;

        @Setter @Getter
        @Indexed(type=HASHED)
        @JsonProperty("value_2")
        private String value2;

    }
}
