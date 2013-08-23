#!/usr/bin/env python
# encoding: utf-8

import liblo, sys, serial, optparse

DEFAULT_PORT = '/dev/ttyACM0'
DEFAULT_BAUD_RATE = 115200
USAGE = 'usage: %prog [options]'
SERIAL_DELIM = chr(255)

arduinoPort = None

def _init_serial(port, baud_rate):
    """Initialize serial connection.

    Args:
      port: That port to send data on.
      baud_rate: The baud rate to use.

    Returns:
      An initialized serial.Serial object.
    """
    return serial.Serial(port, baud_rate, timeout=2)

# create a server, listening on port 1234
try:
    server = liblo.Server(5678)
except liblo.ServerError, err:
    print str(err)
    sys.exit()

def organ_tube_callback(path, args):
    """Called when a message is received to set an organ tube

    :r: @todo
    :g: @todo
    :b: @todo
    :returns: @todo

    """
    tubeIndex, r, g, b = args
    #print("tube[{0}]: ({1}, {2}, {3})".format(tubeIndex, r, g, b))
    arduinoPort.write(SERIAL_DELIM)
    arduinoPort.write(chr(tubeIndex) + chr(r) + chr(g) + chr(b))

def organ_flush_callback(path):
    """Called when organ is to render lights

    :path: @todo
    :returns: @todo

    """
    arduinoPort.write(SERIAL_DELIM*6)

def fallback(path, args, types, src):
    """@todo: Docstring for fallback

    :path: @todo
    :args: @todo
    :types: @todo
    :src: @todo
    :returns: @todo

    """
    print "got unknown message '%s' from '%s'" % (path, src.get_url())
    for a, t in zip(args, types):
        print "argument of type '%s': %s" % (t, a)
    


if __name__ == '__main__':
    parser = optparse.OptionParser(usage=USAGE)
    parser.add_option("-p", "--port", dest="port", help="port to use",
                      default=DEFAULT_PORT)
    parser.add_option("-a", "--rate", dest="rate", help="baud rate to use",
                      default=DEFAULT_BAUD_RATE)

    (options, args) = parser.parse_args()
    if len(args) != 0:
        print('Error: No arguments.')
        parser.print_help()
        sys.exit(1)

    arduinoPort = _init_serial(options.port, options.rate)
    server.add_method("/organ/tube", "iiii", organ_tube_callback)
    server.add_method("/organ/flush", None, organ_flush_callback)

    server.add_method(None, None, fallback)

    while True:
        server.recv(500)
