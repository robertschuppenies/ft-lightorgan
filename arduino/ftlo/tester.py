#!/usr/bin/python
"""Test script for light organ LED setup."""

import datetime
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
SERIAL_UNIT_WRITE_SLEEP = 0.005
# Time to wait between sending new data packages.
SERIAL_FLUSH_SLEEP = 0.04

# Number of steps from lowest to maximum brightness.
BRIGHTNESS_STEPS = 30
# Maximum possible color value (basically one below maximum possible allowed by
# LED strip since that value is used as a delimiter).
MAX_COLOR_VALUE = 254

MODES = ['hsv', 'hsv_chaser', 'rgb']
DEFAULT_MODE = MODES[0]

MAX_DISTANCE_TO_MIDDLE = 12

class XAxis(object):
    LEFT = -1
    MIDDLE = 0
    RIGHT = 1


class ZAxis(object):
    FRONT = 0
    BACK = 1


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
    time.sleep(SERIAL_UNIT_WRITE_SLEEP)
    ser.write(SERIAL_DELIM)
    ser.write(chr(unit) + chr(red) + chr(green) + chr(blue))
    # print unit, red, green, blue


def _send_set_color_set():
    # Flush out old data.
    ser.write(SERIAL_DELIM*6)
    time.sleep(SERIAL_FLUSH_SLEEP)


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


def _get_coordinate(unit):
    if 0 <= unit <= 8:
        return (XAxis.LEFT, ZAxsis.FRONT)
    elif unit < 21:
        return (XAxis.LEFT, ZAxsis.BACK)
    elif unit == 21:
        return (XAxis.MIDDLE, ZAxsis.BACK)
    elif unit <= 34:
        return (XAxis.RIGHT, ZAxsis.BACK)
    elif unit <= 47:
        return (XAxis.RIGHT, ZAxsis.FRONT)
    elif unit <= 51:
        return (XAxis.LEFT, ZAxsis.FRONT)
    else:
        msg = 'given unit could not be mapped to coordinates: %s' % unit
        raise ValueError(msg)


def _get_distance_to_middle(unit):
    if 0 <= unit <= 8:
        return unit + 4
    elif unit < 21:
        return 21 - unit
    elif unit == 21:
        return 0
    elif unit <= 34:
        return unit - 22
    elif unit <= 47:
        return 47 - unit
    elif unit <= 51:
        return math.fabs(unit - 46)
    else:
        msg = 'given unit could not be mapped to coordinates: %s' % unit
        raise ValueError(msg)



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


def rotate_hsv(ser, step_sleep=0.1, hue_step_size=3, value=0.4, saturation=1,
               stop_after=0):
    """Rotate HSV (hue, saturation, value) color mapping.

    Args:
      ser: An initialized serial.Serial object.
    """
    hue = 0
    time_started = datetime.datetime.now()
    while True:
        if hue < 360:
            hue += hue_step_size
        else:
            hue = 0
        unit_values = []
        for unit in range(0, UNITS):
            r, g, b = _hsv_to_rgb(hue, saturation, value)
            unit_values.append([unit, r, g, b])
        set_color_units(ser, unit_values)
        time.sleep(step_sleep)
        _send_set_color_set()
        if stop_after:
            time_passed = (datetime.datetime.now() - time_started)
            if (time_passed.seconds > stop_after):
                return


def rotate_hsv_centered(ser, min_radius=1, max_radius=15, step_sleep=0.1,
                        hue_step_size=2, value=0.7, saturation=1,
                        stop_after=0):
    """Rotate HSV (hue, saturation, value) color mapping.

    Args:
      ser: An initialized serial.Serial object.
    """
    hue = 0
    ring_count = max_radius - min_radius
    intensity_grows = True
    intensity = 0
    growth_speed = 0.008
    time_started = datetime.datetime.now()
    while True:
        if hue < 360:
            hue += hue_step_size
        else:
            hue = 0
        if intensity_grows:
            intensity += growth_speed
            if intensity >= 1:
                intensity_grows = False
        else:
            intensity -= growth_speed
            if intensity <= 0:
                intensity_grows = True
        unit_values = []
        for unit in range(0, UNITS):
            distance = float(_get_distance_to_middle(unit))
            if distance <= min_radius:
                local_value = value
            elif distance > max_radius:
                local_value = 0
            else:
                # In addition to growth speed we slow it we take the current
                # intensity into account which slows down the
                # expansion/contraction nicely.
                speed_multiplier = max(0.001, intensity)
                # local_diff is the difference in brightness from the maximum
                # value of brightness the tubes inside the minimum radius have.
                local_diff = (100/ring_count*distance/speed_multiplier)/100
                local_value = value*(1-local_diff)
            local_value = local_value*intensity
            # Make sure we pass on a well-defined value.
            if local_value > 1:
                local_value = 1
            elif local_value < 0:
                local_value = 0
            r, g, b = _hsv_to_rgb(hue, saturation, local_value)
            unit_values.append([unit, r, g, b])
        set_color_units(ser, unit_values)
        time.sleep(step_sleep)
        _send_set_color_set()
        if stop_after:
            time_passed = (datetime.datetime.now() - time_started)
            if (time_passed.seconds > stop_after):
                return


def hsv_chaser(ser, hue_step_size=3, value=1.0, saturation=0.8, max_beacons=5,
               stop_after=0):
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
    time_started = datetime.datetime.now()
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
        time.sleep(0.000001)
        _send_set_color_set()
        if stop_after:
            time_passed = (datetime.datetime.now() - time_started)
            if (time_passed.seconds > stop_after):
                return


def twister(ser):
    pass


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
        print('Error: No arguments.')
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
        elif options.mode == 'hsv_chaser':
            hsv_chaser(ser)
        elif options.mode == 'hsv_centered':
            rotate_hsv_centered(ser)
        elif options.mode == 'all':
            while True:
                rotate_hsv(ser, stop_after=5*60)
                rotate_hsv_centered(ser, stop_after=5*60)
                hsv_chaser(ser, stop_after=2*60)
        else:
            rotate_color_brightness(ser)
