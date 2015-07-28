# openNetworkMeasurer-server

Hello! My name is José Maia, and this is the Java server I developed for the project in my Master's dissertation.

The project is deployed at http://fedora-srv01.alunos.dcc.fc.up.pt/, although the servers have been spotty lately.

# To run

Tested on a Jetty installation on a Fedora-based server. 

Compile locally, then put the jars anywhere on the server (run manually or with a cron script); put the WAR under the
Jetty folder.

Furthermore, you need to implement the PrivateValues interface into a file called myPrivateValues, 
containing the data to access your SQL server.

# License

Released under the MIT License. Apache license also included, to comply with a few imported libraries.