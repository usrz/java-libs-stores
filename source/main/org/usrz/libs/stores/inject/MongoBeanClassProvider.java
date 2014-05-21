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
package org.usrz.libs.stores.inject;

import static org.usrz.libs.utils.Check.notNull;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.usrz.libs.stores.AbstractDocument;
import org.usrz.libs.stores.Document;
import org.usrz.libs.utils.beans.BeanBuilder;
import org.usrz.libs.utils.inject.InjectingProvider;
import org.usrz.libs.utils.inject.Injections;

import com.google.inject.Injector;
import com.google.inject.Key;

public class MongoBeanClassProvider<D extends Document> extends InjectingProvider<Class<D>> {

    private final Key<BeanBuilder> key;
    private final Class<? super D> original;

    private Class<?> type;
    private final Set<Class<?>> interfaces = new HashSet<>();

    public MongoBeanClassProvider(Class<? super D> type) {
        this.key = Key.get(BeanBuilder.class);
        setBeanDetails(type);
        original = type;
    }

    public void setBeanDetails(Class<?> type, Class<?>... interfaces) {
        this.type = notNull(type, "Null type");

        /* Determine what to do (abstract class or interface? */
        if (type.isInterface()) {
            this.type = AbstractDocument.class;
            this.interfaces.add(type);
        } else {
            this.type = type;
        }

        /* Add any remaining interface */
        if (interfaces == null) return;
        for (Class<?> interfaceClass: interfaces)
            this.interfaces.add(interfaceClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<D> get(Injector injector) {
        final BeanBuilder builder = Injections.getInstance(injector, key);

        /* Create the actual type (if needed) or just the one specified */
        final Class<?> bean;
        if (Modifier.isAbstract(type.getModifiers()) || (!interfaces.isEmpty())) {
            bean = builder.newClass(type, interfaces.toArray(new Class<?>[interfaces.size()]));
        } else {
            bean = this.type;
        }

        /* Verify that the class we created is <D> and MongoDocument */
        if (!original.isAssignableFrom(bean))
            throw new IllegalArgumentException("Constructed bean does not extendd required class " + original.getName());
        if (!Document.class.isAssignableFrom(bean))
            throw new IllegalArgumentException("Constructed bean does not extendd Document class");

        /* Done */
        return (Class<D>) bean;
    }

}
