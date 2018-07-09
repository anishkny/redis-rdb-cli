package com.moilioncircle.redis.cli.tool.glossary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Baoyi Chen
 */
public enum Escape {
    RAW("raw"),
    REDIS("redis");

    private static final char[] NUMERALS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private String value;

    Escape(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static Escape parse(String escape) {
        if (escape == null) return RAW;
        switch (escape) {
            case "raw":
                return RAW;
            case "redis":
                return REDIS;
            default:
                throw new AssertionError("Unsupported escape '" + escape + "'");
        }
    }

    public void encode(double value, OutputStream out) throws IOException {
        encode(String.valueOf(value).getBytes(), out);
    }

    public void encode(long value, OutputStream out) throws IOException {
        encode(String.valueOf(value).getBytes(), out);
    }

    public void encode(int b, OutputStream out) throws IOException {
        switch (this) {
            case RAW:
                out.write(b);
                break;
            case REDIS:
                if (b == '\n') {
                    out.write('\\');
                    out.write('n');
                } else if (b == '\r') {
                    out.write('\\');
                    out.write('r');
                } else if (b == '\t') {
                    out.write('\\');
                    out.write('t');
                } else if (b == '\b') {
                    out.write('\\');
                    out.write('b');
                } else if (b == 7) {
                    out.write('\\');
                    out.write('a');
                } else if ((b == 34 || b == 39 || b == 92 || b <= 32 || b >= 127)) {
                    // encode " ' \ unprintable and space
                    out.write('\\');
                    out.write('x');
                    b = b & 0xFF;
                    int ma = b / 16;
                    int mi = b % 16;
                    out.write(NUMERALS[ma]);
                    out.write(NUMERALS[mi]);
                } else {
                    out.write(b);
                }
                break;
        }
    }

    public static void main(String[] args) {
        System.out.println((char) 34);
        System.out.println((char) 39);
        System.out.println((char) 92);
    }

    public void encode(byte[] bytes, int off, int len, OutputStream out) throws IOException {
        if (bytes == null) return;
        switch (this) {
            case RAW:
                out.write(bytes, off, len);
                break;
            case REDIS:
                for (int i = off; i < len; i++) {
                    encode(bytes[i] & 0xFF, out);
                }
                break;
            default:
                throw new AssertionError(this);
        }
    }

    public void encode(byte[] bytes, OutputStream out) throws IOException {
        encode(bytes, 0, bytes.length, out);
    }

    public byte[] encode(byte[] bytes) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length)) {
            encode(bytes, 0, bytes.length, out);
            return out.toByteArray();
        }
    }
}