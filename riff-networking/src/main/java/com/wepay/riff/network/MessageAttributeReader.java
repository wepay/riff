package com.wepay.riff.network;

import java.util.List;

public abstract class MessageAttributeReader {

    public abstract byte readByte();

    public abstract short readShort();

    public abstract int readInt();

    public abstract long readLong();

    public abstract double readDouble();

    public abstract byte[] readByteArray();

    public abstract short[] readShortArray();

    public abstract int[] readIntArray();

    public abstract List<Integer> readIntList();

    public abstract boolean readBoolean();

    public abstract String readString();

    public abstract void ensureReadCompletely();

}
