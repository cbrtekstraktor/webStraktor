/*
	  *
	  * Copyright 2014 - webStraktor
	  *
	  * webStraktor is free software; 
	  * Permission is granted to copy, distribute and/or modify this software 
	  * under the terms of the GNU General Public License as published 
	  * by the Free Software Foundation; either version 2 of the License, 
	  * or (at your option) any later version,
	  *
	  * You should have received a copy of the GNU General Public License 
	  * along with this program; if not, write to the Free Software Foundation, Inc.,
	  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
	  * GNU General Public License 
	  *
	  * This program is distributed in the hope that it will be useful,
	  * but WITHOUT ANY WARRANTY; without even the implied warranty of
	  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	  * GNU General Public License for more details.
	  *
	  * Contact details for copyright holder: webstraktor@gmail.com
	  * GNU General Public License : www.gnu.org/copyleft/gpl.html
	  *
	  * 
	 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;

public class webStraktorLogger {

	private webStraktorDateTime xdatetime = null;
	private String LogFileName = "null";
	private int LogLevel;
	private PrintStream LogFile=null;
	private boolean LogFileIsOpen = false;
	private ArrayList<String> errorLst = null;
	private String LoggingDateFormaat="HHMISSMIL";
	private int MaxLogLijnen = 2500;
	
	//
	//---------------------------------------------------------------------------------
	webStraktorLogger(int iL , String FNm , String TimeZone , String iDF)
	//---------------------------------------------------------------------------------
	{
		LogLevel = iL;
		LogFileName = FNm;
		LoggingDateFormaat=iDF;
		xdatetime = new webStraktorDateTime(TimeZone);
		errorLst = new ArrayList<String>();
		OpenLogs();
	}
	//
	//---------------------------------------------------------------------------------
	void setLogLevel(int i)
	//---------------------------------------------------------------------------------
	{
		LogLevel = i;
	}
	//
	//---------------------------------------------------------------------------------
	void Logit(int level , String sIn)
	//---------------------------------------------------------------------------------
	{
		if( level > LogLevel ) return;;
		String sLijn = xdatetime.DateTimeNow(LoggingDateFormaat) + " " + sIn;
		if( LogFileIsOpen ) LogFile.println(sLijn);
		System.out.println(sLijn);
		if( level == 0 ) {
			String s = sIn;
			errorLst.add(s);
		}
	}
	//
	//---------------------------------------------------------------------------------
	void OpenLogs()
	//---------------------------------------------------------------------------------
	{
		try{
			LogFile=new PrintStream(new FileOutputStream(LogFileName,true));
			LogFileIsOpen=true;
			Logit(1,"============ Logger started =================");
		}catch(Exception e){
			e.printStackTrace(System.err);
		}
	}
	//
	//---------------------------------------------------------------------------------
	void CloseLogs()
	//---------------------------------------------------------------------------------
	{
		if( LogFileIsOpen == false ) return;
		Logit(1,"============ Logger stopped =================");
		if( LogFile != null ) LogFile.close();
		System.out.println("Closed logfiles [" + LogFileName + "]" );
		PruneLog();
	}
	//
	//---------------------------------------------------------------------------------
	String getLastError()
	//---------------------------------------------------------------------------------
	{
	  int idx = errorLst.size();
	  if( idx == 0 ) return "";
	  return errorLst.get(idx-1);
	}
	//
	//---------------------------------------------------------------------------------
	String getErrorList()
	//---------------------------------------------------------------------------------
	{
	  String sOut="";
	  for(int i=0;i<errorLst.size();i++) sOut = sOut + "\n" + errorLst.get(i);
	  return sOut;
	}
	//
	//---------------------------------------------------------------------------------
	private void PruneLog()
	//---------------------------------------------------------------------------------
	{
		int loglijnen=0;
		String sLijn = null;
		String LogFileNameTemp = LogFileName + ".new";
        // counter
		try {
			  File inFile  = new File(this.LogFileName);  // File to read from.
	       	  BufferedReader reader = new BufferedReader(new FileReader(inFile));
	       	  while ((sLijn=reader.readLine()) != null) { loglijnen++; }
	          reader.close();
	        }
		catch (Exception e) {
				System.out.println("Error reading file [" + LogFileName + "]");
				return;
		}
		
		int skip = loglijnen - MaxLogLijnen;
		
		if( skip < 0 ) return;
		//
		loglijnen =0;
		try {
			  File inFile  = new File(this.LogFileName);  // File to read from.
	       	  BufferedReader reader = new BufferedReader(new FileReader(inFile));
	       	  LogFile=new PrintStream(new FileOutputStream(LogFileNameTemp,true));
	       	  LogFile.println(xdatetime.DateTimeNow(LoggingDateFormaat) + "       ======= LOG TRUNCATED =======");
	       	  LogFile.println(xdatetime.DateTimeNow(LoggingDateFormaat) + "       Lines truncated " + skip);
	       	  while ((sLijn=reader.readLine()) != null) { 
	       		  loglijnen++;
	       		  if( loglijnen < skip ) continue;
	       		  LogFile.println(sLijn);
	       	  }
	       	  LogFile.println(xdatetime.DateTimeNow(LoggingDateFormaat) + "       ======= LOG TRUNCATED =======");
	       	  LogFile.println(xdatetime.DateTimeNow(LoggingDateFormaat) + "       Lines truncated " + skip);
	       	  LogFile.close();
	          reader.close();
	        }
		catch (Exception e) {
				System.out.println("Error reading file [" + LogFileName + "]");
				return;
		}
		//
		// delete LOG
		File FObj = new File(LogFileName);
        if ( FObj.isFile() != true ) {
        	return;
        }
        if ( FObj.getAbsolutePath().length() < 10 ) {
        	return;  // domme veiligheid
        }
        FObj.delete();
        // rename new naar log
        File oldFile = new File(LogFileNameTemp); 
        //Now invoke the renameTo() method on the reference, oldFile in this case
        oldFile.renameTo(new File(LogFileName));
		System.out.println("Log [" + LogFileName + "] has been truncated to [" + MaxLogLijnen +"] lines by deleting [" + skip + "] lines");
	}
}
