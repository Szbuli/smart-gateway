package hu.szbuli.smarthome.rpi.monitor.mqtt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

public class MqttManager {

  public static final byte[] ONLINE_PAYLOAD = "online".getBytes();
  public static final byte[] OFFLINE_PAYLOAD = "offline".getBytes();

  private Mqtt5AsyncClient mqttClient;

  public void init(Properties props) throws FileNotFoundException, IOException {
    mqttClient = MqttClient.builder()
        .automaticReconnectWithDefaultConfig()
        .useMqttVersion5()
        .identifier(UUID.randomUUID().toString())
        .serverHost(props.getProperty("host"))
        .serverPort(Integer.parseInt(props.getProperty("port")))
        // .sslWithDefaultConfig()
        .buildAsync();

    CompletableFuture<Mqtt5ConnAck> connect = mqttClient.connectWith()
        .simpleAuth()
        .username(props.getProperty("username"))
        .password(props.getProperty("password").getBytes())
        .applySimpleAuth()
        .send();
    CompletableFuture.allOf(connect).join();
  }

  public void send(String topic, String payload) {
    this.send(topic, payload.getBytes());
  }

  public void send(String topic, byte[] payload) {
    mqttClient.publishWith()
        .topic(topic)
        .payload(payload)
        .qos(MqttQos.AT_MOST_ONCE)
        .send();
  }

}
