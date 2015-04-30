# MPDC
Java app for showing the current song on any [Music Player Daemon](http://www.musicpd.org/) server. 
It also has got play/pause and next song button and you can connect buttons to your Raspberry Pi for taking control of
the whole thing. OK, only the play/pause and next song, but still... it's useful!

## Connection
Connect the display as described [here](https://github.com/ondryaso/pi-ssd1306-java). Also, connect two buttons between
ground and __WiringPi__ pins 3 for play/pause button and 5 for next song button. You can change these pins in the Main constructor:

```java
GpioPinDigitalInput playBtn = c.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_UP);
GpioPinDigitalInput nextBtn = c.provisionDigitalInputPin(RaspiPin.GPIO_05, PinPullResistance.PULL_UP);
```

## Build and usage
You can build it using Maven. You'll have to [add](http://softwarecave.org/2014/06/14/adding-external-jars-into-maven-project/) 
my [SSD1306 library](https://github.com/ondryaso/pi-ssd1306-java) to your local repo. Then add it as dependency to your POM.
It should package without errors... at least in theory. Or you can __[download](https://github.com/ondryaso/pi-mpdc/releases/download/1.0/MPC.jar)__ the jar itself.

Run using 
```
sudo java -cp /home/pi/:/home/pi/MPC.jar:/opt/pi4j/lib/'*' eu.ondryaso.mpcdisplay.Main
```

You'll have to have the Pi4J libraries installed.

## Disclaimer
Do everything you wan't with this piece of code, but it would be nice to address me as the author. The JAR contains compiled
files of [JavaMPD](http://www.thejavashop.net/javampd/) library. These files are licensed under GNU GPL v3 so I suppose this
should be under this license as well but I don't know... just know that I don't claim any rights for this library.
