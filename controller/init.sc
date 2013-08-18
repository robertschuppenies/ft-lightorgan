
/*(
  ~arduinoPort = SerialPort.new("/dev/tty.usbmodemfd121", 115200);
)*/

/*(
  var command, i = 0;

  {

    while({ true }, {
     
      command = Int8Array[255, i, 254.rand(), 254.rand(), 254.rand()];

      ~arduinoPort.putAll(command);

      i = i + 1;

      if (i > 50, {
        i = 0;    
      });

      0.007.wait();
    
    });

    
  }.fork();


)

(
  var command;

  {

    while({true}, {
      "blink on".postln();
      
      command = Int8Array[0, 1, 0, 0, 0];

      ~arduinoPort.putAll(command);

      1.0.wait();

      "blink off".postln();

      command = Int8Array[0, 1, 100, 100, 100];

      ~arduinoPort.putAll(command);

      1.0.wait();
    
    });

    
  }.fork();


)


(
  Env(
    [0,   1,    1,    0,  0],
    [ 0.4,  0.1,  0.4,  0.1 ],
    \sin
    //[ 4,   8,   -8   ]
  ).plot();
)

(
  Quarks.gui();
)*/

(

  var m, mBounds;
  SerialPort.listDevices();

  s.quit;

  //s.options.inDevice = "Built-in Microphone";
  //s.options.inDevice = "Line In";
  //s.options.inDevice = "JackRouter";
  /*s.options.inDevice = "PreSonus FIREPOD (2112)";*/
  /*s.options.inDevice = "SF + 1818";*/
  /*s.options.inDevice = "AudioBox 1818 VSL ";*/
  /*s.options.inDevice = "Soundflower (64ch)";*/
  /*s.options.inDevice = "H4";*/

  s.options.memSize = 4096;
  /*s.options.outDevice = "Soundflower (64ch)";*/
  //s.options.sampleRate = 48000;
  /*s.options.blockSize = 8;*/
  s.boot();

  //m = s.meter();


  /*mBounds = m.window.bounds;*/
  /*mBounds.left = 1680;
  mBounds.top = 1000;*/
  /*mBounds.left = 1440;
  mBounds.top = 900;*/
  
  /*m.window.setTopLeftBounds(mBounds);*/

  Instr.dir = "lib/".resolveRelative();
  Instr.loadAll();

  s.doWhenBooted({
    /*var outPatch;
  
    
    outPatch = Patch("GhettoFigure8");

    "preparing".postln();

    outPatch.prepareForPlay();

    1.0.wait();

    "playing".postln();

    outPatch.play();*/


    var organ, visualizer;

    //m = s.meter();
    /*outSock = NetAddr.new("192.168.1.110", 5001);
    outSock.sendMsg("/organ/tube", 1, "rgb/", 255, 255, 0);*/

    /*~testRoutine = Routine({
      //var testPort = SerialPort.new("/dev/ttyACM1", 115200),
      var testPort = SerialPort.new("/dev/tty.usbmodemfd121", 115200),
        r = 0,
        g = 254,
        b = 0;


      [>51.do({
        arg i;

        testPort.putAll(Int8Array[255]);
        testPort.putAll(Int8Array[i, r, g, b]);
        0.2.wait();
      });<]

      1.0.wait();
      "writing!".postln();

      testPort.putAll(Int8Array[255]);
      testPort.putAll(Int8Array[25, r, g, b]);
      0.2.wait();
      [>testPort.putAll(Int8Array[255]);
      testPort.putAll(Int8Array[26, r, g, b]);
      0.2.wait();<]
      "sending delimiter".postln();
      0.25.wait();
      testPort.putAll(Int8Array[255, 255, 255, 255, 255, 255]);
      0.25.wait();

      2.do({
        arg testi;
        b = 254.rand();
        g = 254.rand();
        r = 0;

        ("running test " ++ testi).postln();
        51.do({
          arg i;

          testPort.putAll(Int8Array[255]);
          testPort.putAll(Int8Array[i, r, g, b]);
          0.01.wait();
        });

        testPort.putAll(Int8Array[255, 255, 255, 255, 255, 255]);
        0.25.wait();
      });
    });*/
    //SystemClock.play(~testRoutine);


    
    organ = Organ.new((
      connectToVisualizer: true,
      //address: "192.168.2.1",
      address: "localhost",
      //port: 5001,
      port: 5678,
      connectToArduino: false,
      arduinoAddress: "/dev/tty.usbmodemfd121",
      //arduinoAddress: "/dev/ttyACM1",
      arduinoBaudRate: 115200
    ));
    visualizer = Visualizer.new((
      organ: organ
    ));

    "organ.start_updating".postln();
    organ.start_updating();

    2.0.wait();

    organ.do_startup_animation();

    organ.startup_animation_duration().wait();

    organ.allLightsOff();

    visualizer.start_sleep_monitor();
    visualizer.start_visualizer();
    //organ.start_sleep_mode();

  });

)
