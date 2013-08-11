
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

    
    organ = Organ.new((
      connectToVisualizer: false,
      address: "192.168.2.1",
      //address: "localhost",
      port: 5001,
      connectToArduino: true,
      //arduinoAddress: "/dev/tty.usbmodemfd121",
      arduinoAddress: "/dev/ttyACM1",
      arduinoBaudRate: 115200
    ));

    "connected!".postln();
    5.0.wait();

    organ.allLightsOff();

    //organ.doBrightnessTest(5.0);
    organ.doSleepMode();
    //organ.doTubeIndexTest();


    //visualizer = Visualizer.new((
      //organ: organ
    //));


  });

)
