
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;


/**
 * @author Utkarsh
 *
 */
public class QueryFiles {

	//Decalring global variables for query simulation
	String dirPath;
	Scanner scan;
	ArrayList<String> serverIds;
	long startTime, endTime;
	BufferedReader buffReader;
	DateFormat formatDate1, formatDate2;
	

	/**
	 * default constructor
	 */
	public QueryFiles(){
		serverIds = new ArrayList<String>();
		scan = new Scanner(System.in);
		buffReader = null;
		formatDate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		formatDate2 = new SimpleDateFormat("MM/dd/yyyy HH:mm");
	}
	
	/**
	 * @param args
	 * 			takes data path as argument
	 */
	public static void main(String[] args){
		QueryFiles queryFile = new QueryFiles();
		if(args.length == 1)
			queryFile.initialize(args[0]);
		else
			System.err.println("Enter data path correctly.");
	}
	
	/**
	 * @param inputPath
	 * 				takes inputPath as directory path in which queries will be run
	 */
	private void initialize(String inputPath) {
		try{
			//forms 1000 server IP's which are predefined in the system
			String StartId = "192.168.";
			String tempId = null;
			int prefix1 = 1;
			int prefix2 = 0;
			for (int loop = 0; loop < 1000;) {
				while(prefix2<256 && loop<1000){
					tempId = new String(StartId + prefix1+ "." + prefix2);
					prefix2++;
					loop++;
					serverIds.add(tempId);
				}
				prefix1++;
				prefix2 = 0;
			}
			File checkPath = new File(inputPath);
			//checks the existence of the input directory
			if(checkPath.exists()){
				dirPath = inputPath;
				System.out.println("Monitoring Initialization complete.");
				startQuery();
			} else
				throw new InvalidPathException("Path does not exist. Try again", dirPath);
		} catch(InvalidPathException e1){
			System.err.println("Error: Given path: "+dirPath+" does not exist. Please restart and try again.");
		}
		catch(Exception e){
			System.err.println("Error: " + e);
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void startQuery(){
		//declares variables for forming the query and creating the output string
		String getInput = null;
		String readFile = null;
		String outputString = "", header = "";
		boolean noRecord = true;
		String[] tempArray, readArray;
		//instructions for the user
		System.out.println("Enter query in format: QUERY <serverIP> <cpuId> <yyyy-MM-dd> <startingTime> <yyyy-MM-dd> <endTime>");
		System.out.println("To exit the system: EXIT");
		System.out.print(">");
		getInput = scan.nextLine();
		//exit when user prompts
		while(!getInput.equalsIgnoreCase("exit")){
			try{
				tempArray = getInput.split(" ");
				//check the usage of the input query
				checkUsage(tempArray);
				outputString = "";
				noRecord = true;
				//find the corresponding server log file
				buffReader = new BufferedReader(new FileReader(dirPath + "/" + tempArray[1]+ "/" + tempArray[1] + ".txt"));
				readFile = buffReader.readLine();
				readFile = buffReader.readLine();
				header = "CPU" + tempArray[2] + " usage on " + tempArray[1];
				System.out.println(header);
				//read the file
				while(readFile != null){
					readArray = readFile.split("\t");
					//check if the records found in the given time period
					if(checkCpuId(tempArray, readArray) && checkTime(readArray)){
						//form the output string
						if(outputString.length()<1)
							outputString = outputString+formOutput(readArray);
						else
							outputString = outputString+","+formOutput(readArray);
						noRecord = false;
					} else if(Long.parseLong(readArray[0]) > endTime)
						break;
					//do not read the file if time period in logs after the end time of input query
					if(Long.parseLong(readArray[0]) > endTime){
						break;
					}
					readFile = buffReader.readLine();
				}
				//if no record found inform the user
				if(noRecord == true){
					System.out.println("No record of usage on given cpuId has been found in the time period specified. Try again.");
				} else //else print the output
					System.out.println(outputString);
			} catch(InputNotMatched e1){ //throw customized input not matched exception
				e1.printStackTrace();
			} catch (FileNotFoundException e2) { //in case file not found throw the exception
				System.err.println("Usage Error: Arguments are not matched. File not found. Please refer to Query Instructions");
				System.err.println("Enter query in format: QUERY <serverIP> <cpuId> <yyyy-MM-dd> <startingTime> <yyyy-MM-dd> <endTime>");
				System.err.println("<serverIP>: Server IP should be present in the records");
			} catch (IOException e3) {
				System.err.println("IOError: " + e3);
				e3.printStackTrace();
			} catch(Exception e){
				System.err.println("Error: " + e);
				e.printStackTrace();
			}
			System.out.print(">");
			getInput = scan.nextLine();
		}
		System.out.println("Exiting");
	}

	/**
	 * @param readArray
	 * @return
	 * 		check if the log matches the time period in the given query and return true if matched
	 */
	private boolean checkTime(String[] readArray) {
		if((Long.parseLong(readArray[0]) >= startTime && Long.parseLong(readArray[0]) <= endTime))
			 return true;
		return false;
	}

	/**
	 * @param tempArray
	 * @param readArray
	 * @return
	 * 		check if the log matches the cpu_id in the given query and return true if matched
	 */
	private boolean checkCpuId(String[] tempArray, String[] readArray) {
		if(Integer.parseInt(tempArray[2]) == Integer.parseInt(readArray[2]))
			return true;
		return false;
	}

	/**
	 * @param readArray
	 * @return
	 * 		form the output of the log matched and return it in string format
	 */
	private String formOutput(String[] readArray) {
		long unixTime = Long.parseLong(readArray[0]);
		int usage = Integer.parseInt(readArray[3]);
		Date readDate = new Date(unixTime*1000);
		String formStr = "("+formatDate1.format(readDate) + ", " + usage+"%)";
		return formStr;
	}

	/**
	 * @param tempArray
	 * @throws InputNotMatched
	 * 					check the usage of the input query and throw and exception if query entered is wrong
	 */
	private void checkUsage(String[] tempArray) throws InputNotMatched {
		if(tempArray.length != 7)
			throw new InputNotMatched();
		if(!tempArray[0].equalsIgnoreCase("query"))
			throw new InputNotMatched();
		if(!serverIds.contains(tempArray[1]))
			throw new InputNotMatched();
		if(!((Integer.parseInt(tempArray[2]) != 0) || (Integer.parseInt(tempArray[2]) != 1)))
			throw new InputNotMatched();
		System.out.println("5");
		String startT = new String(tempArray[3] + " " + tempArray[4]);
		String endT = new String(tempArray[5] + " " + tempArray[6]);
		Date st = null, et = null;
		try {
			st = formatDate1.parse(startT);
			et = formatDate1.parse(endT);
		} catch (ParseException e) {
			System.err.println("Usage Error: Arguments are not matched. Please refer to Query Instructions");
			System.err.println("<yyyy-MM-dd>: Should match the correct date format");
			System.err.println("<startTime> and <endTime> should be valid inputs");
			e.printStackTrace();
		}
		startTime = st.getTime()/1000;
		endTime = et.getTime()/1000;
		if(startTime > endTime)
			throw new InputNotMatched();
	}
	
	
}

/**
 * @author Utkarsh
 *
 */
class InputNotMatched extends Throwable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see java.lang.Throwable#printStackTrace()
	 * 									throw customized exception if input query is wrong
	 */
	@Override
	public void printStackTrace() {
		System.err.println("Usage Error: Arguments are not matched. Please refer to Query Instructions");
		System.err.println("Enter query in format: QUERY <serverIP> <cpuId> <yyyy-MM-dd> <hh:mm>(starting time) <yyyy-MM-dd> <hh:mm>(ending time)");
		System.err.println("<yyyy-MM-dd>: Should match the correct date format");
		System.err.println("<serverIP>: Server IP should be present in the records");
		System.err.println("<cpuId>: cpu Id should be 0 or 1");
		System.err.println("<startTime> and <endTime> should be valid inputs");
		System.err.println("<endTime> should be after <startTime>");
		System.err.println("To exit the system: EXIT");
	}
	
}
