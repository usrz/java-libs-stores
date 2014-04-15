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
package org.usrz.libs.stores;

import static org.usrz.libs.utils.Check.check;
import static org.usrz.libs.utils.Check.notNull;

import java.security.SecureRandom;
import java.util.Arrays;

import org.usrz.libs.utils.codecs.Base32Codec;
import org.usrz.libs.utils.codecs.Codec;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A simple identifier for a {@link Document}, basically 160 bits of data
 * encoded in BASE-32 lower-case.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public final class Id {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Codec CODEC = new Base32Codec(false);
    private static final int LENGTH = 20;

    private final byte[] value;

    /**
     * Create a new {@link Id} with a random value.
     */
    public Id() {
        value = new byte[LENGTH];
        RANDOM.nextBytes(value);
    }

    /**
     * Create a new {@link Id} from the specified <em>byte array</em>.
     */
    public Id(byte[] value) {
        notNull(value, "Null array");
        check(value, value.length == LENGTH, "Invalid array length: " + value.length + " bytes");
        this.value = value;
    }

    /**
     * Create a new {@link Id} from the specified {@link String}.
     */
    @JsonCreator
    public Id(String value) {
        notNull(value, "Null string");
        check(value, value.length() == 32, "Invalid string length: \"" + value + "\" (" + value.length() +" chars)");
        try {
            this.value = CODEC.decode(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid string: \"" + value + "\"", exception);
        }
    }

    /**
     * Return the <em>XOR</em>-ed value of this instance with the specified one.
     */
    public Id xor(Id id) {
        final byte[] xor = new byte[LENGTH];
        for (int x = 0; x < LENGTH; x ++)
            xor[x] = (byte) (value[x] ^ id.value[x]);
        return new Id(xor);
    }

    /**
     * Return a <em>byte array</em> representation of this {@link Id}.
     */
    public byte[] toByteArray() {
        final byte[] array = new byte[LENGTH];
        System.arraycopy(value, 0, array, 0, LENGTH);
        return array;
    }

    /**
     * Return the BASE-32 lower-case representation of this {@link Id}.
     */
    @Override
    @JsonValue
    public String toString() {
        return CODEC.encode(value);
    }

    /**
     * Compute the <em>hash code</em> of this instance.
     * <p>
     * This is basically equivalent to calling
     * {@link Arrays#hashCode(byte[]) Arrays.hashCode(toByteArray())}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    /**
     * Check if the specified object is equal to this {@link Id}.
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        try {
            return Arrays.equals(value, ((Id) object).value);
        } catch (ClassCastException exception) {
            return false;
        }
    }

}
