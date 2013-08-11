#!/usr/bin/python

import optparse
import serial

USAGE = 'usage: %prog [options]'

DEFAULT_PORT = '/dev/ttyACM0'
DEFAULT_BAUD_RATE = 115200


def init_and_read(port, baud_rate):
    ser = serial.Serial(port, baud_rate, timeout=2)
    while True:
        print ser.readline()


if __name__ == '__main__':
    parser = optparse.OptionParser(usage=USAGE)
    parser.add_option("-p", "--port", dest="port", help="port to use",
                      default=DEFAULT_PORT)
    parser.add_option("-a", "--rate", dest="rate", help="baud rate to use",
                      default=DEFAULT_BAUD_RATE)
    (options, args) = parser.parse_args()
    if len(args) != 0:
        print 'Error: No arguments.'
        parser.print_help()
        sys.exit(1)
    init_and_read(options.port, options.rate)

