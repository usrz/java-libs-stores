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
package org.usrz.libs.stores.inject;

import static org.usrz.libs.utils.Check.notEmpty;
import static org.usrz.libs.utils.Check.notNull;

import java.lang.reflect.ParameterizedType;

import org.usrz.libs.logging.Log;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Relation;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.mongo.MongoRelation;
import org.usrz.libs.utils.inject.InjectingProvider;
import org.usrz.libs.utils.inject.Injections;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.mongodb.DBCollection;

public class MongoRelationProvider<L extends Document, R extends Document>
extends InjectingProvider<Relation<L, R>> {

    private static final Log log = new Log();

    private final TypeLiteral<L> typeL;
    private final TypeLiteral<R> typeR;
    private final String collection;

    protected MongoRelationProvider(TypeLiteral<L> typeL, TypeLiteral<R> typeR, String collection) {
        this.typeL = notNull(typeL, "Null type for left association");
        this.typeR = notNull(typeR, "Null type for right association");
        this.collection = notEmpty(collection, "Invalid collection name");
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Relation<L, R> get(Injector injector) {
        final DBCollection collection = Injections.getInstance(injector, DBCollection.class, Names.named(this.collection));

        final ParameterizedType storeTypeL = Types.newParameterizedType(Store.class, typeL.getType());
        final ParameterizedType storeTypeR = Types.newParameterizedType(Store.class, typeR.getType());
        final TypeLiteral<Store<L>> literalL = (TypeLiteral<Store<L>>) TypeLiteral.get(storeTypeL);
        final TypeLiteral<Store<R>> literalR = (TypeLiteral<Store<R>>) TypeLiteral.get(storeTypeR);

        /* Grab our stores */
        final Store<L> storeL = injector.getInstance(Key.get(literalL));
        final Store<R> storeR = injector.getInstance(Key.get(literalR));

        /* Create our relation */
        log.info("Createding Relation<%s, %s> in collection %s", typeL, typeR, collection.getName());
        return new MongoRelation(collection, storeL, storeR);
    }

}
