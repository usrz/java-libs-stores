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
package org.usrz.libs.stores;

import static java.lang.Integer.toHexString;

import java.util.Date;

import org.usrz.libs.stores.annotations.BsonIgnore;
import org.usrz.libs.utils.Check;

/**
 * An abstract implementation of the {@link Document} interface.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public abstract class Document {

    @BsonIgnore
    private final String id;
    @BsonIgnore
    private final Store<?> store;
    @BsonIgnore
    private final Date lastModifiedAt;

    protected Document() {
        id = null;
        store = null;
        lastModifiedAt = null;
    }

    protected Document(String id) {
        this.id = Check.notNull(id, "Null ID for document");
        store = null;
        lastModifiedAt = null;
    }

    @BsonIgnore
    public final String id() {
        return id;
    }

    @BsonIgnore
    public final String collection() {
        return store.getCollection();
    }

    @BsonIgnore
    public final Date lastModifiedAt() {
        return lastModifiedAt;
    }

    /* ====================================================================== */

    @Override
    public String toString() {
        final String detail = id == null ? "-never-stored-" : id;
        return getClass().getName() + "[" + detail + "]@" + toHexString(hashCode());
    }

    @Override
    public int hashCode() {
        if (id == null) return 31 * getClass().hashCode();
        else return (31 * getClass().hashCode()) ^ id.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object == this) return true;
        try {
            final Document document = (Document) object;
            return document.getClass().equals(getClass())
                && document.id == null ? id == null : document.id.equals(id)
                && document.lastModifiedAt == null ? lastModifiedAt == null : document.lastModifiedAt.equals(lastModifiedAt);
        } catch (Exception exception) {
            return false;
        }
    }
}
