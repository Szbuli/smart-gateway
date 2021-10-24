from gpiozero import CPUTemperature, DiskUsage, LoadAverage
import mqtt_topics


class RpiStatus:
    def __init__(self, mqtt):
        self.mqtt = mqtt
        self.cpu = CPUTemperature()
        self.disk = DiskUsage()
        self.load = LoadAverage()

    def read_and_publish(self):
        self.mqtt.publishWithBaseTopic(mqtt_topics.rpiCoreTempTopic, str(
            round(self.cpu.temperature, 2)))
        self.mqtt.publishWithBaseTopic(mqtt_topics.rpiAverageLoadTopic, str(
            round(self.load.value * 100, 2)))
        self.mqtt.publishWithBaseTopic(mqtt_topics.rpiDiskUsageTopic,
                                       str(round(self.disk.usage, 2)))
