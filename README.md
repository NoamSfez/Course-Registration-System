# Course-Registration-System
An assignment implementing a “Course Registration System” server (JAVA) and client (c++). Using Binary communication protocol, Thread-Per-Client (TPC) and  Reactor server.


Client side:
  1) cd Client
  2) make clean
  3) make
  8) cd bin
  9) ./BGRSclient <host> <port>

  
Server side:
  4) cd Server
  5) mvn clean
  6) mvn compile
  7) Reactor server execute:
         mvn exec:java -Dexec.mainClass=“bgu.spl.net.impl.BGRSServer.ReactorMain“ -Dexec.args=“<port> <number of thread>“
     or:
     Thread per client server execute:
          mvn exec:java -Dexec.mainClass=“bgu.spl.net.impl.BGRSServer.TPCMain“ -Dexec.args=“<port>“
  
  
