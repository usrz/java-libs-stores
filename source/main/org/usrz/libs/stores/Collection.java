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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Consumer;

import javassist.CtField.Initializer;

/**
 * An annotation defining a {@link Consumer} for {@link Initializer}s.
 * <p>
 * {@link Document} classes can be annotated with this annotation, and the
 * specified {@link Consumer} will be invoked on creation and retrieval.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface Collection {

    String value();

}
