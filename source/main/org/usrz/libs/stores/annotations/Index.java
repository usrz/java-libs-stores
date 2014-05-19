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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.usrz.libs.stores.annotations.Indexes.Type.ASCENDING;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.annotations.Indexes.Option;
import org.usrz.libs.stores.annotations.Indexes.Type;

/**
 * An annotation for {@link Document} types specifying that an index must be
 * ensured for the specified property.
 * <pre>
 * {@literal @}Index(name="foo_index" ,
 *         options=Index.Options.UNIQUE ,
 *         keys={{@literal @}Key(field="foo", type=Index.Type.ASCENDING),
 *               {@literal @}Key(field="bar", type=Index.Type.DESCENDING)} )
 * public class MyDocument extends Document { ... }
 * </pre>
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
@Inherited
@Documented
@Target({TYPE})
@Retention(RUNTIME)
@Repeatable(Indexes.class)
public @interface Index {

    /**
     * A <em>key</em> definition for an index, specifiable when this annotation
     * is used on a <em>type</em> (and forbidden on <em>field</em>s or
     * <em>method</em>s).
     *
     * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
     */
    public @interface Key {
        /** The name of the property to index. */
        public String field();
        /** The type of the index to create (default {@link Type#ASCENDING}). */
        public Type type() default ASCENDING;
    }

    /** The name of the index to create. */
    public String name() default "";

    /** The options for the index to create. */
    public Option[] options() default {};

    /** The list of property keys to index. */
    public Key[] keys();

}
