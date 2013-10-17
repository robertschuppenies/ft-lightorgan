FtloEmulator {

  var <>socket;

  *new { arg address, port;
	^super.new.init(address, port);
  }

  init {
	arg address, port;

	this.socket = NetAddr.new(address, port);
  }

  flush {
	// TODO(schuppe): add support for '/organ/flush' messages to emulator.
	// this.socket.sendMsg("/organ/flush");
  }

  setTube {
	arg index, r, g, b;

	if (index.isInteger, {
	  this.socket.sendMsg("/organ/tube", index, r, g, b);
	}, {
	  ("Error: received invalid tube index " ++ index).postln();
	})
  }

}

