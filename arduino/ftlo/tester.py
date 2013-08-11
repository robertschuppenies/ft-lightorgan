#!/usr/bin/python
"""Test script for light organ LED setup."""

import math
import optparse
import serial
import sys
import time

USAGE = 'usage: %prog [options]'

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

MODES = ['hsv', 'rgb']
DEFAULT_MODE = MODES[0]


def _init_serial(port, baud_rate):
    """Initialize serial connection.

    Args:
      port: That port to send data on.
      baud_rate: The baud rate to use.

    Returns:
      An initialized serial.Serial object.
    """
    return serial.Serial(port, baud_rate, timeout=2)


def _send_unit_data(unit, red, green, blue):
    time.sleep(SERIAL_WRITE_SLEEP)
    ser.write(SERIAL_DELIM)
    # print unit, red, green, blue
    ser.write(chr(unit) + chr(red) + chr(green) + chr(blue))


def _send_set_color_set():
    # Flush out old data.
    ser.write(SERIAL_DELIM*6)
    time.sleep(0.05)


def set_color_units(ser, unit_values):
    """Set the color of a unit.

    Args:
      ser: An initialized serial.Serial object.
      unit_values: A list of 4-value tuples (unit number, red, green, blue).
    """
    for unit_value in unit_values:
        unit = unit_value[0]
        red = unit_value[1]
        green = unit_value[2]
        blue = unit_value[3]
        if (unit < 0) or (unit > UNITS):
            raise ValueError('"unit" value must be between 0 and %s, not %s' %
                             (UNITS, unit))
        if red < 0 or red > MAX_COLOR_VALUE:
            raise ValueError('"red" value must be between 0 and %s, not %s' %
                             (MAX_COLOR_VALUE, red))
        if green < 0 or green > MAX_COLOR_VALUE:
            raise ValueError('"green" value must be between 0 and %s, not %s' %
                             (MAX_COLOR_VALUE, green))
        if blue < 0 or blue > MAX_COLOR_VALUE:
            raise ValueError('"blue" value must be between 0 and %s, not %s' %
                             (MAX_COLOR_VALUE, blue))
        _send_unit_data(unit, red, green, blue)
    _send_set_color_set()


def rotate_color_brightness(ser):
    """Rotate brightness in red, green, blue, and white.

    Args:
      ser: An initialized serial.Serial object.
    """
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
            for unit in range(0, UNITS):
                unit_values.append([unit, red, green, blue])
            set_color_units(ser, unit_values)
        if color == 3:
            color = 0
        else:
            color += 1


def _hsv_to_rgb(hue, saturation, value):
    """Convert HSV to RGB tuples."""
    chroma = value * saturation
    m = value - chroma
    h1 = hue / 60.0
    x = chroma*(1-math.fabs((h1 % 2) - 1))
    if 0 <= h1 <= 1:
        r1, g1, b1 = chroma, x, 0
    elif 1 <= h1 <= 2:
        r1, g1, b1 = x, chroma, 0
    elif 2 <= h1 <= 3:
        r1, g1, b1 = 0, chroma, x
    elif 3 <= h1 <= 4:
        r1, g1, b1 = 0, x, chroma
    elif 4 <= h1 <= 5:
        r1, g1, b1 = x, 0, chroma
    elif 5 <= h1 <= 6:
        r1, g1, b1 = chroma, 0, x
    r, g, b = r1+m, g1+m, b1+m
    # At this point the RGB values are on a [0,1] scale. We need to map this to
    # our color range [0,244].
    return int(244*r), int(244*g), int(244*b)


def rotate_hsv(ser, hue_step_size=10, value=1.0, saturation=0.8,
               max_beacons=3):
    """Rotate HSV (hue, saturation, value) color mapping.

    Args:
      ser: An initialized serial.Serial object.
      max_beacons: Maximum number of bright beacons shown at the same time.
    """
    beacons = 1
    hue = 0
    # Offset indicating the distance from the first LED unit to the current
    # position of the beacon.
    beacon_offset = 0
    while True:
        if beacons > max_beacons:
            beacons = 1
        if hue < 360:
            hue += hue_step_size
        else:
            hue = 0
            beacons += 1
        if beacon_offset < UNITS:
            beacon_offset += 1
        else:
            beacon_offset = 0
        unit_values = []
        for unit in range(0, UNITS):
            # denominator used to compute the brightness of a particular
            # unit. We base this on the distance of the unit from the current
            # beacon and the number of beacons to show.
            d = ((unit + beacon_offset) % (UNITS/beacons)) or 999999
            unit_value = value / d
            r, g, b = _hsv_to_rgb(hue, saturation, unit_value)
            unit_values.append([unit, r, g, b])
        set_color_units(ser, unit_values)
        time.sleep(0.05)
    _send_set_color_set()


if __name__ == '__main__':
    parser = optparse.OptionParser(usage=USAGE)
    parser.add_option("-u", "--unit", dest="unit", help="unit to set",
                      type="int")
    parser.add_option("-r", "--red", dest="red", help="red color value",
                      default=0, type="int")
    parser.add_option("-g", "--green", dest="green", help="green color value",
                      default=0, type="int")
    parser.add_option("-b", "--blue", dest="blue", help="blue color value",
                      default=0, type="int")
    parser.add_option("-m", "--mode", dest="mode", help="demo mode name",
                      default=DEFAULT_MODE)
    parser.add_option("-p", "--port", dest="port", help="port to use",
                      default=DEFAULT_PORT)
    parser.add_option("-a", "--rate", dest="rate", help="baud rate to use",
                      default=DEFAULT_BAUD_RATE)

    (options, args) = parser.parse_args()
    if len(args) != 0:
        print 'Error: No arguments.'
        parser.print_help()
        sys.exit(1)

    ser = _init_serial(options.port, options.rate)
    if options.unit:
        # If user has declared a unit, set only that unit.
        unit_values = [[options.unit, options.red, options.green, options.blue]]
        set_color_units(ser, unit_values)
    else:
        if options.mode == 'hsv':
            rotate_hsv(ser)
        else:
            rotate_color_brightness(ser)
