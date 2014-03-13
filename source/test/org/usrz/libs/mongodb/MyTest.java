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
package org.usrz.libs.mongodb;

import org.usrz.libs.testing.AbstractTest;

public class MyTest extends AbstractTest {

//    @Test(groups="local")
//    public void testReferences()
//    throws Exception {
//
//        Stores store = new Stores(new MongoClient().getDB("test"));
//
//        final Store<ReferencedBean> referenced = store.getStore("referenced", ReferencedBean.class);
//        final Store<ReferencingBean> referencing = store.getStore("referencing", ReferencingBean.class);
//
//        ReferencedBean referencedBean = new ReferencedBean();
//        referenced.store(referencedBean);
//        ReferencedBean referencedBean2 = referenced.get(referencedBean.getObjectId());
//
//        assertNotNull(referencedBean2);
//        assertNotSame(referencedBean2, referencedBean);
//        assertEquals(referencedBean2.getObjectId(), referencedBean.getObjectId());
//        assertEquals(referencedBean2.uuid, referencedBean.uuid);
//
//        ReferencingBean referencingBean = new ReferencingBean(referencedBean2);
//        assertNotNull(referencingBean.bean);
//        assertNull(referencingBean.ref);
//
//        referencing.store(referencingBean);
//        assertNotNull(referencingBean.bean);
//        assertNotNull(referencingBean.ref);
//
//        ReferencingBean referencingBean2 = referencing.get(referencingBean.getObjectId());
//        assertNotNull(referencingBean2);
//        assertNull(referencingBean2.bean);
//        assertNotNull(referencingBean2.ref);
//
//    }
//
//    /* ====================================================================== */
//
//    public static class ReferencingBean extends MongoDocument {
//
//        private ReferencedBean bean;
//        private DBRef ref = null;
//
//        public ReferencingBean() {
//            super();
//        }
//
//        public ReferencingBean(ReferencedBean bean) {
//            this.bean = bean;
//        }
//
//        @JsonIgnore
//        public ReferencedBean getReferenced() {
//            if (bean != null) return bean;
//            return bean = this.fetchRef(ref, ReferencedBean.class);
//        }
//
//        public DBRef getReference() {
//            if (ref != null) return ref;
//            return ref = findRef(bean);
//        }
//
//        public void setReference(DBRef ref) {
//            this.ref = ref;
//        }
//    }
//
//    /* ====================================================================== */
//
//    public static class ReferencedBean extends MongoDocument {
//
//        protected ReferencedBean(ObjectId objectId, UUID uuid,
//                MongoStore<?> store) {
//            super(objectId, uuid, store);
//            // TODO Auto-generated constructor stub
//        }
//
//
//    }

}
