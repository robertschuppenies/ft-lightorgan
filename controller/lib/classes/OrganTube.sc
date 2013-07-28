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

  /*arduinoTubeIndex {
    arg tubeIndexIn;
    var arduinoTubeIndexOut;

    if (tubeIndexIn % 2 == 0, {
      arduinoTubeIndexOut
    });
    
  }*/

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
