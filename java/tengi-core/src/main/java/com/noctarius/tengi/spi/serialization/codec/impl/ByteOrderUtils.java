/*
 * Copyright (c) 2015-2016, Christoph Engelbert (aka noctarius) and
 * contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.noctarius.tengi.spi.serialization.codec.impl;

import com.noctarius.tengi.spi.buffer.ReadableMemoryBuffer;
import com.noctarius.tengi.spi.buffer.WritableMemoryBuffer;

final class ByteOrderUtils {

    static void putShort(short value, WritableMemoryBuffer memoryBuffer) {
        memoryBuffer.writeByte((byte) (value >> 8));
        memoryBuffer.writeByte((byte) (value >> 0));
    }

    static short getShort(ReadableMemoryBuffer memoryBuffer) {
        byte b1 = memoryBuffer.readByte();
        byte b0 = memoryBuffer.readByte();
        return buildShort(b1, b0);
    }

    static void putInt(int value, WritableMemoryBuffer memoryBuffer) {
        memoryBuffer.writeByte((byte) (value >>> 24));
        memoryBuffer.writeByte((byte) (value >>> 16));
        memoryBuffer.writeByte((byte) (value >>> 8));
        memoryBuffer.writeByte((byte) (value >>> 0));
    }

    static int getInt(ReadableMemoryBuffer memoryBuffer) {
        byte b3 = memoryBuffer.readByte();
        byte b2 = memoryBuffer.readByte();
        byte b1 = memoryBuffer.readByte();
        byte b0 = memoryBuffer.readByte();
        return buildInt(b3, b2, b1, b0);
    }

    static void putLong(long value, WritableMemoryBuffer memoryBuffer) {
        memoryBuffer.writeByte((byte) (value >> 56));
        memoryBuffer.writeByte((byte) (value >> 48));
        memoryBuffer.writeByte((byte) (value >> 40));
        memoryBuffer.writeByte((byte) (value >> 32));
        memoryBuffer.writeByte((byte) (value >> 24));
        memoryBuffer.writeByte((byte) (value >> 16));
        memoryBuffer.writeByte((byte) (value >> 8));
        memoryBuffer.writeByte((byte) (value >> 0));
    }

    static long getLong(ReadableMemoryBuffer memoryBuffer) {
        byte b7 = memoryBuffer.readByte();
        byte b6 = memoryBuffer.readByte();
        byte b5 = memoryBuffer.readByte();
        byte b4 = memoryBuffer.readByte();
        byte b3 = memoryBuffer.readByte();
        byte b2 = memoryBuffer.readByte();
        byte b1 = memoryBuffer.readByte();
        byte b0 = memoryBuffer.readByte();
        return buildLong(b7, b6, b5, b4, b3, b2, b1, b0);
    }

    static short buildShort(byte b1, byte b0) {
        return (short) ((((b1 & 0xFF) << 8) | ((b0 & 0xFF) << 0)));
    }

    static int buildInt(byte b3, byte b2, byte b1, byte b0) {
        return ((((b3 & 0xFF) << 24) | ((b2 & 0xFF) << 16) | ((b1 & 0xFF) << 8) | ((b0 & 0xFF) << 0)));
    }

    static long buildLong(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1, byte b0) {
        return ((((b7 & 0xFFL) << 56) | ((b6 & 0xFFL) << 48) | ((b5 & 0xFFL) << 40) | ((b4 & 0xFFL) << 32) | ((b3 & 0xFFL) << 24)
                | ((b2 & 0xFFL) << 16) | ((b1 & 0xFFL) << 8) | ((b0 & 0xFFL) << 0)));
    }

    private ByteOrderUtils() {
    }

}
