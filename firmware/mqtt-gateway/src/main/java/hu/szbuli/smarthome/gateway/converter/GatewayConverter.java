package hu.szbuli.smarthome.gateway.converter;

public interface GatewayConverter {

	byte[] uint8ToOnOff(byte[] uint8);

	byte[] onOffToUint8(String state);

	byte[] uint8ToNumber(byte[] uint8);

	byte[] numberToUint8(String numberString);
	
	byte[] uint16ToNumber(byte[] uint16);
	
	byte[] numberToUint16(String numberString);

	byte[] uint64ToNumber(byte[] uint64);

	byte[] numberToUint64(String numberString);

}
