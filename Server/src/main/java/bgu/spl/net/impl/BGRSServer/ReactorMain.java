package bgu.spl.net.impl.BGRSServer;

import java.io.IOException;

import bgu.spl.net.srv.Reactor;
import bgu.spl.net.srv.Server;

public class ReactorMain {

	public static void main(String[] args) {
		Database db = Database.getInstance();
		db.initialize("Courses.txt");
		try(Server<String[]> reactor = new Reactor<String[]>(Integer.valueOf(args[0]), Integer.valueOf(args[1]), ()->{return new CommandInvocationProtocol(db);}, ()->{return new CommandEncoderDecoder();});){
			reactor.serve();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
