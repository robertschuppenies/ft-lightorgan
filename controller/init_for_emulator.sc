(
s.quit;
s.boot;
s.options.inDevice = "Built-in Microphone";
s.options.memSize = 4096;

Instr.dir = "lib/Instr".resolveRelative();
Instr.loadAll();

s.doWhenBooted({
	var organ, visualizer;

    organ = FtloOrgan.new((
      connectToVisualizer: true,
      address: "127.0.0.1",
      port: 5001,
      connectToArduino: false,
      arduinoBaudRate: 115200
    ));
    visualizer = FtloVisualizer.new((
      organ: organ
    ));

    organ.start_updating();
    organ.allLightsOff();
    visualizer.start_visualizer();
});
)
