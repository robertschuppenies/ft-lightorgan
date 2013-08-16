Visualizer : Object {
  var <>freqMagnitudeToPitchMap, <>organ, <>hueUpdater, <>spectrumUpdater, <>sleepMonitor, <>sleepMonitorHistory, <>sleepMonitorVarianceWasUnderThresholdCount;
  classvar <sleepMonitorOrder = 20, <sleepMonitorPollTime = 0.5, <sleepMonitorVarianceThreshold=1e-05;

  *new {
    arg initParams;
    ^super.new.init(initParams);
  }

  init {

    arg initParams;

    var inputPatch, visualizerDataBuf, fftSize = 2048, loudnessBus,
      previousValue, n, valuesSincePrevious, hueModulatorBus, me = this,
      serverPollTime = 0.1;

    this.freqMagnitudeToPitchMap =  [0, 1, 2, 3, 4, 5, 6, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 12, 12, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24];
    this.organ = initParams['organ'];

    visualizerDataBuf = Buffer.alloc(Server.default, fftSize);

    loudnessBus = Bus.control(numChannels: 1);
    hueModulatorBus = Bus.control(numChannels: 1);

    inputPatch = Patch(Instr.at("VisualizerData"), (
      outputBuffer: visualizerDataBuf,
      loudnessBus: loudnessBus.index,
      hueModulatorBus: hueModulatorBus.index
    ));

    0.5.wait();

    inputPatch.play();

    0.5.wait();

    this.hueUpdater = Routine.new({
      while({true}, {
        hueModulatorBus.get({
          arg val;
          this.handle_hue_modulation(val);
        });
        serverPollTime.wait();
      });
    });

    this.spectrumUpdater = Routine.new({
      while({true}, {
        visualizerDataBuf.getn(0, fftSize, {
          arg data;
          this.handle_spectrum_update(data);
        });
        serverPollTime.wait();
      });
    });

    this.sleepMonitorHistory = [];
    this.sleepMonitorVarianceWasUnderThresholdCount = 0;
    this.sleepMonitor = Routine.new({
      loudnessBus.get({
        arg val;
        this.handle_loudness_update(val);
      });
      sleepMonitorPollTime.wait();
    }).loop();
    this.start_sleep_monitor();

    
    //// keep track of last Nth value
    //n = 20;
    //valuesSincePrevious = n;
    //previousValue = 0.0;

    //{

      /**
       *  handle onset detection
       **/

      //loudnessBus.get({
        //arg loudness;

        //if (loudness > (20 * previousValue), {
          //this.loud_event_happened();
        //});

        //valuesSincePrevious = valuesSincePrevious + 1;

        //if (valuesSincePrevious >= n, {
          //previousValue = loudness;
          //valuesSincePrevious = 0;
        //});

      //});
      

      //0.05.wait();
    
    //}.loop();


  }

  start_visualizer {
    SystemClock.play(this.hueUpdater);
    SystemClock.play(this.spectrumUpdater);
  }

  stop_visualizer {
    this.hueUpdater.stop();
    this.spectrumUpdater.stop();
  }

  start_sleep_monitor {
    SystemClock.play(this.sleepMonitor);
  }

  handle_loudness_update {
    arg loudness;
    var avg, variance;

    "loudness:".postln;
    loudness.postln;

    this.sleepMonitorHistory = this.sleepMonitorHistory.add(loudness);

    if (this.sleepMonitorHistory.size() == Visualizer.sleepMonitorOrder, {

      // calculate average
      avg = 0.0;
      this.sleepMonitorHistory.do({
        arg val;

        avg = avg + val;
      });
      avg = avg / this.sleepMonitorHistory.size();

      // calculate variance
      variance = 0.0;
      this.sleepMonitorHistory.do({
        arg val;

        variance = variance + (val - avg)**2;
      });
      variance = variance / this.sleepMonitorHistory.size();

      if (variance < Visualizer.sleepMonitorVarianceThreshold, {
        this.sleepMonitorVarianceWasUnderThresholdCount = this.sleepMonitorVarianceWasUnderThresholdCount + 1;

        if (this.sleepMonitorVarianceWasUnderThresholdCount >= Visualizer.sleepMonitorOrder, {
          if (this.organ.sleepModeRunning == false, {
            this.organ.start_sleep_mode();
          });
        });

      }, {
        this.sleepMonitorVarianceWasUnderThresholdCount = 0;
        if (this.organ.sleepModeRunning == true, {
          this.organ.stop_sleep_mode();    
        });
      });

      // remove oldest value
      this.sleepMonitorHistory.removeAt(0);

    });
  }

  /**
   *  Changes in loudness are mapped to hue here.
   **/
  handle_hue_modulation {
    arg hue;
    //"handle_hue_modulation".postln();
    this.organ.set_tubes_hue(hue);
  }

  /**
   *  Changes in audio spectrum are rendered to lights here.
   **/
  handle_spectrum_update {
    arg data;
    var normalizedData = data.normalize(),
      z,
      x,
      pitchMagnitudes;

    //"handle_spectrum_update".postln();
    z = data.clump(2).flop;
    z = [Signal.newFrom(z[0]), Signal.newFrom(z[1])];
    x = Complex(z[0], z[1]);
    //{x.magnitude.plot}.defer();

    // map the frequency magnitudes into pitch bins
    //pitchMagnitudes = this.freqBinsToPitchBins(x.magnitude).normalize();
    pitchMagnitudes = 0.01*this.freqBinsToPitchBins(x.magnitude).ampdb().add(50);
    //{Signal.newFrom(pitchMagnitudes).plot}.defer();
    // map to organ!
    pitchMagnitudes.do({
      arg pitchMag, i;

      this.organ.tubes[i].set_brightness(pitchMag);
      this.organ.tubes[26 + i].set_brightness(pitchMag);
    });
  }

  freqBinsToPitchBins {
    arg freqBins;

    var pitchBinMap = this.freqMagnitudeToPitchMap,
      numPitchBins,
      pitchBins,
      pitchBinIndex;

    pitchBins = Array.fill(25, {
      arg i;
      0.0;
    });

    for(0, freqBins.size()-1, {
      arg i;

      // pitch bin index for this frequency bin
      pitchBinIndex = pitchBinMap[i];

      // add frequency magnitude into this pitch bin
      pitchBins[pitchBinIndex] = pitchBins[pitchBinIndex] + freqBins[i];
    });

    ^pitchBins;
  }


}
