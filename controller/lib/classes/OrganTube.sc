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

    this.tubeIndex = initParams['index'];

    this.organ = initParams['organ'];

    this.color = (
      r: 0.0,
      g: 0.0,
      b: 0.0
    )

  }

  update {

    var r, g, b;

    r = (this.color['r'] * 255).round().asInteger();
    g = (this.color['g'] * 255).round().asInteger();
    b = (this.color['b'] * 255).round().asInteger();

    this.organ.outSock.sendMsg(
      "/organ/tube",
      this.tubeIndex,
      "rgb/",
      r,
      g,
      b
    );
  
  }
}
