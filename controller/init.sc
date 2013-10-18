var argument_parser = ArgumentParser();
if (argument_parser.parse() != True, {
  0.exit;
}, {  s.quit;
  s.boot;

  Instr.dir = "lib/Instr".resolveRelative();
  Instr.loadAll();

  s.options.inDevice = argument_parser.in_device;
  s.options.memSize = argument_parser.memsize;

  s.doWhenBooted({
    var arduino, organ, visualizer;

    organ = FtloOrgan(argument_parser.connectors);
    visualizer = FtloVisualizer.new((organ: organ));

    organ.startUpdating();
    organ.turnOff();
    "startup animation triggered ..".post();
    organ.startDemo();
	organ.getDemoDuration().wait();
	organ.turnOff();
	" and completed. Running visualizer now.".postln();
	visualizer.start_visualizer();
  });
});

