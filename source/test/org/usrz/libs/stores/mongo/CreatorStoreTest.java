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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.JsonConfigurations;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.stores.inject.MongoBuilder;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;




public class CreatorStoreTest extends AbstractTest {

    @Test
    public void testInjection()
    throws Exception {

        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));
        final AtomicReference<MyBean> created = new AtomicReference<>();

        final Injector injector = Guice.createInjector(MongoBuilder.apply((builder) -> {
            builder.configure(configurations.strip("mongo"));
            builder.store(MyBean.class, UUID.randomUUID().toString())
                   .withCreator((bean) -> { created.set(bean); return bean; } );
        }));

        final Store<MyBean> store = injector.getInstance(Key.get(new TypeLiteral<Store<MyBean>>(){}));
        assertNotNull(store, "Null store");

        final MyBean bean = store.create();
        assertNotNull(bean, "Null bean created");
        assertNotNull(created.get(), "No bean passed to creator");
        assertSame(bean, created.get());
    }

    public interface MyBean extends Document {}
}
