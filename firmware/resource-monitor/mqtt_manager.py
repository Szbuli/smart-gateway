import paho.mqtt.client as mqtt
import logging


class MqttManager:
    def __init__(self, host, port, username, password, deviceName, baseTopic):
        self.client = mqtt.Client()
        self.baseTopic = baseTopic
        self.deviceName = deviceName

        logging.info("init mqtt...")
        self.client.on_connect = self.on_connect
        self.client.on_disconnect = self.on_disconnect
        self.client.username_pw_set(username, password)
        self.client.connect(host, int(port), 20)

        self.client.will_set(self.getStatusTopic(), 'offline', retain=True)

    def publish(self, topic, message, qos=0, retain=False):
        logging.debug("publish" + message)
        self.client.publish(topic, message, qos, retain)

    def publishWithBaseTopic(self, topic, message, qos=0, retain=False):
        logging.debug("publish" + message)
        self.publish(self.getTopicWithBase(topic), message, qos, retain)

    def getTopicWithBase(self, topic):
        return self.baseTopic + self.deviceName + '/resource-monitor/' + topic

    def getStatusTopic(self):
        return self.getTopicWithBase('status')

    def on_connect(self, mqtt_client, userdata, flags, rc):
        self.publish(self.getStatusTopic(), 'online', retain=True)
        logging.info("Connected with result code " + str(rc))

    def on_disconnect(mqtt_client, userdata, rc):
        logging.warning("Disconnected with result code " + str(rc))

    def loop(self):
        self.client.loop_forever()
