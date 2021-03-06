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
package com.noctarius.tengi.spi.serialization.impl;

import com.noctarius.tengi.core.model.Packet;
import com.noctarius.tengi.core.serialization.debugger.SerializationDebugger;
import com.noctarius.tengi.spi.buffer.MemoryBuffer;
import com.noctarius.tengi.spi.serialization.Protocol;
import com.noctarius.tengi.spi.serialization.Serializer;
import com.noctarius.tengi.spi.serialization.codec.impl.DefaultCodec;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class StackTraceFixTestCase {

    @Test(expected = NullPointerException.class)
    public void testSerializationStackTraceFix()
            throws Exception {

        SerializationDebugger.Debugger.ENABLED = true;
        try {
            Protocol protocol = new DefaultProtocol(Collections.emptyList());
            Serializer serializer = Serializer.create(protocol);

            Packet packet = new Packet("Test");
            Packet innerPacket = new Packet("innerPacket");
            packet.setValue("innerPacket", innerPacket);
            Packet throwing = new SerializationClasses.SubPacketMarshallException("throwing");
            innerPacket.setValue("throwing", throwing);

            serializer.writeObject("packet", packet);

        } catch (NullPointerException e) {

            int serializationFrames = 0;

            Throwable throwable = e;
            do {
                StackTraceElement[] stackTrace = throwable.getStackTrace();
                for (int i = 0; i < stackTrace.length; i++) {
                    if (stackTrace[i].getMethodName().contains("[SERIALIZE => ")) {
                        serializationFrames++;
                    }
                }
            } while ((throwable = throwable.getCause()) != null);

            assertEquals(3, serializationFrames);
            e.printStackTrace();
            throw e;

        } finally {
            SerializationDebugger.Debugger.ENABLED = false;
        }
    }

    @Test(expected = NullPointerException.class)
    public void testDeserializationStackTraceFix()
            throws Exception {

        SerializationDebugger.Debugger.ENABLED = true;
        try {
            Protocol protocol = new DefaultProtocol(Collections.emptyList());
            Serializer serializer = Serializer.create(protocol);

            Packet packet = new Packet("Test");
            Packet innerPacket = new Packet("innerPacket");
            packet.setValue("innerPacket", innerPacket);
            Packet throwing = new SerializationClasses.SubPacketUnmarshallException("throwing");
            innerPacket.setValue("throwing", throwing);

            MemoryBuffer memoryBuffer = serializer.writeObject("packet", packet);
            serializer.readObject("object", new DefaultCodec(protocol, memoryBuffer));

        } catch (NullPointerException e) {
            int serializationFrames = 0;

            Throwable throwable = e;
            do {
                StackTraceElement[] stackTrace = throwable.getStackTrace();
                for (int i = 0; i < stackTrace.length; i++) {
                    if (stackTrace[i].getMethodName().contains("[DESERIALIZE => ")) {
                        serializationFrames++;
                    }
                }
            } while (throwable == throwable.getCause() || (throwable = throwable.getCause()) != null);

            assertEquals(3, serializationFrames);

            e.printStackTrace();
            throw e;

        } finally {
            SerializationDebugger.Debugger.ENABLED = false;
        }
    }

}
