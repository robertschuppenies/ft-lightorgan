FtloOrgan {
  // Time to wait between sending tube updates. This is a limit by the Arduino
  // used (determined emperically).
  classvar <tubePauseTime = 0.001,
  // Time to wait between telling the Arduino to write a new set of
  // colors. This is a limit by the LED strip used (determined emperically).
  <messagePauseTime = 0.04;

  var <> connectors,
  <>brightnessTestIsOn,
  // A mapping of controller LED index/array position to physical LED
  // index. Due to how LEDs are physically wired the first LED on the board is
  // not indexed as 0. Instead, physical LED indexing starts at the
  // controller-index position 0 and then moves clockwise. The last LED has the
  // controller index 9.
  //
  // The front row as 26 (13+13) tubes, the back row has 25 (12+1+12) tubes.
  //
  // physical indices ..
  // backrow  : 9-10-11-12-                .. -31-32-33
  // front row: 8-7-6-5-4-3-2-1-0-51-50-49 .. -36-35-34
  //
  // controller indices:
  // backrow  : 26-27-28-  .. -50-51
  // front row: 0-1-2-3-4- .. -24-25
  //
  // We define this as an instance variable because creating an Array as a
  // classvar always gives me a "syntax error, unexpected CLASSNAME"
  // exception. My(schuppe) supercollider understanding is too limited, but
  // maybe it is because you cannot hvae mutable objects as classvars.
  <>physicalTubeIndex,
  <>sleepModeAnimator,
  <>sleepModeRunning,
  <>tubes,
  <>updater;

  *new {
    arg connectors;
    ^super.new.init(connectors);
  }

  init {
    arg connectors;
    var tube;

    this.physicalTubeIndex = [ 8, 7, 6, 5, 4, 3, 2, 1, 0, 50, 49, 48, 47, 46,
      45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 9, 10, 11, 12, 13, 14,
      15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
      33 ];

    this.connectors = connectors;
    this.tubes = [];
    for(0, 51, {
      arg i;
      this.tubes = this.tubes.add(Tube());
    });

    this.updater = Routine.new({
      while({true}, {
        this.pushUpdate();
      })
    });

    this.brightnessTestIsOn = false;
    this.sleepModeRunning = false;
  }

  // Push an update to the organ. This function will query all tubes for their
  // color values and then send the information to light organ.
  pushUpdate {
    this.tubes.do({
      arg tube, index;
      var color, r, g, b;

      color = tube.getColorForSent(true);
      if (color != nil, {
        r = (color.alpha * color.red * 254).round().asInteger();
        g = (color.alpha * color.green * 254).round().asInteger();
        b = (color.alpha * color.blue * 254).round().asInteger();
        this.connectors.do({
          arg connector;
          connector.setTube(this.physicalTubeIndex[index], r, g, b);
        });
        tubePauseTime.wait();
      });
    });
    this.connectors.do({ arg connector; connector.flush() });
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
