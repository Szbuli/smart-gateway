from gpiozero import CPUTemperature, DiskUsage, LoadAverage
import paho.mqtt.client as mqtt


class CoreTemperature:
    def __init__(self, mqtt, coreTempTopic, averageLoadTopic, diskUsageTopic):
        self.coreTempTopic = coreTempTopic
        self.averageLoadTopic = averageLoadTopic
        self.diskUsageTopic = diskUsageTopic
        self.mqtt = mqtt
        self.cpu = CPUTemperature()
        self.disk = DiskUsage()
        self.load = LoadAverage()

    def read_and_publish(self):
        self.mqtt.publish(self.coreTempTopic, str(round(self.cpu.temperature, 2)))
        self.mqtt.publish(self.averageLoadTopic, str(round(self.load.load_average, 2)))
        self.mqtt.publish(self.diskUsageTopic, str(round(self.disk.usage, 2)))
