FtloArduino : Object {

  var <>socket;

  *new { arg address, baudRate;
  	^super.new.init(address, baudRate);
  }

  init {
	arg address, baudRate;

	this.socket = SerialPort.new(address, baudRate);
  }

  flush {
	this.socket.putAll(Int8Array[255, 255, 255, 255, 255, 255]);
  }

  setTube {
	arg index, r, g, b;

	if (index.isInteger, {
	  this.socket.putAll(Int8Array[255]);
	  this.socket.putAll(Int8Array[index, r, g, b]);
	}, {
	  ("Error: received invalid tube index " ++ index).postln();
	})
  }

}