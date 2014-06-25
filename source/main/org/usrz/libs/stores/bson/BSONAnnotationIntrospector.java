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
package org.usrz.libs.stores.bson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Stores;

import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.google.inject.Injector;

@Singleton
public class BSONAnnotationIntrospector extends NopAnnotationIntrospector {

    private static final Log log = new Log();

    private final Stores stores;
    private final ConcurrentHashMap<Type, BSONReferenceSerializer<?>> serializers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Type, BSONReferenceDeserializer<?>> deserializers = new ConcurrentHashMap<>();

    @Inject
    private BSONAnnotationIntrospector(Injector injector) {
        Stores stores = null;
        try {
            stores = injector.getInstance(Stores.class);
        } catch (Exception exception) {
            log.warn("Warning, Stores implementation not available, Document/Reference resolution will fail", exception);
        } finally {
            this.stores = stores;
        }
    }

    @Override
    public Object findSerializer(Annotated a) {

        final Type type;
        if (a instanceof AnnotatedField) {
            type = ((AnnotatedField) a).getGenericType();
        } else if (a instanceof AnnotatedMethod) {
            type = ((AnnotatedMethod) a).getGenericType();
        } else {
            return null;
        }

        if (isDocument(type)) {
            if (stores == null) throw new IllegalStateException("Stores not available");
            log.debug("Serializing %s reference for %s", type, a);
            return serializers.computeIfAbsent(type, (t) -> {
                log.debug("Creating new references serializer for type %s", t);
                return BSONReferenceSerializer.get(stores.getStore(t));
            });
        }

        return null;
    }

    @Override
    public Object findDeserializer(Annotated a) {

        final Type type;
        if (a instanceof AnnotatedField) {
            type = ((AnnotatedField) a).getGenericType();
        } else if (a instanceof AnnotatedMethod) {
            final AnnotatedMethod m = (AnnotatedMethod) a;
            if (m.getParameterCount() != 1)
                throw new RuntimeJsonMappingException("@Reference annotated method " + a + " must have a single parameter");
            type = m.getParameter(0).getGenericType();
        } else if (a instanceof AnnotatedParameter) {
            type = ((AnnotatedParameter) a).getGenericType();
        } else {
            return null;
        }

        if (isDocument(type)) {
            /* Check that we have our stores */
            if (stores == null) throw new IllegalStateException("Stores not available");

            log.debug("De-serializing %s reference for %s", type, a);
            return deserializers.computeIfAbsent(type, (t) -> {
                log.debug("Creating new references de-serializer for type %s", t);
                return BSONReferenceDeserializer.get(stores.getStore(t));
            });
        }

        return null;
    }

    private boolean isDocument(Type type) {

        /* No type? Default deserializer */
        if (type == null) return false;

        if (type instanceof Class) {

            /* A Document? Serialize reference */
            if (Document.class.isAssignableFrom((Class<?>) type)) return true;

        } else if (type instanceof ParameterizedType) {

            /* Check the raw type of this parameterized type */
            return isDocument(((ParameterizedType) type).getRawType());

        }

        /* Type is not a Class or Parameterized Type? Boo! */
        return false;
    }

}
