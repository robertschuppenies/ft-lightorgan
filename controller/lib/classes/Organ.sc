FtloOrgan {
  // Time to wait between sending tube updates. This is a limit by the Arduino
  // used (determined emperically).
  classvar <tubePauseTime = 0.001,
  // Time to wait between telling the Arduino to write a new set of
  // colors. This is a limit by the LED strip used (determined emperically).
  <messagePauseTime = 0.04;

  var <> connectors,
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
  <>updater,
  <>updaterRunning;

  *new {
    arg connectors;
    ^super.new.init(connectors);
  }

  init {
    arg connectors;
    var tube;

    this.connectors = connectors;
    this.sleepModeRunning = false;
    this.updaterRunning = false;
    this.physicalTubeIndex = [ 8, 7, 6, 5, 4, 3, 2, 1, 0, 50, 49, 48, 47, 46,
      45, 44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 9, 10, 11, 12, 13, 14,
      15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
      33 ];
    this.tubes = [];
    for(0, 50, {
      arg i;
      this.tubes = this.tubes.add(Tube());
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

  // Start continuous update cycle.
  startUpdating {
    SystemClock.play(this.updater);
    this.updaterRunning = true;
  }

  // Turn all lights off.
  turnOff {
    this.tubes.do({
      arg tube;
      tube.turnOff();
    });
    // Only explicitly update if the updater is not runing.
    if (this.updaterRunning == false, {
      this.pushUpdate();
    })
  }

  // Set the hue of all the tubes at once.
  //
  // Args:
  //   hue: A hue value in the range of [0,1].
  setallTubeHues {
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

  getDemoDuration {
    ^10.0;
  }

  startDemo {
    var startupAnimationRunner;

    startupAnimationRunner = Routine({
      51.do({
        arg i;

        this.tubes[i].color.red = 0;
        this.tubes[i].color.blue = 0;
        this.tubes[i].color.green = 1;
        this.tubes[i].alpha = 0.8;
        this.pushUpdate();
        (0.1 * this.getDemoDuration() / 25.0).wait();
      });
      this.turnOff();
    });
    SystemClock.play(startupAnimationRunner);
  }

  // TODO(schuppe): figure out what to do with sleep mode; currently it is not
  // used.
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
      this.setallTubeHues(0.999.rand());

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

}
