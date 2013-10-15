(
s.quit;
s.boot;
s.options.inDevice = "Built-in Microphone";
s.options.memSize = 4096;

Instr.dir = "lib/Instr".resolveRelative();
Instr.loadAll();

s.doWhenBooted({
  var emulator, organ, visualizer;

  emulator = FtloEmulator.new("127.0.0.1", 5001);
  organ = FtloOrgan([emulator]);
  visualizer = FtloVisualizer.new((organ: organ));

  organ.start_updating();
  organ.allLightsOff();
  visualizer.start_visualizer();
});
)
