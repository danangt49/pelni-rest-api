package com.pelni.boarding.ticket.config.aspect;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CustomOutputStream extends ServletOutputStream {

    private final ByteArrayOutputStream outputStream;

    public CustomOutputStream() {
        this.outputStream = new ByteArrayOutputStream();
    }

    @Override
    public void write(int b) {
        outputStream.write(b);
    }

    public byte[] getCopy() {
        return outputStream.toByteArray();
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
}
