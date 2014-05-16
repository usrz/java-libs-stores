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

import org.usrz.libs.utils.Check;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An abstract implementation of the {@link Document} interface.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public abstract class AbstractDocument implements Document {

    private final String id;

    @JsonCreator
    protected AbstractDocument(@JsonProperty("id") String id) {
        this.id = Check.notNull(id, "Null ID");
    }

    @Override
    public final String getId() {
        return id;
    }

    /* ====================================================================== */

    @Override
    public String toString() {
        return getClass().getName() + "[" + getId() + "]@" + toHexString(hashCode());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() ^ getId().hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object == this) return true;
        try {
            final Document document = (Document) object;
            return document.getClass().equals(getClass())
                && document.getId().equals(getId());
        } catch (ClassCastException exception) {
            return false;
        }
    }
}
