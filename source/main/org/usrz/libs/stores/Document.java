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

import java.util.Date;

import org.usrz.libs.stores.annotations.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The core interface defining a <em>storable</em> object.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public interface Document {

    /**
     * Return the unique identifier of this {@link Document}.
     *
     * @return A <b>non-null</b> {@link String}.
     */
    @Id
    public String id();

    /**
     * Return when this {@link Document} was last
     * {@linkplain Store#store(Document) stored} or <b>null</b>.
     */
    @JsonIgnore
    public Date lastModifiedAt();

}
