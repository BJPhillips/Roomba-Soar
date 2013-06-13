Roomba-Soar
===========

Control an [iRobot Roomba](http://www.irobot.com) 595 with a
[Soar](http://sitemaker.umich.edu/soar/home) agent running under a
[Jsoar](https://github.com/soartech/jsoar) kernel.

The kernel and a Roomba interface layer run under Java on a host
machine. The host might be a netbook mounted on the Roomba, or as in
the photograph below, a desktop connected via WiFi.

![](www/media/RoombaSoar.jpg)

To demonstrate the system, a trivial obstacle avoiding agent has been
developed ([video](www/media/TableCircumnavigatorExample.mov)).

Hardware Requirements
---------------------

The hardware requirements, along with approximate costs, are shown in
the [system diagram](www/media/Roomba-SoarSystemDiagram.pdf).

![](www/media/Roomba-SoarSystemDiagram.png)

Note that for a system with the host mounted on the Roomba (e.g. using
a netbook), the Wireless USB Sharing Station is not required and the
host is connected directly to the Roomba via the FTDI Cable and Custom
Data Cable.

Software
--------

The software, including the latest version of the Roomba interface
layer, is provided in the [TableCircumnavigator](TableCircumnavigator)
(obstacle avoiding) example.

The RoombaIF abstract base class provides methods to open and close a
connection to the Roomba and a mechanism to request the Roomba stream
any subset of its sensor data. (The sensors are described in the
[iRobot Roomba 500 Open Interface (OI) Specification](www/iRobot_Roomba_500_Open_Interface_Spec.pdf). Methods
to issue motor commands are available and it should be straightforward
to add new methods for other commands.

Two implementations of the RoombaIF class are included: one that uses
the
[Java Simple Serial Connector](https://github.com/scream3r/java-simple-serial-connector)
to establish a connection with an actual Roomba; and one that
demonstrates a trivial simulated Roomba. Documentation is provided in
the [Javadoc](www/javadoc/index.html).


