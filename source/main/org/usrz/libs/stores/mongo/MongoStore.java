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

import static org.usrz.libs.utils.Check.notNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Date;

import org.bson.BSONException;
import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.AbstractStore;
import org.usrz.libs.stores.Cursor;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Query;
import org.usrz.libs.stores.annotations.Indexed;
import org.usrz.libs.stores.bson.BSONObjectMapper;
import org.usrz.libs.utils.Strings;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;


public class MongoStore<D extends Document> extends AbstractStore<D> {

    private static final String ID = "_id";
    private static final String LAST_MODIFIED_AT = "_last_modified_at";
    private static final Log log = new Log();

    private final DBCollection collection;
    private final BSONObjectMapper mapper;
    private final Class<D> rawType;
    private final Type type;

    private final Field idField;
    private final Field lastModifiedAtField;

    public MongoStore(BSONObjectMapper mapper,
                      DBCollection collection,
                      Class<D> rawType,
                      Type type) {
        this.collection = notNull(collection, "Null collection");
        this.mapper = notNull(mapper, "Null mapper");
        this.rawType = notNull(rawType, "Null raw type");
        this.type = notNull(type, "Null type");

        /* Figure out possible indexes from the bean description */
        final JavaType javaType = SimpleType.construct(rawType);
        final SerializationConfig config = mapper.getSerializationConfig();
        final BeanDescription description = config.getClassIntrospector().forSerialization(config, javaType, null);

        for (BeanPropertyDefinition property: description.findProperties()) {

            /* Check if we have some "_id" property */
            if (property.getName().equals(ID)) {
                if (property.couldSerialize())
                    log.warn("Type %s defines accessor for \"%s\" property",
                             rawType.getName(), ID);
            }

            /* Check if we have some "_last_modified" property */
            if (property.getName().equals(LAST_MODIFIED_AT)) {
                if (property.couldSerialize())
                    log.warn("Type %s defines accessor for \"%s\" property",
                             rawType.getName(), LAST_MODIFIED_AT);
            }

            /* Do we need to index this property? */
            ensureIndex(property);
        }

        /* Be sneaky, use reflection to set ID and Last Modified final fields */
        try {
            this.idField = Document.class.getDeclaredField("id");
            this.idField.setAccessible(true);
            this.lastModifiedAtField = Document.class.getDeclaredField("lastModifiedAt");
            this.lastModifiedAtField.setAccessible(true);
        } catch (NoSuchFieldException exception) {
            throw new IllegalArgumentException("Unable to access Document fields", exception);
        }
    }

    private void ensureIndex(BeanPropertyDefinition property) {
        final String name = property.getName();
        final AnnotatedMember accessor = property.getAccessor();
        final AnnotatedMember mutator = property.getMutator();

        /* Jackson copies annotations from mutator to accessor, too */
        final AnnotatedMember member = accessor != null ? accessor : mutator;

        final Indexed annotation = member.getAnnotation(Indexed.class);
        if (annotation == null) return;
        new MongoIndex().withAnnotation(name, annotation).ensureIndex(collection);
    }

    @Override
    public Type getDocumentType() {
        return type;
    }

    @Override
    public Class<D> getDocumentClass() {
        return rawType;
    }

    @Override
    public String getCollection() {
        return collection.getName();
    }

    @Override
    public D find(String id) {
        return convert(collection.findOne(id(id)));
    }

    @Override
    public D store(D object) {
        final BasicDBObject bson;
        try {
            bson = mapper.writeValueAsBson(object);
        } catch (IOException exception) {
            throw new BSONException("Exception writing BSON for " + object, exception);
        }
        final String id = object.id();
        bson.put(ID, id == null ? Strings.random(32) : id);
        bson.put(LAST_MODIFIED_AT, new Date());
        log.debug("Saving %s in collection \"%s\"", bson, collection);
        collection.save(bson);
        return convert(bson);
    }

    @Override
    public boolean delete(String id) {
        return collection.remove(id(id)).getN() != 0;
    }

    /* ====================================================================== */

    @Override
    public Query<D> query() {
        return new MongoQuery<D>(new BasicDBObject()) {

            @Override
            public Cursor<D> documents() {
                return new MongoCursor<D>(collection.find(query), (o) -> convert(o));
            }
        };
    }

    /* ====================================================================== */

    private BasicDBObject id(String id) {
        return new BasicDBObject(ID, notNull(id, "Null ID"));
    }

    /* ====================================================================== */

    private D convert(DBObject object) {
        if (object == null) return null;

        /* Strip ID and Last Modified Date from BSON */
        final String id = object.removeField(ID).toString();
        if (id == null) throw new IllegalStateException("No ID found for document");

        final Date lastModified = (Date) object.removeField(LAST_MODIFIED_AT);
        if (lastModified == null) log.warn("No \"%s\" field in BSON for \"%s/%s\"", LAST_MODIFIED_AT, collection.getName(), id);

        /* Map the (partial) BSON to the object */
        final D instance;
        try {
            instance = mapper.readValue(object, rawType);
        } catch (IOException exception) {
            throw new BSONException("Exception reading BSON from " + object, exception);
        }

        /* Forcedly inject ID and LAST MODIFIED (hackzone, be sneaky) */
        try {
            this.idField.set(instance, id);
            this.lastModifiedAtField.set(instance, lastModified);
            return instance;
        } catch (IllegalAccessException exception) {
            throw new MongoException("Unable to set Document fields", exception);
        }
    }
}
