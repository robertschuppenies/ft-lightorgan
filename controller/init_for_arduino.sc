(
s.quit;
s.boot;
s.options.inDevice = "Line In";
s.options.memSize = 4096;

Instr.dir = "lib/Instr".resolveRelative();
Instr.loadAll();

s.doWhenBooted({
	var organ, visualizer;

    organ = FtloOrgan.new((
      connectToVisualizer: false,
      address: "127.0.0.1",
      port: 5001,
      connectToArduino: true,
      arduinoAddress: "/dev/ttyACM0",
      arduinoBaudRate: 115200
    ));
    visualizer = FtloVisualizer.new((
      organ: organ
    ));

    organ.start_updating();
    2.0.wait();
    organ.do_startup_animation();
    organ.startup_animation_duration().wait();
    organ.allLightsOff();
    visualizer.start_visualizer();
});

)
