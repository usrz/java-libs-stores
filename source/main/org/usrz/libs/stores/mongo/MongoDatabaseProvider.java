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

import java.io.IOException;

import org.usrz.libs.utils.configurations.Configurations;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MongoDatabaseProvider implements Provider<DB> {

    private DB database = null;

    public MongoDatabaseProvider() {
        /* Nothing to do */
    }

    @Inject
    public void setConfigurations(Configurations configurations)
    throws IOException {
        final Configurations mongo = configurations.strip("mongo");
        final String host = mongo.get("host", "localhost");
        final int port = mongo.get("port", 27017);
        final String db = mongo.get("database");
        if (db == null) throw new IllegalStateException("Missing mongo.db configuration");
        final MongoClient client = new MongoClient(host, port);
        database = client.getDB(db);
    }

    @Override
    public DB get() {
        if (database == null) throw new IllegalStateException("Missing DB, not injected?");
        return database;
    }

}
