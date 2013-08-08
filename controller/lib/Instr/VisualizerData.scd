Instr("VisualizerData", {
  arg outputBuffer, loudnessBus;
  
  var in,
    fftChain,
    out,
    onsetDetector,
    ambNoiseLevel,
    loudness,
    loudEventDetector,
    tempBuf,
    fftOut;

  out = Silence.ar();
  in = SoundIn.ar(0);

  tempBuf = LocalBuf(outputBuffer.numFrames);
  fftChain = FFT(tempBuf, in);
  fftChain = PV_MagSmooth(fftChain, 0.9);
  fftOut = IFFT(fftChain);
  fftChain = FFT(outputBuffer, fftOut);          // put it back into normal fft form


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
  //loudness = Integrator.kr(Loudness.kr(fftChain), 0.9);
  loudness = EnvDetect.ar(in, attack: 0.1, release: 1.0);
  Out.kr(loudnessBus, loudness);

  out;
}, [
  \buffer,
  \controlbus
]);
