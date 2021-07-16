package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class CommandEncoderDecoder implements MessageEncoderDecoder<String[]> {

    private byte[] bytes;
    private int length;
    private short opcode;
    private int currNumOfArgs;
    private boolean firstByte;
    private String[] commands;
    private HashMap<String, Integer> numOfArgs;
    private String[] ans;

    public CommandEncoderDecoder (){
        initialize();
        commands = new String[]{"ADMINREG","STUDENTREG","LOGIN","LOGOUT","COURSEREG","KDAMCHECK","COURSESTAT","STUDENTSTAT","ISREGISTERED","UNREGISTER","MYCOURSES","ACK","ERR"};
        numOfArgs = new HashMap<String, Integer>();
        numOfArgs.put(commands[0], 3);
        numOfArgs.put(commands[1], 3);
        numOfArgs.put(commands[2], 3);
        numOfArgs.put(commands[3], 1);
        numOfArgs.put(commands[4], 2);
        numOfArgs.put(commands[5], 2);
        numOfArgs.put(commands[6], 2);
        numOfArgs.put(commands[7], 2);
        numOfArgs.put(commands[8], 2);
        numOfArgs.put(commands[9], 2);
        numOfArgs.put(commands[10], 1);
        numOfArgs.put(commands[11], 3);
        numOfArgs.put(commands[12], 2);
    }

    private void initialize() {
        bytes = new byte [1<<10];
        length = 0;
        opcode = -1;
        currNumOfArgs = 0;
        ans = null;
    }

    @Override
    public String[] decodeNextByte(byte nextByte) {
    	String[] ans2 = null;
    	boolean fresh = false;//indicates whether opcode assign in this run of the function
        if (opcode == -1){
            bytes[length++] = nextByte;
            if (length == 2){
                opcode = twoBytesToShort(Arrays.copyOfRange(bytes,0,2));
                ans = new String[numOfArgs.get(commands[opcode-1])];
                ans[currNumOfArgs++] = commands[opcode-1];
                fresh = true;
                length = 0;
            }
        }
        if(!fresh & opcode != -1 && numOfArgs.get(commands[opcode-1])==2 & opcode != 8) {
        	bytes[length++] = nextByte;

            if (length == 2){
            	short courseNum = twoBytesToShort(Arrays.copyOfRange(bytes,0,2));
                ans[currNumOfArgs++] = Short.toString(courseNum);
                fresh = true;
                length = 0;
            }
        }
        if(fresh && numOfArgs.get(commands[opcode-1]) == currNumOfArgs) {
            ans2 = ans;
            initialize();
            return ans2;
        }
        
        if(!fresh && opcode != -1 && (numOfArgs.get(commands[opcode-1])==3 | opcode == 8) && decodeByteByOpcode(nextByte)){
        	ans2 = ans;
            initialize();
            return ans2;
        }
        return null;
    }

    private boolean decodeByteByOpcode(byte nextByte) {
    	if (nextByte != '\0' & opcode < 12) 
        	pushByte(nextByte);
    	else {//the word is complete and ready in the buffer
        	ans[currNumOfArgs++] = new String(bytes,0,length, StandardCharsets.UTF_8);
            length = 0;
        }
        return currNumOfArgs == numOfArgs.get(commands[opcode-1]);//are there no more args to get?
    }
    
    private void pushByte(byte nextByte) {
        if (length >= bytes.length)
            bytes = Arrays.copyOf(bytes, length * 2);
        bytes[length++] = nextByte;
    }

    private short twoBytesToShort(byte[] arr) {
//    	System.out.println("got message");
        return ByteBuffer.wrap(arr).getShort();
    }

    private short getOpcode(String str) {
    	for (short i = 0;  i< commands.length; i++)
			if (commands[i].equals(str))
				return ++i;//return the next num since the opcode start from 1
    	return -1;
    }

    @Override
    public byte[] encode(String[] message) {
    	int space = 2;
    	for (int i = 1 ; i < message.length ; i++) {
    		if((message[0].equals("ACK") | message[0].equals("ERR")) & i < 2) {
    			space += 2;
    		}else
    			space += message[i].length() + 1;//calculate how much space needed for the strings and zero bytes
		}
        ByteBuffer sb = ByteBuffer.allocate(space);
        sb.putShort(getOpcode(message[0]));
        int placeToContinue = 1;
        if((message[0].equals("ACK") | message[0].equals("ERR"))) {
            short s = Short.valueOf(message[1]);
        	sb.putShort(s);
    		placeToContinue = 2;
    	}
        for(int i = placeToContinue; i < message.length; i++) {
            sb.put(message[i].getBytes(StandardCharsets.UTF_8));
            sb.put((byte) 0);
        }
        return sb.array();
    }
    
    public static void main(String[] args) {
    	byte [] arr = new byte[1<<10];
    	CommandEncoderDecoder encDec = new CommandEncoderDecoder();
    	
//    	//ENCODE
//    	String[] mess = new String[]{"LOGIN","NOA","a123"};
//    	arr = encDec.encode(mess);
//    	for (int i = 0; i < arr.length; i++)
//			System.out.print(arr[i]);
//    	
//    	//DECODE
//    	String [] ans = null;
//    	for (int i = 0; i < arr.length && ans == null; i++) {
//    		ans = encDec.decodeNextByte(arr[i]);
//		}
//    	System.out.print('\n');
//    	for (String string : ans) {
//			System.out.print(string+" ");
//		}
    	
    	String[][] test = new String [][] {
    		//{"ACK","6","SUCCESS OF THE YEAR"},
    		{"STUDENTREG","NOA","a123"},
			{"ADMINREG","AVI","a123"},
			//{"STUDENTREG","NOA","a123"}, // should be err
			{"LOGIN","NOA","a123"},
			{"COURSEREG","101"},
			{"COURSEREG","102"},
			{"COURSEREG","103"},
			{"MYCOURSES"},
			//{"STUDENTSTAT","NOA"}, // should be err
			{"LOGOUT"},
			{"LOGIN","AVI","a123"},
			//{"COURSEREG","101"}, // should be err
			{"STUDENTSTAT","NOA"},
			{"COURSESTAT","101"},
			{"LOGOUT"},
			{"LOGIN","NOA","a123"},
			{"ISREGISTERED","103"},
			//{"ERR","4"}
			};
			
		for (String[] strings : test) {
			boolean equal = true;
			arr = encDec.encode(strings);
			int i = 0;
			String[] str;
			while ((str = encDec.decodeNextByte(arr[i])) == null)
				i++;
			System.out.println(encDec.arrToString(str));
			System.out.println(encDec.arrToString(strings));
			for(int j = 0 ; j < str.length ; j++)
				if(!str[j].equals(strings[j]))
					equal = false;
			System.out.println(equal);
		}
	}
    
    ////just for fun
    private String arrToString(String[] str) {
    	String result = "";
    	for (String string : str) {
			result += string + " ";
		}
    	if(result.length()>0)
    		result = result.substring(0,result.length()-1);
    	return result;
    }

}
