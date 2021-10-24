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

    def registerSelfConnectivity(self):
        self.registerHaBinarySensorConfig(
            'status', mqtt_topics.statusTopic, 'connectivity', useAvailabilityTopic=False,
            payloadOn='online', payloadOff='offline')

    def registerTamper(self):
        self.registerHaBinarySensorConfig(
            'tamper', mqtt_topics.tamperTopic, 'lock')

    def registerPower(self):
        self.registerHaSensorConfig(
            'power', mqtt_topics.ina219PowerTopic, 'W', 'power')
        self.registerHaSensorConfig(
            'voltage', mqtt_topics.ina219VoltageTopic, 'V', 'voltage')
        self.registerHaSensorConfig(
            'current', mqtt_topics.ina219CurrentTopic, 'A', 'current')

    def registerRpiStatus(self):
        self.registerHaSensorConfig(
            'coreTemperature', mqtt_topics.rpiCoreTempTopic, '°C', 'temperature')
        self.registerHaSensorConfig(
            'diskUsage', mqtt_topics.rpiDiskUsageTopic, '%')
        self.registerHaSensorConfig(
            'averageLoad', mqtt_topics.rpiAverageLoadTopic, '%')

    def registerTemperature(self):
        self.registerHaSensorConfig(
            'temperature', mqtt_topics.bme280TemperatureTopic, '°C', 'temperature')
        self.registerHaSensorConfig(
            'humidity', mqtt_topics.bme280HumidityTopic, '%', 'humidity')

    def registerHaSensorConfig(self, sensorId, topic, uom, deviceClass=None):
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

        if deviceClass is not None:
            entityConfig['device_class'] = deviceClass

        self.mqtt.publish(self.getHaDiscoveryTopic(
            'sensor', sensorId), json.dumps(entityConfig), retain=True)

    def registerHaBinarySensorConfig(self, sensorId, topic, deviceClass=None,
                                     useAvailabilityTopic=True, payloadOn=None, payloadOff=None):
        stateTopic = self.mqtt.getTopicWithBase(topic)

        entityConfig = {
            'device': self.deviceConfig,
            'unique_id': stateTopic,
            'platform': 'mqtt',
            'name': stateTopic,
            'state_topic': stateTopic
        }

        if useAvailabilityTopic:
            entityConfig['availability_topic'] = self.mqtt.getStatusTopic()

        if deviceClass is not None:
            entityConfig['device_class'] = deviceClass

        if payloadOff is not None:
            entityConfig['payload_off'] = payloadOff
        if payloadOn is not None:
            entityConfig['payload_on'] = payloadOn

        self.mqtt.publish(self.getHaDiscoveryTopic(
            'binary_sensor', sensorId), json.dumps(entityConfig), retain=True)

    def getHaDiscoveryTopic(self, type, sensorId):
        return 'homeassistant/' + type + '/resource-monitor_' + self.deviceName + '/' + sensorId + '/config'
