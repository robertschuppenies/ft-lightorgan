OrganTube : Object {
  var <>tubeIndex,
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
  }

  arduinoTubeIndex {
    arg tubeIndexIn;
    var arduinoTubeIndexMap, result;

    /*
    map = [0, 2, 3, 4]
    arduinoTubeIndexMap = (
      0: 8,
      1: 7,
      2: 6,
      3: 5,
      4: 4,
      5: 3,
      6: 2,
      7: 1,
      8: 0,
      9: 50,
      10: 49,
      11: 48,
      12: 47,
      13: 46,
      14: 45,
      15: 44,
      16: 43,
      17: 42,
      18: 41,
      ...
      26: 9,
      27: 10,
      28: 11,
      29: 12

    );*/

    if (tubeIndexIn < 9, {
      result = 8 - tubeIndexIn;
    }, {
      if (tubeIndexIn < 26, {
        result = 50 - (tubeIndexIn - 9);    
      }, {
        result = (tubeIndexIn - 26) + 9;
      });
    });

    ^result;
    
  }

  update {

    var r, g, b;

    r = (this.brightness * this.color.red() * 254).round().asInteger();
    g = (this.brightness * this.color.green() * 254).round().asInteger();
    b = (this.brightness * this.color.blue() * 254).round().asInteger();

    if ((
      this.lastSentCache['r'] != r || this.lastSentCache['g'] != g
      || this.lastSentCache['b'] != b
    ), {

      if (this.organ.oscSock != nil, {
        this.organ.oscSock.sendMsg(
          "/organ/tube",
          this.tubeIndex,
          "rgb/",
          r,
          g,
          b
        );
      });

      if (this.organ.arduinoSock != nil, {
        this.organ.arduinoSock.putAll(Int8Array[this.arduinoTubeIndex(this.tubeIndex), r, g, b]);
      });

      this.lastSentCache['r'] = r;
      this.lastSentCache['g'] = g;
      this.lastSentCache['b'] = b;

      ^true;
    });

    ^false;
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
