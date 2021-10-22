package hu.szbuli.smarthome.gateway.homeassistant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HASensorConfig extends HAEntityConfig {

  @JsonProperty("state_topic")
  private String stateTopic;
  @JsonProperty("unit_of_measurement")
  private String unitOfMeasurement;

  public String getStateTopic() {
    return stateTopic;
  }

  public void setStateTopic(String stateTopic) {
    this.stateTopic = stateTopic;
  }

  public String getUnitOfMeasurement() {
    return unitOfMeasurement;
  }

  public void setUnitOfMeasurement(String unitOfMeasurement) {
    this.unitOfMeasurement = unitOfMeasurement;
  }

}
