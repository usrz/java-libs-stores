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

import java.util.Date;

import org.bson.BSONObject;
import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

import com.google.inject.Guice;

public class BSonMapperTest extends AbstractTest {

    private final BSONObjectMapper mapper = Guice.createInjector().getInstance(BSONObjectMapper.class);

    @Test
    public void testMapper()
    throws Exception {

        final Date date = new Date();
        final TestBean bean = new TestBean();
        bean.setTheDate(date);
        bean.setTheDouble(123.456);
        bean.setTheInteger(987654321);
        bean.setTheString("Hello, world!");

        BSONObject object = mapper.writeValueAsBson(bean);

        TestBean read = mapper.readValue(object, TestBean.class);

        assertNotSame(read, bean);
        assertEquals(read, bean);
    }
}
