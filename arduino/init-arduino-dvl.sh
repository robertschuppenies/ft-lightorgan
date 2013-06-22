# Location of this script.
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

ARDUINO_MAKEFILE_DIR="$SCRIPT_DIR/tools/Arduino-Makefile-0.12.0"
export ARDUINO_DIR="$SCRIPT_DIR/tools/arduino-1.0.4"
# Location of Arduino.mk install. Required for ard-parse-boards invocation
export ARDMK_DIR="$ARDUINO_MAKEFILE_DIR"
# AV comes from system tools.
export AVR_TOOLS_DIR="/usr"
# Location of mk file for usage in build scripts (include $(ARDUINO_MK))
export ARDUINO_MAKE_MK="$ARDUINO_MAKEFILE_DIR/arduino-mk/Arduino.mk"
export ARDUINO_MAKE_PORT="/dev/ttyACM*"
