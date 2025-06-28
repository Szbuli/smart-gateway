package hu.szbuli.smarthome.gateway.converter;

import hu.szbuli.smarthome.gateway.state.OnOff;
import hu.szbuli.smarthome.gateway.util.NumberUtils;

public class CanMqttPayloadConverter implements GatewayConverter {

  @Override
  public byte[] uint8ToOnOff(byte[] uint8) {
    return Byte.toUnsignedInt(uint8[0]) == 0 ? OnOff.OFF.name()
        .getBytes()
        : OnOff.ON.name()
            .getBytes();
  }

  @Override
  public byte[] onOffToUint8(String stateString) {
    OnOff state = OnOff.valueOf(stateString);
    return state == OnOff.OFF ? new byte[] {0x00} : new byte[] {0x01};
  }

  @Override
  public byte[] uint8ToNumber(byte[] uint8) {
    return Integer.toString(NumberUtils.uint8ToInteger(uint8[0]))
        .getBytes();
  }

  @Override
  public byte[] numberToUint8(String numberString) {
    return new byte[] {(byte) Integer.parseInt(numberString)};
  }

  @Override
  public byte[] uint16ToNumber(byte[] uint16) {
    int number = NumberUtils.uint16ToInteger(uint16);
    return Integer.toString(number)
        .getBytes();
  }

  @Override
  public byte[] numberToUint16(String numberString) {
    return new byte[] {(byte) (Integer.parseInt(numberString) & 0xff),
        (byte) ((Integer.parseInt(numberString) >> 8) & 0xff)};
  }

  @Override
  public byte[] uint64ToNumber(byte[] uint64) {
    long number = NumberUtils.uint64ToLong(uint64);
    return Long.toString(number)
        .getBytes();
  }

}
