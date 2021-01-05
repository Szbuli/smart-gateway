package hu.szbuli.smarthome.gateway.homeassistant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HADeviceConfig {

  @JsonProperty("sw_version")
  private String swVersion;
  private String identifiers;
  @JsonProperty("via_device")
  private String viaDevice;
  private String manufacturer;
  private String model;
  private String name;

  public String getSwVersion() {
    return swVersion;
  }

  public void setSwVersion(String swVersion) {
    this.swVersion = swVersion;
  }

  public String getIdentifiers() {
    return identifiers;
  }

  public void setIdentifiers(String identifiers) {
    this.identifiers = identifiers;
  }

  public String getViaDevice() {
    return viaDevice;
  }

  public void setViaDevice(String viaDevice) {
    this.viaDevice = viaDevice;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
