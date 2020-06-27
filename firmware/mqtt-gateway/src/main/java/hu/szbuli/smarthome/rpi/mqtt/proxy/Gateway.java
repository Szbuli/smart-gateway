package hu.szbuli.smarthome.rpi.mqtt.proxy;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import hu.szbuli.smarthome.can.CanMessage;
import hu.szbuli.smarthome.gateway.converter.CanMqttPayloadConverter;
import hu.szbuli.smarthome.gateway.converter.CanMqttTopicConverter;
import hu.szbuli.smarthome.gateway.converter.GatewayConverter;
import hu.szbuli.smarthome.gateway.heartbeat.HeartBeatService;
import hu.szbuli.smarthome.mqtt.MqttListener;

public class Gateway {

  private static final Logger logger = LoggerFactory.getLogger(Gateway.class);

  private static final String[] HEADERS = { "topic", "mqtt-topic", "converter" };
  private Mqtt5AsyncClient mqttClient;
  private BlockingQueue<CanMessage> sendCanQueue;

  private Map<Integer, ConversionConfig> can2Mqtt = new HashMap<>();
  private Map<String, ConversionConfig> mqtt2Can = new HashMap<>();
  private GatewayConverter gatewayConverter = new CanMqttPayloadConverter();
  private CanMqttTopicConverter canMqttTopicConverter = new CanMqttTopicConverter();
  private HeartBeatService heartBeatService;

  public Gateway(String configFile, Mqtt5AsyncClient mqttClient, BlockingQueue<CanMessage> sendCanQueue) throws IOException {
    this.mqttClient = mqttClient;
    this.sendCanQueue = sendCanQueue;

    heartBeatService = new HeartBeatService(mqttClient);
    heartBeatService.start();

    Reader in = new FileReader(new File(configFile).getAbsolutePath());
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader(HEADERS).withFirstRecordAsHeader().parse(in);

    for (CSVRecord record : records) {
      Integer canId = Integer.parseInt(record.get("can-id"));
      String mqttTopic = record.get("mqtt-topic");
      String converter = record.get("converter");
      ConversionConfig conversionConfig = new ConversionConfig();
      conversionConfig.setCanTopic(canId);
      conversionConfig.setMqttTopic(mqttTopic);
      conversionConfig.setConverter(converter);

      can2Mqtt.put(canId, conversionConfig);
      mqtt2Can.put(mqttTopic, conversionConfig);
    }

    mqtt2Can.keySet().forEach(topic -> {
      MqttListener mqttListener = new MqttListener(this.mqttClient, this, topic);

      Map<String, String> valuesMap = new HashMap<>();
      valuesMap.put("deviceId", "+");
      StringSubstitutor s = new StringSubstitutor(valuesMap);
      mqttListener.subscribe(s.replace(topic));
    });
  }

  public void processIncomingCanMessage(CanMessage canMessage) {
    logger.info("Incoming can message:  deviceId {}, topcId {}", canMessage.getDeviceId(), canMessage.getTopicId());
    ConversionConfig conversionConfig = can2Mqtt.get(canMessage.getTopicId());
    if (conversionConfig == null) {
      logger.info("conversion config not found for can message");
      return;
    }

    String mqttTopic = injectDeviceIdIntoMqttTopic(conversionConfig.getMqttTopic(), canMessage.getDeviceId());

    if (conversionConfig.getConverter().equals("heartbeat")) {
      heartBeatService.refreshDeviceTimestamp(Instant.now(), mqttTopic);
    } else {
      byte[] payload = toMqttPayload(canMessage, conversionConfig.getConverter());

      logger.info("can message publishing to {}", mqttTopic);

      mqttClient.publishWith()
          .topic(mqttTopic)
          .payload(payload)
          .qos(MqttQos.AT_MOST_ONCE)
          .send();
    }
  }

  public void processIncomingMqttMessage(Mqtt5Publish mqttMessage, String originalTopic) {
    logger.info("Incoming mqtt message:  topic {}", mqttMessage.getTopic());
    ConversionConfig conversionConfig = mqtt2Can.get(originalTopic);
    if (conversionConfig == null) {
      logger.info("conversion config not found for mqtt message");
      return;
    }
    CanMessage canMessage = toCan(mqttMessage, conversionConfig.getConverter());
    try {
      int topicId = conversionConfig.getCanTopic();
      Integer deviceId = canMqttTopicConverter.getDeviceId(originalTopic, mqttMessage.getTopic().toString());
      if (deviceId == null) {
        deviceId = 0;
      }

      canMessage.setTopicId(topicId);
      canMessage.setDeviceId(deviceId);

      logger.info("mqtt message publishing to deviceId {}, topicId {}", deviceId, topicId);

      this.sendCanQueue.put(canMessage);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private CanMessage toCan(Mqtt5Publish mqttMessage, String converter) {
    CanMessage canMessage = new CanMessage();
    String mqttPayloadString = new String(mqttMessage.getPayloadAsBytes(), StandardCharsets.UTF_8);
    byte[] payload;
    switch (converter) {
    case "uint8ToOnOff":
      payload = gatewayConverter.onOffToUint8(mqttPayloadString);
      break;
    case "uint8ToNumber":
      payload = gatewayConverter.numberToUint8(mqttPayloadString);
      break;
    case "uint16ToNumber":
      payload = gatewayConverter.numberToUint16(mqttPayloadString);
      break;
    default:
      throw new UnsupportedOperationException(converter);
    }
    canMessage.setData(payload);
    return canMessage;
  }

  private byte[] toMqttPayload(CanMessage canMessage, String converter) {
    byte[] payload;
    switch (converter) {
    case "uint8ToOnOff":
      payload = gatewayConverter.uint8ToOnOff(canMessage.getData());
      break;
    case "uint8ToNumber":
      payload = gatewayConverter.uint8ToNumber(canMessage.getData());
      break;
    case "uint16ToNumber":
      payload = gatewayConverter.uint16ToNumber(canMessage.getData());
      break;
    default:
      throw new UnsupportedOperationException(converter);
    }
    return payload;
  }

  private String injectDeviceIdIntoMqttTopic(String topic, int deviceId) {
    Map<String, String> valuesMap = new HashMap<>();
    valuesMap.put("deviceId", Integer.toString(deviceId));
    StringSubstitutor s = new StringSubstitutor(valuesMap);

    return s.replace(topic);
  }

}
