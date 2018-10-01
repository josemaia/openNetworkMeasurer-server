# openNetworkMeasurer-server

This is the Java server I developed for the project in my Master's dissertation. It is destined to collect Wi-Fi/Cellular measurements from smartphones that perform the measuring, process those measurements, and display them to the web. The other half of the project is also available on [GitHub](https://github.com/josemaia/openNetworkMeasurer).

# To run

Tested on a Jetty installation on a Fedora-based server. 

Compile locally, then put the jars anywhere on the server (run manually or with a cron script); put the WAR under the Jetty folder.

Furthermore, you need to implement the PrivateValues interface into a file called myPrivateValues, 
containing the data to access your SQL server.

# License

Released under the MIT License. Apache license also included, to comply with a few imported libraries;
Eclipse Public License included due to usage of Jetty.
