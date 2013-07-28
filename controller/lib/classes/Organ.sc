Organ : Object {

  var <>arduinoSock, <>oscSock, <>tubes, <>brightnessTestIsOn, <>tubePauseTime;

  *new {
    arg initParams;
    ^super.new.init(initParams);
  }

  init {
    arg initParams;
    var tube;

    this.brightnessTestIsOn = false;

    this.tubePauseTime = 0.007;

    this.oscSock = NetAddr.new(initParams['address'], initParams['port']);
    this.arduinoSock = SerialPort.new(initParams['arduinoAddress'], initParams['arduinoBaudRate']);

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


    "Organ: Initial update...".postln();
    this.update();
  }

  updateTime {
    ^(this.tubePauseTime * this.tubes.size())
  }

  update {
    this.tubes.do({
      arg tube;

      this.tubePauseTime.wait();
      //("Updating tube " ++ tube.tubeIndex).postln();
      tube.update();
    });
  }

  doPositionTest {
    var lightPositionEnv,
      numSideTubes = 5,
      centerTubeIndex,
      leftTubeIndex,
      rightTubeIndex,
      tubeSpan = numSideTubes * 2,
      offset = 0.0;

    lightPositionEnv = Env(
      [0,   1,    0],
      [ 0.5,  0.5],
      \sin
    );

    centerTubeIndex = 10;

    // animate to the left
    {
      while({true}, {

        offset = offset + 0.009;
        lightPositionEnv.offset = offset;

        // once offset is an entire tube length, we can reset the center
        if (offset >= (1.0/tubeSpan), {
          offset = 0.0;
          centerTubeIndex = centerTubeIndex + 1;
        });

        leftTubeIndex = centerTubeIndex - numSideTubes;
        rightTubeIndex = centerTubeIndex + numSideTubes;
        for(leftTubeIndex, rightTubeIndex, {
          arg i;

          var relativeTubePosition;

          relativeTubePosition = (i - leftTubeIndex) / tubeSpan;

          this.tubes[i].color['r'] = lightPositionEnv.at(relativeTubePosition);
          this.tubes[i].color['g'] = lightPositionEnv.at(relativeTubePosition);
          this.tubes[i].color['b'] = lightPositionEnv.at(relativeTubePosition);
          this.tubes[i].update();

          0.02.wait();

        });
      
      });
      
    
    }.loop();


  }

  doSleepMode {
    var brightnessCycle, brightnessStream, val, test, updateTime, t;

    "Organ: doSleepMode".postln();

    updateTime = this.updateTime();
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

        });

        this.update();
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
