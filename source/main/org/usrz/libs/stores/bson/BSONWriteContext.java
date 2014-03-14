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

import java.util.Set;

import org.bson.BSONObject;

import com.fasterxml.jackson.core.JsonStreamContext;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public abstract class BSONWriteContext extends JsonStreamContext {

    private static final Set<Class<?>> handledTypes = BSONIdentityMappings.handledTypes();

    public static final BSONWriteContext newContext() {
        return new BSONRootContext();
    }

    /* ====================================================================== */

    private BSONWriteContext(int type) {
        _type = type;
    }

    @Override
    public abstract BSONWriteContext getParent();

    public abstract BSONObject getBSONObject();

    public abstract BSONWriteContext newObject();

    public abstract BSONWriteContext newArray();

    public abstract void writeName(String name);

    public abstract void writeValue(Object object);

    public final boolean canWrite(Object object) {
        if (object == null) return true;
        if (object instanceof Class) return canWrite((Class<?>) object);
        return handledTypes.contains(object.getClass());
    }

    public final boolean canWrite(Class<?> type) {
        if (type == null) return true;
        return handledTypes.contains(type);
    }

    /* ====================================================================== */

    private static abstract class BSONAbstractContext<T extends BSONObject> extends BSONWriteContext {

        protected final BSONAbstractContext<?> _parent;
        protected final T _object;

        private BSONAbstractContext(int type, BSONAbstractContext<?> parent, T object) {
            super(type);
            _parent = parent;
            _object = object;
        }

        @Override
        public BSONAbstractContext<?> getParent() {
            return _parent;
        }

        @Override
        public String getCurrentName() {
            return null;
        }

        @Override
        public T getBSONObject() {
            return _object;
        }

        @Override
        public final void writeValue(Object object) {
            if (object == null) {
                writeObject(null);
            } else if (canWrite(object.getClass())) {
                writeObject(object);
            } else {
                throw new IllegalArgumentException("Type \"" + object.getClass() + "\" not supported natively");
            }
        }

        protected abstract void writeObject(Object object);
    }

    /* ====================================================================== */

    private static final class BSONRootContext extends BSONAbstractContext<BasicDBObject> {

        private BSONRootContext() {
            super(TYPE_ROOT, null, new BasicDBObject());
        }

        @Override
        public BSONObjectContext newObject() {
            return new BSONObjectContext(this, _object);
        }

        @Override
        public BSONArrayContext newArray() {
            throw new UnsupportedOperationException("Root objects can not be arrays");
        }

        @Override
        public void writeName(String name) {
            throw new UnsupportedOperationException("Root objects can not have names");
        }

        @Override
        protected void writeObject(Object object) {
            throw new UnsupportedOperationException("Root objects can not have values");
        }

    }

    /* ====================================================================== */

    private static final class BSONObjectContext extends BSONAbstractContext<BasicDBObject> {

        private String _name;

        private BSONObjectContext(BSONAbstractContext<?> parent, BasicDBObject object) {
            super(TYPE_OBJECT, parent, object);
        }

        @Override
        public String getCurrentName() {
            return _name;
        }

        @Override
        public BSONObjectContext newObject() {
            if (_name == null) throw new IllegalStateException("Name not set");
            final BasicDBObject child = new BasicDBObject();
            _object.put(_name, child);
            _name = null;
            return new BSONObjectContext(this, child);
        }

        @Override
        public BSONArrayContext newArray() {
            if (_name == null) throw new IllegalStateException("Name not set");
            final BasicDBList child = new BasicDBList();
            _object.put(_name, child);
            _name = null;
            return new BSONArrayContext(this, child);
        }

        @Override
        public void writeName(String name) {
            if (_name != null) throw new IllegalStateException("Name already set to \"" + _name + "\"");
            _name = name;
        }

        @Override
        protected void writeObject(Object value) {
            if (_name == null) throw new IllegalStateException("Name not set");
            _object.put(_name, value);
            _index = _object.size();
            _name = null;
        }

    }

    /* ====================================================================== */

    private static final class BSONArrayContext extends BSONAbstractContext<BasicDBList> {

        private final BasicDBList list;

        private BSONArrayContext(BSONAbstractContext<?> parent, BasicDBList list) {
            super(TYPE_ARRAY, parent, list);
            this.list = list;
        }

        @Override
        public BSONObjectContext newObject() {
            final BasicDBObject object = new BasicDBObject();
            list.add(object);
            return new BSONObjectContext(this, object);
        }

        @Override
        public BSONArrayContext newArray() {
            final BasicDBList child = new BasicDBList();
            list.add(child);
            return new BSONArrayContext(this, child);
        }

        @Override
        public void writeName(String name) {
            throw new UnsupportedOperationException("Array objects can not have names");
        }

        @Override
        protected void writeObject(Object value) {
            list.add(value);
            _index = list.size();
        }

    }



}
