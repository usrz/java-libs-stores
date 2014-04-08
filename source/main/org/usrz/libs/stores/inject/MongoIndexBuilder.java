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

import static org.usrz.libs.stores.inject.MongoIndexBuilder.Type.ASCENDING;
import static org.usrz.libs.stores.inject.MongoIndexBuilder.Type.DESCENDING;

public interface MongoIndexBuilder {

    public enum Type { ASCENDING, DESCENDING, HASHED };

    public MongoIndexBuilder withName(String name);

    default MongoIndexBuilder withKey(String key) {
        return withKey(key, ASCENDING);
    }

    default MongoIndexBuilder withKey(String key, boolean ascending) {
        return withKey(key, ascending ? ASCENDING : DESCENDING);
    }

    public MongoIndexBuilder withKey(String key, Type type);

    default MongoIndexBuilder unique() {
        return this.unique(true);
    }

    public MongoIndexBuilder unique(boolean unique);

    default MongoIndexBuilder sparse() {
        return this.sparse(true);
    }

    public MongoIndexBuilder sparse(boolean sparse);

}