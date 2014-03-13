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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.bson.types.ObjectId;
import org.usrz.libs.stores.Document;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MongoDocument implements Document {

    private final AtomicReference<ObjectId> objectId = new AtomicReference<>();
    //private final AtomicReference<Store<?>> store = new AtomicReference<>();
    private final AtomicReference<UUID> uuid = new AtomicReference<>();

    protected MongoDocument() {
        /* Simply await for injection */
    }

    /* ====================================================================== */

    @JsonProperty("uuid")
    private void setUUID(UUID uuid) {
        if (uuid == null) throw new NullPointerException("Null UUID");
        if (this.uuid.compareAndSet(null, uuid)) return;
        throw new IllegalStateException("UUID already set");
    }

    @JsonProperty("_id")
    private void setObjectId(ObjectId objectId) {
        if (objectId == null) throw new NullPointerException("Null Object ID");
        if (this.objectId.compareAndSet(null, objectId)) return;
        throw new IllegalStateException("Object ID already set");
    }

//    @JacksonInject
//    private void setStore(MongoStore<?> store) {
//        if (store == null) throw new NullPointerException("Null Store");
//        if (this.store.compareAndSet(null, store)) return;
//        throw new IllegalStateException("Store already set");
//    }

    /* ====================================================================== */

    @Override
    @JsonProperty("uuid")
    public UUID getUUID() {
        final UUID uuid = this.uuid.get();
        if (uuid == null) throw new IllegalStateException("UUID not set");
        return uuid;
    }

    @JsonProperty("_id")
    protected final ObjectId getObjectId() {
        final ObjectId objectId = this.objectId.get();
        if (objectId == null) throw new IllegalStateException("Object ID not set");
        return objectId;
    }

//    @JsonIgnore
//    protected final Store<?> getStore() {
//        final Store<?> store = this.store.get();
//        if (store == null) throw new IllegalStateException("Store not set");
//        return store;
//    }

}
