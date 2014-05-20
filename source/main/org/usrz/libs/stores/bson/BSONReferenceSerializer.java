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
package org.usrz.libs.stores.bson;

import java.io.IOException;

import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.utils.Check;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mongodb.DBRef;

public class BSONReferenceSerializer<D extends Document> extends JsonSerializer<D> {

    private final Store<D> store;

    private BSONReferenceSerializer(Store<D> store) {
        this.store = Check.notNull(store, "Null store");
    }

    public static <X extends Document> BSONReferenceSerializer<X> get(Store<X> store) {
        return new BSONReferenceSerializer<X>(store);
    }

    @Override
    public void serialize(final D object,
                          final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider)
    throws IOException {
        final DBRef ref = new DBRef(null, store.getCollection(), object.id());
        jsonGenerator.writeObject(ref);
    }

    @Override
    public Class<D> handledType() {
        return store.getDocumentClass();
    }

}
