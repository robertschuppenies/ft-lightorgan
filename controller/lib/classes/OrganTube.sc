OrganTube : Object {

  // Maximum brightness is smaller than 1.0 because the sequential wiring of
  // the LEDs leads to a voltage drop. Due to this, LEDs towards the end show a
  // color skew which is less visible when driving all LEDs with less
  // brightness overall.
  classvar <maxAlpha=0.8;
  // Maximum color value is 254, which is one smaller than the maximum an LED
  // would accept. This is because we declared 255 as a delimiter value.
  classvar <maxColorValue=254;

  var <>physicalTubeIndex,
  <>tubeIndex,
  // Color of the tube.
  <>color,
  <>lastColor,
  // Brightness of the tube. A value of zero means the tube is turned off. For
  // some reason we cannot reuse the alpha attribute of Color (setting one tube
  // affected all tubes) and instead have to use separate values to track
  // brightness.
  <>alpha,
  <>lastAlpha,
  <>updateSent;

  *new {
    arg initParams;
    ^super.new.init(initParams;);
  }

  init {
    arg initParams;
    this.tubeIndex = initParams['index'].asInteger();
    this.color = Color();
    // Initialize to undefined values so that a first change to off is picked
    // up as well.
    this.alpha = 0;
    this.lastAlpha = -1;
    this.lastColor = Color(-1, -1, -1, -1);
    // We start with no changes, so no sending necessary.
    this.updateSent = true;

    // A mapping of controller LED index/arry position to physical LED
    // index. Due to how LEDs are physically wired the first LED on the board
    // is not indexed as 0. Instead, physical LED indexing starts at the
    // controller-index position 0 and then moves clockwise. The last LED has
    // the controller index 9.
    //
    // The front row as 26 (13+13) tubes, the back row has 25 (12+1+12) tubes.
    //
    // physical indices ..
    // backrow  : 9-10-11-12-                .. -31-32-33
    // front row: 8-7-6-5-4-3-2-1-0-51-50-49 .. -36-35-34
    //
    // controller indices:
    // backrow  : 26-27-28-  .. -50-51
    // front row: 0-1-2-3-4- .. -24-25
    //
    // We define this as an instance variable because creating an Array as a
    // classvar always gives me a "syntax error, unexpected CLASSNAME"
    // exception. My(schuppe) supercollider understanding is too limited, but
    // maybe it is because you cannot hvae mutable objects as classvars.
    this.physicalTubeIndex = [ 8, 7, 6, 5, 4, 3, 2, 1, 0, 50, 49, 48, 47, 46,
      45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 9, 10, 11, 12, 13, 14,
      15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
      33 ];

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
      updateSent = false;
    });
    if (this.color.green != this.lastColor.green, {
      this.lastColor.green = this.color.green;
      updateSent = false;
    });
    if (this.color.blue != this.lastColor.blue, {
      this.lastColor.blue = this.color.blue;
      updateSent = false;
    });
    if (this.alpha != this.lastAlpha, {
      this.lastAlpha = this.alpha;
      updateSent = false;
    });
    this.color.alpha = this.alpha;

    if (updateSent == false, {
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
