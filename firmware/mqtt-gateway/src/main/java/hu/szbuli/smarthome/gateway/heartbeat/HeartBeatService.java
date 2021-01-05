package hu.szbuli.smarthome.gateway.heartbeat;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import hu.szbuli.smarthome.can.SafeThread;
import hu.szbuli.smarthome.mqtt.MqttManager;

public class HeartBeatService extends SafeThread {

  private static final int MAX_IDLE_TIME_SECONDS = 70;

  public static final byte[] ONLINE_PAYLOAD = "online".getBytes();
  public static final byte[] OFFLINE_PAYLOAD = "offline".getBytes();

  private Map<String, Instant> heartbeatMap;
  private MqttManager mqttManager;

  public HeartBeatService(MqttManager mqttManager) {
    super();
    heartbeatMap = new HashMap<>();
    this.mqttManager = mqttManager;
  }

  @Override
  public void doRun() {
    try {
      while (true) {
        Instant currentTime = Instant.now();

        Iterator<Entry<String, Instant>> it = heartbeatMap.entrySet().iterator();
        while (it.hasNext()) {
          Entry<String, Instant> entry = it.next();
          if (Duration.between(entry.getValue(), currentTime).compareTo(Duration.ofSeconds(MAX_IDLE_TIME_SECONDS)) > 0) {
            it.remove();
            mqttManager.publishMqttMessage(entry.getKey(), OFFLINE_PAYLOAD, true);
          }
        }

        TimeUnit.SECONDS.sleep(15);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Override
  public void setThreadName() {
    this.threadName = "HEARTBEAT";
  }

  synchronized public void refreshDeviceTimestamp(Instant time, String mqttTopic) {
    if (!heartbeatMap.containsKey(mqttTopic)) {
      mqttManager.publishMqttMessage(mqttTopic, ONLINE_PAYLOAD, true);
    }
    heartbeatMap.put(mqttTopic, time);
  }

}
