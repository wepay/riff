package com.wepay.riff.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ByteArrayMessageAttributeReaderWriterTest {

    @Test
    public void testReadFromByteArray() {
        Random rand = new Random();
        ByteArrayMessageAttributeWriter writer = new ByteArrayMessageAttributeWriter();

        boolean booleanVal = rand.nextBoolean();

        byte byteVal = (byte) rand.nextInt();

        byte[] byteArrayVal = new byte[rand.nextInt(1000)];
        rand.nextBytes(byteArrayVal);

        short shortVal = (short) rand.nextInt();

        short[] shortArrayVal = new short[rand.nextInt(1000)];
        for (int i = 0; i < shortArrayVal.length; i++) {
            shortArrayVal[i] = (short) rand.nextInt();
        }

        int intVal = rand.nextInt();

        int[] intArrayVal = new int[rand.nextInt(1000)];
        for (int i = 0; i < intArrayVal.length; i++) {
            intArrayVal[i] = rand.nextInt();
        }

        int size = rand.nextInt(1000);
        List<Integer> intListVal = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            intListVal.add(rand.nextInt());
        }

        long longVal = rand.nextLong();

        double doubleVal = rand.nextDouble();

        StringBuilder sb = new StringBuilder();
        int count = rand.nextInt(1000);
        for (int i = 0; i < count; i++) {
            sb.append(Integer.toHexString(rand.nextInt(16)));
        }
        String stringVal = sb.toString();

        writer.writeBoolean(booleanVal);

        writer.writeByte(byteVal);
        writer.writeByteArray(byteArrayVal);
        writer.writeByteArray(null);

        writer.writeShort(shortVal);
        writer.writeShortArray(shortArrayVal);
        writer.writeShortArray(null);

        writer.writeInt(intVal);
        writer.writeIntArray(intArrayVal);
        writer.writeIntArray(null);
        writer.writeIntList(intListVal);
        writer.writeIntList(null);

        writer.writeLong(longVal);

        writer.writeDouble(doubleVal);

        writer.writeString(stringVal);
        writer.writeString(null);

        ByteArrayMessageAttributeReader reader = new ByteArrayMessageAttributeReader(writer.toByteArray());

        assertEquals(booleanVal, reader.readBoolean());

        assertEquals(byteVal, reader.readByte());
        assertTrue(Arrays.equals(byteArrayVal, reader.readByteArray()));
        assertNull(reader.readByteArray());

        assertEquals(shortVal, reader.readShort());
        assertTrue(Arrays.equals(shortArrayVal, reader.readShortArray()));
        assertNull(reader.readShortArray());

        assertEquals(intVal, reader.readInt());
        assertTrue(Arrays.equals(intArrayVal, reader.readIntArray()));
        assertNull(reader.readIntArray());
        assertTrue(intListVal.equals(reader.readIntList()));
        assertNull(reader.readIntList());

        assertEquals(longVal, reader.readLong());

        assertTrue(Math.abs(doubleVal - reader.readDouble()) < Double.MIN_VALUE);

        assertEquals(stringVal, reader.readString());
        assertNull(reader.readString());

        reader.ensureReadCompletely();
    }

    @Test
    public void testReadFromByteBuf() {
        Random rand = new Random();
        byte[] bytes = new byte[15000];

        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        byteBuf.resetWriterIndex();
        ByteBufMessageAttributeWriter writer = new ByteBufMessageAttributeWriter(byteBuf);

        boolean booleanVal = rand.nextBoolean();

        byte byteVal = (byte) rand.nextInt();

        byte[] byteArrayVal = new byte[rand.nextInt(1000)];
        rand.nextBytes(byteArrayVal);

        short shortVal = (short) rand.nextInt();

        short[] shortArrayVal = new short[rand.nextInt(1000)];
        for (int i = 0; i < shortArrayVal.length; i++) {
            shortArrayVal[i] = (short) rand.nextInt();
        }

        int intVal = rand.nextInt();

        int[] intArrayVal = new int[rand.nextInt(1000)];
        for (int i = 0; i < intArrayVal.length; i++) {
            intArrayVal[i] = rand.nextInt();
        }

        int size = rand.nextInt(1000);
        List<Integer> intListVal = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            intListVal.add(rand.nextInt());
        }

        long longVal = rand.nextLong();

        double doubleVal = rand.nextDouble();

        StringBuilder sb = new StringBuilder();
        int count = rand.nextInt(1000);
        for (int i = 0; i < count; i++) {
            sb.append(Integer.toHexString(rand.nextInt(16)));
        }
        String stringVal = sb.toString();

        writer.writeBoolean(booleanVal);

        writer.writeByte(byteVal);
        writer.writeByteArray(byteArrayVal);
        writer.writeByteArray(null);

        writer.writeShort(shortVal);
        writer.writeShortArray(shortArrayVal);
        writer.writeShortArray(null);

        writer.writeInt(intVal);
        writer.writeIntArray(intArrayVal);
        writer.writeIntArray(null);
        writer.writeIntList(intListVal);
        writer.writeIntList(null);

        writer.writeLong(longVal);

        writer.writeDouble(doubleVal);

        writer.writeString(stringVal);
        writer.writeString(null);

        ByteArrayMessageAttributeReader reader = new ByteArrayMessageAttributeReader(bytes, 0, writer.bytesWritten());

        assertEquals(booleanVal, reader.readBoolean());

        assertEquals(byteVal, reader.readByte());
        assertTrue(Arrays.equals(byteArrayVal, reader.readByteArray()));
        assertNull(reader.readByteArray());

        assertEquals(shortVal, reader.readShort());
        assertTrue(Arrays.equals(shortArrayVal, reader.readShortArray()));
        assertNull(reader.readShortArray());

        assertEquals(intVal, reader.readInt());
        assertTrue(Arrays.equals(intArrayVal, reader.readIntArray()));
        assertNull(reader.readIntArray());
        assertTrue(intListVal.equals(reader.readIntList()));
        assertNull(reader.readIntList());

        assertEquals(longVal, reader.readLong());

        assertTrue(Math.abs(doubleVal - reader.readDouble()) < Double.MIN_VALUE);

        assertEquals(stringVal, reader.readString());
        assertNull(reader.readString());

        reader.ensureReadCompletely();
    }

}
