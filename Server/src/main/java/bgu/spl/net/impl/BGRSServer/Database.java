package bgu.spl.net.impl.BGRSServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.srv.Course;
import bgu.spl.net.srv.User;

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {

	private ConcurrentHashMap<String,User> Users;
	private ConcurrentHashMap<Integer,Course> Courses;
	private ConcurrentHashMap<Integer, List<Integer>> KdamCoursesLists;
	private List<Integer> sortedCourses;
	
	
	//to prevent user from creating new Database
	private Database() {
		Users = new ConcurrentHashMap<String, User>();
		Courses = new ConcurrentHashMap<Integer, Course>();
		KdamCoursesLists = new ConcurrentHashMap<Integer, List<Integer>>();
		sortedCourses = new ArrayList<Integer>();
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static Database getInstance() {
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder{
		private static Database instance = new Database();
	}
	
	/**
	 * loades the courses from the file path specified 
	 * into the Database, returns true if successful.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public boolean initialize(String coursesFilePath){////////////////////////////////////////////
		try {
			String f = new File(".").getCanonicalPath() + "/" + coursesFilePath;
			try (BufferedReader br = new BufferedReader(new FileReader(f))){
			    String line;
				String[] part;
				synchronized (sortedCourses) {
					while((line = br.readLine()) != null) {
				    	part = line.split("\\|");
						Integer courseNum = Integer.valueOf(part[0]);
						String courseName = part[1];
						String[] kdams = part[2].substring(1, part[2].length() - 1).split("\\,");
						List<Integer> kdamsNums = new ArrayList<Integer>();
						for (int i = 0 ; i < kdams.length ; i++) {
							if(kdams[i].length()>0)
								kdamsNums.add(Integer.valueOf(kdams[i]));
						}
						Integer numMaxStudents = Integer.valueOf(part[3]);
						Course course = new Course(courseNum, courseName, numMaxStudents, kdamsNums);
						Courses.put(courseNum, course);
						sortedCourses.add(courseNum);
						KdamCoursesLists.put(courseNum, kdamsNums);
					}
				}

				return true;
			} catch (IOException e) {
				return false;
			}
		} catch (IOException e1) {
			return false;
		}			
		
		
	}
	
	public boolean registerUser(String userName, String password, Boolean isAdmin) {
		if (!Users.containsKey(userName)) {
			Users.put(userName, new User(userName, password, isAdmin));
			return true;
		}
		return false;		
	}
	
	public boolean registerToCourse(String studentName, Integer courseNum) {
		User user = Users.get(studentName);
		if(user != null & Courses.containsKey(courseNum)) {
			synchronized (user) {
				Course course = Courses.get(courseNum);
				synchronized (course) {
					if(isKdamRegistered(user, course) && !user.checkIsRegister(courseNum)) //the only place where check weather is an admin (admin cannot register course)
						return user.addCourse(courseNum) && course.addStudent(studentName);
				}
			}
		}
		return false;	
	}
	
	public boolean isAdmin(String studentName) {
		User user = Users.get(studentName);
		if(user != null) {
			synchronized (user) {
				return user.isAdmin();
			}
		}
		return false;
	}

	public List<Integer> checkKdamCourse (Integer courseNum){
		if (Courses.containsKey(courseNum)) {//no need for synchronization here
			List<Integer> courses = Courses.get(courseNum).getKdams();
			courses.sort((Integer a,Integer b)->{
				Integer ind1 = sortedCourses.indexOf(a);
				Integer ind2 = sortedCourses.indexOf(b);
				return ind1.compareTo(ind2);				
			});
			return courses;
		}
			
		return null;
	}
	
	public String getCourseStatus(Integer courseNum) {
		if (Courses.containsKey(courseNum)) {
			Course myCourse = Courses.get(courseNum);
			synchronized (myCourse) {
				return "Course: " + myCourse.getCourse() + "\nSeats Available: " + myCourse.getSeatsAvailable()+ "\nStudents Registered: " + myCourse.sortedStudents().toString();
			}
		}
		return null;
	}
	
	public String getStudentStatus(String studentName){
		User user = Users.get(studentName);
		if (user != null) {
			synchronized (user) {
				if(!user.isAdmin())
					return "Student: " + studentName + "\nCourses: " + checkMyCurrentsCourses(studentName).toString();
			}
		}
		return null;
	}

	public boolean checkIfRegister(String userName, Integer courseNum) {
		User user = Users.get(userName);
		if (user != null) {
			synchronized (user) {
				if(sortedCourses.contains(courseNum))//doesn't check weather is an Admin.
					return user.checkIsRegister(courseNum);
			}
		}
		throw new IllegalArgumentException("this user/course isn't exists");
	}
	
	public boolean unregisterToCourse(String userName, Integer courseNum) {
		User user = Users.get(userName);
		if (user != null) {
			synchronized (user) {
				if(user.checkIsRegister(courseNum) && user.removeCourse(courseNum)) {//doesn't check here weather is an Admin.
					return Courses.get(courseNum).removeStudent(userName);
				}
			}
		}
		return false;
	}
	
	public List<Integer> checkMyCurrentsCourses(String userName){
		User user = Users.get(userName);
		if (user != null) {
			synchronized (user) {
				if(!user.isAdmin()) {
					ArrayList<Integer> courses = user.getCourses();
					synchronized (sortedCourses) {
						courses.sort((Integer a,Integer b)->{
							Integer ind1 = sortedCourses.indexOf(a);
							Integer ind2 = sortedCourses.indexOf(b);
							return ind1.compareTo(ind2);				
						});
					}
					return courses;
				}
			}
		}
		return null;
	}
	
	public boolean isValidUser(String userName, String password) {
		User user = Users.get(userName);
		if(user != null) {
			synchronized (user) {
				return user.isCorrectPassword(password);
			}
		}
		return false;
	}

	/**
	 * 
	 * @param userName
	 * @param password
	 * @return true if user exists and connected successfully
	 */
	public boolean Login(String userName, String password) {
		User user = Users.get(userName);
		if(user != null) {
			synchronized (user) {
				if(user.isCorrectPassword(password))
					return user.connect();
			}
		}
		return false;
	}

	/**
	 * 
	 * @param userName
	 * @return true if user exists and disconnected successfully
	 */
	public boolean Logout(String userName) {
		User user = Users.get(userName);
		if(user != null) {
			synchronized (user) {
				return user.disConnect();
			}
		}
		return false;
	}
	
	private boolean isKdamRegistered(User user, Course course) {
		for (Integer courseNum : course.getKdams()) {
			if(!user.checkIsRegister(courseNum))
				return false;
		}
		return true;
	}
}
