package com.phoenixkahlo.nodenet.stream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.phoenixkahlo.nodenet.DisconnectionException;
import com.phoenixkahlo.nodenet.ProtocolViolationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Phoenix on 5/17/2017.
 */
public class KryoObjectStream implements ObjectStream {

    private DatagramStream stream;
    private Kryo kryo;

    public KryoObjectStream(DatagramStream stream, Kryo kryo) {
        this.stream = stream;
        this.kryo = kryo;
    }

    @Override
    public void rebuildDeserializer() {}

    @Override
    public void send(Object object) throws DisconnectionException {
        byte[] data;
        try (Output output = new Output(1000, -1)) {
            kryo.writeObject(output, object);
            data = output.toBytes();
        }
        stream.send(data);
    }

    @Override
    public void sendOrdered(Object object) throws DisconnectionException {
        byte[] data;
        try (Output output = new Output(1000, -1)) {
            kryo.writeObject(output, object);
            data = output.toBytes();
        }
        stream.sendOrdered(data);
    }

    @Override
    public Object receive() throws ProtocolViolationException, DisconnectionException {
        byte[] data = stream.receive();
        try (Input input = new Input(data)) {
            return kryo.readClassAndObject(input);
        }
    }

    @Override
    public void disconnect() {
        stream.disconnect();
    }

    @Override
    public void setDisconnectHandler(Runnable handler, boolean launchNewThread) {
        stream.setDisconnectHandler(handler, launchNewThread);
    }

    @Override
    public boolean isDisconnected() {
        return stream.isDisconnected();
    }

    @Override
    public List<Object> getUnconfirmed() {
        return stream.getUnconfirmed().stream()
                .map(data -> {
                    try (Input input = new Input(data)) {
                        return kryo.readClassAndObject(input);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return stream.getRemoteAddress();
    }
}
