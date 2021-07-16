package bgu.spl.net.srv;

import java.util.ArrayList;
import java.util.Comparator;

public class User {
	
	private String userName;	
	private String password;
	private boolean isAdmin;
	private boolean Connected;
	private ArrayList<Integer> courses;

	
	public User(String _userName, String _password, boolean _isAdmin ) {
		userName = _userName;
		password = _password;
		isAdmin = _isAdmin;
		Connected = false;
		if(!isAdmin)
			courses = new ArrayList<Integer>();
		else
			courses = null;
	}
	
	public boolean isAdmin(){
		return isAdmin;
	}
	
	public boolean isCorrectPassword(String _password){
		return password.equals(_password);
	}
	
	/**
	 * 
	 * @param courseNum
	 * @return true when successful
	 */
	public boolean addCourse(Integer courseNum){
		if(!isAdmin() && !courses.contains(courseNum))
			return courses.add(courseNum);
		return false;
	}
	
	/**
	 * 
	 * @param courseNum
	 * @return true when successful
	 */
	public boolean removeCourse(Integer courseNum){
		if(!isAdmin())
			return courses.remove(courseNum);
		return false;
	}
	
	/**
	 * 
	 * @param courseNum
	 * @return
	 */
	public boolean checkIsRegister(Integer courseNum){
		if(!isAdmin())
			return courses.contains(courseNum);
		return false;
	}
	
	public ArrayList<Integer> getCourses(){
		return courses;
	}
	
	public boolean isConnected() {
		return Connected;
	}
	
	/**
	 * 
	 * @return true when is not already connected
	 */
	public boolean connect() {
		if(!Connected)
			Connected = true;
		else return false;
		return true;
	}
	
	/**
	 * 
	 * @return true when is not already disconnected
	 */
	public boolean disConnect() {
		if(Connected)
			Connected = false;
		else return false;
		return true;
	}
}
