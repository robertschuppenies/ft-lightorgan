// Various snippets useful installing/debugging supercollider code.

SerialPort.listDevices();
SerialPort.inDevices();


// Quarks (the supercollider package management system)
// ----------------------------------------------------

// open Quarks GUI
Quarks.gui;
// update repository
Quarks.updateDirectory;
// show directory where extensions are installed
Platform.userAppSupportDir;


// Emulator snippets
// -----------------

outSock = NetAddr.new("127.0.0.1", 5001);
outSock.sendMsg("/organ/tube", 4, "rgb/", 255, 255, 0);
