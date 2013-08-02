Instr("VisualizerData", {
  arg outputBuffer, onsetDetectorBus;
  
  var in, fftChain, out, onsetDetector;

  out = Silence.ar();
  in = SoundIn.ar(0);

  fftChain = FFT(outputBuffer, in);
  //fftChain = PV_MagSmooth(outputBuffer, 0.01);

  //PV_Copy(fftChain, outputBuffer);


  /**
   *  Onset detection
   **/
  onsetDetector = Coyote.kr(
    in,
    //trackFall: 0.3
    //slowLag: 0.1,
    fastMul: 0.8,
    thresh: -18.0.dbamp(),
    minDur: 0.02
  );
  //onsetDetector = Onsets.kr(fftChain);
  onsetDetector.poll();
  ReplaceOut.kr(onsetDetectorBus, onsetDetector);

  out;
}, [
  \buffer,
  \controlbus
]);
