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

import java.util.Arrays;
import java.util.Random;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class IdTest extends AbstractTest {

    @Test
    public void testEquals() {
        final Id id1 = new Id("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567");
        final Id id2 = new Id("abcdefghijklmnopqrstuvwxyz234567");
        assertEquals(id1, id2);
        assertEquals(id1.toString(), "abcdefghijklmnopqrstuvwxyz234567");
        assertEquals(id2.toString(), "abcdefghijklmnopqrstuvwxyz234567");
    }

    @Test
    public void testRandom() {
        final Id id1 = new Id();
        final Id id2 = new Id();
        assertNotEquals(id1, id2);
    }


    @Test
    public void testXor() {
        final Id id1 = new Id();
        final Id id2 = new Id();
        final Id xor1 = id1.xor(id2);
        final Id xor2 = id2.xor(id1);
        assertNotEquals(id1, id2);
        assertNotEquals(id1, xor1);
        assertNotEquals(id1, xor2);
        assertNotEquals(id2, xor1);
        assertNotEquals(id2, xor2);
        assertEquals(xor1, xor2);
    }

    @Test
    public void testXorLoop() {
        Id id = new Id();
        for (int x = 0; x < 10000; x ++) id = id.xor(new Id());
        final byte[] array = new byte[20];
        Arrays.fill(array, (byte) -1);
        assertNotEquals(id, new Id(array));
    }

    @Test
    public void testXorKnown() {
        final byte[] array1 = new byte[20];
        final byte[] array2 = new byte[20];
        Arrays.fill(array1, (byte) 0);
        Arrays.fill(array2, (byte) -1);
        final Id id1 = new Id(array1);
        final Id id2 = new Id(array2);
        final Id xor = id1.xor(id2);
        assertEquals(xor, id2);
    }

    @Test
    public void testXorKnown2() {
        final Random random = new Random();
        final byte byte1 = (byte) random.nextInt();
        final byte byte2 = (byte) random.nextInt();
        final byte byte3 = (byte) (byte1 ^ byte2);
        final byte[] array1 = new byte[20];
        final byte[] array2 = new byte[20];
        final byte[] array3 = new byte[20];
        Arrays.fill(array1, byte1);
        Arrays.fill(array2, byte2);
        Arrays.fill(array3, byte3);
        final Id id1 = new Id(array1);
        final Id id2 = new Id(array2);
        final Id xor = id1.xor(id2);
        assertEquals(xor, new Id(array3));
    }

    @Test(expectedExceptions=IllegalArgumentException.class, expectedExceptionsMessageRegExp="Invalid string length.*31 chars.*")
    public void testStringShort() {
        new Id("ABCDEFGHIJKLMNOPQRSTUVWXYZ23456");
    }

    @Test(expectedExceptions=IllegalArgumentException.class, expectedExceptionsMessageRegExp="Invalid string length.*33 chars.*")
    public void testStringLong() {
        new Id("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567A");
    }

    @Test(expectedExceptions=IllegalArgumentException.class, expectedExceptionsMessageRegExp="Invalid string:.*")
    public void testStringWrong() {
        new Id("ABCDEFGHIJKLMNOPQRSTUVWXYZ234568");
    }

    @Test(expectedExceptions=NullPointerException.class, expectedExceptionsMessageRegExp="Null string")
    public void testStringNull() {
        new Id((String) null);
    }

    @Test(expectedExceptions=IllegalArgumentException.class, expectedExceptionsMessageRegExp="Invalid array length: 19 bytes")
    public void testArrayShort() {
        new Id(new byte[19]);
    }

    @Test(expectedExceptions=IllegalArgumentException.class, expectedExceptionsMessageRegExp="Invalid array length: 21 bytes")
    public void testArrayLong() {
        new Id(new byte[21]);
    }

    @Test(expectedExceptions=NullPointerException.class, expectedExceptionsMessageRegExp="Null array")
    public void testArrayNull() {
        new Id((byte[]) null);
    }

}
