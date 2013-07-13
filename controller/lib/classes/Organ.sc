Organ : Object {

  var <>outSock, <>tubes, <>brightnessTestIsOn;

  *new {
    arg initParams;
    ^super.new.init(initParams);
  }

  init {
    arg initParams;
    var tube;

    this.brightnessTestIsOn = false;

    this.outSock = NetAddr.new(initParams['address'], initParams['port']);

    // initialize organ tubes

    this.tubes = [];

    for(0, 50, {
      arg i;



      tube = OrganTube.new((
        index: i,
        organ: this
      ));
      this.tubes = this.tubes.add(tube);

    });


    this.update();
  }

  update {
    this.tubes.do({
      arg tube;

      tube.update();
    });
  }

  doBrightnessTest {
    arg testDuration;

    {
      this.startBrightnessTest(testDuration);
    }.fork();

    testDuration.wait();

    this.stopBrightnessTest();
  }

  startBrightnessTest {
    arg testDuration;
    var sineEnv, sineVal;

    sineEnv = Env.sine(testDuration).asStream();

    this.brightnessTestIsOn = true;

    while ({ brightnessTestIsOn }, {
      sineVal = sineEnv.next();
      
      this.tubes.do({
        arg tube;

        tube.color['r'] = sineVal;
        tube.color['g'] = sineVal;
        tube.color['b'] = sineVal;

        tube.update();

      });

      0.025.wait();
    })
  }

  stopBrightnessTest {
    this.brightnessTestIsOn = false;
  }

}
