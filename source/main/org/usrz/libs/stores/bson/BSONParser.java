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

import static org.usrz.libs.stores.bson.BSONObjectMapper.VERSION;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.bson.BSONObject;
import org.usrz.libs.logging.Log;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;

public class BSONParser extends JsonParser {

    private static final Log LOG = new Log();

    private BSONReadContext context;
    private BSONReadContext cleared;
    private ObjectCodec codec;
    private boolean closed;

    public BSONParser(ObjectCodec codec, BSONObject object) {
        context = BSONReadContext.newContext(object);
        this.codec = codec;
        closed = false;
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public void setCodec(ObjectCodec codec) {
        this.codec = codec;
    }

    @Override
    public ObjectCodec getCodec() {
        return codec;
    }


    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close()
    throws IOException {
        closed = true;
    }

    /* ====================================================================== */

    @Override
    public JsonLocation getTokenLocation() {
        return JsonLocation.NA;
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return JsonLocation.NA;
    }

    /* ====================================================================== */

    @Override
    public JsonStreamContext getParsingContext() {
        return context;
    }

    @Override
    public JsonToken nextToken()
    throws IOException, JsonParseException {
        LOG.trace("NextToken %s", context);
        return (nextToken(false)).getCurrentToken();
    }

    @Override
    public BSONParser skipChildren()
    throws IOException, JsonParseException {
        LOG.trace("SkipChildren %s", context);
        return (nextToken(true));
    }

    private BSONParser nextToken(boolean skipChildren) {
        context = context != null ? context.getNextContext(skipChildren) :
                  cleared != null ? cleared.getNextContext(skipChildren) :
                  null;
        cleared = null;
        return this;
    }

    @Override
    public void clearCurrentToken() {
        LOG.trace("ClearCurrentToken %s", context);
        cleared = context;
        context = null;
    }

    @Override
    public JsonToken nextValue()
    throws IOException, JsonParseException {
        LOG.trace("NextValue %s", context);
        JsonToken token = null;
        while ((token = nextToken()) != null) switch (token) {
            case START_ARRAY:
            case START_OBJECT:
            case VALUE_EMBEDDED_OBJECT:
            case VALUE_FALSE:
            case VALUE_NULL:
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
            case VALUE_STRING:
            case VALUE_TRUE:
                return token;
            default:
                continue;
        }
        return null;
    }

    @Override
    public JsonToken getCurrentToken() {
        LOG.trace("GetCurrentToken %s", context);
        return context == null ? null : context.getCurrentToken();
    }

    @Override
    public int getCurrentTokenId() {
        LOG.trace("GetCurrentTokenId %s", context);
        final JsonToken token = getCurrentToken();
        return token == null ? -1 : token.id();
    }

    @Override
    public boolean hasCurrentToken() {
        LOG.trace("HasCurrentToken %s", context);
        return getCurrentToken() != null;
    }

    @Override
    public String getCurrentName()
    throws IOException, JsonParseException {
        LOG.trace("GetCurrentName %s", context);
        return context == null ? null : context.getCurrentName();
    }

    @Override
    public JsonToken getLastClearedToken() {
        LOG.trace("GetLastClearedToken %s", context);
        return cleared == null ? null : cleared.getCurrentToken();
    }

    @Override
    public void overrideCurrentName(String name) {
        LOG.trace("OverrideCurrentName %s", context);
        throw new UnsupportedOperationException("Forget about it, Frank!");
    }

    /* ====================================================================== */

    @Override
    public Number getNumberValue()
    throws IOException, JsonParseException {
        LOG.trace("GetNumberValue %s", context);
        return context == null ? null : (Number) context.getCurrentValue();
    }

    @Override
    public NumberType getNumberType()
    throws IOException, JsonParseException {
        LOG.trace("GetNumberType %s", context);
        final Number number = getNumberValue();
        if (number == null) return null;
        if (number instanceof Byte)    return NumberType.INT;
        if (number instanceof Short)   return NumberType.INT;
        if (number instanceof Integer) return NumberType.INT;
        if (number instanceof Long)    return NumberType.LONG;
        if (number instanceof Float)   return NumberType.FLOAT;
        if (number instanceof Double)  return NumberType.DOUBLE;
        return null;
    }

    @Override
    public Object getEmbeddedObject()
    throws IOException, JsonParseException {
        LOG.trace("GetEmbeddedObject %s", context);
        return context == null ? null : context.getCurrentValue();
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant)
    throws IOException, JsonParseException {
        LOG.trace("GetBinaryValue %s", context);
        return context == null ? null : (byte[]) context.getCurrentValue();
    }

    @Override
    public String getText()
    throws IOException, JsonParseException {
        LOG.trace("GetText %s", context);
        return context == null ? null : (String) context.getCurrentValue();
    }

    @Override
    public String getValueAsString(String defaultValue)
    throws IOException, JsonParseException {
        LOG.trace("GetValueAsString %s", context);
        if (context == null) return null;
        final Object value = context.getCurrentValue();
        return value == null ? defaultValue :
               value instanceof String ? (String) value :
               value instanceof Number ? ((Number) value).toString() :
               defaultValue;
    }

    /* ====================================================================== */

    @Override
    public char[] getTextCharacters()
    throws IOException, JsonParseException {
        LOG.trace("GetTextCharacters %s", context);
        return getText().toCharArray();
    }

    @Override
    public int getTextLength()
    throws IOException, JsonParseException {
        LOG.trace("GetTextLength %s", context);
        return getText().length();
    }

    @Override
    public int getTextOffset()
    throws IOException, JsonParseException {
        LOG.trace("GetTextOffset %s", context);
        return 0;
    }

    @Override
    public boolean hasTextCharacters() {
        LOG.trace("HasTextCharacters %s", context);
        return false;
    }

    @Override
    public int getIntValue()
    throws IOException, JsonParseException {
        LOG.trace("GetIntValue %s", context);
        return getNumberValue().intValue();
    }

    @Override
    public long getLongValue()
    throws IOException, JsonParseException {
        LOG.trace("GetLongValue %s", context);
        return getNumberValue().longValue();
    }

    @Override
    public BigInteger getBigIntegerValue()
    throws IOException, JsonParseException {
        LOG.trace("GetBigIntegerValue %s", context);
        return BigInteger.valueOf(getLongValue());
    }

    @Override
    public float getFloatValue()
    throws IOException, JsonParseException {
        LOG.trace("GetFloatValue %s", context);
        return getNumberValue().floatValue();
    }

    @Override
    public double getDoubleValue()
    throws IOException, JsonParseException {
        LOG.trace("GetDoubleValue %s", context);
        return getNumberValue().doubleValue();
    }

    @Override
    public BigDecimal getDecimalValue()
    throws IOException, JsonParseException {
        LOG.trace("GetDecimalValue %s", context);
        return BigDecimal.valueOf(getDoubleValue());
    }

    @Override
    public boolean hasTokenId(int id) {
        return getCurrentTokenId() == id;
    }

}
