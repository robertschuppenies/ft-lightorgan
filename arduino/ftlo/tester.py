#!/usr/bin/python
"""Test script for light organ LED setup."""

import optparse
import serial
import sys
import time

USAGE = 'usage: %prog [options] unit red, green blue'

UNITS = 51

DEFAULT_PORT = '/dev/ttyACM0'
DEFAULT_BAUD_RATE = 115200

# Byte value used as a delimiter between serial packages.
SERIAL_DELIM = chr(255)
# Time to wait between sending new data packages.
SERIAL_WRITE_SLEEP = 0.0007

# Number of steps from lowest to maximum brightness.
BRIGHTNESS_STEPS = 30
# Maximum possible color value (basically one below maximum possible allowed by
# LED strip since that value is used as a delimiter).
MAX_COLOR_VALUE = 254


def set_color_units(unit_values, port, baud_rate):
    """Set the color of a unit.

    Args:
      unit_values: A list of 4-value tuples (unit number, red, green, blue).
      port: That port to send data on.
      baud_rate: The baud rate to use.
    """
    ser = serial.Serial(port, baud_rate, timeout=2)
    # Flush out old data.
    ser.write(SERIAL_DELIM*4)
    time.sleep(0.05)
    for unit_value in unit_values:
        unit = unit_value[0]
        red = unit_value[1]
        green = unit_value[2]
        blue = unit_value[3]
        if (unit < 0) or (unit > UNITS):
            msg = '"unit" value must be between 0 and %s, not %s' % (UNITS, unit)
            raise ValueError(msg)
        if red < 0 or red > MAX_COLOR_VALUE:
            raise ValueError('"red" value must be between 0 and %s, not %s' %
                             (MAX_COLOR_VALUE, red))
        if green < 0 or green > MAX_COLOR_VALUE:
            raise ValueError('"green" value must be between 0 and %s, not %s' %
                             (MAX_COLOR_VALUE, green))
        if blue < 0 or blue > MAX_COLOR_VALUE:
            raise ValueError('"blue" value must be between 0 and %s, not %s' %
                             (MAX_COLOR_VALUE, blue))
        time.sleep(SERIAL_WRITE_SLEEP)
        ser.write(SERIAL_DELIM)
        # print unit, red, green, blue
        ser.write(chr(unit) + chr(red) + chr(green) + chr(blue))
    ser.write(SERIAL_DELIM*2)


def rotate_color_brightness():
    """Rotate brightness in red, green, blue, and white."""
    color = 0
    step_size = 254/BRIGHTNESS_STEPS
    color_steps = range(BRIGHTNESS_STEPS, 0, -1)
    color_steps += range(BRIGHTNESS_STEPS)
    while True:
        red = green = blue = 0
        for color_step in color_steps:
            if color in [0, 3]:
                red = max(0, 254-color_step*step_size)
            if color in [1, 3]:
                green = max(0, 254-color_step*step_size)
            if color in [2, 3]:
                blue = max(0, 254-color_step*step_size)
            unit_values = []
            for unit in range(0, UNITS, 1):
                unit_values.append([unit, red, green, blue])
            set_color_units(unit_values, options.port, options.rate)
        if color == 3:
            color = 0
        else:
            color += 1


if __name__ == '__main__':
    parser = optparse.OptionParser(usage=USAGE)
    parser.add_option("-u", "--unit", dest="unit", help="unit to set")
    parser.add_option("-r", "--red", dest="red", help="red color value",
                      default=0)
    parser.add_option("-g", "--green", dest="green", help="green color value",
                      default=0)
    parser.add_option("-b", "--blue", dest="blue", help="blue color value",
                      default=0)
    parser.add_option("-p", "--port", dest="port", help="port to use",
                      default=DEFAULT_PORT)
    parser.add_option("-a", "--rate", dest="rate", help="baud rate to use",
                      default=DEFAULT_BAUD_RATE)

    (options, args) = parser.parse_args()
    if len(args) != 0:
        print 'Error: No arguments.'
        parser.print_help()
        sys.exit(1)

    if options.unit:
        # If user has declared a unit, set only that unit.
        set_color(options.unit, options.red, options.green, options.blue,
                  options.port, options.rate)
    else:
        # Otherwise run color demo.
        rotate_color_brightness()
