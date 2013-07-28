Instr("VisualizerData", {
  arg outputBuffer;
  
  var in, fftChain, out;

  out = 0;
  in = In.ar(0);

  fftChain = FFT(LocalBuf(1024), in);

  fftChain = PV_Copy(fftChain, outputBuffer);

  out;
}, [
  \buffer
]);
