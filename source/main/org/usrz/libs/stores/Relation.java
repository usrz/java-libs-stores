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

import java.util.Iterator;

import org.usrz.libs.utils.concurrent.Acceptor;
import org.usrz.libs.utils.concurrent.NotifyingFuture;

/**
 * The {@link Relation} interface defines an abstract way to relate two
 * {@link Document}s in a many-to-many fashion.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <L> The type of data stored in the left part of this relation.
 * @param <R> The type of data stored in the right part of this relation.
 */
public interface Relation<L extends Document, R extends Document> {

    /**
     * Ensure that the relation between the two specified {@link Document}s
     * is present.
     */
    public void associate(L left, R right);

    /**
     * Asynchronously ensure that the relation between the two specified
     * {@link Document}s is present.
     */
    public NotifyingFuture<?> associateAsync(L left, R right);

    /**
     * Ensure that the relation between the two specified {@link Document}s
     * is <em>not</em> present.
     */
    public void dissociate(L left, R right);

    /**
     * Asynchronously ensure that the relation between the two specified
     * {@link Document}s is <em>not</em> present.
     */
    public NotifyingFuture<?> dissociateAsync(L left, R right);

    /**
     * Check whether the relation between the two specified {@link Document}s
     * is present or not.
     */
    public boolean isAssociated(L left, R right);

    /**
     * Asynchronously check whether the relation between the two specified
     * {@link Document}s is present or not.
     */
    public NotifyingFuture<Boolean> isAssociatedAsync(L left, R right);

    /**
     * Find all the <em>left</em> {@link Document}s associated with the
     * specified <em>right</em> one.
     */
    public Iterator<L> findL(R right);

    /**
     * Asynchronously find all the <em>left</em> {@link Document}s associated
     * with the specified <em>right</em> one.
     */
    public NotifyingFuture<?> findAsyncL(R right, Acceptor<L> acceptor);

    /**
     * Find all the <em>right</em> {@link Document}s associated with the
     * specified <em>left</em> one.
     */
    public Iterator<R> findR(L left);

    /**
     * Asynchronously find all the <em>right</em> {@link Document}s associated
     * with the specified <em>left</em> one.
     */
    public NotifyingFuture<?> findAsyncR(L left, Acceptor<R> acceptor);

}
