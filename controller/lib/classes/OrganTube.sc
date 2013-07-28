OrganTube : Object {
  var <>tubeIndex,
    <>color,
    <>organ;

  *new {
    arg initParams;
    ^super.new.init(initParams;);
  }

  init {
    arg initParams;

    this.tubeIndex = initParams['index'].asInteger();

    this.organ = initParams['organ'];

    this.color = (
      r: 0.0,
      g: 0.0,
      b: 0.0
    )

  }

  arduinoTubeIndex {
    arg tubeIndexIn;
    var arduinoTubeIndexMap, result;

    /*arduinoTubeIndexMap = (
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
      result = tubeIndexIn + 8;
    }, {
      if (tubeIndexIn < 26, {
        result = 50 - (tubeIndexIn - 9);    
      }, {
        result = (tubeIndexIn - 26) + 9;
      });
    });
    
  }

  update {

    var r, g, b;

    r = (this.color['r'] * 254).round().asInteger();
    g = (this.color['g'] * 254).round().asInteger();
    b = (this.color['b'] * 254).round().asInteger();

    /*this.organ.oscSock.sendMsg(
      "/organ/tube",
      this.tubeIndex,
      "rgb/",
      r,
      g,
      b
    );*/

    //("(" ++ this.color['r'] ++ ", " ++ this.color['g'] ++ ", " ++ this.color['b'] ++ ")").postln();
    this.organ.arduinoSock.putAll(Int8Array[255, this.tubeIndex, r, g, b]);
  
  }
}
