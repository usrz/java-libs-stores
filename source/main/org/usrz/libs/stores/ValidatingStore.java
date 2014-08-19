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
package org.usrz.libs.stores;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import org.usrz.libs.logging.Log;

/**
 * A {@link Store} capable of <em>validating</em> documents using the
 * <a href="http://beanvalidation.org/">Java Bean Validation Framework</a>.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <D> The type of {@link Document}s stored by this {@link Store}.
 */
public class ValidatingStore<D extends Document> extends AbstractStoreWrapper<D> {

    private final Log log = new Log();
    private final Validator validator;

    public ValidatingStore(Store<D> store) {
        super(store);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * Validate and store the specified {@link Document}
     *
     * @throws NullPointerException If the {@link Document} was <b>null</b>.
     * @throws ConstraintViolationException If the specified {@link Document}
     *                                      failed validation.
     */
    @Override
    public D store(D document)
    throws NullPointerException, ConstraintViolationException {
        if (document == null) throw new NullPointerException("Null document");

        log.debug("Validating document %s", document);
        final Set<ConstraintViolation<Object>> violations = validator.validate(document);
        if ((violations == null) || (violations.isEmpty())) return store.store(document);

        throw new ConstraintViolationException("Validation failed for " + document, violations);
    }

}
