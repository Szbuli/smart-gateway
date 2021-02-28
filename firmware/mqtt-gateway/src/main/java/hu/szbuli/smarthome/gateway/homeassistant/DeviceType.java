package hu.szbuli.smarthome.gateway.homeassistant;

public class DeviceType {
  private int deviceTypeId;
  private String model;
  private String manufacturer;

  public int getDeviceTypeId() {
    return deviceTypeId;
  }

  public void setDeviceTypeId(int deviceTypeId) {
    this.deviceTypeId = deviceTypeId;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

}
