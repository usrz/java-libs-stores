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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Guice;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

public class ReferencesTest extends AbstractTest {

    private final String referencingCollection = Strings.random(16);
    private final String referencedCollection = Strings.random(16);

    @BeforeClass
    public void prepare()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector((binder) -> new MongoBuilder(binder)
                .configure(configurations.strip("mongo"))
                .store(ReferencingBean.class, referencingCollection)
                .store(ReferencedBean.class, referencedCollection)
            ).injectMembers(this);
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

        /* Referenced bean comes first */
        final String value = Strings.random(16);
        final ReferencedBean referencedBean = referencedStore.store(new ReferencedBean().setValue(value));
        final ReferencedBean referencedBean2 = referencedStore.find(referencedBean.id());

        assertNotNull(referencedBean2);
        assertNotSame(referencedBean2, referencedBean);
        assertEquals(referencedBean2.id(), referencedBean.id());
        assertEquals(referencedBean2.getValue(), referencedBean.getValue());

        /* Let's create and store the referencing bean */
        final ReferencingBean referencingBean = referencingStore.store(new ReferencingBean().setReferenced(referencedBean));
        assertNotNull(referencingBean.getReferenced());

        /* Verify that in the DB we saved only the reference */
        final DBObject object = db.getCollection(referencingCollection).findOne(referencingBean.id());
        assertNotNull(object);
        final DBRef reference = (DBRef) object.get("referenced");
        assertNotNull(reference);
        assertEquals(reference.getId(), referencedBean.id());
        assertEquals(reference.getRef(), referencedCollection);

        /* Reload the document from the store and make sure we got the right stuff */
        final ReferencingBean referencingBean2 = referencingStore.find(referencingBean.id());
        assertNotNull(referencingBean2);

        final ReferencedBean referencedBean3 = referencingBean2.getReferenced();
        assertNotNull(referencedBean3);
        assertNotSame(referencedBean3, referencedBean);
        assertEquals(referencedBean3.id(), referencedBean.id());
        assertEquals(referencedBean3.getValue(), referencedBean.getValue());

    }

    /* ====================================================================== */

    @Accessors(chain=true)
    public static class ReferencingBean extends Document {

        @Getter
        @Setter
        @JsonProperty("referenced")
        private ReferencedBean referenced;


    }

    /* ====================================================================== */

    @Accessors(chain=true)
    public static class ReferencedBean extends Document {

        @Getter @Setter
        private String value;

    }

}
