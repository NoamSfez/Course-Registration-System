package bgu.spl.net.srv;

import java.util.ArrayList;
import java.util.List;

public class Course {

	private Integer courseNum;
	private String courseName;
	private final Integer numOfMaxStudents;
	private List<Integer> KdamCoursesList;
	private List<String> registeredStudents;
	
	public Course(Integer _courseNum, String _courseName, Integer _numOfMaxStudent, List<Integer> _KdamCoursesList) {
		courseNum = _courseNum;
		courseName = _courseName;
		numOfMaxStudents = _numOfMaxStudent;
		KdamCoursesList = _KdamCoursesList;
		registeredStudents = new ArrayList<String>();
	}

	public boolean addStudent(String StudentName) {
		if (registeredStudents.size()<numOfMaxStudents)
			return registeredStudents.add(StudentName);
		return false;
	}
	
	public boolean removeStudent(String StudentName) {
		if (registeredStudents.size()<numOfMaxStudents)
			return registeredStudents.remove(StudentName);
		return false;
	}
	
	public List<String> sortedStudents(){
		registeredStudents.sort((String a, String b)->{
			return a.compareTo(b);
			});
		return registeredStudents;
	}
	
	
	public String getSeatsAvailable() {
		return (numOfMaxStudents-registeredStudents.size())+ "/" + numOfMaxStudents;
	}
	
	public String getCourse() {
		return "(" + courseNum + ") " + courseName;
	}
	
	public List<Integer> getKdams(){
		return KdamCoursesList;
	}
	
	
	
}
