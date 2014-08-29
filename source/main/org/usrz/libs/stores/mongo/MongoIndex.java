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

import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.annotations.Index;
import org.usrz.libs.stores.annotations.Index.Key;
import org.usrz.libs.stores.annotations.Indexed;
import org.usrz.libs.stores.annotations.Indexes.Option;
import org.usrz.libs.stores.annotations.Indexes.Type;
import org.usrz.libs.stores.inject.MongoIndexBuilder;
import org.usrz.libs.utils.Check;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

public class MongoIndex implements MongoIndexBuilder {

    private static final Log log = new Log();

    private final BasicDBObject index = new BasicDBObject();
    private final BasicDBObject options = new BasicDBObject();

    public MongoIndex() {
        /* Nothing to do */
    }

    @Override
    public MongoIndexBuilder withName(String name) {
        options.put("name", notNull(name, "Null name"));
        return this;
    }

    @Override
    public MongoIndexBuilder withKey(String key, Type type) {
        switch (notNull(type, "Null type")) {
            case ASCENDING:  index.append(notNull(key, "Null name"),  1);       break;
            case DESCENDING: index.append(notNull(key, "Null name"), -1);       break;
            case HASHED:     index.append(notNull(key, "Null name"), "hashed"); break;
            default: throw new IllegalArgumentException("Unsupported type "  + type);
        }
        return this;
    }

    @Override
    public MongoIndexBuilder withOptions(Option... o) {
        for (Option option: o) switch(notNull(option, "Null option")) {
            case SPARSE: options.put("sparse", true); break;
            case UNIQUE: options.put("unique", true); break;
            default: throw new IllegalArgumentException("Unsupported option " + option);
        }
        return this;
    }

    @Override
    public MongoIndexBuilder expiresAfterSeconds(long seconds) {
        if ((seconds > Integer.MAX_VALUE) || (seconds < 0))
            throw new IllegalArgumentException("Invalid expiration: " + seconds + " seconds");
        options.put("expireAfterSeconds", (int) seconds);
        return this;
    }

    /* ====================================================================== */

    public MongoIndex withAnnotation(Index index) {
        Check.notNull(index, "Null @Index annotation");

        if (index.keys().length == 0) throw new IllegalArgumentException("Index keys must be non-empty");
        for (Key key: index.keys()) withKey(key.field(), key.type());
        if (!"".equals(index.expiresAfter())) expiresAfter(index.expiresAfter());
        if (!"".equals(index.name())) withName(index.name());
        withOptions(index.options());

        return this;
    }

    public MongoIndex withAnnotation(String property, Indexed annotation) {
        Check.notEmpty(property, "Invalid property name");
        Check.notNull(annotation, "Null @Indexed annotation");

        withKey(property, annotation.type());
        withOptions(annotation.options());
        if (!"".equals(annotation.name())) withName(annotation.name());
        if (!"".equals(annotation.expiresAfter())) expiresAfter(annotation.expiresAfter());

        return this;
    }

    /* ====================================================================== */

    public void ensureIndex(DBCollection collection) {
        if (options.isEmpty()) {
            log.info("Ensuring index %s with no options on collection \"%s\"", index, collection.getName());
        } else {
            log.info("Ensuring index %s with options %s on collection \"%s\"", index, options, collection.getName());
        }
        collection.createIndex(index, options);
    }

}
