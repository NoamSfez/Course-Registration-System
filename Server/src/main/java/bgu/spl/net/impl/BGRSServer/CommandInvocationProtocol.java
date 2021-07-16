package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.rci.Command;

//import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class CommandInvocationProtocol implements MessagingProtocol<String[]> {

	private Database arg;
	private String activeUserName;
	private boolean isAdmin;
	private boolean terminate;
    private HashMap<String, Command<Database>> map;

    public CommandInvocationProtocol(Database arg) {
        this.arg = arg;
        activeUserName = null;
        terminate = false;
        map = new HashMap<String, Command<Database>>();
        getCommands();
    }

    @Override
    public String[] process(String[] msg) {
    	Command<Database> c = map.get(msg[0]);
        return c.execute(arg, msg);
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }
    
    private void getCommands(){
	    map.put("ADMINREG", getAdminRegister());
	    map.put("STUDENTREG", getStudentRegister());
	    map.put("LOGIN", getLoginRequest());
	    map.put("LOGOUT", getLogoutRequest());
	    map.put("COURSEREG", getRegisterToCourse());
	    map.put("KDAMCHECK", getCheckKdamCourse());
	    map.put("COURSESTAT", getPrintCourseStatus());
	    map.put("STUDENTSTAT", getPrintStudentStatus());
	    map.put("ISREGISTERED", getCheckIfRegister());
	    map.put("UNREGISTER", getUnregisterToCourse());
	    map.put("MYCOURSES", getCheckMyCurrentCourses());
	}
    
    private Command<Database> getAdminRegister() {
		Command<Database> c = (Database argument, String[] strings)->{
			String numOfCommand = "1";
			if(strings.length != 3)
				return getErrorReply(numOfCommand);
			String userName = strings[1];
			String password = strings[2];
			if(activeUserName == null && argument.registerUser(userName, password, true))
				return getAckReply(numOfCommand, "registered as admin");
			return getErrorReply(numOfCommand);
        };
		return c;
	}
    
	private Command<Database> getStudentRegister() {
		Command<Database> c = (Database argument, String[] strings)->{
			String numOfCommand = "2";
			if(strings.length != 3)
				return getErrorReply(numOfCommand);
			String userName = strings[1];
			String password = strings[2];
			if(activeUserName == null && argument.registerUser(userName, password, false))
				return getAckReply(numOfCommand, "registered as student");
			return getErrorReply(numOfCommand);
        };
		return c;
	}
    
    private Command<Database> getLoginRequest() {
    	Command<Database> c = (Database argument, String[] strings)->{
			String numOfCommand = "3";
			if(strings.length != 3)
				return getErrorReply(numOfCommand);
    		String userName = strings[1];
			String password = strings[2];
			if(activeUserName == null && argument.Login(userName, password)) {// not sure if second check necessary
				activeUserName = userName;
				isAdmin = argument.isAdmin(userName);
				return getAckReply(numOfCommand, "logged in successfully");
			}
    		return getErrorReply(numOfCommand);
        };
		return c;
	}

	private Command<Database> getLogoutRequest() {
		Command<Database> c = (Database argument, String[] strings)->{
			String numOfCommand = "4";
			if(activeUserName != null && argument.Logout(activeUserName)) {
				activeUserName = null;
				return getAckReply(numOfCommand, "logged out successfully");
			}
			return getErrorReply(numOfCommand);
        };
		return c;
	}

	private Command<Database> getRegisterToCourse() {
		Command<Database> c = (Database argument, String[] strings)->{
			String numOfCommand = "5";
			if(strings.length != 2)
				return getErrorReply(numOfCommand);
			int courseNum = Integer.valueOf(strings[1]);
			if(activeUserName != null & !isAdmin && argument.registerToCourse(activeUserName, courseNum))
				return getAckReply(numOfCommand, "registered to course: " + courseNum);
			return getErrorReply(numOfCommand);
        };
		return c;
	}

	private Command<Database> getCheckKdamCourse() {
		Command<Database> c = (Database argument, String[] strings)->{
			String numOfCommand = "6";
			if(activeUserName != null & strings.length == 2) {
				int courseNum = Integer.valueOf(strings[1]);
				List<Integer> courses = argument.checkKdamCourse(courseNum);
				if(courses != null) {
					String courseList = "[";
					for (Integer course : courses) {
						courseList += course + ",";
					}
					courseList = courseList.length() > 1 ? courseList.substring(0, courseList.length() - 1) : courseList;
					courseList += "]";
					return getAckReply(numOfCommand, courseList);
				}
				else {
					return getErrorReply(numOfCommand);
				}
			}
			return getErrorReply(numOfCommand);
        };
		return c;
	}

	private Command<Database> getPrintCourseStatus() {
		Command<Database> c = (Database argument, String[] strings)->{
			String numOfCommand = "7";
			if(activeUserName != null & isAdmin & strings.length == 2) {
				int courseNum = Integer.valueOf(strings[1]);
				String status = argument.getCourseStatus(courseNum);
				if(status != null)
					return getAckReply(numOfCommand, status);
				else return getErrorReply(numOfCommand);
			}
			return getErrorReply(numOfCommand);
        };
		return c;
	}

	private Command<Database> getPrintStudentStatus() {
		Command<Database> c = (Database argument, String[] strings)->{
			String numOfCommand = "8";
			if(activeUserName != null & isAdmin & strings.length == 2) {
				String studentName = strings[1];
				String ans = argument.getStudentStatus(studentName);
				if(ans != null)
					return getAckReply(numOfCommand, ans);
				else return getErrorReply(numOfCommand);
			}
			return getErrorReply(numOfCommand);
        };
		return c;
	}

	private Command<Database> getCheckIfRegister() {
		Command<Database> c = (Database argument, String[] strings)->{
			String numOfCommand = "9";
			try {
				if(activeUserName != null & !isAdmin & strings.length == 2){
					int courseNum = Integer.valueOf(strings[1]);
					boolean ans = argument.checkIfRegister(activeUserName, courseNum);
					String msgToClient = ans ? "REGISTERED" : "NOT REGISTERED";
					return getAckReply(numOfCommand, msgToClient);
				}
				return getErrorReply(numOfCommand);
			}catch (IllegalArgumentException e) {
				return getErrorReply(numOfCommand);
			}
        };
		return c;
	}

	private Command<Database> getUnregisterToCourse() {
		Command<Database> c = (Database argument, String[] strings)->{
			String numOfCommand = "10";
			if(strings.length != 2)
				return getErrorReply(numOfCommand);
			Integer courseNum = Integer.valueOf(strings[1]);
			if(activeUserName != null && argument.unregisterToCourse(activeUserName, courseNum))//if admin ,returns false since no admin can register to any course
				return getAckReply(numOfCommand, "unregistered from course: " + courseNum);
			return getErrorReply(numOfCommand);
        };
		return c;
	}

	private Command<Database> getCheckMyCurrentCourses() {
		Command<Database> c = (Database argument, String[] strings)->{
			String numOfCommand = "11";
			if(activeUserName != null & !isAdmin) {
				List<Integer> courses = argument.checkMyCurrentsCourses(activeUserName);
				String courseList = "[";
				for (Integer course : courses) {
					courseList += course + ",";
				}
				courseList = courseList.length() > 1 ? courseList.substring(0, courseList.length() - 1) : courseList;
				courseList += "]";
				return getAckReply(numOfCommand, courseList);
			}
			return getErrorReply(numOfCommand);
        };
		return c;
	}

	private String[] getAckReply(String numOfCommand, String msgToClient) {
		String[] reply = {"ACK", numOfCommand, msgToClient};
		return reply;
	}

	private String[] getErrorReply(String numOfCommand) {
		String[] reply = {"ERR", numOfCommand};
		return reply;
	}
}
