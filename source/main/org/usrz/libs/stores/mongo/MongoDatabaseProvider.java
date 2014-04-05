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

import java.util.Objects;

import javax.inject.Named;

import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.configurations.ConfiguredProvider;

import com.google.inject.Inject;
import com.google.inject.ProvisionException;
import com.mongodb.DB;
import com.mongodb.MongoClient;

@SuppressWarnings("restriction")
public class MongoDatabaseProvider extends ConfiguredProvider<DB> {

    private static final Log log = new Log();

    private String host = "localhost";
    private int port = 27017;
    private String db = null;

    public MongoDatabaseProvider() {
        /* Nothing to do */
    }

    @Inject(optional=true)
    private void setHost(@Named("host") String host) {
        System.err.println("HOST IS " + host);
        this.host = Objects.requireNonNull(host, "Null host");
    }

    @Inject(optional=true)
    private void setPort(@Named("port") int port) {
        if ((port < 1) || port > Short.MAX_VALUE)
            throw new IllegalArgumentException("Invalid port " + port);
        this.port = port;
    }

    @Inject
    private void setDatabase(@Named("database") String db) {
        this.db = Objects.requireNonNull(host, "Null database");
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
