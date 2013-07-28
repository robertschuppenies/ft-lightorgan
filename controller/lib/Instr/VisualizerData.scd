Instr("VisualizerData", {
  arg outputBuffer;
  
  var in, fftChain, out;

  out = Silence.ar();
  in = SoundIn.ar(3);

  fftChain = FFT(outputBuffer, in);
  fftChain = PV_MagSmooth(outputBuffer, 0.01);

  //PV_Copy(fftChain, outputBuffer);

  out;
}, [
  \buffer
]);
