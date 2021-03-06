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


/**
 * An abstract implementation of the {@link Relation} interface.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <L> The type of data stored in the left part of this relation.
 * @param <R> The type of data stored in the right part of this relation.
 */
public abstract class AbstractRelation<L extends Document, R extends Document>
implements Relation<L, R> {

}
