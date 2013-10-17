Tube {
  // Maximum brightness is smaller than 1.0 because the sequential wiring of
  // the LEDs leads to a voltage drop. Due to this, LEDs towards the end show a
  // color skew which is less visible when driving all LEDs with less
  // brightness overall.
  classvar <maxAlpha=0.8;
  // Maximum color value is 254, which is one smaller than the maximum an LED
  // would accept. This is because we declared 255 as a delimiter value.
  classvar <maxColorValue=254;

  // Users can modify 'color' directly, but 'lastColor' is only used to cache
  // values and reduce the number of updates sent.
  var <>color,
  <>lastColor,
  // Brightness of the tube. A value of zero means the tube is turned off. For
  // some reason we cannot reuse the alpha attribute of Color (setting one tube
  // affected all tubes) and instead have to use separate values to track
  // brightness. Same as with 'lastColor', 'lastAlpha' is used for caching.
  <>alpha,
  <>lastAlpha,
  // Indicate whether the current color values hvae be sent.
  <>updateSent;

  *new {
    ^super.new.init();
  }

  init {
    this.color = Color();
    // Initialize to undefined values so that a first change to off is picked
    // up as well.
    this.alpha = 0;
    this.lastAlpha = -1;
    this.lastColor = Color(-1, -1, -1, -1);
    // We start with no changes, so no sending necessary.
    this.updateSent = true;
  }

  // Get the LED values that should be sent when updating a tube. The returned
  // value is nil, if no changes have been applied (and therefore no update is
  // necessary) since the last time this method was called. Otherwisea Color
  // object is returned which's 'red', 'green', 'blue', and 'alpha'
  // (brightness) values contain the data to send.
  //
  // Values are computed based on Tube attributes red, green, blue, and
  // brightness. If doBoundaryChecks is True, values will be changed if they
  // are outside the given boundaries.
  //
  // Args:
  //   doBoundaryChecks: boolean value indicating wether values should be
  //   checked for boundary conditions (e.g., not smaller than 0).
  //
  // Returns:
  //  A Color object with red, green, blue, and alpha attributes.
  getColorForSent {
    // Boundary checks may be disabled if we trust callers.
    arg doBoundaryChecks;

    var r, g, b, alpha;

    if (doBoundaryChecks === true, {
      if (this.color.red < 0, { this.color.red = 0; });
      if (this.color.green < 0, { this.color.green = 0; });
      if (this.color.blue < 0, { this.color.blue = 0; });
      if (this.alpha < 0, { this.alpha = 0; });
      if (this.color.red > maxColorValue, { this.color.red = maxColorValue; });
      if (this.color.green > maxColorValue, { this.color.green = maxColorValue; });
      if (this.color.blue > maxColorValue, { this.color.blue = maxColorValue; });
      if (this.alpha > maxAlpha, {
        this.alpha = maxAlpha;
      });
    });

    if (this.color.red != this.lastColor.red, {
      this.lastColor.red = this.color.red;
      this.updateSent = false;
    });
    if (this.color.green != this.lastColor.green, {
      this.lastColor.green = this.color.green;
      this.updateSent = false;
    });
    if (this.color.blue != this.lastColor.blue, {
      this.lastColor.blue = this.color.blue;
      this.updateSent = false;
    });
    if ((this.alpha == 0) && (this.lastAlpha == 0), {
	  // shortcut: If the tube was turned off last cycle and is still turned
	  // off, no need to update.
      this.updateSent = true;
    }, {
	  this.lastAlpha = this.alpha;
	  this.updateSent = false;
	});

    if (this.updateSent == false, {
	  this.color.alpha = this.alpha;
	  this.updateSent = true;
      ^this.color;
    }, {
      ^nil;
    });
  }

  turn_off {
    this.alpha = 0.0;
  }

  is_on {
    ^(this.alpha != 0);
  }
}
