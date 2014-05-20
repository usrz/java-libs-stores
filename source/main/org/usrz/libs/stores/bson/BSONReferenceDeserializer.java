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

import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.utils.Check;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.mongodb.DBRef;

public class BSONReferenceDeserializer<D extends Document> extends JsonDeserializer<D> {

    private final Log log = new Log();
    private final Store<D> store;

    private BSONReferenceDeserializer(Store<D> store) {
        this.store = Check.notNull(store, "Null store");
    }

    public static <X extends Document> BSONReferenceDeserializer<X> get(Store<X> store) {
        return new BSONReferenceDeserializer<X>(store);
    }

    @Override
    public D deserialize(JsonParser jsonParser, DeserializationContext ctxt)
    throws IOException, JsonProcessingException {
        final Object object = jsonParser.getEmbeddedObject();
        if (object == null) return null;

        if (object instanceof DBRef) {
            final DBRef ref = (DBRef) object;
            if (store.getCollection().equals(ref.getRef())) {
                final D document = store.find((String) ref.getId());
                if (document == null) log.warn("Document reference not found: collection %s, id %s", ref.getRef(), ref.getId());
                return document;
            } else {
                throw new JsonParseException("Unable to fetch " + object.getClass().getName() + " from collection " + ref.getRef() +
                                            " (currently using " + store.getCollection() + ")", jsonParser.getCurrentLocation());
            }
        } else {
            throw new JsonParseException("Unable to cast " + object.getClass().getName() + " to a DB reference", jsonParser.getCurrentLocation());
        }
    }

    @Override
    public Class<D> handledType() {
        return store.getDocumentClass();
    }

}
