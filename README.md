# Course-Registration-System

Implementation of a Course Registration system where a student can register for a course if he already completes dependent courses (prerequisites) and it remains places.
The system follow a Client/Sever architecture with a C++ client and a Java server, that communicate using a custom binary protocol over TCP/IP.
The server implementation illustrates the use of the [Reactor design pattern](https://en.wikipedia.org/wiki/Reactor_pattern).

## Client compilation:
```
$ cd Client
$ make clean
$ make
```

## Server compilation:
```
$ cd ../Server/
$ mvn clean
$ mvn compile  
```

## Launch Server:
### Using Reactor server:
```
$ mvn exec:java -Dexec.mainClass=“bgu.spl.net.impl.BGRSServer.ReactorMain“ -Dexec.args=“<port> <number of thread>“
```
### or using Thread per client server:
```
$ mvn exec:java -Dexec.mainClass=“bgu.spl.net.impl.BGRSServer.TPCMain“ -Dexec.args=“<port>“
```

## Launch Client:
```
$ cd ../Client/bin/
$ ./BGRSclient <host> <port>
```
