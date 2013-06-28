light_organ_emulator
----------------
About:
This is a project written in Processing to emulate the appearance of the light-organ.
It is intended to be used for testing algorithms for converting sound into light-organ 
commands.  The emulator responds to the same OSC commands that the light-organ will, and
attempts to appear as the light-organ will.

The project includes some test functions which run by default and which cycle some random 
colors through the tubes of the light organ.  Hitting the space bar will pause or resume
this.

Running:
To run the emulator you need to install Processing 2 from http://www.processing.org/
You will also need to add the oscP5 and netP5 libraries which can be found at 
http://www.sojamo.de/libraries/oscP5/

Design Decisions:
I'm not really a graphics programmer, so some of this is a bit hacky. This is a 2D 
drawing.  I add some curvature to the location of the tubes to approximate the perspective 
of looking at the tubes which are layed out in a curve, but there is no true perspective.

The light-organ LEDs respond to messages which specify 8-bit RGB values.  However I 
cannot use these RGB values directly as pixel RGB values, because the nature of light
in a tube of water is different. For example, when RGB = {0,0,0} the LED will be off,
and the tube will be transparent.  However a pixel of RGB = {0,0,0} will be black.
I've come up with an algorithm I hope is close to accurate.  But if you want to tweak the 
algorithm, or come up with your own, please feel free to implement your ideas and try them
out.