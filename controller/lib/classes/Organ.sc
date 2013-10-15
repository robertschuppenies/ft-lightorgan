FtloOrgan {

  var <>arduino,
    <>emulator,
    <>tubes,
    <>brightnessTestIsOn,
    <>updater,
    <>sleepModeAnimator,
    <>sleepModeRunning;
  // Time to wait between sending tube updates. This is a limit by the Arduino
  // used (determined emperically).
  classvar <tubePauseTime = 0.001,
  // Time to wait between telling the Arduino to write a new set of
  // colors. This is a limit by the LED strip used (determined emperically).
  <messagePauseTime = 0.04;

  *new {
    arg initParams;
    ^super.new.init(initParams);
  }

  init {
    arg initParams;
    var tube;

    this.brightnessTestIsOn = false;

    this.emulator = nil;
    if (initParams['connectToVisualizer'], {
      "connectToVisualizer".postln();
        this.emulator = FtloEmulator.new(
            initParams['address'],
            initParams['port']);
    });

    this.arduino = nil;
    if (initParams['connectToArduino'], {
      this.arduino = FtloArduino.new(
        initParams['arduinoAddress'],
        initParams['arduinoBaudRate']);
    });

    this.sleepModeRunning = false;

    // initialize organ tubes
    this.tubes = [];
    for(0, 51, {
      arg i;
      this.tubes = this.tubes.add(Tube(i));
    });

    this.updater = Routine.new({
      while({true}, {
        this.pushUpdate();
      })
    });
  }

  // Push an update to the organ. This function will query all tubes for their
  // color values and then send the information to light organ.
  pushUpdate {
    this.tubes.do({
      arg tube;
      var color, r, g, b;

      color = tube.getColorForSent(true);
      if (color != nil, {
        r = (color.alpha * color.red * 254).round().asInteger();
        g = (color.alpha * color.green * 254).round().asInteger();
        b = (color.alpha * color.blue * 254).round().asInteger();
        if (this.emulator != nil, {
          this.emulator.setTube(tube.index, r, g, b);
        });
        if (this.arduino != nil, {
          this.arduino.setTube(
            tube.physicalTubeIndex[tube.index], r, g, b);
        });
        tubePauseTime.wait();
      });
    });

    if (this.arduino != nil, { this.arduino.flush(); });
    if (this.emulator != nil, { this.emulator.flush(); });
    messagePauseTime.wait();
  }


  start_updating {
    SystemClock.play(this.updater);
  }

  allLightsOff {
    "Organ: All lights off!".postln();
    this.tubes.do({
      arg tube;
      tube.turn_off();
    });
    this.pushUpdate();
  }

  // Set the hue of all the tubes at once.
  //
  // Args:
  //   hue: A hue value in the range of [0,1].
  set_tubes_hue {
    arg hue;
    var color;

    color = Color.hsv(hue, 1, 1);
    this.tubes.do({
      arg tube;

      tube.color.red = color.red;
      tube.color.green = color.green;
      tube.color.blue = color.blue;
    });
  }

  startup_animation_duration {
    ^10.0;
  }

  do_startup_animation {
    var startupAnimationRunner;

    startupAnimationRunner = Routine({
      26.do({
        arg i;

        this.tubes[i].color.red = 0;
        this.tubes[i].color.blue = 0;
        this.tubes[i].color.green = 1;
        this.tubes[i].alpha = 0.8;
        this.tubes[26 + i].color.red = 0;
        this.tubes[26 + i].color.blue = 0;
        this.tubes[26 + i].color.green = 1;
        this.tubes[26 + i].alpha = 0.8;

        this.pushUpdate();
        (0.5 * this.startup_animation_duration() / 25.0).wait();
      });

      3.do({
        this.tubes.do({
          arg tube;

          tube.color.red = 0;
          tube.color.blue = 0;
          tube.color.green = 1;
          tube.alpha = 1.0;
          this.pushUpdate();
        });
        0.7.wait();

        this.tubes.do({
          arg tube;

          tube.alpha = 0.0;
        });
        this.pushUpdate();
        0.3.wait();
      })
    });
    SystemClock.play(startupAnimationRunner);

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

  start_sleep_mode {
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

    updateTime = 0.1;
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

          tube.alpha = brightness;

        });
        t = t + updateTime;
        updateTime.wait();
      });
    }).loop();

    this.sleepModeRunning = true;
    SystemClock.play(this.sleepModeAnimator);
  }

  stop_sleep_mode {
    this.sleepModeAnimator.stop();
    this.sleepModeRunning = false;
  }

  doTubeIndexTest {
    var i, ledName, led;

    "tubeIndexTest!".postln();

    {
      i = 0;
      while({ i < this.tubes.size() }, {
        led = this.tubes[i].color.red = 1.0;
        this.pushUpdate();
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
        this.sentUpdate();
      });

      0.025.wait();
    })
  }

  stopBrightnessTest {
    this.brightnessTestIsOn = false;
  }

}
