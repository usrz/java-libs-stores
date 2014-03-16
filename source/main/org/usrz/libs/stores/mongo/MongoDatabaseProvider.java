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

import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.configurations.Configurations;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MongoDatabaseProvider implements Provider<DB> {

    private static final Log log = new Log();

    private String host;
    private int port;
    private String db;

    public MongoDatabaseProvider() {
        /* Nothing to do */
    }

    @Inject
    public void setConfigurations(Configurations configurations) {
        final Configurations mongo = configurations.strip("mongo");
        host = mongo.get("host", "localhost");
        port = mongo.get("port", 27017);
        db = mongo.get("database");
        if (db == null) throw new IllegalStateException("Missing mongo.db configuration");
    }

    @Override
    public DB get() {
        if (db == null) throw new ProvisionException(this.getClass().getSimpleName() + " not injected");
        log.info("Creating new MongoDB client for host: %s, port: %d, database: %s", host, port, db);
        try {
            return new MongoClient(host, port).getDB(db);
        } catch (Exception exception) {
            throw new ProvisionException("Unable to create MongoDB client", exception);
        }
    }

}
