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

import org.usrz.libs.configurations.ConfigurableProvider;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.logging.Log;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class MongoClientProvider extends ConfigurableProvider<MongoClient, MongoClientProvider> {

    private final Log log = new Log();
    private final MongoClient client = null;

    public MongoClientProvider() {
        /* Nothing to do ... */
    }

    @Override
    protected MongoClient get(Configurations configurations) {
        if (client != null) return client;

        final String host = configurations.get("host", "localhost");
        final int port = configurations.get("port", 27017);

        log.info("Creating new MongoDB client for %s:%d", host, port);
        try {
            return new MongoClient(host, port);
        } catch (Exception exception) {
            throw new MongoException("Unable to create MongoDB client", exception);
        }
    }
}
