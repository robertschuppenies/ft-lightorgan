// a simulator for the visuals of the light organ
// started 6/22/2013, Luke Dahl
// version b adds receiving Open Sound Control (OSC) messages and test functions that sends OSC

// globals
float g_width = 20;            // the width of a tube
float g_length_scale = 9.0;    // scaling from the nominal tube heights to the drawing height

// the length of the tubes in order from left-to-right
int[] tube_lengths_rear =  {29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 40, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29};
int[] tube_lengths_front = {20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 32, 31, 30, 29, 28, 27, 26, 25, 24, 23, 22, 21, 20};
int g_num_front_row = tube_lengths_front.length;
int g_num_rear_row = tube_lengths_rear.length;


// tubes are numbered front row left-to-right, followed by back row left-to-right
int g_num_tubes = g_num_front_row + g_num_rear_row;
Tube[] tubes = new Tube[g_num_tubes];
PVector[] tube_loc = new PVector[g_num_tubes];

// network communication
import oscP5.*;
import netP5.*;
OscP5 oscP5;
int g_osc_port = 5001;        // port for receiving OSC messages
NetAddress myRemoteLocation;  // where to send test OSC messages

void setup() 
{
  size(1200, 800);
  
  initOSC();
  initTubeLocations();        // TEST
  
  // init front tubes
  for (int i = 0; i < g_num_front_row; i++) {
    tubes[i] = new Tube(tube_loc[i], tube_lengths_front[i]*g_length_scale);
  }
  // init rear tubes
  for (int i = 0; i < g_num_rear_row; i++) {
    PVector loc = new PVector(width/2, height/2);
    tubes[i+g_num_front_row] = new Tube(tube_loc[i+g_num_front_row], tube_lengths_rear[i]*g_length_scale);
  }
  
  initTestColors();    // TEST
}

void draw()
{
  background(color(60,60,60,255));

  updateTestColors();    // TEST
  
  // draw rear tubes
  for (int i = 0; i < g_num_rear_row; i++) 
  {
    tubes[i+g_num_front_row].draw();
  }
  // then draw front tubes
  for (int i = 0; i < g_num_front_row; i++) 
  {
    tubes[i].draw();
  }
}

// calculate the locations of the tubes along an arc to approximate a 3D view with the tubes layed out in curve
void initTubeLocations() 
{
  PVector arc_center = new PVector(width/2, height/2+2000);
  float arc_radius = 1800;
  float arc_span = 0.15*PI;        // how many radians does our arc span?
  float phase_inc = arc_span/g_num_tubes;
  float phase_start = 0.5*PI - 0.5*arc_span;
  
  // front row: even locations
  for (int i=0; i < g_num_front_row; i++) 
  {
    float phase = phase_start + 2*i*phase_inc;
    tube_loc[i] = new PVector(arc_radius*cos(phase), -arc_radius*sin(phase));
    tube_loc[i].add(arc_center);
  }
  
  // rear row: odd locations
  for (int i=0; i < g_num_rear_row; i++) 
  {
    float phase = phase_start + (2*i+1) * phase_inc;
    tube_loc[i+g_num_front_row] = new PVector(arc_radius*cos(phase), -arc_radius*sin(phase));
    tube_loc[i+g_num_front_row].add(arc_center);
  }
  
}

// OSC listener stuff -------------------------
void initOSC()
{
  // start listening for OSC messages
  oscP5 = new OscP5(this, g_osc_port);  
  oscP5.plug(this, "oscOrgan", "/organ/tube");  // call oscOrgan() when messages arrive formatted /organ/tube/i str i i i
}

// processes osc messages formatted as /organ/tube/i/rgb i i i
void oscOrgan(int tube_num, String str, int red, int grn, int blu)
{
  // print("OSC received: " +tube_num + str + red + " " + grn + " " + blu + "\n");  // DEBUG
  // TODO: this really should check whether the string is "/rgb"
  tubes[tube_num].setColor(red, grn, blu);
}

// this catches any other osc messages
void oscEvent(OscMessage theOscMessage) 
{  
  if(theOscMessage.isPlugged()==false) 
  {
    /* print the address pattern and the typetag of the received OscMessage */
    println("### WARNING: received an unkown OSC message.");
    println("### addrpattern\t"+theOscMessage.addrPattern());
    println("### typetag\t"+theOscMessage.typetag());
  }
}


// Tube class --------------------------------
class Tube 
{
  PVector location;
  float xWidth;
  float yHeight;
  color ledColor;
  color pixelColor;
  
  // ctor
  Tube(PVector loc, float ht)
  {
    location = loc;
    yHeight = ht;
    xWidth = g_width;
    setColor(0 , 0, 0);
  }
 
 void draw() 
 {
   fill(pixelColor);
   stroke(100, 100, 100);
   strokeWeight(1);
   rectMode(CENTER);
   rect(location.x, location.y - yHeight/2, xWidth, yHeight);  
 }
 
 // this function tries to transform the colors so that they will appear as LEDs on bubbly water might, i.e.
 // when LED is 0,0,0 pixel should be completely transparent, not black
 // this means ledColor = (0,0,0) needs to become pixeColor(1,1,1)
 void setColor(int red, int grn, int blu) 
 {
   ledColor = color(red, grn, blu);
   int sum = red + grn + blu;
   if (sum == 0) {
     pixelColor = color(1,1,1,0);
   }
   else {
     // make alpha between 50 and 200
     int alpha = min(int(sum/3.0) + 50, 200);
     pixelColor = color(red, grn, blu, alpha); 
    // print("alpha is " + alpha + "\n");    // DEBUG
   }
 }
}

// Test Code (this is a bit hacky for now, e.g. uses a bunch of globals!) --------------------------
// This code sends OSC messages to emulate what might be received from the sound processing hardware
int[][] g_test_colors = new int[3][g_num_tubes];    // these are initialized to zero
int g_test_frame = 0;                               // how many graphics frames have passed?
int g_test_index = 0;
boolean g_test_run = true;

// this func sets a certain number of random tubes to random colors
void initTestColors() 
{
  myRemoteLocation = new NetAddress("127.0.0.1", g_osc_port);  // set up to send OSC messages to this same machine
  
  int N = 15;                          // set N tubes to random colors
  for (int i = 0; i < N; i++)
  {
    int j = floor(random(g_num_tubes));
    g_test_colors[0][j] = floor(random(255));
    g_test_colors[1][j] = floor(random(255));
    g_test_colors[2][j] = floor(random(255));
  }
}

// this function rotates array of colors through the array of tubes so that colors appear to move rightwards
// accross the front row and then leftwards across the back row
void updateTestColors()
{
  if (g_test_run)
  {
    int num_frames = 20;
    if ((g_test_frame % num_frames) == 0)    // update every num_frames
    {
      g_test_index++;
      // iterate forwards over the first row then backwards over the second
      for (int i = 0; i < g_num_front_row; i++)
      {
        int j = (i + g_test_index) % g_num_tubes;
        sendOSC( i, g_test_colors[0][j], g_test_colors[1][j], g_test_colors[2][j]); 
      }
      int k = 1;
      for (int i = g_num_front_row; i < g_num_tubes; i++)
      {
        int j = (i + g_test_index) % g_num_tubes;
        sendOSC( g_num_tubes - k, g_test_colors[0][j], g_test_colors[1][j], g_test_colors[2][j]); 
        k++;
      }
      
    }
    g_test_frame++;
  }
}

void sendOSC(int i, int r, int g, int b)
{
  OscMessage myMessage = new OscMessage("/organ/tube");
  myMessage.add(i); // add tube number
  myMessage.add("rgb/"); // add a string
  myMessage.add(r); // add rgb int values
  myMessage.add(g); // add rgb int values
  myMessage.add(b); // add rgb int values
 
  // send the message
  oscP5.send(myMessage, myRemoteLocation); 
}

// press 'space bar' to enable/disable moving colors
void keyPressed() {
  if (keyCode == 32)
  {
    g_test_run = !g_test_run;
  }
}




