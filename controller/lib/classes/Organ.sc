Organ : Object {

  var <>arduinoSock, <>oscSock, <>tubes, <>brightnessTestIsOn, <>tubePauseTime, <>updater, <>sleepModeAnimator;

  *new {
    arg initParams;
    ^super.new.init(initParams);
  }

  init {
    arg initParams;
    var tube;

    this.brightnessTestIsOn = false;

    this.tubePauseTime = 0.007;
    //this.tubePauseTime = 0.0001;

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
    
    this.updater = Routine.new({
      while({true}, {
        this.update();
      })
    });
    SystemClock.play(this.updater);

  }

  updateTime {
    ^(this.tubePauseTime * this.tubes.size())
  }

  allLightsOff {
    "Organ: All lights off!".postln();
    this.tubes.do({
      arg tube;
      tube.turn_off();
    });
  }

  /**
   *  Set the hue of all the tubes at once.
   **/
  set_tubes_hue {
    arg newHue;

    var newColor;

    newColor = Color.hsv(newHue, 1.0, 1.0);

    this.tubes.do({
      arg tube;

      tube.color = newColor;
    });

  }

  update {
    //"Organ.update...".postln();
    if (this.arduinoSock != nil, {
      this.arduinoSock.putAll(Int8Array[255]);
    });

    this.tubes.do({
      arg tube;
      //("Updating tube " ++ tube.tubeIndex).postln();
      tube.update();
      this.tubePauseTime.wait();
    });

    if (this.arduinoSock != nil, {
      this.arduinoSock.putAll(Int8Array[255, 255, 255]);
    });
    //"Organ.update done".postln();
  }

  /*doPositionTest {
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


  }*/

  doSleepMode {
    var brightnessCycle,
      brightnessStream,
      brightness,
      test,
      updateTime,
      t,
      breathHue,
      breathSaturation,
      breathColor,
      duration = 16.0;

    "Organ: doSleepMode".postln();

    updateTime = this.updateTime();
    t = 0.0;
    
    brightnessCycle = Env(
      [0,   1,    1,    0,  0],
      [ 0.4,  0.1,  0.4,  0.1 ] * duration,
      \sin
      //[ 4,   8,   -8   ]
    );
    //brightnessCycle.duration = 8.0;

    this.sleepModeAnimator = Routine.new({
      brightnessStream = brightnessCycle.asStream();
      t = 0;
      this.set_tubes_hue(0.999.rand());

      while({ t <= duration }, {
        brightness = brightnessStream.next();

        this.tubes.do({
          arg tube;

          tube.brightness = brightness;

        });

        this.update();
        t = t + updateTime;
      });
    
    }).loop();

    SystemClock.play(this.sleepModeAnimator);
  }

  doTubeIndexTest {
    var i, ledName, led;

    "tubeIndexTest!".postln();

    {
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
