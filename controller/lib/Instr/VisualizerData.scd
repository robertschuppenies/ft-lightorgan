Instr("VisualizerData", {
  arg outputBuffer, loudnessBus;
  
  var in,
    fftChain,
    out,
    onsetDetector,
    ambNoiseLevel,
    loudness,
    loudEventDetector;

  "loudnessBus:".postln;
  loudnessBus.postln;

  out = Silence.ar();
  in = SoundIn.ar(0);

  fftChain = FFT(outputBuffer, in);
  //fftChain = PV_MagSmooth(outputBuffer, 0.01);

  //PV_Copy(fftChain, outputBuffer);


  /**
   *  Onset detection
   **/
  /*onsetDetector = Coyote.kr(
    in,
    trackFall: 0.1
    //slowLag: 0.1,
    //fastMul: 0.8,
    //thresh: -18.0.dbamp(),
    //minDur: 0.9
  );
  //onsetDetector = Onsets.kr(fftChain);
  onsetDetector.poll();
  ReplaceOut.kr(onsetDetectorBus, onsetDetector);*/

  /*onsetDetector = Onsets.kr(fftChain);
  onsetDetector.poll();*/

  ambNoiseLevel = 5.0;
  loudness = Integrator.kr(Loudness.kr(fftChain), 0.999);
  Out.kr(loudnessBus, loudness);

  out;
}, [
  \buffer,
  \controlbus
]);
