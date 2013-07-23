(
  Env(
    [0,   1,    1,    0,  0],
    [ 0.4,  0.1,  0.4,  0.1 ],
    \sin
    //[ 4,   8,   -8   ]
  ).plot();
)

(

  var m, mBounds;

  s.quit;

  s.options.inDevice = "Built-in Input";
  /*s.options.inDevice = "PreSonus FIREPOD (2112)";*/
  /*s.options.inDevice = "SF + 1818";*/
  /*s.options.inDevice = "AudioBox 1818 VSL ";*/
  /*s.options.inDevice = "Soundflower (64ch)";*/
  /*s.options.inDevice = "H4";*/

  /*s.options.memSize = 262144; // 256 Mb*/
  /*s.options.outDevice = "Soundflower (64ch)";*/
  s.options.sampleRate = 48000;
  /*s.options.blockSize = 8;*/
  s.boot();


  /*mBounds = m.window.bounds;*/
  /*mBounds.left = 1680;
  mBounds.top = 1000;*/
  /*mBounds.left = 1440;
  mBounds.top = 900;*/
  
  /*m.window.setTopLeftBounds(mBounds);*/

  /*Instr.dir = "lib/".resolveRelative();
  Instr.loadAll();*/

  s.doWhenBooted({
    /*var outPatch;
  
    m = s.meter();
    
    outPatch = Patch("GhettoFigure8");

    "preparing".postln();

    outPatch.prepareForPlay();

    1.0.wait();

    "playing".postln();

    outPatch.play();*/


    var organ;

    /*outSock = NetAddr.new("192.168.1.110", 5001);
    outSock.sendMsg("/organ/tube", 1, "rgb/", 255, 255, 0);*/

    organ = Organ.new((
      address: "192.168.1.110",
      port: 5001
    ));

    //organ.doBrightnessTest(5.0);
    organ.doSleepMode();

  });

)
