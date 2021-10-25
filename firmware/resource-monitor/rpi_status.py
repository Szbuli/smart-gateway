from gpiozero import CPUTemperature, DiskUsage, LoadAverage
import mqtt_topics
import stat_util


class RpiStatus:
    def __init__(self, mqtt):
        self.mqtt = mqtt
        self.cpu = CPUTemperature()
        self.disk = DiskUsage()
        self.load = LoadAverage()

        self.cpuTemperature = self.cpu.temperature
        self.diskUsage = self.disk.usage

    def read(self):
        self.cpuTemperature = stat_util.smooth(
            self.cpu.temperature, self.cpuTemperature, 0.2)
        self.diskUsage = stat_util.smooth(self.disk.usage, self.diskUsage, 0.2)

    def publish(self):
        self.mqtt.publishWithBaseTopic(mqtt_topics.rpiCoreTempTopic, str(
            round(self.cpuTemperature, 2)))
        self.mqtt.publishWithBaseTopic(mqtt_topics.rpiAverageLoadTopic, str(
            round(self.load.value * 100, 2)))
        self.mqtt.publishWithBaseTopic(mqtt_topics.rpiDiskUsageTopic,
                                       str(round(self.diskUsage, 2)))
