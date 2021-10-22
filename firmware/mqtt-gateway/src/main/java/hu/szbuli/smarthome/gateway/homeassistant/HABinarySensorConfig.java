package hu.szbuli.smarthome.gateway.homeassistant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HABinarySensorConfig extends HAEntityConfig {

  @JsonProperty("state_topic")
  private String stateTopic;
  @JsonProperty("payload_on")
  private String payloadOn;
  @JsonProperty("payload_off")
  private String payloadOff;

  public String getStateTopic() {
    return stateTopic;
  }

  public void setStateTopic(String stateTopic) {
    this.stateTopic = stateTopic;
  }

  public String getPayloadOn() {
    return payloadOn;
  }

  public void setPayloadOn(String payloadOn) {
    this.payloadOn = payloadOn;
  }

  public String getPayloadOff() {
    return payloadOff;
  }

  public void setPayloadOff(String payloadOff) {
    this.payloadOff = payloadOff;
  }

}
