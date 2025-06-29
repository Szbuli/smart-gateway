package hu.szbuli.smarthome.gateway.util;

public class NumberUtils {

  public static int uint16ToInteger(byte[] uint16) {
    return Byte.toUnsignedInt(uint16[0]) >> 8 | Byte.toUnsignedInt(uint16[1]);
  }

  public static int uint8ToInteger(byte uint8) {
    return Byte.toUnsignedInt(uint8);
  }

  public static long uint64ToLong(byte[] uint64) {
    return ((long) Byte.toUnsignedInt(uint64[0]) << 56)
        | ((long) Byte.toUnsignedInt(uint64[1]) << 48)
        | ((long) Byte.toUnsignedInt(uint64[2]) << 40)
        | ((long) Byte.toUnsignedInt(uint64[3]) << 32)
        | ((long) Byte.toUnsignedInt(uint64[4]) << 24)
        | ((long) Byte.toUnsignedInt(uint64[5]) << 16)
        | ((long) Byte.toUnsignedInt(uint64[6]) << 8)
        | ((long) Byte.toUnsignedInt(uint64[7]));
  }

}
