package hu.szbuli.smarthome.gateway.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CanMqttTopicConverterTest {

  private static CanMqttTopicConverter topicConverter;

  @BeforeAll
  static void init() {
    topicConverter = new CanMqttTopicConverter();
  }

  @Test
  void mqttTopicWithDeviceId() {
    Integer deviceId = topicConverter.getDeviceId("a/b/${deviceId}/c", "a/b/10/c");
    assertEquals(10, deviceId);
  }

  @Test
  void mqttTopicWithoutDeviceId() {
    Integer deviceId = topicConverter.getDeviceId("a/b/c/2", "a/b/c/2");
    assertNull(deviceId);
  }

}
