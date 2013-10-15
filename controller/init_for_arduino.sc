(
s.quit;
s.boot;
s.options.inDevice = "Line In";
s.options.memSize = 4096;

Instr.dir = "lib/Instr".resolveRelative();
Instr.loadAll();

s.doWhenBooted({
  var arduino, organ, visualizer;

  arduino = FtloArduino.new("/dev/ttyACM0", 115200);
  organ = FtloOrgan([arduino]);
  visualizer = FtloVisualizer.new((organ: organ));

  organ.start_updating();
  2.0.wait();
  organ.do_startup_animation();
  organ.startup_animation_duration().wait();
  organ.allLightsOff();
  visualizer.start_visualizer();
});

)
