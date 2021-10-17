package hu.szbuli.smarthome.gateway.util;

public class ExponentialSmoothing {

  public static double smooth(double current, double prev, double smoothingFactor) {
    return current * smoothingFactor + (1 - smoothingFactor) * prev;
  }

}
