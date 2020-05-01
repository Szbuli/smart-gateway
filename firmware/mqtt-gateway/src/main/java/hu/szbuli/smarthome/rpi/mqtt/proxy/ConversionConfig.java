package hu.szbuli.smarthome.rpi.mqtt.proxy;

public class ConversionConfig {

	private int canTopic;
	private String mqttTopic;
	private String converter;

	public int getCanTopic() {
		return canTopic;
	}

	public void setCanTopic(int canTopic) {
		this.canTopic = canTopic;
	}

	public String getMqttTopic() {
		return mqttTopic;
	}

	public void setMqttTopic(String mqttTopic) {
		this.mqttTopic = mqttTopic;
	}

	public String getConverter() {
		return converter;
	}

	public void setConverter(String converter) {
		this.converter = converter;
	}

}
