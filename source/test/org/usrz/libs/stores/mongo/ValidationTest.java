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
import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.EqualsAndHashCode;
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

public class ValidationTest extends AbstractTest {

    private final String collection = Strings.random(16);

    @BeforeClass
    public void prepare()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));

        Guice.createInjector((binder) ->
                new MongoBuilder(binder)
                        .configure(configurations.strip("mongo"))
                        .store(ValidatedBean.class, collection)
                        .withValidation()
            ).injectMembers(this);
    }

    @AfterClass(alwaysRun = true)
    public void cleanup()
    throws IOException {
        db.getCollection(collection).drop();
    }

    /* ====================================================================== */

    @Inject
    private Store<ValidatedBean> store;
    @Inject
    private DB db;

    /* ====================================================================== */

    @Test
    public void testValidation()
    throws Exception {

        final String value = Strings.random(16);

        ValidatedBean bean = new ValidatedBean();

        assertNull(bean.getValue());

        /* Null test! */
        try {
            store.store(bean);
            fail("ConstraintViolationException not thrown");
        } catch (ConstraintViolationException exception) {
            final Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
            assertEquals(violations.size(), 1, "Wrong number of violations");
            final ConstraintViolation<?> violation = violations.iterator().next();
            assertNotNull(violation, "Null violation");
            assertEquals(violation.getMessage(), "The null value shall not be saved!", "Wrong violation message");
            assertEquals(violation.getPropertyPath().toString(), "value", "Wrong violation path");

            System.out.println(violation.getPropertyPath().toString());
            // TODO validate!
        }

        /* Empty string test! */
        try {
            bean.setValue("");
            store.store(bean);
            fail("ConstraintViolationException not thrown");
        } catch (ConstraintViolationException exception) {
            final Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
            assertEquals(violations.size(), 1, "Wrong number of violations");
            final ConstraintViolation<?> violation = violations.iterator().next();
            assertNotNull(violation, "Null violation");
            assertEquals(violation.getMessage(), "The value shall be longer than 1 characters!", "Wrong violation message");
            assertEquals(violation.getPropertyPath().toString(), "value", "Wrong violation path");
        }

        /* Good value test! */
        bean.setValue(value);
        bean = store.store(bean);
        assertNotNull(bean, "Null stored bean");
        assertEquals(bean.value, value, "Wrong stored value");

        /* Re-read to ensure */
        bean = store.find(bean.id());
        assertNotNull(bean, "Null retrieved bean");
        assertEquals(bean.value, value, "Wrong retrieved value");

    }

    @EqualsAndHashCode(callSuper=true)
    public static class ValidatedBean extends Document {

        @Getter @Setter
        @NotNull(message="The null value shall not be saved!")
        @Size(min=1, message="The value shall be longer than {min} characters!")
        private String value;

    }
}
