# Code written for RaspberryPi Pico runnning MicroPython
from machine import Pin, I2C

import time

# Pins
I2C_SDA = Pin(0)
I2C_SCL = Pin(1)

# Configure I2C
i2c = I2C(0, scl=Pin(1), sda=Pin(0), freq=400000)

# Find I2C devices, report result
devices = i2c.scan()
if len(devices) == 0:
    print("FAULT - LLv3 - No I2C Device Found!")
else:
    print('SUCCESS - LLv3 - I2C Device(s) Found:', len(devices))
for device in devices:
     print("Address: ", device, " (HEX: ", hex(device), ")")

# Laser Range Finder Config.
LLv3_DEF_ADR = 0x62  # default address for Laser Rangefinder
LLv3_ACQ_CMD = 0x00
LLv3_STATUS = 0x01
LLv3_SIG_CNT_VAL = 0x02
LLv3_ACQ_CONFIG = 0x04
LLv3_DISTANCE = 0x0f
LLv3_REF_CNT_VAL = 0x12
LLv3_UNIT_ID_HIGH = 0x16
LLv3_UNIT_ID_LOW = 0x17
LLv3_I2C_ID_HIGH = 0x18
LLv3_I2C_ID_LOW = 0x19
LLv3_I2C_SEC_ADR = 0x1a
LLv3_THRESH_BYPASS = 0x1c
LLv3_I2C_CONFIG = 0x1e
LLv3_COMMAND = 0x40
LLv3_CORR_DATA = 0x52
LLv3_ACQ_SETTINGS = 0x5d

def checkBusyFlag():
    flag = True  # set busy flag to True (device is busy)
    print("STATUS - LLv3 - Reading busyFlag from: ", hex(LLv3_DEF_ADR + LLv3_STATUS))
    isBusy = i2c.readfrom_mem(LLv3_DEF_ADR, LLv3_STATUS, 1)
    # Read status of device - nth bit:
    # 0: busy flag
    # 1: reference overflow
    # 2: signal overflow
    # 3: invalid signal
    # 4: secondary return
    # 5: !error detected
    # 6: process error
    if not (int.from_bytes(isBusy, "little") & (1 << 0)):  # if 0th bit = 1
        print("STATUS - LLv3 - Device Not Ready. busyFlag Result: ", bin(int.from_bytes(isBusy, "little")))
        flag = True
    elif isBusy == 0x20:  # if 0th bit = 0 (ready) and 5th bit = 1 (no errors)
        flag = False
    else:
        return flag

def laseTarget():
    print("STATUS - LLv3 - Lasing Target...")
    distanceBytes = i2c.readfrom_mem(LLv3_DEF_ADR, (LLv3_DISTANCE | 0x80), 2)
    measuredDistance = distanceBytes[0] << 8 | distanceBytes[1]
    print("Distance in cm: ", measuredDistance)
    return measuredDistance

while True:  # main loop
    print("STATUS - LLv3 - Setting CMD Byte at: ", hex(LLv3_DEF_ADR + LLv3_ACQ_CMD))
    i2c.writeto_mem(LLv3_DEF_ADR, LLv3_ACQ_CMD, b'/x4')  # Write 0x04 to register 0x00
    busyFlag = checkBusyFlag()
    while busyFlag:
        busyFlag = checkBusyFlag()
        if not busyFlag:
            laseTarget()
    time.sleep_ms(500)