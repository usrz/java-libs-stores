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

import java.lang.reflect.ParameterizedType;

import javax.inject.Inject;

import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Relation;
import org.usrz.libs.stores.Store;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public class MongoRelationProvider<L extends Document, R extends Document>
implements Provider<Relation<L, R>> {

    private static final Log log = new Log();

    private final String collection;
    private final Class<L> typeL;
    private final Class<R> typeR;

    @Inject private Injector injector;
    @Inject private DB db;


    protected MongoRelationProvider(String collection, Class<L> typeL, Class<R> typeR) {
        this.collection = collection;
        this.typeL = typeL;
        this.typeR = typeR;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Relation<L, R> get() {
        if (injector == null) throw new ProvisionException(this.getClass().getSimpleName() + " not injected");

        log.debug("Creating Mongo instance for Relation<%s, %s>", typeL.getSimpleName(), typeR.getSimpleName());
        try {
            final ParameterizedType storeTypeL = Types.newParameterizedType(Store.class, typeL);
            final ParameterizedType storeTypeR = Types.newParameterizedType(Store.class, typeR);
            final TypeLiteral<Store<L>> literalL = (TypeLiteral<Store<L>>) TypeLiteral.get(storeTypeL);
            final TypeLiteral<Store<R>> literalR = (TypeLiteral<Store<R>>) TypeLiteral.get(storeTypeR);

            /* Grab our stores */
            final Store<L> storeL = injector.getInstance(Key.get(literalL));
            final Store<R> storeR = injector.getInstance(Key.get(literalR));

            /* Get a hold on our DB collection */
            final DBCollection collection = db.getCollection(this.collection);

            /* Create our relation */
            return new MongoRelation<L, R>(collection, storeL, storeR);
        } catch (Exception exception) {
            throw new ProvisionException(String.format("Unable to created Relation<%s, %s>", typeL.getSimpleName(), typeR.getSimpleName()), exception);
        }
    }


}
