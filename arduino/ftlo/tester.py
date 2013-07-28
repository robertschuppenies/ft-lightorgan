#!/usr/bin/python

import optparse
import serial
import sys

USAGE = 'usage: %prog [options] unit red, green blue'

MAX_COLOR_VALUE = 255
UNITS = 51

DEFAULT_PORT = '/dev/ttyACM1'
DEFAULT_BAUD_RATE = 9600

NULL_BYTE = chr(0)


def set_color(unit, red, green, blue, port, baud_rate):
    """Set the color of a unit."""
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
    ser = serial.Serial(port, baud_rate, timeout=2)
    ser.write(NULL_BYTE + chr(unit) + chr(red) + chr(green) + chr(blue))


if __name__ == '__main__':
    parser = optparse.OptionParser(usage=USAGE)
    # parser.add_option("-u", "--unit", dest="unit", help="unit to set")
    # parser.add_option("-g", "--green", dest="green", help="green color value")
    # parser.add_option("-b", "--blue", dest="blue", help="blue color value")
    parser.add_option("-p", "--port", dest="port", help="port to use",
                      default=DEFAULT_PORT)
    parser.add_option("-r", "--rate", dest="rate", help="baud rate to use",
                      default=DEFAULT_BAUD_RATE)

    (options, args) = parser.parse_args()
    if len(args) != 4:
        print 'Error: expected exactly 4 arguments.'
        parser.print_help()
        sys.exit(1)
    set_color(int(args[0]), int(args[1]), int(args[2]), int(args[3]),
              options.port, options.rate)
