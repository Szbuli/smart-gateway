package hu.szbuli.smarthome.mqtt;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

public class MqttTopic {

  private String topic;

  public MqttTopic(String topic) {
    this.topic = topic;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public MqttTopic injectValues(String name, int value) {
    return injectValues(name, Integer.toString(value));
  }

  public MqttTopic injectValues(String name, String value) {
    Map<String, String> valuesMap = new HashMap<>();
    valuesMap.put(name, value);
    StringSubstitutor s = new StringSubstitutor(valuesMap);

    this.topic = s.replace(topic);
    return this;
  }
}
