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

import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.utils.inject.ConfigurableProvider;
import org.usrz.libs.utils.inject.Injections;

import com.google.inject.Injector;
import com.mongodb.DB;
import com.mongodb.MongoClient;

public class MongoDatabaseProvider extends ConfigurableProvider<DB> {

    public MongoDatabaseProvider() {
        super(MongoConfigurations.class);
    }

    @Override
    protected DB get(Injector injector, Configurations configurations) {
        final MongoClient client = Injections.getInstance(injector, this.key(MongoClient.class));
        final String database = configurations.requireString("database");
        return client.getDB(database);
    }
}
