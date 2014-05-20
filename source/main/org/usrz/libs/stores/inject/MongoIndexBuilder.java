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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.usrz.libs.stores.annotations.Indexes.Type.ASCENDING;
import static org.usrz.libs.stores.annotations.Indexes.Type.DESCENDING;
import static org.usrz.libs.utils.Check.notNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.usrz.libs.stores.annotations.Indexes.Option;
import org.usrz.libs.stores.annotations.Indexes.Type;
import org.usrz.libs.utils.Times;

public interface MongoIndexBuilder {

    public MongoIndexBuilder withName(String name);

    default MongoIndexBuilder withKey(String key) {
        return withKey(key, ASCENDING);
    }

    default MongoIndexBuilder withKey(String key, boolean ascending) {
        return withKey(key, ascending ? ASCENDING : DESCENDING);
    }

    public MongoIndexBuilder withKey(String key, Type type);

    default MongoIndexBuilder unique() {
        return withOptions(Option.UNIQUE);
    }

    default MongoIndexBuilder sparse() {
        return withOptions(Option.SPARSE);
    }

    public MongoIndexBuilder withOptions(Option... options);

    default MongoIndexBuilder expiresAfter(String duration) {
        return expiresAfter(Times.duration(notNull(duration, "Null duration")));
    }

    default MongoIndexBuilder expiresAfter(Duration duration) {
        return expiresAfterSeconds(notNull(duration, "Null duration").getSeconds());
    }

    default MongoIndexBuilder expiresAfter(long amount, TimeUnit unit) {
        return expiresAfterSeconds(SECONDS.convert(amount, unit));
    }

    public MongoIndexBuilder expiresAfterSeconds(long seconds);

}