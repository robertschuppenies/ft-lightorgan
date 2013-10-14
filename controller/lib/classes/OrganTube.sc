OrganTube : Object {

  // Maximum brightness is smaller than 1.0 because the sequential wiring of
  // the LEDs leads to a voltage drop. Due to this, LEDs towards the end show a
  // color skew which is less visible when driving all LEDs with less
  // brightness overall.
  classvar <maximumBrightness=0.8;

  var <>physicalTubeIndex,
  <>tubeIndex,
  <>color,
  <>lastSentCache,
  <>brightness,
  <>organ;

  *new {
    arg initParams;
    ^super.new.init(initParams;);
  }

  init {
    arg initParams;

    this.tubeIndex = initParams['index'].asInteger();

    this.organ = initParams['organ'];

    this.color = Color.new();
    this.brightness = 0.0;

    // set last sent color to white just so messages from first update
    // will be sent
    this.lastSentCache = (
      r: 254,
      g: 254,
      b: 254
    );

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
      33 ]; }


  update {
    var r, g, b, actualBrightness;

	// HACK(schuppe): Ignore all calls to tubes >50. This is a temporary hack
	// because somewhere in a code 52 tubes are initialized which is actually
	// not supported.
	if (this.tubeIndex <= 50, {

    actualBrightness = maximumBrightness * this.brightness;
    r = (actualBrightness * this.color.red() * 254).round().asInteger();
    g = (actualBrightness * this.color.green() * 254).round().asInteger();
    b = (actualBrightness * this.color.blue() * 254).round().asInteger();

    if ((
      this.lastSentCache['r'] != r || this.lastSentCache['g'] != g
      || this.lastSentCache['b'] != b
    ), {

      if (this.organ.emulator != nil, {
        this.organ.emulator.setTube(this.tubeIndex, r, g, b);
      });

      if (this.organ.arduino != nil, {
		this.organ.arduino.setTube(
		  this.physicalTubeIndex[this.tubeIndex], r, g, b);
      });

      this.lastSentCache['r'] = r;
      this.lastSentCache['g'] = g;
      this.lastSentCache['b'] = b;

      ^true;
    });

    ^false;
	})
  }

  turn_off {
    this.brightness = 0.0;
  }

  is_on {
    ^(this.brightness != 0);
  }

  set_brightness {
    arg aBrightness;

    if (aBrightness < 0, {
      aBrightness = 0;
    });

    this.brightness = aBrightness;
  }


}
