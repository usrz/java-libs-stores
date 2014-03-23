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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Relation;
import org.usrz.libs.stores.Store;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.configurations.Configurations;
import org.usrz.libs.utils.configurations.JsonConfigurations;

import com.google.inject.Guice;
import com.mongodb.DB;

public class RelationTest extends AbstractTest {

    private final String fooCollection = UUID.randomUUID().toString();
    private final String barCollection = UUID.randomUUID().toString();
    private final String relCollection = UUID.randomUUID().toString();
    private final Log log = new Log();

    @BeforeClass
    public void prepare()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector(
            new MongoDatabaseModule(configurations.strip("mongo")) {
                @Override public void configure() {
                    this.bind(Foo.class).toCollection(fooCollection);
                    this.bind(Bar.class).toCollection(barCollection);
                    this.join(Foo.class, Bar.class).toCollection(relCollection);
                }
            }).injectMembers(this);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() {
        if (db != null) {
            try { db.getCollection(fooCollection).drop(); } catch (Exception exception) { log.error(exception, "Exception dropping FOO"); }
            try { db.getCollection(barCollection).drop(); } catch (Exception exception) { log.error(exception, "Exception dropping BAR"); }
            try { db.getCollection(relCollection).drop(); } catch (Exception exception) { log.error(exception, "Exception dropping REL"); }
        }
    }

    /* ====================================================================== */

    @Inject private DB db;
    @Inject private Store<Foo> fooStore;
    @Inject private Store<Bar> barStore;
    @Inject private Relation<Foo, Bar> relation;

    /* ====================================================================== */

    private final <D> Set<D> fromIterator(Iterator<D> iterator) {
        final Set<D> set = new HashSet<D>();
        while (iterator.hasNext()) {
            D entry = iterator.next();
            assertNotNull(entry);
            set.add(entry);
        }
        return set;
    }

    @Test
    public void testRelation() {
        final Foo foo1 = fooStore.store(fooStore.create()); // unjoined
        final Foo foo2 = fooStore.store(fooStore.create()); // joined with bar2
        final Foo foo3 = fooStore.store(fooStore.create()); // joined with bar4, bar5
        final Foo foo4 = fooStore.store(fooStore.create()); // joined from bar3
        final Foo foo5 = fooStore.store(fooStore.create()); // joined from bar3

        final Bar bar1 = barStore.store(barStore.create()); // unjoined
        final Bar bar2 = barStore.store(barStore.create()); // joined with foo2
        final Bar bar3 = barStore.store(barStore.create()); // joined with foo4, foo5
        final Bar bar4 = barStore.store(barStore.create()); // joined from foo3
        final Bar bar5 = barStore.store(barStore.create()); // joined from foo3

        relation.associate(foo2, bar2);
        relation.associate(foo3, bar4);
        relation.associate(foo3, bar5);
        relation.associate(foo4, bar3);
        relation.associate(foo5, bar3);

        assertTrue(relation.isAssociated(foo2, bar2));
        assertTrue(relation.isAssociated(foo3, bar4));
        assertTrue(relation.isAssociated(foo3, bar5));
        assertTrue(relation.isAssociated(foo4, bar3));
        assertTrue(relation.isAssociated(foo5, bar3));

        assertFalse(relation.isAssociated(foo1, bar1));
        assertFalse(relation.isAssociated(foo3, bar3));
        assertFalse(relation.isAssociated(foo4, bar4));
        assertFalse(relation.isAssociated(foo5, bar5));

        final Set<Bar> foo1joined = fromIterator(relation.findR(foo1));
        final Set<Bar> foo2joined = fromIterator(relation.findR(foo2));
        final Set<Bar> foo3joined = fromIterator(relation.findR(foo3));
        final Set<Bar> foo4joined = fromIterator(relation.findR(foo4));
        final Set<Bar> foo5joined = fromIterator(relation.findR(foo5));

        final Set<Foo> bar1joined = fromIterator(relation.findL(bar1));
        final Set<Foo> bar2joined = fromIterator(relation.findL(bar2));
        final Set<Foo> bar3joined = fromIterator(relation.findL(bar3));
        final Set<Foo> bar4joined = fromIterator(relation.findL(bar4));
        final Set<Foo> bar5joined = fromIterator(relation.findL(bar5));

        assertTrue(foo1joined.isEmpty());
        assertTrue(bar1joined.isEmpty());

        assertTrue(foo2joined.contains(bar2));
        assertEquals(foo2joined.size(), 1);
        assertTrue(bar2joined.contains(foo2));
        assertEquals(bar2joined.size(), 1);

        assertTrue(foo3joined.contains(bar4));
        assertTrue(foo3joined.contains(bar5));
        assertEquals(foo3joined.size(), 2);
        assertTrue(bar4joined.contains(foo3));
        assertTrue(bar5joined.contains(foo3));
        assertEquals(bar4joined.size(), 1);
        assertEquals(bar5joined.size(), 1);

        assertTrue(bar3joined.contains(foo4));
        assertTrue(bar3joined.contains(foo5));
        assertEquals(bar3joined.size(), 2);
        assertTrue(foo4joined.contains(bar3));
        assertTrue(foo5joined.contains(bar3));
        assertEquals(foo4joined.size(), 1);
        assertEquals(foo5joined.size(), 1);

        relation.dissociate(foo2, bar2);

        assertFalse(relation.isAssociated(foo2, bar2));
        assertTrue(fromIterator(relation.findR(foo2)).isEmpty());
        assertTrue(fromIterator(relation.findL(bar2)).isEmpty());

        relation.dissociate(foo3, bar4);
        assertFalse(relation.isAssociated(foo3, bar4));
        assertTrue(fromIterator(relation.findL(bar4)).isEmpty());
        assertEquals(fromIterator(relation.findR(foo3)).size(), 1);

        relation.dissociate(foo4, bar3);
        assertFalse(relation.isAssociated(foo4, bar3));
        assertTrue(fromIterator(relation.findR(foo4)).isEmpty());
        assertEquals(fromIterator(relation.findL(bar3)).size(), 1);

    }

    /* ====================================================================== */

    private static interface Foo extends Document {}

    private static interface Bar extends Document {}

}
