package hu.szbuli.smarthome.gateway.heartbeat;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import hu.szbuli.smarthome.can.SafeThread;

public class HeartBeatService extends SafeThread {

  private static final int MAX_IDLE_TIME_SECONDS = 70;

  public static final byte[] ONLINE_PAYLOAD = "online".getBytes();
  public static final byte[] OFFLINE_PAYLOAD = "offline".getBytes();

  private Map<String, Instant> heartbeatMap;
  private Mqtt5AsyncClient mqttClient;

  public HeartBeatService(Mqtt5AsyncClient mqttClient) {
    super();
    heartbeatMap = new HashMap<>();
    this.mqttClient = mqttClient;
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
            mqttClient.publishWith()
                .topic(entry.getKey())
                .payload(OFFLINE_PAYLOAD)
                .qos(MqttQos.AT_MOST_ONCE)
                .retain(true)
                .send();
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
      mqttClient.publishWith()
          .topic(mqttTopic)
          .payload(ONLINE_PAYLOAD)
          .qos(MqttQos.AT_MOST_ONCE)
          .retain(true)
          .send();
    }
    heartbeatMap.put(mqttTopic, time);
  }

}
