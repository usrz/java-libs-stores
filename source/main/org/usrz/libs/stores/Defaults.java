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

import com.google.inject.Injector;

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
public @interface Defaults {

    /**
     * The {@link Consumer} for initialization used both for creation and
     * retrieval of {@link Document}s.
     */
    Class<? extends Consumer<Initializer>> value() default Finder.Null.class;

    /**
     * The {@link Consumer} for initialization used for creation of new
     * {@link Document}s.
     */
    Class<? extends Consumer<Initializer>> create() default Finder.Null.class;

    /**
     * The {@link Consumer} for initialization used for retrieval of existing
     * {@link Document}s.
     */
    Class<? extends Consumer<Initializer>> update() default Finder.Null.class;

    /* ===================================================================== */

    /**
     * An object which can be {@linkplain Consumer#accept(Object) consumed}
     * at creation and retrieval of {@link Document}s specifying the default
     * initialization values.
     *
     * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
     */
    public interface Initializer {

        /**
         * Set the property with the given name to the given value.
         */
        public Initializer property(String name, Object value);

    }

    /* ===================================================================== */

    /**
     * A simple utility class finding the {@link Defaults} annotation value
     * for a {@link Document} class.
     *
     * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
     */
    public static final class Finder {

        private static final Null NULL = new Null();

        private static final class Null implements Consumer<Initializer> {
            @Override public void accept(Initializer i) {}
        }

        private Finder() {
            throw new IllegalStateException("Do not construct");
        }

        /**
         * Find the {@link Defaults} annotation value for a {@link Document}.
         */
        public static final Consumer<Initializer> find(Class<? extends Document> type, Injector injector, boolean create) {
            final Set<Defaults> defaults = new HashSet<>();
            findDefaults(type, defaults);
            if (defaults.size() == 0) return NULL;
            if (defaults.size() > 1) {
                final StringBuilder builder = new StringBuilder("Multiple @Defaults found in class hierarchy for ")
                .append(type.getName());
                defaults.forEach((current) -> builder.append("\n    " + current));
                throw new IllegalArgumentException(builder.toString());
            }
            final Defaults annotation = defaults.iterator().next();

            return injector.getInstance(annotation.value()).andThen(
                       create ? injector.getInstance(annotation.create()) :
                                injector.getInstance(annotation.update()));
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
