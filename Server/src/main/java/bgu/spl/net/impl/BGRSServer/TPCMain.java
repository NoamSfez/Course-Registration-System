package bgu.spl.net.impl.BGRSServer;
import java.io.IOException;
import bgu.spl.net.srv.Server;

public class TPCMain {
	public static void main(String[] args) {
		Database db = Database.getInstance();
		db.initialize("Courses.txt");
		try(Server<String[]> TPC = Server.threadPerClient(Integer.valueOf(args[0]), ()->{return new CommandInvocationProtocol(db);}, ()->{return new CommandEncoderDecoder();});){
			TPC.serve();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
