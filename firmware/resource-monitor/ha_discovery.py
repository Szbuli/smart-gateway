import json
import mqtt_topics


class HaDiscovery:
    def __init__(self, mqtt, deviceName):
        self.mqtt = mqtt
        self.deviceName = deviceName
        self.deviceConfig = {
            'model': 'Resource monitor',
            'manufacturer': 'zbl',
            'sw_version': '0.0.1',
            'identifiers': 'Resource monitor - ' + deviceName,
            'name': 'Resource monitor - ' + deviceName
        }

    def registerTamper(self):
        self.registerHaBinarySensorConfig('tamper', mqtt_topics.tamperTopic)

    def registerPower(self):
        self.registerHaSensorConfig('power', mqtt_topics.ina219PowerTopic, 'W')
        self.registerHaSensorConfig(
            'voltage', mqtt_topics.ina219VoltageTopic, 'V')
        self.registerHaSensorConfig(
            'current', mqtt_topics.ina219CurrentTopic, 'A')

    def registerRpiStatus(self):
        self.registerHaSensorConfig(
            'coreTemperature', mqtt_topics.rpiCoreTempTopic, '°C')
        self.registerHaSensorConfig(
            'diskUsage', mqtt_topics.rpiDiskUsageTopic, '%')
        self.registerHaSensorConfig(
            'averageLoad', mqtt_topics.rpiAverageLoadTopic, '%')

    def registerTemperature(self):
        self.registerHaSensorConfig(
            'temperature', mqtt_topics.bme280TemperatureTopic, '°C')
        self.registerHaSensorConfig(
            'humidity', mqtt_topics.bme280HumidityTopic, '%')

    def registerHaSensorConfig(self, sensorId, topic, uom):
        stateTopic = self.mqtt.getTopicWithBase(topic)

        entityConfig = {
            'device': self.deviceConfig,
            'availability_topic': self.mqtt.getStatusTopic(),
            'unique_id': stateTopic,
            'platform': 'mqtt',
            'name': stateTopic,
            'state_topic': stateTopic,
            'unit_of_measurement': uom
        }

        self.mqtt.publish(self.getHaDiscoveryTopic(
            'sensor', sensorId), json.dumps(entityConfig), retain=True)

    def registerHaBinarySensorConfig(self, sensorId, topic):
        stateTopic = self.mqtt.getTopicWithBase(topic)

        entityConfig = {
            'device': self.deviceConfig,
            'availability_topic': self.mqtt.getStatusTopic(),
            'unique_id': stateTopic,
            'platform': 'mqtt',
            'name': stateTopic,
            'state_topic': stateTopic
        }

        self.mqtt.publish(self.getHaDiscoveryTopic(
            'binary_sensor', sensorId), json.dumps(entityConfig), retain=True)

    def getHaDiscoveryTopic(self, type, sensorId):
        return 'homeassistant/'+type+'/rpi_' + self.deviceName + '/' + sensorId+'/config'
