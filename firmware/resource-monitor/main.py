import time
import mqtt_manager
import logging
import configparser
from rpi_status import RpiStatus
from bme280 import BME280
from tamper import Tamper
from ads1115 import ADS1115

from timeloop import Timeloop
from datetime import timedelta

tl = Timeloop()
rpi_status: RpiStatus
bme_280: BME280
tamper: Tamper
ads1115: ADS1115


def start():
    config = configparser.ConfigParser()
    config.read('settings.ini')

    logging.basicConfig(level=logging.getLevelName(
        config['logger']['logLevel']))

    mqtt_client = mqtt_manager.init(config['mqtt']['host'],
                                    config['mqtt']['port'],
                                    config['mqtt']['username'],
                                    config['mqtt']['password'])

    if (config.has_option('rpi', 'coreTempTopic') and
        config.has_option('rpi', 'averageLoadTopic') and
        config.has_option('rpi', 'diskUsageTopic')
        ):
        global rpi_status
        rpi_status = RpiStatus(
            mqtt_client,
            config['rpi']['coreTempTopic'],
            config['rpi']['averageLoadTopic'],
            config['rpi']['diskUsageTopic'])

    if config.has_option('rpi', 'temperatureTopic') and config.has_option('rpi', 'humidityTopic'):
        global bme_280
        bme_280 = BME280(mqtt_client, config['sensors']['temperatureTopic'],
                         config['sensors']['humidityTopic'])

    if config.has_option('sensors','tamperTopic'):
        global tamper
        tamper = Tamper(mqtt_client, config['sensors']['tamperTopic'])
        tamper.start()

    if config.has_option('sensors','voltageTopic'):
        global ads1115
        ads1115 = ADS1115(mqtt_client, config['sensors']['voltageTopic'])

    tl.start()

    mqtt_manager.loop()


@tl.job(interval=timedelta(seconds=60))
def read_and_publish():
    if rpi_status != None:
        rpi_status.read_and_publish()

    if bme_280 != None:
        bme_280.read_and_publish()

    if ads1115 != None:
        ads1115.read_and_publish()


if __name__ == '__main__':
    start()
