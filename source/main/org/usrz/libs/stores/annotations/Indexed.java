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
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.usrz.libs.stores.annotations.Indexes.Type.ASCENDING;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.annotations.Indexes.Option;
import org.usrz.libs.stores.annotations.Indexes.Type;

/**
 * An annotation for {@link Document} <em>field</em>s, <em>getter</em> and
 * <em>setter</em> method specifying that an index must be ensured
 * for the property.
 * <pre>
 * public class MyDocument extends Document {
 *    {@literal @}Index(options=Index.Options.UNIQUE)
 *     public String getValue() { ... }
 * }
 * </pre>
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface Indexed {

    /** The name of the index to create. */
    public String name() default "";

    /** The type of the index to create (default {@link Type#ASCENDING}). */
    public Type type() default ASCENDING;

    /** The options for the index to create. */
    public Option[] options() default {};

    /** The duration for automatic expiration. */
    public String expiresAfter() default "";

}
