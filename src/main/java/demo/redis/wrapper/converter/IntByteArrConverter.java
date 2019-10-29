package demo.redis.wrapper.converter;

import java.nio.ByteBuffer;

public class IntByteArrConverter {

    private IntByteArrConverter() {
    }

    public static Integer convert(byte[] value) {
        return ByteBuffer.wrap(value).getInt();
    }

    public static byte[] convert(Integer value) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(value).array();
    }
}
