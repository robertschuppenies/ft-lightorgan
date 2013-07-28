Instr("VisualizerData", {
  arg outputBuffer;
  
  var in, fftChain, out;

  out = 0;
  in = In.ar(0);

  fftChain = FFT(outputBuffer, in);

  //PV_Copy(fftChain, outputBuffer);

  [out, out];
}, [
  \buffer
]);
