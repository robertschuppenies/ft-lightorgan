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

  doSleepMode {
    var brightnessCycle, brightnessStream, val, test, updateTime, t;

    updateTime = 0.05;
    t = 0.0;
    
    brightnessCycle = Env(
      [0,   1,    1,    0,  0],
      [ 0.4,  0.1,  0.4,  0.1 ],
      \sin
      //[ 4,   8,   -8   ]
    );
    brightnessCycle.duration = 8.0;

    {
      brightnessStream = brightnessCycle.asStream();
      t = 0;

      while({ t <= brightnessCycle.totalDuration() }, {
        val = brightnessStream.next();

        this.tubes.do({
          arg tube;

          tube.color['r'] = val;
          tube.color['g'] = val;
          tube.color['b'] = val;

          tube.update();

        });
        
        updateTime.wait();
        t = t + updateTime;
      });
    
    }.loop();
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
