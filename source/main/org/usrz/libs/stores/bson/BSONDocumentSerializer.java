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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class BSONDocumentSerializer extends JsonSerializer<Document> {

    public BSONDocumentSerializer() {

        // TODO Auto-generated constructor stub
    }

    @Override
    public void serialize(Document value,
                          JsonGenerator jgen,
                          SerializerProvider provider)
    throws IOException, JsonProcessingException {
        System.err.println("SERIALIZING -> " + value);
        provider.defaultSerializeValue(value, jgen);

        // TODO Auto-generated method stub

    }

    @Override
    public Class<Document> handledType() {
        return Document.class;
    }

}
