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

import static org.usrz.libs.utils.Check.notEmpty;
import static org.usrz.libs.utils.Check.notNull;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Provider;

import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.Injections;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public class MongoCollectionProvider implements Provider<DBCollection> {

    private final Log log = new Log();
    private final Map<BasicDBObject, BasicDBObject> indexes = new HashMap<>();
    private final Key<DB> database;
    private final String name;

    private DBCollection collection;

    public MongoCollectionProvider(String collection) {
        name = notEmpty(collection, "Empty collection");
        database = Key.get(DB.class);
    }

    public MongoCollectionProvider(String collection, Annotation database) {
        name = notEmpty(collection, "Empty collection");
        this.database = Key.get(DB.class, database);
    }

    public MongoIndexBuilder requireIndex() {
        final BasicDBObject index = new BasicDBObject();
        final BasicDBObject options = new BasicDBObject();
        indexes.put(index, options);

        return new MongoIndexBuilder() {

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
            public MongoIndexBuilder unique(boolean unique) {
                options.put("unique", unique);
                return this;
            }

            @Override
            public MongoIndexBuilder sparse(boolean sparse) {
                options.put("sparse", sparse);
                return this;
            }

            @Override
            public MongoIndexBuilder expiresAfterSeconds(long seconds) {
                if ((seconds > Integer.MAX_VALUE) || (seconds < 0))
                    throw new IllegalArgumentException("Invalid expiration: " + seconds + " seconds");
                options.put("expireAfterSeconds", (int) seconds);
                return this;
            }
        };
    }

    @Inject
    private void setup(Injector injector) {
        final DB database = Injections.getInstance(injector, this.database);
        final DBCollection collection = database.getCollection(name);

        /* Indexes */
        for (Entry<BasicDBObject, BasicDBObject> entry: indexes.entrySet()) {
            log.debug("Ensuring index on collection %s: keys: %s, options: %s", collection, entry.getKey(), entry.getValue());
            collection.ensureIndex(entry.getKey(), entry.getValue());
        }

        /* Done */
        this.collection = collection;
    }

    @Override
    public DBCollection get() {
        if (collection == null) throw new IllegalStateException("Not constructed");
        return collection;
    }

}
