ArgumentParser {

  // Settings with default values. Actual values may change based on input
  // arguments.
  var <>arduino_port = "/dev/ttyACM0";
  var <>arduino_baudrate = 115200;
  var <>emulator_host = "127.0.0.1";
  var <>emulator_port = 5001;
  // Common options are "Line In" and "Built-in Microphone"
  var <>in_device = "Line In";
  // Memory allocated to the server (in kilobytes).
  var <>memsize = 4096;

  // A list of connectors to use. This is filled based on input arguments.
  var <>connectors = nil;

  *new {
    ^super.new.init();
  }

  init {
	// We initialize the array to a maximum size of 2 (that's all the possible
	// connectors we have).
	this.connectors = Array.new(2);
  }

  // Print help output.
  printHelp {
	"------------------------------------------------------".postln();
	"usage: $ sclang init.sc [OPTIONS]".postln();
	"".postln();
	"OPTIONS (shown with defaults):".postln();
	(("  arduino_port=" ++ this.arduino_port).padRight(30) ++
	"serial port at which to talk to the Arduino").postln();
	(("  arduino_baudrate=" ++ this.arduino_baudrate).padRight(30) ++
	"baudrate at which to send data to the Arduino").postln();
	(("  connectors=").padRight(30) ++
	"comma-separated list of connectors (arduino,emulator)").postln();
	(("  emulator_host=" ++ this.emulator_host).padRight(30) ++
	"host address of the emulator").postln();
	(("  emulator_port=" ++ this.emulator_port).padRight(30) ++
	"port at which the emulator is listening").postln();
	(("  in_device=\"" ++ this.in_device ++ "\"").padRight(30) ++
	"device from which to process audio signals").postln();
	(("  memsize=" ++ this.memsize).padRight(30) ++
	"amount of memory allocated to the server in kB").postln();
	"------------------------------------------------------".postln();
  }

  // Parse input arguments.
  //
  // Returns: True, if arguments where parsed, False if not (e.g., if "help"
  //   was requested).
  parse {
	var connector_names;

	thisProcess.argv.do {
	  arg argument;

	  if (argument == "help", {
		this.printHelp();
		^False;
	  });
	  if (argument.find("arduino_port=") == 0, {
		this.arduino_port = argument.split($=)[1].split($,)[0];
	  });
	  if (argument.find("arduino_baudrate=") == 0, {
		this.arduino_baudrate = argument.split($=)[1].split($,)[0].asInteger();
	  });
	  if (argument.find("emulator_host=") == 0, {
		this.emulator_host = argument.split($=)[1].split($,)[0];
	  });
	  if (argument.find("emulator_port=") == 0, {
		this.emulator_port = argument.split($=)[1].split($,)[0].asInteger();
	  });
	  if (argument.find("connectors=") == 0, {
		connector_names = argument.split($=)[1].split($,);
	  });
	  if (argument.find("indevice=") == 0, {
		this.in_device = argument.split($=)[1].split($,)[0];
	  });
	};

	connector_names.do {
	  arg connector_name;

	  if (connector_name == "emulator", {
		this.connectors.add(FtloEmulator.new(
			emulator_host, emulator_port));
	  });
	  if (connector_name == "arduino", {
		this.connectors.add(FtloArduino.new(
		  arduino_port, arduino_baudrate));
	  });
	}
	^True;
  }
}