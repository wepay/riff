package com.wepay.riff.network;

public final class MessageType {

    private MessageType() {
    }

    //Non-negative message type ids are reserved
    public static final byte HELLO = -1;
    public static final byte KEEP_ALIVE = -2;

}
