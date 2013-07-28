Instr("VisualizerData", {
  arg outputBuffer;
  
  var in, fftChain, out;

  out = Silence.ar();
  in = SoundIn.ar(0);

  fftChain = FFT(outputBuffer, in);

  //PV_Copy(fftChain, outputBuffer);

  out;
}, [
  \buffer
]);
