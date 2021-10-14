package com.wepay.riff.network;

public class KeepAliveMessage extends Message {

    public KeepAliveMessage() {
    }

    @Override
    public byte type() {
        return MessageType.KEEP_ALIVE;
    }

}
