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

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_EMBEDDED_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_FALSE;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NULL;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_FLOAT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
import static com.fasterxml.jackson.core.JsonToken.VALUE_TRUE;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.BSONObject;

import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;

public class BSONReadContext extends JsonStreamContext {

    private final BSONReadContext _parent;
    private BSONReadContext _next;

    private final JsonToken _token;
    private final Object _value;
    private final String _name;

    private BSONReadContext(int type, JsonToken token, int index, String name, Object value, BSONReadContext previous, BSONReadContext parent) {
        _type = type;
        _token = token;
        _index = index;
        _name = name;
        _value = value;
        _parent = parent;
        if (previous != null) previous._next = this;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(_token.name());
        switch (_type) {
            case TYPE_ROOT :  builder.append("[ROOT]"); return builder.toString();
            case TYPE_OBJECT: builder.append("[OBJECT|name=").append(_name); break;
            case TYPE_ARRAY:  builder.append("(ARRAY|index=").append(_index); break;
        }
        builder.append(",value=");
        if (_value == null) builder.append("null");
        else builder.append(_value.getClass().getName());

        return builder.append("]@").append(Integer.toHexString(hashCode())).toString();
    }

    /* ====================================================================== */

    public static BSONReadContext newContext(BSONObject object) {
        final BSONReadContext first = new BSONReadContext(TYPE_ROOT, START_OBJECT, 0, null, object, null,  null);
        /* ----------------------- */ new BSONReadContext(TYPE_ROOT, END_OBJECT,   0, null, null,   first, null);
        return first;
    }

    /* ====================================================================== */

    @Override
    public JsonStreamContext getParent() {
        return _parent;
    }

    public JsonToken getCurrentToken() {
        return _token;
    }

    @Override
    public String getCurrentName() {
        return _name;
    }

    public Object getCurrentValue() {
        return _value;
    }

    public BSONReadContext getNextContext(boolean skipChildren) {
        /* Do we need to enter a list of sub-contexts? */
        if ((_token == START_OBJECT) || (_token == START_ARRAY)) {
            /* Skip all the children? We're lucky! */
            if (skipChildren) return _next;

            /* Process token */
            final BSONReadContext child = _value == null ? null :
                                          _token == START_OBJECT ? newObjectContext((Map<?, ?>) _value) :
                                          _token == START_ARRAY  ? newArrayContext((Collection<?>) _value) :
                                          null;

            /* If there is no child context (like {} or []) just go next */
            if (child == null) return _next;

            /* Set up the "next" of the last entry for the children */
            BSONReadContext last = child;
            while (last._next != null) last = last._next;
            last._next = _next;

            /* Return the child */
            return child;
        }

        /* If we have no children, return _this_ */
        return skipChildren ? this : _next;
    }

    /* ====================================================================== */

    private BSONReadContext newObjectContext(Map<?, ?> map) {

        int index = 0;
        BSONReadContext first = null;
        BSONReadContext context = null;
        for (Entry<?, ?> entry: map.entrySet()) {

            /* Key, should always be a string, but you never know */
            final Object keyObject = entry.getKey();
            final String key = keyObject == null ? null :
                               keyObject instanceof String ? (String) keyObject :
                               keyObject.toString();

            /* Field Name */
            context = new BSONReadContext(TYPE_OBJECT, FIELD_NAME, index, key, key, context, this);
            if (first == null) first = context;

            /* Field value */
            context = valueContext(entry.getValue(), context, index);

            /* Increment index and continue */
            index ++;
        }

        return first;
    }

    private BSONReadContext newArrayContext(Collection<?> collection) {

        int index = 0;
        BSONReadContext first = null;
        BSONReadContext context = new BSONReadContext(TYPE_ARRAY, null, -1, null, null, null, this); // dummy initial value
        for (Object object: collection) {

            /* Simple, create a value token, check for first, increment index */
            context = valueContext(object, context, index);
            if (first == null) first = context;
            index ++;
        }

        return first;
    }

    /* ====================================================================== */

    private static BSONReadContext valueContext(Object value, BSONReadContext previous, int index) {

        final JsonToken token = value == null ? VALUE_NULL :
                                value instanceof String ? VALUE_STRING :
                                value instanceof Boolean ?
                                        ((Boolean) value).booleanValue() ?
                                                VALUE_TRUE :
                                                VALUE_FALSE :
                                value instanceof Number ?
                                                value instanceof Float         ? VALUE_NUMBER_FLOAT :
                                                value instanceof Double        ? VALUE_NUMBER_FLOAT :
                                                value instanceof BigDecimal    ? VALUE_NUMBER_FLOAT :
                                             // value instanceof Byte          ? JsonToken.VALUE_NUMBER_INT :
                                             // value instanceof Short         ? JsonToken.VALUE_NUMBER_INT :
                                             // value instanceof Integer       ? JsonToken.VALUE_NUMBER_INT :
                                             // value instanceof Long          ? JsonToken.VALUE_NUMBER_INT :
                                             // value instanceof AtomicInteger ? JsonToken.VALUE_NUMBER_INT :
                                             // value instanceof AtomicLong    ? JsonToken.VALUE_NUMBER_INT :
                                             // value instanceof BigInteger    ? JsonToken.VALUE_NUMBER_INT :
                                                VALUE_NUMBER_INT :
                                value instanceof Collection ? START_ARRAY  :
                                value instanceof Map        ? START_OBJECT :
                                value instanceof BSONObject ? START_OBJECT :
                                VALUE_EMBEDDED_OBJECT;

        /* Check if we have a BSON Object that does not cast to a map... Bleurk :-( */
        if ((token == START_OBJECT) && (!(value instanceof Map)) && (value instanceof BSONObject)) {
            value = ((BSONObject) value).toMap();
        }

        /* Build our "VALUE..." token or "START..."/"END..." token pair */
        final BSONReadContext context = new BSONReadContext(previous._type, token, index, previous._name, value, previous, previous._parent);
        if (token == START_OBJECT) {
            return new BSONReadContext(context._type, END_OBJECT, context._index, context._name, null, context, context._parent);
        } else if (token == START_ARRAY) {
            return new BSONReadContext(context._type, END_ARRAY, context._index, context._name, null, context, context._parent);
        } else {
            return context;
        }
    }
}
