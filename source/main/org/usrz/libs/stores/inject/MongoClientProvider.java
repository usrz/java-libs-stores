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

import static com.mongodb.MongoCredential.createMongoCRCredential;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.inject.ConfigurableProvider;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;

public class MongoClientProvider extends ConfigurableProvider<MongoClient> {

    private final Log log = new Log();
    private final MongoClient client = null;

    public MongoClientProvider() {
        super(MongoConfigurations.class);
    }

    private ServerAddress getServerAddress(Configurations configurations) {
        final String host = configurations.get("host", "localhost");
        final int port = configurations.get("port", 27017);

        try {
            return new ServerAddress(host, port);
        } catch (UnknownHostException exception) {
            throw new MongoException("Unknown host " + host + ":" + port, exception);
        }
    }

    private MongoCredential getMongoCredential(Configurations configurations) {
        final String username = configurations.requireString("username");
        final String password = configurations.requireString("password");
        final String authDb = configurations.get("auth_db", "admin");

        return createMongoCRCredential(username, authDb, password.toCharArray());
    }

    @Override
    protected MongoClient get(Configurations configurations) {
        if (client != null) return client;

        /* Start processing our list of server addresses */
        final List<ServerAddress> servers = new ArrayList<>();
        configurations.group("servers").forEach((key, server) ->
            servers.add(getServerAddress(server))
        );

        /* Add the optional host/port */
        if (!servers.isEmpty() && (configurations.containsKey("host") || configurations.containsKey("port"))) {
            servers.add(getServerAddress(configurations));
        }

        if (servers.isEmpty()) {
            servers.add(getServerAddress(configurations));
        }

        /* Start processing our list of credentials */
        final List<MongoCredential> credentials = new ArrayList<>();
        configurations.group("credentials").forEach((key, credential) ->
            credentials.add(getMongoCredential(credential))
        );

        /* Anything on the root configuration node? */
        if (configurations.containsKey("username") || configurations.containsKey("password")) {
            credentials.add(getMongoCredential(configurations));
        }

        log.info("Creating new MongoDB client");
        servers.forEach((address) -> log.info("- MongoDB server: %s, port: %d", address.getHost(), address.getPort()));
        credentials.forEach((credential) -> log.info("- MongoDB user name: %s, source: %s", credential.getUserName(), credential.getSource()));

        return new MongoClient(servers, credentials);
    }
}
