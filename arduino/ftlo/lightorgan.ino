#include <Adafruit_NeoPixel.h>

#define UNIT_COUNT 51
#define LEDS_PER_UNIT 4

// Size of packages read from the serial port (4: LED unit ID, red, green, blue)
#define SERIAL_PKG_SIZE 4
// Maximum time to wait for a single package to be sent.
#define SERIAL_PKG_TIMEOUT 100
// Byte value used as a delimiter between serial packages.
#define SERIAL_DELIM 255


// Parameter 1 = number of pixels in strip
// Parameter 2 = pin number (most are valid)
// Parameter 3 = pixel type flags, add together as needed:
//   NEO_RGB     Pixels are wired for RGB bitstream
//   NEO_GRB     Pixels are wired for GRB bitstream
//   NEO_KHZ400  400 KHz bitstream (e.g. FLORA pixels)
//   NEO_KHZ800  800 KHz bitstream (e.g. High Density LED strip)
Adafruit_NeoPixel strip = Adafruit_NeoPixel(UNIT_COUNT*LEDS_PER_UNIT,
                                            6, NEO_GRB + NEO_KHZ800);


/******************************************************
 * LED strip code
 ******************************************************/

void initStrip() {
  strip.begin();
  strip.show(); // Initialize all pixels to 'off'
}


// Set color
void setUnitColor(uint16_t unit, uint32_t color) {
  uint16_t offset;
  for(offset=0; offset<LEDS_PER_UNIT; offset++) {
    strip.setPixelColor(unit*LEDS_PER_UNIT+offset, color);
  }
}

/******************************************************
 * serial processing code
 ******************************************************/

void initSerial() {
  Serial.begin(115200);
  Serial.setTimeout(SERIAL_PKG_TIMEOUT);
}

void readSerialAndSetUnit() {
  uint8_t unit, red, green, blue;
  uint32_t color;
  byte null_count = 0;
  int current_byte = 0;
  boolean units_changed = false;

  // Note that we read bytes individually. If any of them are the delimiter
  // byte, skip to the next read loop (and hopefully get a full package).
  while (Serial.available()) {
    delay(1); // without this delay data won't be read correctly.
    while (true) {
      current_byte = Serial.read();
      if (current_byte == SERIAL_DELIM) {
        if ((null_count > 1) && (units_changed)) {
          strip.show();
        }
        null_count += 1;
      } else {
        break;
      }
    }
    unit = current_byte;
    red = Serial.read();
    if (red == SERIAL_DELIM) { continue; }
    green = Serial.read();
    if (green == SERIAL_DELIM) { continue; }
    blue = Serial.read();
    if (blue == SERIAL_DELIM) { continue; }

    color = strip.Color(red, green, blue);
    setUnitColor(unit, color);

    null_count = 0;
    units_changed = true;

    /* Serial.print("unit "); */
    /* Serial.print(unit); */
    /* Serial.print("; "); */
    /* Serial.print(red); */
    /* Serial.print("-"); */
    /* Serial.print(green); */
    /* Serial.print("-"); */
    /* Serial.println(blue); */
  }
}


/******************************************************
 * color demos
 ******************************************************/

// Input a value 0 to 255 to get a color value.
// The colours are a transition r - g - b - back to r.
uint32_t Wheel(byte WheelPos) {
  if(WheelPos < 85) {
   return strip.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
  } else if(WheelPos < 170) {
   WheelPos -= 85;
   return strip.Color(255 - WheelPos * 3, 0, WheelPos * 3);
  } else {
   WheelPos -= 170;
   return strip.Color(0, WheelPos * 3, 255 - WheelPos * 3);
  }
}

// Fill the dots one after the other with a color
void colorWipe(uint32_t c, uint8_t wait) {
  for(uint16_t i=0; i<UNIT_COUNT; i++) {
      setUnitColor(i, c);
      strip.show();
      delay(wait);
  }
}

// Slightly different, this makes the rainbow equally distributed throughout
void rainbowCycle(uint8_t wait) {
  uint16_t i, j;

  for(j=0; j<256; j++) {
    for(i=0; i<strip.numPixels(); i++) {
      setUnitColor(i, Wheel((i+j) & 255));
    }
    strip.show();
    delay(wait);
  }
}

void loopDemo() {
  // Some example procedures showing how to display to the pixels:
  colorWipe(strip.Color(100, 0, 0), 20); // Red
  colorWipe(strip.Color(0, 100, 0), 20); // Green
  colorWipe(strip.Color(0, 0, 100), 20); // Blue
  rainbowCycle(20);
}

void loop() {
  /* loopDemo(); */
  readSerialAndSetUnit();
}

void setup() {
  initStrip();
  initSerial();
}

