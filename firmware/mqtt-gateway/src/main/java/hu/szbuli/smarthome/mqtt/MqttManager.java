package hu.szbuli.smarthome.mqtt;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;

import hu.szbuli.smarthome.gateway.heartbeat.HeartBeatService;
import hu.szbuli.smarthome.rpi.mqtt.proxy.MqttConfiguration;

public class MqttManager {

  private static final Logger logger = LoggerFactory.getLogger(MqttManager.class);

  private Mqtt5AsyncClient mqttClient;
  private String healthStatusTopic;
  private MqttConfiguration mqttConfiguration;

  public MqttManager(MqttConfiguration mqttConfiguration, String healthStatusTopic) {
    this.mqttConfiguration = mqttConfiguration;
    this.healthStatusTopic = healthStatusTopic;
  }

  public void connect(boolean reconnect, boolean logEvents) {
    Mqtt5ClientBuilder clientBuilder = MqttClient.builder()
        .addConnectedListener(context -> {
          publishMqttMessage(healthStatusTopic, HeartBeatService.ONLINE_PAYLOAD, true);
        })
        .useMqttVersion5()
        .willPublish()
        .topic(healthStatusTopic).retain(true)
        .payload(HeartBeatService.OFFLINE_PAYLOAD)
        .applyWillPublish()
        .simpleAuth()
        .username(mqttConfiguration.getUsername())
        .password(mqttConfiguration.getPassword().getBytes())
        .applySimpleAuth()
        .identifier(UUID.randomUUID().toString())
        .serverHost(mqttConfiguration.getHost())
        .serverPort(mqttConfiguration.getPort());
    // .sslWithDefaultConfig()

    if (reconnect) {
      clientBuilder.automaticReconnectWithDefaultConfig();
    }

    if (logEvents) {
      clientBuilder
          .addDisconnectedListener(context -> {
            logger.error("disconected from mqtt ({})", Instant.now(), context.getCause());
          })
          .addConnectedListener(context -> {
            logger.info("connected to mqtt ({})", Instant.now());
          });
    }

    mqttClient = clientBuilder.buildAsync();

    mqttClient.connectWith()
        .send();
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

  public void disconnect() {
    if (mqttClient != null) {
      publishMqttMessage(healthStatusTopic, HeartBeatService.OFFLINE_PAYLOAD, true);
      mqttClient.disconnect();
    }
  }

}
