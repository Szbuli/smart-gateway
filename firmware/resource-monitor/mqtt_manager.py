import paho.mqtt.client as mqtt
import logging

client = mqtt.Client()


def publish(message):
    logging.debug("publish" + message)


def on_connect(mqtt_client, userdata, flags, rc):
    logging.info("Connected with result code " + str(rc))


def on_disconnect(mqtt_client, userdata, rc):
    logging.warning("Disconnected with result code " + str(rc))


def init(host, port, username, password):
    logging.info("init mqtt...")

    broker = host
    port = int(port)
    client.on_connect = on_connect
    client.on_disconnect = on_disconnect
    client.username_pw_set(username, password)
    client.connect(broker, port, 20)
    return client


def loop():
    client.loop_forever()
