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

    this.tubePauseTime = 0.009;

    this.oscSock = nil;
    if (initParams['connectToVisualizer'], {
      this.oscSock = NetAddr.new(initParams['address'], initParams['port']);
    });

    this.arduinoSock = nil;
    if (initParams['connectToArduino'], {
      this.arduinoSock = SerialPort.new(
        initParams['arduinoAddress'],
        initParams['arduinoBaudRate']
      );
    });

    // initialize organ tubes

    this.tubes = [];

    for(0, 51, {
      arg i;
      tube = OrganTube.new((
        index: i,
        organ: this
      ));
      this.tubes = this.tubes.add(tube);

    });

  }

  updateTime {
    ^(this.tubePauseTime * this.tubes.size())
  }

  allLightsOff {
    "Organ: All lights off!".postln();
    this.tubes.do({
      arg tube;
      tube.color.red = 0;
      tube.color.green = 0;
      tube.color.blue = 0;
    });

    this.update();
  }

  update {
    if (this.arduinoSock != nil, {
      this.arduinoSock.putAll(Int8Array[255]);
    });

    this.tubes.do({
      arg tube;

      this.tubePauseTime.wait();
      //("Updating tube " ++ tube.tubeIndex).postln();
      tube.update();
    });

    if (this.arduinoSock != nil, {
      this.arduinoSock.putAll(Int8Array[255, 255, 255]);
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

          this.tubes[i].color.red = lightPositionEnv.at(relativeTubePosition);
          this.tubes[i].color.green = lightPositionEnv.at(relativeTubePosition);
          this.tubes[i].color.blue = lightPositionEnv.at(relativeTubePosition);
          this.tubes[i].update();

          0.02.wait();

        });
      
      });
      
    
    }.loop();


  }

  doSleepMode {
    var brightnessCycle,
      brightnessStream,
      val,
      test,
      updateTime,
      t,
      breathHue,
      breathSaturation,
      breathColor;

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
      breathHue = 0.999.rand();
      breathSaturation = 0.8;

      while({ t <= brightnessCycle.totalDuration() }, {
        val = brightnessStream.next();

        breathColor = Color.hsv(breathHue, breathSaturation, val);

        this.tubes.do({
          arg tube;

          tube.color = breathColor;

        });

        this.update();
        t = t + updateTime;
      });
    
    }.loop();
  }

  doTubeIndexTest {
    var i, ledName, led;

    "tubeIndexTest!".postln();

    {
      this.allLightsOff();
      i = 0;
      while({ i < this.tubes.size() }, {
        led = this.tubes[i].color.red = 1.0;
        this.update();
        i = i + 1;
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

        tube.color.red = sineVal;
        tube.color.green = sineVal;
        tube.color.blue = sineVal;

        tube.update();

      });

      0.025.wait();
    })
  }

  stopBrightnessTest {
    this.brightnessTestIsOn = false;
  }

}
