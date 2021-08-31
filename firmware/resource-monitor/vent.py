import board
import pulseio
import digitalio
import RPi.GPIO as GPIO
import pigpio
import signal
import sys


speed_max = 1000000
speed_medium = 750000
speed_low = 500000

class Vent:
    def __init__(self, mqtt, ventTopic):
        self.mqtt = mqtt
        self.ventTopic = ventTopic
        print("init vent")
        #GPIO.setmode(GPIO.BCM)
        #GPIO.setup(18, GPIO.OUT)
        #GPIO.output(18, GPIO.HIGH)
        #self.pwm = GPIO.PWM(18, 30000)
        #self.pwm.start(70)
        
        self.pi=pigpio.pi()
        self.pi.hardware_PWM(18, 80000, speed_medium) # 800Hz 25% dutycycle
        self.mqtt.publish(self.ventTopic, 'speed_medium')
        print("init vent")
        ##self.vent_pwm = pulseio.PWMOut(board.D12, frequency=400, duty_cycle=2 ** 15)
        signal.signal(signal.SIGINT, self.exit_gracefully)
        signal.signal(signal.SIGTERM, self.exit_gracefully)

    def exit_gracefully(self,signum, frame):
        #self.pi.stop()
       self.pi.hardware_PWM(18, 0, 0) # 800Hz 25% dutycycle
       sys.exit(0)

    def __exit__(self, exc_type, exc_value, traceback):
        
        print("closse")