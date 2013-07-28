Visualizer : Object {
  *new {
    arg initParams;
    ^super.new.init(initParams);
  }

  init {

    arg initParams;

    var inputPatch, visualizerDataBuf, fftSize = 1024;

    visualizerDataBuf = Buffer.alloc(Server.default, fftSize);

    inputPatch = Patch(Instr.at("VisualizerData"), (
      outputBuffer: visualizerDataBuf
    ));

    0.5.wait();

    inputPatch.play();

    0.5.wait();

    {

      while({ true }, {
        
        visualizerDataBuf.getn(0, fftSize, {
          arg data;
          var normalizedData = data.normalize(),
            z, x;

          z = data.clump(2).flop;
          z = [Signal.newFrom(z[0]), Signal.newFrom(z[1])];
          x = Complex(z[0], z[1]);
          {x.magnitude.plot}.defer();
        });

        0.5.wait();
      
      });
    
    }.loop();

  
  }


}
