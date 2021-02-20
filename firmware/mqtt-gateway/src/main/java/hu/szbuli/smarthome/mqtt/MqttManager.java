package hu.szbuli.smarthome.mqtt;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

public class MqttManager {

  private Mqtt5AsyncClient mqttClient;

  public MqttManager(Mqtt5AsyncClient mqttClient) {
    this.mqttClient = mqttClient;
  }

  public void publishMqttMessage(String topic, byte[] payload) {
    this.publishMqttMessage(topic, payload, false);
  }

  public void publishMqttMessage(String topic, String payload, boolean retain) {
    this.publishMqttMessage(topic, payload.getBytes(), retain);
  }

  public void publishMqttMessage(String topic, byte[] payload, boolean retain) {
    mqttClient.publishWith()
        .topic(topic)
        .payload(payload)
        .qos(MqttQos.AT_MOST_ONCE)
        .retain(retain)
        .send();
  }

  public Mqtt5AsyncClient getMqttClient() {
    return mqttClient;
  }

}
