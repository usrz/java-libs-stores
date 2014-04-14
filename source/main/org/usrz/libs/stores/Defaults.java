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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface Defaults {

     Class<? extends Consumer<Initializer>> value();

     /* ===================================================================== */

     public interface Initializer {

         public Initializer property(String name, Object value);

         public Initializer inject(String name, Key<?> key);

         default Initializer inject(String name, TypeLiteral<?> type) {
             return inject(name, Key.get(type));
         }

         default Initializer inject(String name, Class<?> type) {
             return inject(name, Key.get(type));
         }

     }

     /* ===================================================================== */

     public static final class Finder {

         private static final class Null implements Consumer<Initializer> {
            @Override public void accept(Initializer i) {}
         }

         private Finder() {
             throw new IllegalStateException("Do not construct");
         }

         public static final Class<? extends Consumer<Initializer>> find(Class<?> type) {
             final Set<Defaults> defaults = new HashSet<>();
             findDefaults(type, defaults);
             if (defaults.size() == 0) return Null.class;
             if (defaults.size() > 1) {
                 final StringBuilder builder = new StringBuilder("Multiple @Defaults found in class hierarchy for ")
                                                         .append(type.getName());
                 defaults.forEach((current) -> builder.append("\n    " + current));
                 throw new IllegalArgumentException(builder.toString());
             }
             return defaults.iterator().next().value();
         }

         private static void findDefaults(Class<?> type, Set<Defaults> defaults) {
             if (type == null) return;
             final Defaults current = type.getAnnotation(Defaults.class);
             if (current != null) defaults.add(current);
             for (Class<?> interfaceClass: type.getInterfaces())
                 findDefaults(interfaceClass, defaults);
             findDefaults(type.getSuperclass(), defaults);
         }

     }
}
