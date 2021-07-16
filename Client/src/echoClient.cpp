#include <stdlib.h>
#include <connectionHandler.h>
#include <string>
#include <thread>

using namespace std;
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
//    std::string host = "0.0.0.0";//argv[1];
//    short port = atoi("7777");//argv[2]);

    std::string host = argv[1];

    short port = atoi(argv[2]);
    bool* stop1 = new bool(false);//keyboard thread
    bool* stop2 = new bool(false);//main thread
    int len;
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    auto f = [](ConnectionHandler* connectionHandler, bool* stop1){
        while (1) {
            const short bufsize = 1 << 10;
            char buf[bufsize];
            std::cin.getline(buf, bufsize);
            std::string line(buf);
            int len = line.length();
            if(line == "LOGOUT")
                *stop1 = true;
            connectionHandler->sendBytes(line);            
        }
    };

    thread t1(f,&connectionHandler, stop1);

	//From here we will see the rest of the ehco client implementation:
    while (!*stop2) {
                string s = connectionHandler.getBytes();
                if (s == ""){
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        cout << s << endl;
        if (*stop1 && s[0]=='E' && s[1]=='R'&& s[2]=='R') {
            *stop1 = false;
        }else if(*stop1 && s[0]=='A' && s[1]=='C' && s[2]=='K'){
            *stop2 = true;
            t1.detach();
            std::cout << "Exiting...\n" << std::endl;
        }
    }
    delete stop1;
    delete stop2;
    //delete &connectionHandler;
    return 0;
}
