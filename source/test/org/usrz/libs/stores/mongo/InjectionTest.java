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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.testng.annotations.Test;
import org.usrz.libs.stores.Document;
import org.usrz.libs.stores.Store;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;
import org.usrz.libs.utils.configurations.Configurations;
import org.usrz.libs.utils.configurations.JsonConfigurations;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class InjectionTest extends AbstractTest {

    @Test
    public void testInjection()
    throws IOException {
        final Configurations configurations = new JsonConfigurations(IO.resource("test.js"));


        final Injector injector = Guice.createInjector(
                new AbstractModule() {
                    @Override public void configure() {
                        this.bind(Configurations.class).toInstance(configurations);
                    }
                }, new MongoDatabaseModule() {
                    @Override public void configure() {
                        this.bind(MyBean.class).toCollection("foobar");
                    }
                });

        final Store<MyBean> store = injector.getInstance(Key.get(new TypeLiteral<Store<MyBean>>(){}));
        assertNotNull(store);

        final AtomicReference<Store<MyBean>> reference = new AtomicReference<>();
        injector.injectMembers(new Object() {
            @Inject
            public void setStore(Store<MyBean> store) {
                if (reference.compareAndSet(null, store)) return;
                throw new IllegalStateException("Injected multiple times");
            }
        });

        assertNotNull(reference.get());
        assertSame(store, reference.get());

    }

    public interface MyBean extends Document {
        public void setString(String string);
        public String getString();
    }
}