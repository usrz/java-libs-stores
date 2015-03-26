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
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.JsonConfigurations;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.annotations.Indexed;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.Strings;

import com.google.inject.Guice;
import com.mongodb.DB;

public class QueriesTest extends AbstractTest {

    private final String collection = Strings.random(16);

    @BeforeClass
    public void prepare()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector((binder) -> new MongoBuilder(binder)
                .configure(configurations.strip("mongo"))
                .store(SimpleBean.class, collection)
            ).injectMembers(this);

        bean0 = store.store(new SimpleBean(0, "zero"));
        bean1 = store.store(new SimpleBean(1, "one"));
        bean2 = store.store(new SimpleBean(2, "two"));
        bean3 = store.store(new SimpleBean(3, "three"));
        bean4 = store.store(new SimpleBean(4, "four"));
        bean5 = store.store(new SimpleBean(5, "five"));
        bean6 = store.store(new SimpleBean(6, "six"));
        bean7 = store.store(new SimpleBean(7, "seven"));
        bean8 = store.store(new SimpleBean(8, "eight"));
        bean9 = store.store(new SimpleBean(9, "nine"));
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

    SimpleBean bean0;
    SimpleBean bean1;
    SimpleBean bean2;
    SimpleBean bean3;
    SimpleBean bean4;
    SimpleBean bean5;
    SimpleBean bean6;
    SimpleBean bean7;
    SimpleBean bean8;
    SimpleBean bean9;

    /* ====================================================================== */

    @Test
    public void testQueryAll() {
        List<SimpleBean> list = store.query().list();
        assertEquals(list.size(), 10);
        assertEquals(list.get(0), bean0);
        assertEquals(list.get(1), bean1);
        assertEquals(list.get(2), bean2);
        assertEquals(list.get(3), bean3);
        assertEquals(list.get(4), bean4);
        assertEquals(list.get(5), bean5);
        assertEquals(list.get(6), bean6);
        assertEquals(list.get(7), bean7);
        assertEquals(list.get(8), bean8);
        assertEquals(list.get(9), bean9);
    }

    @Test
    public void testGreaterThan() {
        List<SimpleBean> list = store.query("value").gt(7).list();
        assertEquals(list.size(), 2);
        assertEquals(list.get(0), bean8);
        assertEquals(list.get(1), bean9);

    }

    @Test
    public void testGreaterThanOrEqual() {
        List<SimpleBean> list = store.query("value").gte(7).list();
        assertEquals(list.size(), 3);
        assertEquals(list.get(0), bean7);
        assertEquals(list.get(1), bean8);
        assertEquals(list.get(2), bean9);
    }

    @Test
    public void testLesserThan() {
        List<SimpleBean> list = store.query("value").lt(2).list();
        assertEquals(list.size(), 2);
        assertEquals(list.get(0), bean0);
        assertEquals(list.get(1), bean1);
    }

    @Test
    public void testLesserThanOrEqual() {
        List<SimpleBean> list = store.query("value").lte(2).list();
        assertEquals(list.size(), 3);
        assertEquals(list.get(0), bean0);
        assertEquals(list.get(1), bean1);
        assertEquals(list.get(2), bean2);
    }

    @Test
    public void testInCollection() {
        List<SimpleBean> list = store.query("value").in(Arrays.asList(new Integer[] { 7, 5, 3 })).list();
        assertEquals(list.size(), 3);
        assertEquals(list.get(0), bean3);
        assertEquals(list.get(1), bean5);
        assertEquals(list.get(2), bean7);
    }

    @Test
    public void testNotInCollection() {
        List<SimpleBean> list = store.query("value").notIn(Arrays.asList(new Integer[] { 0, 1, 3, 4, 6, 7, 9})).list();
        assertEquals(list.size(), 3);
        assertEquals(list.get(0), bean2);
        assertEquals(list.get(1), bean5);
        assertEquals(list.get(2), bean8);
    }

    @Test
    public void testModulo() {
        List<SimpleBean> list = store.query("value").mod(4).list();
        assertEquals(list.size(), 3);
        assertEquals(list.get(0), bean0);
        assertEquals(list.get(1), bean4);
        assertEquals(list.get(2), bean8);
    }

    @Test
    public void testModuloWithRemainder() {
        List<SimpleBean> list = store.query("value").mod(3, 1).list();
        assertEquals(list.size(), 3);
        assertEquals(list.get(0), bean1);
        assertEquals(list.get(1), bean4);
        assertEquals(list.get(2), bean7);
    }

    @Test
    public void testRegularExpression1() {
        List<SimpleBean> list = store.query("named").matches("^f").list();
        assertEquals(list.size(), 2);
        assertEquals(list.get(0), bean5); // alphabetical order here
        assertEquals(list.get(1), bean4); // "four" > "five"
    }

    @Test
    public void testRegularExpression2() {
        List<SimpleBean> list = store.query("named").matches("o").list();
        assertEquals(list.size(), 4);
        assertEquals(list.get(0), bean4); // alphabetical order here
        assertEquals(list.get(1), bean1); // "four" > "one" > "two" > "zero"
        assertEquals(list.get(2), bean2);
        assertEquals(list.get(3), bean0);
    }

    @Test
    public void testComplexQuery1() {
        List<SimpleBean> list = store.query("named").matches("o").and("value").lt(3).list();
        assertEquals(list.size(), 3);
        assertTrue(list.contains(bean0)); // hmm.. dunno sort order
        assertTrue(list.contains(bean1));
        assertTrue(list.contains(bean2));
    }

    @Test
    public void testComplexQuery2() {
        List<SimpleBean> list = store.query("value").lt(3).and("named").matches("o").list();
        assertEquals(list.size(), 3);
        assertTrue(list.contains(bean0)); // hmm.. dunno sort order
        assertTrue(list.contains(bean1));
        assertTrue(list.contains(bean2));
    }

    @Test
    public void testSortAscendingString() {
        List<SimpleBean> list = store.query().orderBy("named").list();
        assertEquals(list.size(), 10);
        assertEquals(list.get(0), bean8); // eight
        assertEquals(list.get(1), bean5); // five
        assertEquals(list.get(2), bean4); // four
        assertEquals(list.get(3), bean9); // nine
        assertEquals(list.get(4), bean1); // one
        assertEquals(list.get(5), bean7); // seven
        assertEquals(list.get(6), bean6); // six
        assertEquals(list.get(7), bean3); // three
        assertEquals(list.get(8), bean2); // two
        assertEquals(list.get(9), bean0); // zero
    }

    @Test
    public void testSortAscendingNumber() {
        List<SimpleBean> list = store.query().orderBy("value").list();
        assertEquals(list.size(), 10);
        assertEquals(list.get(0), bean0);
        assertEquals(list.get(1), bean1);
        assertEquals(list.get(2), bean2);
        assertEquals(list.get(3), bean3);
        assertEquals(list.get(4), bean4);
        assertEquals(list.get(5), bean5);
        assertEquals(list.get(6), bean6);
        assertEquals(list.get(7), bean7);
        assertEquals(list.get(8), bean8);
        assertEquals(list.get(9), bean9);
    }

    @Test
    public void testSortDescendingString() {
        List<SimpleBean> list = store.query().orderBy("named", false).list();
        assertEquals(list.size(), 10);
        assertEquals(list.get(0), bean0); // zero
        assertEquals(list.get(1), bean2); // two
        assertEquals(list.get(2), bean3); // three
        assertEquals(list.get(3), bean6); // six
        assertEquals(list.get(4), bean7); // seven
        assertEquals(list.get(5), bean1); // one
        assertEquals(list.get(6), bean9); // nine
        assertEquals(list.get(7), bean4); // four
        assertEquals(list.get(8), bean5); // five
        assertEquals(list.get(9), bean8); // eight
    }

    @Test
    public void testSortDescendingNumber() {
        List<SimpleBean> list = store.query().orderBy("value", false).list();
        assertEquals(list.size(), 10);
        assertEquals(list.get(0), bean9);
        assertEquals(list.get(1), bean8);
        assertEquals(list.get(2), bean7);
        assertEquals(list.get(3), bean6);
        assertEquals(list.get(4), bean5);
        assertEquals(list.get(5), bean4);
        assertEquals(list.get(6), bean3);
        assertEquals(list.get(7), bean2);
        assertEquals(list.get(8), bean1);
        assertEquals(list.get(9), bean0);
    }

    @RequiredArgsConstructor
    public static class SimpleBean extends Document {

        @Indexed @Getter private final int value;
        @Indexed @Getter private final String named;

        @Override
        public boolean equals(Object object) {
            if (super.equals(object)) {
                return ((SimpleBean) object).value == value;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return "SimpleBean[" + value + "->" + named + "]";
        }
    }

}
