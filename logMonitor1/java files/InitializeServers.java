
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;


public class InitializeServers {

	//Decalring global variables for log generation
	private ArrayList<String> serverIds;
	
	/**
	 * default constructor
	 */
	public InitializeServers(){
		serverIds = new ArrayList<String>();
	}
	
	/**
	 * @param args
	 * 			takes data path as argument
	 */
	public static void main(String[] args){
		InitializeServers iServer = new InitializeServers();
		if(args.length == 1)
			iServer.initialiseServer(args[0]);
		else
			System.err.println("Enter data path correctly.");
	}

	/**
	 * @param dirPath
	 * 			takes inputPath as directory path in which log generation will be done
	 */
	private void initialiseServer(String dirPath) {
		try{
			System.out.println("Initialising Servers with log files..");
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
			File checkPath = new File(dirPath);
			//checks the existence of the input directory
			if(checkPath.exists()){
				logDir(dirPath);
				System.out.println("Server Initialization complete.");
			} else
				throw new InvalidPathException("Path does not exist. Try again", dirPath);
		} catch(InvalidPathException e1){
			System.err.println("Error: Given path: "+dirPath+" does not exist. Please restart and try again.");
		}
		catch(Exception e){
			System.err.println("Error: " + e);
		}
	}

	/**
	 * @param dirPath
	 * 			creates 1000 sub directories corresponding to 1000 predefined server IP's
	 */
	private void logDir(String dirPath) {
		for (int loop = 0; loop < serverIds.size(); loop++) {
			try {
				Runtime.getRuntime().exec("mkdir " + dirPath + "/" + serverIds.get(loop));
			} catch (Exception e) {
				System.out.println("IOFile");
				e.printStackTrace();
			}
		}
		writeLogs(dirPath);
	}

	/**
	 * @param dirPath
	 * 			spawns thread corresponding to each server IP
	 */
	private void writeLogs(String dirPath) {
		System.out.println("Writing. Please wait..");
		for (int loop = 0; loop < serverIds.size(); loop++) {
			new Thread(new WriteToLogs(dirPath, serverIds.get(loop))).start();
		}
	}
	
}

/**
 * @author Utkarsh
 *
 */
class WriteToLogs implements Runnable{

	String serverId, dirPath;
	FileWriter fileWrite;
	TreeSet<Long> timeStamps;
	
	/**
	 * @param dirPath
	 * @param serverId
	 */
	public  WriteToLogs(String dirPath, String serverId) {
		this.serverId = serverId;
		this.dirPath = dirPath;
		timeStamps = new TreeSet<Long>();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		try {
			fileWrite = new FileWriter(dirPath + "/" + serverId+ "/" + serverId+".txt");
			fileWrite.write(" timestamp           IP      cpu_id   usage");
			DateFormat insFormat = new SimpleDateFormat("MM/dd/yyyy");
			Calendar getIns = Calendar.getInstance();
			createTimestamps(insFormat.format(getIns.getTime()));
			
			enterLogs(serverId);
			fileWrite.close();
		} catch (IOException e) {
			System.err.println("Input directory path error: " + e);
		} 
	}

	/**
	 * @param serverId
	 * 			writes logs corresponding to each generated timestamp
	 */
	private void enterLogs(String serverId) {
		try{
			String tempLog = null;
			int cpuId = 0, usage = 0;
			for (Iterator iterator = timeStamps.iterator(); iterator.hasNext();) {
				Long tempTS = (Long) iterator.next();
				cpuId = (int)(Math.random()*100)%2;
				usage = (int)(Math.random()*((100)+1));
				tempLog = new String(tempTS+"	"+serverId+"	"+cpuId+"	"+usage);
				fileWrite.write("\n"+tempLog);
			}
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * @param insDate
	 * 			generates timestamp based on a random number chosen
	 */
	private void createTimestamps(String insDate) {
		try{
			DateFormat formatDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
			String startDate = insDate + " 00:00:00";		
			Date st = formatDate.parse(startDate);
		
			Integer numLogs = (int)(Math.random()*((300)+1))+2700;
			long startUT = st.getTime()/1000;
			long tempUT;
			while(timeStamps.size() != numLogs){
				tempUT = (long) Math.abs((Math.random()*((86399)+1)) + startUT);
				timeStamps.add(tempUT);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
