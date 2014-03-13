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
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.bson.BSONObject;
import org.usrz.libs.logging.Log;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;

public class BSONGenerator extends JsonGenerator {

    private static final int FEATURES = Feature.collectDefaults();
    private static final Log LOG = new Log();

    private final BSONWriteContext rootContext;
    private BSONWriteContext context;
    private ObjectCodec codec;
    private boolean closed;

    public BSONGenerator(ObjectCodec codec) {
        rootContext = context = BSONWriteContext.newContext();
        this.codec = codec;
        closed = false;
    }

    @Override
    public Version version() {
        return VERSION;
    }

    @Override
    public BSONGenerator setCodec(ObjectCodec codec) {
        this.codec = codec;
        return this;
    }

    @Override
    public ObjectCodec getCodec() {
        return codec;
    }

    @Override
    public JsonGenerator enable(Feature feature) {
        if ((FEATURES & feature.getMask()) != 0) return this;
        throw new IllegalArgumentException("Can not enable feature " + feature.name());
    }

    @Override
    public JsonGenerator disable(Feature feature) {
        if ((FEATURES & feature.getMask()) == 0) return this;
        throw new IllegalArgumentException("Can not disable feature " + feature.name());
    }

    @Override
    public boolean isEnabled(Feature feature) {
        return (FEATURES & feature.getMask()) != 0;
    }

    @Override
    public int getFeatureMask() {
        return FEATURES;
    }

    @Override
    public JsonGenerator setFeatureMask(int mask) {
        if (FEATURES == mask) return this;
        throw new IllegalArgumentException("Unable to modify feature mask");
    }

    @Override
    public JsonGenerator useDefaultPrettyPrinter() {
        return this;
    }

    @Override
    public boolean canWriteBinaryNatively() {
        return true;
    }

    /* ====================================================================== */

    @Override
    public void flush() {
        /* Do nothing */
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
    public BSONWriteContext getOutputContext() {
        return context;
    }

    @Override
    public BSONObject getOutputTarget() {
        return rootContext.getBSONObject();
    }

    /* ====================================================================== */

    @Override
    public void writeFieldName(String name)
    throws IOException, JsonGenerationException {
        LOG.trace("Field name: \"%s\"", name);
        if (closed) throw new IOException("Closed");
        context.writeName(name);

    }

    @Override
    public void writeStartObject()
    throws IOException, JsonGenerationException {
        LOG.trace("Start object");
        if (closed) throw new IOException("Closed");
        context = context.newObject();
    }

    @Override
    public void writeEndObject()
    throws IOException, JsonGenerationException {
        LOG.trace("End object");
        if (closed) throw new IOException("Closed");
        context = context.getParent();
    }

    @Override
    public void writeStartArray()
    throws IOException, JsonGenerationException {
        LOG.trace("Start array");
        if (closed) throw new IOException("Closed");
        context = context.newArray();
    }

    @Override
    public void writeEndArray()
    throws IOException, JsonGenerationException {
        LOG.trace("End array");
        if (closed) throw new IOException("Closed");
        context = context.getParent();
    }

    @Override
    public void writeString(String text)
    throws IOException, JsonGenerationException {
        LOG.trace("String: \"%s\"", text);
        if (closed) throw new IOException("Closed");
        context.writeValue(text);

    }

    @Override
    public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len)
    throws IOException, JsonGenerationException {
        LOG.trace("Byte array: len=%d", len);
        if (closed) throw new IOException("Closed");
        byte[] copy = new byte[len];
        System.arraycopy(data, offset, copy, 0, len);
        context.writeValue(copy);
    }

    @Override
    public void writeNumber(int v)
    throws IOException, JsonGenerationException {
        LOG.trace("Integer: %d", v);
        if (closed) throw new IOException("Closed");
        context.writeValue(v);
    }

    @Override
    public void writeNumber(long v)
    throws IOException, JsonGenerationException {
        LOG.trace("Long: %d", v);
        if (closed) throw new IOException("Closed");
        context.writeValue(v);
    }

    @Override
    public void writeNumber(BigInteger v)
    throws IOException, JsonGenerationException {
        LOG.trace("BigInteger: %s", v);
        if (closed) throw new IOException("Closed");
        if (v == null) context.writeValue(null);
        else context.writeValue(v.longValue());
    }

    @Override
    public void writeNumber(double v)
    throws IOException, JsonGenerationException {
        LOG.trace("Double: %f", v);
        if (closed) throw new IOException("Closed");
        context.writeValue(v);
    }

    @Override
    public void writeNumber(float v)
    throws IOException, JsonGenerationException {
        LOG.trace("Float: %f", v);
        if (closed) throw new IOException("Closed");
        context.writeValue(v);
    }

    @Override
    public void writeNumber(BigDecimal v)
    throws IOException, JsonGenerationException {
        LOG.trace("BigDecimal: %s", v);
        if (closed) throw new IOException("Closed");
        if (v == null) context.writeValue(null);
        else context.writeValue(v.doubleValue());
    }

    @Override
    public void writeBoolean(boolean v)
    throws IOException, JsonGenerationException {
        LOG.trace("Boolean: %b", v);
        if (closed) throw new IOException("Closed");
        context.writeValue(v);
    }

    @Override
    public void writeNull()
    throws IOException, JsonGenerationException {
        LOG.trace("Null: null");
        if (closed) throw new IOException("Closed");
        context.writeValue(null);
    }

    @Override
    public void writeObject(Object object)
    throws IOException, JsonProcessingException {
        LOG.trace("Object: %s", object);
        if (closed) throw new IOException("Closed");
        if (object == null) context.writeValue(null);
        else if (context.canWrite(object)) context.writeValue(object);
        else if (codec != null) codec.writeValue(this, object);
        else _writeSimpleObject(object);
    }

    @Override
    public void writeTree(TreeNode tree)
    throws IOException, JsonProcessingException {
        LOG.trace("Tree: %s", tree);
        if (closed) throw new IOException("Closed");
        codec.writeTree(this, tree);
    }

    /* ====================================================================== */

    @Override
    public void writeFieldName(SerializableString name)
    throws IOException, JsonGenerationException {
        this.writeFieldName(name.getValue());
    }

    @Override
    public void writeString(char[] text, int offset, int len)
    throws IOException, JsonGenerationException {
        this.writeString(new String(text, offset, len));
    }

    @Override
    public void writeString(SerializableString text)
    throws IOException, JsonGenerationException {
        this.writeString(text.getValue());
    }

    @Override
    public int writeBinary(Base64Variant b64variant, InputStream data, int len)
    throws IOException, JsonGenerationException {
        final byte[] buffer = new byte[len];

        int read = -1;
        int offset = 0;
        while ((read = data.read(buffer, offset, len - offset)) >= 0) {
            if ((offset += read) >= len) break;
        }

        this.writeBinary(b64variant, buffer, 0, offset);
        return offset;
    }

    /* ====================================================================== */

    @Override
    public void writeNumber(String encodedValue) {
        throw new UnsupportedOperationException("Un-typized number unsupported");
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) {
        throw new UnsupportedOperationException("RAW writes unsupported");
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) {
        throw new UnsupportedOperationException("RAW writes unsupported");
    }

    @Override
    public void writeRaw(String text) {
        throw new UnsupportedOperationException("RAW writes unsupported");
    }

    @Override
    public void writeRaw(String text, int offset, int len) {
        throw new UnsupportedOperationException("RAW writes unsupported");
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) {
        throw new UnsupportedOperationException("RAW writes unsupported");
    }

    @Override
    public void writeRaw(char c) {
        throw new UnsupportedOperationException("RAW writes unsupported");
    }

    @Override
    public void writeRawValue(String text) {
        throw new UnsupportedOperationException("RAW writes unsupported");
    }

    @Override
    public void writeRawValue(String text, int offset, int len) {
        throw new UnsupportedOperationException("RAW writes unsupported");
    }

    @Override
    public void writeRawValue(char[] text, int offset, int len) {
        throw new UnsupportedOperationException("RAW writes unsupported");
    }

}
