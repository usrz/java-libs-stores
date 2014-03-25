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

import static java.lang.Integer.toHexString;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.usrz.libs.stores.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MongoDocument implements Document {

    private final AtomicReference<UUID> uuid = new AtomicReference<>();

    protected MongoDocument() {
        /* Simply await for injection */
    }

    /* ====================================================================== */

    @JsonProperty("uuid")
    private final void setUUID(UUID uuid) {
        Objects.requireNonNull(uuid, "Null UUID");
        if (this.uuid.compareAndSet(null, uuid)) return;
        throw new IllegalStateException("UUID already set");
    }

    /* ====================================================================== */

    @Override
    @JsonProperty("uuid")
    public UUID getUUID() {
        final UUID uuid = this.uuid.get();
        if (uuid == null) throw new IllegalStateException("UUID not set");
        return uuid;
    }

    /* ====================================================================== */

    @Override
    public String toString() {
        return getClass().getName() + "[" + getUUID() + "]@" + toHexString(hashCode());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() ^ getUUID().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object == this) return true;
        try {
            final MongoDocument document = (MongoDocument) object;
            return document.getClass().equals(getClass())
                && document.getUUID().equals(getUUID());
        } catch (ClassCastException exception) {
            return false;
        }
    }
}
