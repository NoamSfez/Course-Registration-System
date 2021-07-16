#include <connectionHandler.h>

using boost::asio::ip::tcp;
using namespace std;

ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_){}

ConnectionHandler::~ConnectionHandler() {
    close();
}

bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

string ConnectionHandler::getBytes() {
    size_t tmp = 0;
    boost::system::error_code error;
    std::string ans;
    char myArr[2];
    try {
        tmp += socket_.read_some(boost::asio::buffer(myArr, 2), error);
        if(error)
            throw boost::system::system_error(error);
        short opcode = bytesToShort(myArr);
        tmp += socket_.read_some(boost::asio::buffer(myArr, 2), error);
        if(error)
            throw boost::system::system_error(error);
        short opcodeMessage = bytesToShort(myArr);

        if(opcode==12) {
            ans += "ACK " + std::to_string(opcodeMessage) + " ";
            bool end = false;
            char myArr2[10];

            while (!error && !end) {
		int i = 0;
                tmp = socket_.read_some(boost::asio::buffer(myArr2, 10), error);
                while (!error && (i < tmp) & !end ) {
                    if (myArr2[i] == '\0'){
                        end = true;
                    }else
                        ans += myArr2[i];
                    i++;
                }
            }
        }else if(opcode == 13){
            ans += "ERR " + std::to_string(opcodeMessage);
        }else{
            throw new exception();
        }

    } catch (std::exception& e) {
        throw e;
    }
    return ans;
}

bool ConnectionHandler::sendBytes(string message) {
    int tmp = 0;
    boost::system::error_code error;
    std::string delimiter = " ";
    size_t pos = 0;
    size_t size = 1;
    vector<string> token;
    while ((pos = message.find(delimiter)) != std::string::npos && pos > 0) {
        token.push_back(message.substr(0, pos));
        message.erase(0, pos + delimiter.length());
        size++;
    }
    token.push_back(message.substr(0, message.size()));
    string commands[size];
    std::copy(token.begin(),token.end(), commands);

    short opcode;
    short courseNum;

    int index2 = 1;//keep the index of string in commands to continue (since some commands add another short)
    if(commands[0]=="ADMINREG") {
        opcode = 1;
    }else if(commands[0]=="STUDENTREG"){
        opcode = 2;
    }else if(commands[0]=="LOGIN"){
        opcode = 3;
    }else if(commands[0]=="LOGOUT"){
        opcode = 4;
    }else if(commands[0]=="COURSEREG"){
        courseNum = atoi(commands[1].c_str());
        index2++;
        opcode = 5;
    }else if(commands[0]=="KDAMCHECK"){
        courseNum = atoi(commands[1].c_str());
        index2++;
        opcode = 6;
    }else if(commands[0]=="COURSESTAT"){
        courseNum = atoi(commands[1].c_str());
        index2++;
        opcode = 7;
    }else if(commands[0]=="STUDENTSTAT"){
        opcode = 8;
    }else if(commands[0]=="ISREGISTERED"){
        courseNum = atoi(commands[1].c_str());
        index2++;
        opcode = 9;
    }else if(commands[0]=="UNREGISTER"){
        courseNum = atoi(commands[1].c_str());
        index2++;
        opcode = 10;
    }else if(commands[0]=="MYCOURSES"){
        opcode = 11;
    }else{
        cout << "The command is invalid" << endl;
        return false;
	}

    int len = 2 * index2;
    for (int i = index2;i<size;i++){
        len += commands[i].length() + 1;
    }
    char bytesArr[2];
    shortToBytes(opcode, bytesArr);
    tmp += socket_.write_some(boost::asio::buffer(bytesArr, 2), error);
    if(index2==2 && !error){
        shortToBytes(courseNum, bytesArr);
        tmp += socket_.write_some(boost::asio::buffer(bytesArr, 2), error);
    }

    try {
        for (int j = index2 ;!error && j < size;j++){
            tmp += sendFrameByAscii(commands[j], '\0');
        }
        if(error)
            throw boost::system::system_error(error);
        if(len != tmp)
            throw std::exception();
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

void ConnectionHandler::shortToBytes(short num, char *bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

short ConnectionHandler::bytesToShort(char* bytesArr) {
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

int ConnectionHandler::sendFrameByAscii(string basicString, const char delimiter) {
    boost::system::error_code error;
    int tmp = socket_.write_some(boost::asio::buffer(basicString.c_str(), basicString.length()), error);
    if(!error)
        tmp += socket_.write_some(boost::asio::buffer(&delimiter, 1), error);
    if(error)
        throw boost::system::system_error(error);
    return tmp;
}
