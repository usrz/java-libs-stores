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
package org.usrz.libs.stores.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Date;

import org.usrz.libs.stores.AbstractDocument;
import org.usrz.libs.stores.Document;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A simple annotation usable in constructors when implementing the
 * {@link Document} interface or extending {@link AbstractDocument}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target({PARAMETER, FIELD, METHOD})

@JacksonAnnotationsInside
@JsonProperty(value = LastModified.LAST_MODIFIED, required = true)
public @interface LastModified {

    /**
     * The BSON field name for a {@link Document}
     * <em>last modified</em> {@link Date}.
     */
    public static final String LAST_MODIFIED = "_last_modified_at";

}
