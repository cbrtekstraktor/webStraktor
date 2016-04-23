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

import java.net.URL;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;


public class webStraktorParseController {

	webStraktorSettings xMSet = null;
	webStraktorHTML xHTML = null;
	private webStraktorDateTime xDateTime = null;
	private webStraktorParseModel iParseSourceCodeFile = null;
	private webStraktorParseURL iparseURL = null;
	private webStraktorLogger iLogger = null;
	//
	ArrayList<parseFunction> lstFuncties = null;
	private int LogLevel = 5;
	private String sDIR = null;
	private String sMainUrl=null;
	private String BaseUrl=null;
	private String Protocol=null;
	private ArrayList<String> cmdLineParams = null;
	private String baseOutputFileName = null;
	private String HTMLDumpBaseFileName = null;
	private String HTMLTempBaseFileName = null;
	private String HTMLBlobDir = null;
	private String HTMLResultFileName = null;
	private String SourceCodeFileName   = null;
	private String HTMLConsolidatedFileName = null;
	private String LogFileName = null;
	private String TraceFileName = null;
	private int tellertje=0;
	private boolean InitOK=false;
	private boolean isSimulation=false;
	private boolean bUseProxy=false;
	private String sProxyName="";
	private String Browser="";
	private String NormalizedSourceCodeName="";
	private int diepte=-1;
	private boolean keepOutputFile=true;
	private boolean consolidate=true;
	private boolean useRobots=false;
	private int fetchteller=0;
	private int blobteller=0;
	private boolean naarLatin1 = true;

	//
	//---------------------------------------------------------------------------------
	void LogIt(int level, String sIn)
	//---------------------------------------------------------------------------------
	{
		if( iLogger != null ) iLogger.Logit(level, "CTR - " + sIn); else System.out.println(sIn);
	}
	//
	//---------------------------------------------------------------------------------
	void Error( String sIn)
	//---------------------------------------------------------------------------------
	{
		LogIt(0,sIn);
	}
	//
	//---------------------------------------------------------------------------------
	String getLastError()
	//---------------------------------------------------------------------------------
	{
		return iLogger.getLastError();
	}
	//
	//---------------------------------------------------------------------------------
	String getErrorList()
	//---------------------------------------------------------------------------------
	{
		return iLogger.getErrorList();
	}
	//
	//---------------------------------------------------------------------------------
	webStraktorParseController(webStraktorSettings iX , String[] args)
	//---------------------------------------------------------------------------------
	{
		xMSet = iX;
		xDateTime = new webStraktorDateTime(xMSet.TimeZone);
		cmdLineParams = new ArrayList<String>();
		lstFuncties = new ArrayList<parseFunction>();
	    //  -C <configFile.xml> -D <DirectoryName> -V 10 -P <sourcecode>  params
		evalueerParam( args );
		// only now create a logger
		iLogger = new webStraktorLogger(this.getLogLevel(),this.LogFileName,xMSet.TimeZone,xMSet.LoggingDateFormaat);
		// create the HTML parser
		xHTML = new webStraktorHTML(xMSet,xMSet.getFullReportHTMLMode(),this);
		if( InitOK == false ) Error("ERROR - initializing");
	}
	//
	//---------------------------------------------------------------------------------
	boolean execController()
	//---------------------------------------------------------------------------------
	{   
		iParseSourceCodeFile = new webStraktorParseModel(xMSet,this.iLogger,this.xHTML);
		if( iParseSourceCodeFile.LoadAndParseSourceCodeFile( this.getSourceCodeFileName(), this.lstFuncties) == false ) {
			Error("Error when parsing [" + this.getSourceCodeFileName() + "]");
			iLogger.CloseLogs();
			return false;
		}
		//
		// post verwerking op de controller met gegevens uit de Source
		this.setLogLevel(iParseSourceCodeFile.getLogLevel());
		this.setIsSimulation(iParseSourceCodeFile.getSimulation());
		this.setUseProxy(iParseSourceCodeFile.getUseProxy());
		this.setProxyName(iParseSourceCodeFile.getProxyName());
		this.consolidate = iParseSourceCodeFile.getConsolidate();
		this.keepOutputFile = iParseSourceCodeFile.getKeepOutput();
		this.setUserAgent(iParseSourceCodeFile.getBrowser());
		this.setRobots(iParseSourceCodeFile.getRobots());
		xMSet.setLogLevel(this.getLogLevel());
		iLogger.setLogLevel(this.getLogLevel());
		// Maak het tracemodel
		xMSet.xTrc.maakTraceModel(this);
		//
		// Er is ofwel een URl op de main; ofwel een instructie op de main met FORM
	    parseFunction iFunc = this.getForeach("main");
		if( iFunc == null ) {
				Error("Could not find main in parser program");
				iLogger.CloseLogs();
				return false;
		}
		// Is there a HTML Form
		parseInstruction iStruc = this.getHTMLForm(iFunc);
		if( (iFunc.URL == null) && (iStruc==null) ) {
			Error("Could not find URL on main nor a HTMlForm");
			iLogger.CloseLogs();
			return false;
		}
		if (iStruc != null ) {
		 if( (iFunc.URL != null) && (iStruc.Form.FormURL!=null) ) {
			Error("There is a URL on main and HTML form on main. Only one URL is supported");
			iLogger.CloseLogs();
			return false;
		 }
		}
		String sTempUrl = null;
		// URL op main
		if( iFunc.URL != null )
		{
			//
			// Initialiseer de URL informatie op de Controller met info uit uit de SourceCode en vervang de params
			sTempUrl = this.substituteParamsOnString(iFunc.URL);
		}
		// URL op een FORM
		else {
			if( iStruc.Form.FormURL.indexOf("$1") >= 0 ) sTempUrl = this.substituteParamsOnString(iStruc.Form.FormURL);
								                    else sTempUrl = iStruc.Form.FormURL;
			//isHTMLForm = true;
		}
		if( sTempUrl == null ) {
			Error("Error when substituting params in main URL [" + sTempUrl + "]");
			iLogger.CloseLogs();
			return false;
		}
		LogIt(5,"MAIN URL [" + sTempUrl + "]");
		if( this.setBaseUrlAndProtocol(sTempUrl) == false ) {
			Error("Error on the URL [" + sMainUrl + "]");
			iLogger.CloseLogs();
			return false;
		}
		//
		this.sho();
		//
		// Create the URL parser
		iparseURL = new webStraktorParseURL(xMSet,this,this.iLogger);
		//
		// process the source script
		if( iparseURL.processURL(sMainUrl,iStruc) == false ) {
			Error("Error when processing [" + sMainUrl + "] with script [" + this.getSourceCodeFileName() + "]" );
			iLogger.CloseLogs();
			return false;
		}
		//
		// Merge de out files
		//
		if( this.consolidate == true ) {
			if( MergeOutputFiles() == false ) {
				iLogger.CloseLogs();
				return false;
			}
		}
		// converteer de gemergede file naar UTF8 of LATIN1
		webStraktorIconv iconvertor = new webStraktorIconv(xMSet,iLogger);
		String FXML = VervangSuffix( this.HTMLConsolidatedFileName , "xml");
		FXML = xMSet.xU.RemplaceerNEW(FXML,"\\Out\\Ascii\\","\\Out\\utf8\\");
		iconvertor.iconv(this.HTMLConsolidatedFileName,FXML,naarLatin1);
		
		// if KeepOutputfile move file and append params to name
		if( this.keepOutputFile ) {
			String sKeptFile = KeepFile();
			if( sKeptFile == null ) {
				iLogger.CloseLogs();
				return false;  
			}
			// naar binair
			FXML = VervangSuffix( sKeptFile , "xml" );
			FXML = xMSet.xU.RemplaceerNEW(FXML,"\\Out\\Ascii\\","\\Out\\utf8\\");
			//LogIt(5,"SAVE-->" + FXML);
			xMSet.xTrc.traceFunctionFile( this.getForeach("main").OID , FXML);
			iconvertor.iconv( sKeptFile , FXML , naarLatin1);
		}
		//
		xMSet.xTrc.closeTrace();
		iLogger.CloseLogs();
		xMSet.restoreRobotsSettings();
		return true;
	}
	//
	//---------------------------------------------------------------------------------
	void evalueerParam(String[] args)
	//---------------------------------------------------------------------------------
	{
		String sCode = "??";
		int verwacht=0;
	    // -D <DirectoryName> -P <sourcecode>  params
		// loglevels, simul and proxy are on the program
		for(int i=0;i<args.length;i++)
		{
			if ( args[i].trim().compareTo("-D")==0) {
				 verwacht = 1;
				 continue;
			}
			if ( args[i].trim().compareTo("-P")==0) {
				 verwacht = 3;
				 continue;
			}
			if ( args[i].trim().compareTo("-C")==0) {
				 verwacht = 4;
				 continue;
			}
			switch( verwacht )
			{
				case 1 : {
					sDIR = args[i];
					verwacht=0;
					break;
				}
				case 3 : {
					sCode = args[i];
					verwacht=0;
					break;
				}
				case 4 : {  // - C negeer
					verwacht=0;
					break;
				}
				default : {
					// gewone arg
					cmdLineParams.add(args[i]);
					break;
				}
			}
			
		}
		//
		process_params();
		//
		// Optimistic
		InitOK = true;
		// DIR
		if( sDIR == null ) sDIR = "Unknown";
		if (xMSet.xU.IsDir(sDIR)== false ) {
			InitOK = false;
			Error("Directory [" + sDIR + "] does not exist");
		}
		//
		String sTest = sDIR + "\\In";
		if (xMSet.xU.IsDir(sTest)== false ) {
			InitOK = false;
			Error("Directory [" + sTest + "] does not exist");
		}
		sTest = sDIR + "\\Out";
		if (xMSet.xU.IsDir(sTest)== false ) {
			InitOK = false;
			Error("Directory [" + sTest + "] does not exist");
		}
		else {
		 sTest = sDIR + "\\Out\\utf8";
		 if (xMSet.xU.IsDir(sTest)== false ) {
			xMSet.xU.CreateDirectory(sTest);
		 }
		 if (xMSet.xU.IsDir(sTest)== false ) {
			InitOK = false;
			Error("Directory [" + sTest + "] does not exist");
		 }
		 sTest = sDIR + "\\Out\\latin1";
		 if (xMSet.xU.IsDir(sTest)== false ) {
			xMSet.xU.CreateDirectory(sTest);
		 }
		 if (xMSet.xU.IsDir(sTest)== false ) {
			InitOK = false;
			Error("Directory [" + sTest + "] does not exist");
		 }
		 sTest = sDIR + "\\Out\\Ascii";
		 if (xMSet.xU.IsDir(sTest)== false ) {
			xMSet.xU.CreateDirectory(sTest);
		 }
		 if (xMSet.xU.IsDir(sTest)== false ) {
			InitOK = false;
			Error("Directory [" + sTest + "] does not exist");
		 }
		}
		sTest = sDIR + "\\Src";
		if (xMSet.xU.IsDir(sTest)== false ) {
			InitOK = false;
			Error("Directory [" + sTest + "] does not exist");
		}
		sTest = sDIR + "\\Temp";
		if (xMSet.xU.IsDir(sTest)== false ) {
			InitOK = false;
			Error("Directory [" + sTest + "] does not exist");
		}
		// Source code
		SourceCodeFileName = sDIR + "\\Src\\" + sCode;
		if( xMSet.xU.IsBestand(SourceCodeFileName)==false) {
			InitOK = false;
			Error("SourceCodeFile [" + SourceCodeFileName + "] does not exist");
		}
		else {
			// lees de source code en extraheer de naam
			String sLijn = xMSet.xU.ReadContentFromFile(SourceCodeFileName,1000);
			int aantal = xMSet.xU.TelDelims(sLijn,'\n');
			for(int i=0;i<aantal;i++)
			{
				String sTmp = xMSet.xU.GetVeld(sLijn,i,'\n').trim();
				sTmp = xMSet.xU.RemplaceerNEW(sTmp,"\t","");
				sTmp = xMSet.xU.OntdubbelKarakter(sTmp,' ');
				String sEen= xMSet.xU.GetVeld(sTmp,1,' ');
				if( sEen.trim().compareToIgnoreCase("OUTPUTFILENAME") == 0 ) {
					String sTwee = xMSet.xU.GetVeld(sTmp,2,' ').trim();
					if( sTwee.length() > 2) {
						this.baseOutputFileName = sTwee;
						NormalizedSourceCodeName = sTwee;
					}
				}
			}
		}
		if( baseOutputFileName == null ) {
			// Code en Base name maken door de suffix uit de naam van de sourcecode file te halen
			NormalizedSourceCodeName = xMSet.xU.GetFileName(sCode.toLowerCase().trim());
			String ssuf = xMSet.xU.GetSuffix(NormalizedSourceCodeName).toLowerCase();
			NormalizedSourceCodeName = xMSet.xU.RemplaceerNEW(NormalizedSourceCodeName,"."+ssuf,"");
			baseOutputFileName = NormalizedSourceCodeName;
		}
		HTMLDumpBaseFileName = sDIR + "\\In\\" + baseOutputFileName + ".txt";
		HTMLTempBaseFileName = sDIR + "\\Temp\\" + baseOutputFileName + ".txt";
		HTMLResultFileName = sDIR + "\\Out\\" + baseOutputFileName + ".txt";
		HTMLConsolidatedFileName = sDIR + "\\Out\\Ascii\\" + baseOutputFileName + "-cons.txt";
		LogFileName = sDIR + "\\Log\\" +  baseOutputFileName + "-log.txt";
		TraceFileName = sDIR + "\\Log\\" +  baseOutputFileName + "-Trace.txt";
		HTMLBlobDir = sDIR + "\\Out\\Blob\\";
		
	}
	//
	//---------------------------------------------------------------------------------
	String getNormalizedName(String sIn)
	//---------------------------------------------------------------------------------
	{
	  if( sIn == null ) return NormalizedSourceCodeName;
	  if( sIn.compareToIgnoreCase("MAIN")==0) return NormalizedSourceCodeName;
	  return sIn.toLowerCase();
	}
	//
	//---------------------------------------------------------------------------------
	String getResultFileName()
	//---------------------------------------------------------------------------------
	{
	  return this.HTMLResultFileName;
	}
	//
	//---------------------------------------------------------------------------------
	parseFunction getForeach( String sNaam )
	//---------------------------------------------------------------------------------
	{
		if( sNaam == null ) return null;
		for(int i=0;i<lstFuncties.size();i++) {
			if( sNaam.compareTo(lstFuncties.get(i).Name) == 0 ) return lstFuncties.get(i);
		}
		return null;
	}
	//
	//---------------------------------------------------------------------------------
	String getNewDumpFileName()
	//---------------------------------------------------------------------------------
	{
		fetchteller++;
		return getCurrentDumpFileName();
	}
	//
	//---------------------------------------------------------------------------------
	String getNewBLOBFileName()
	//---------------------------------------------------------------------------------
	{
		blobteller++;
		return getCurrentBLOBFileName();
	}
	//
	//---------------------------------------------------------------------------------
	String getCurrentDumpFileName()
	//---------------------------------------------------------------------------------
	{
	  return this.HTMLDumpBaseFileName + "-" + fetchteller + ".txt";	
	}
	//
	//---------------------------------------------------------------------------------
	private String getCurrentBLOBFileName()
	//---------------------------------------------------------------------------------
	{
	  String sParams = "";
	  for(int i=0;i<this.cmdLineParams.size();i++)
	  {
		  if( i==0 ) sParams = "-" + cmdLineParams.get(i);
	  }
	  String FNaam = HTMLBlobDir + NormalizedSourceCodeName + sParams.trim().toLowerCase() + "-" + blobteller;
	  return FNaam;	
	}
	//
	//---------------------------------------------------------------------------------
	String getTempFileName()
	//---------------------------------------------------------------------------------
	{
	  return this.HTMLTempBaseFileName + "-" + (tellertje++) + ".txt";	
	}
	//
	//---------------------------------------------------------------------------------
	String getSourceCodeFileName()
	//---------------------------------------------------------------------------------
	{
	  return this.SourceCodeFileName;
	}
	//
	//---------------------------------------------------------------------------------
	String getLogFileName()
	//---------------------------------------------------------------------------------
	{
	  return this.LogFileName;	
	}
	//
	//---------------------------------------------------------------------------------
	String getTraceFileName()
	//---------------------------------------------------------------------------------
	{
	  return this.TraceFileName;	
	}
	//
	//---------------------------------------------------------------------------------
	void shoFunc(parseFunction x)
	//---------------------------------------------------------------------------------
	{
		  String sDiepte = "";
		  for(int i=0;i<diepte;i++) sDiepte = sDiepte + " ";
		  LogIt(1,sDiepte+x.sho());
		  for(int j=0;j<x.lstInstructies.size();j++)
		  {
			  LogIt(1,sDiepte + x.lstInstructies.get(j).sho());
			  for(int k=0;k<x.lstInstructies.get(j).lstCommands.size();k++) {
				  LogIt(1,sDiepte+"               COMMMAND : " + x.lstInstructies.get(j).lstCommands.get(k).sho());
			  }
		  }
	}
	//
	//---------------------------------------------------------------------------------
	void sho2()
	//---------------------------------------------------------------------------------
	{
	  //
	  for(int i=0;i<lstFuncties.size();i++)
	  {
		  shoFunc(lstFuncties.get(i));
	  }
	}
	//
	//---------------------------------------------------------------------------------
	void sho()
	//---------------------------------------------------------------------------------
	{
		LogIt(1,"FOLDER         = [" + this.sDIR + "]" );
		LogIt(1,"OUTPUTFILE     = [" + this.baseOutputFileName + "]" );
		LogIt(1,"SOURCEFILE     = [" + this.SourceCodeFileName + "]" );
		LogIt(1,"LOGLEVEL       = " + this.LogLevel );
		LogIt(1,"PROXY          = " + this.bUseProxy + " " + this.sProxyName );
		LogIt(1,"isSIMULATION   = " + this.isSimulation );
		LogIt(1,"KEEPOUTPUTFILE = " + this.keepOutputFile );
		LogIt(1,"CONSOLIDATE    = " + this.consolidate );
		LogIt(1,"USERAGENT      = [" + this.Browser + "] [" + xMSet.getUserAgent() +"]" );
		LogIt(1,"ROBOTS         = " + this.useRobots );
		LogIt(1,"INIT OK      = " + this.InitOK );
		// get main and walk the tree of functions
		int idx =1;
		for(int i=0;i<lstFuncties.size();i++) {
			if( "main".compareToIgnoreCase(lstFuncties.get(i).Name) == 0 ) idx=i;
		}
		if ( idx < 0 ) {
			Error("There is no main function");
			sho2();
			return;
		}
	    sho3(lstFuncties.get(idx));	
	}
	//
	//---------------------------------------------------------------------------------
	void sho3(parseFunction x)
	//---------------------------------------------------------------------------------
	{
		diepte++;
		shoFunc(x);
		for(int i=0;i<x.lstInstructies.size();i++)
		{
		  if( x.lstInstructies.get(i).Tipe == parseInstruction.isVARIABLE )	continue;
		  if( x.lstInstructies.get(i).Tipe == parseInstruction.isFOREACH )	{
			  parseFunction y =  getForeach( x.lstInstructies.get(i).Name );
			  if( y == null ) continue;
			  sho3( y );
		  }
		  if( x.lstInstructies.get(i).Tipe == parseInstruction.isLINK )	{
			  parseFunction y =  getForeach( x.lstInstructies.get(i).referencedForEach );
			  if( y == null ) continue;
			  sho3( y );
		  }
		}
		diepte--;
	}
	//
	//---------------------------------------------------------------------------------
	boolean setBaseUrlAndProtocol(String sUrlIn)
	//---------------------------------------------------------------------------------
	{
		sMainUrl = sUrlIn;
		try {
		  URL aURL = new URL(sMainUrl);
		  BaseUrl = aURL.getHost();
		  Protocol = aURL.getProtocol();
		  return true;
		}
		catch (Exception e ) {
			  Error("Error on getHost " + sUrlIn + " " + e.getMessage() );
	     	  return false;
	    }
	}
	//
	//---------------------------------------------------------------------------------
	String constructFullUrlFromPart(String sIn)
	//---------------------------------------------------------------------------------
	{
		
		if( sIn == null ) return null;
		if( sIn.length() < 1 ) return null;
		
		// If a proxy is used no need to expand
		if( bUseProxy ) return sIn;
		// No proxy used
		if( sIn.startsWith("http://")==true) return sIn;
		if( sIn.startsWith("https://")==true) return sIn;
		if( sIn.startsWith("/")==false) return Protocol + "://" + BaseUrl + "/" + sIn;
		return Protocol + "://" + BaseUrl + sIn;
	}
	
	//
	//---------------------------------------------------------------------------------
	String substituteParamsOnString(String sIn)
	//---------------------------------------------------------------------------------
	{
		if( sIn == null ) return null;
		if( sIn.length() < 1 ) return null;
		// replace $nn
		String sOut = sIn;
		for(int i=0;i<cmdLineParams.size();i++)
		{
			String sP = "$" + (i+1);
			if( sOut.indexOf(sP)<0) {
				Error("Could not find a parameter reference " + sP + " on URL in main [" + sOut + "]");
				return null;
			}
			sOut = xMSet.xU.RemplaceerNEW(sOut,sP,cmdLineParams.get(i));
		}
		return sOut;
	}
	//
	//---------------------------------------------------------------------------------
	boolean MergeOutputFiles()
	//---------------------------------------------------------------------------------
	{
	 
	  
      // Consolidated filed does not exist - write out the consolidated tags and XML charset
	  String ConsolidatedTag = this.NormalizedSourceCodeName+"Consolidated";
	  if( xMSet.xU.IsBestand(this.HTMLConsolidatedFileName) == false ) {
		 webStraktorPrintStream init = new webStraktorPrintStream(this.HTMLConsolidatedFileName);
		 init.println("<?xml version=\"1.0\" encoding=\"" + xMSet.getTargetCodePageString() + "\"?>");
		 init.println("<!-- Script : " + xMSet.xU.GetFileName(this.SourceCodeFileName) + "-->");
		 init.println("");
		 init.println("<" + ConsolidatedTag + ">");
		 init.println("");
		 init.println("</" + ConsolidatedTag + ">");
		 init.close();
	  }
	  // Copying the existing file to a BCK file
	  String FBckNaam = this.HTMLConsolidatedFileName + ".BCK";
	  try {
		      xMSet.xU.copyFile(this.HTMLConsolidatedFileName,FBckNaam);
	  }
	  catch (Exception e) {
			  Error(e.getMessage());
			  Error("FILE COPY " + this.HTMLResultFileName + " to " + FBckNaam);
			  return false;
	  }
	  //
	  String sTgtParams = this.iparseURL.naarInternFormaat(this.getCmdLineParams().trim());	  
	  // Read the Backup file and continue reading until CMDLINEPARAMS matches the current params
	  boolean merged = false;
	  String EndConsolidatedTag = "</"+ConsolidatedTag+">";
	  try {
		  BufferedWriter writer = new BufferedWriter(new FileWriter(this.HTMLConsolidatedFileName));
		  File inFile = new File(FBckNaam);
		  BufferedReader reader = new BufferedReader(new FileReader(inFile));
		  String sLijn = null;
		  boolean found = false;
		  while( (sLijn = reader.readLine()) != null ) {
			  if( (sLijn.startsWith("<!-- CMDLINEPARAMS")) && (found == false) ) {
				  String sTmp = xMSet.xU.RemplaceerIgnoreCase(sLijn,"]","[");
				  String sCmd = xMSet.xU.GetVeld(sTmp,2,'[');
				  if( sCmd.trim().compareToIgnoreCase(sTgtParams)==0) {
					  LogIt(5,"Target Params found [" + sTgtParams + "]");
					  found = true;
				  }
			  }
			  // keep reading until end of current section in consolidated file
			  if( (sLijn.startsWith("<!-- Stopped at")) && (found == true) ) {
				  found = false;
				  // now merge in the other file
				     File inFile2 = new File(this.HTMLResultFileName);
				     BufferedReader reader2 = new BufferedReader(new FileReader(inFile2));
				     boolean found2 = false;
				     writer.write("<!-- " + xDateTime.DateTimeNow(xMSet.DateFormat) + "   -->" + xMSet.xU.EOL);
				     while( (sLijn = reader2.readLine()) != null ) {
				    	if( sLijn.startsWith("<!-- CMDLINEPARAMS")) found2 = true;
				    	if( found2 == false ) continue;
				    	writer.write(sLijn + xMSet.xU.EOL);
				     }
				     reader2.close();
				     merged = true;
				  //   
				  continue;
			  }
			  if( found == true ) continue;  // keep reading and ignore
			  // if the ConsolidatedTag is found and new file merged then quit without writing the tag
			  if( (sLijn.trim().compareTo(EndConsolidatedTag)==0) && (merged==false) ) break; 
			  // just write out the line
			  writer.write(sLijn + xMSet.xU.EOL);
		  }
		  // indien niet gemerged - gewoon eraan plakken
		  if( merged == false )
		  {
			     File inFile2 = new File(this.HTMLResultFileName);
			     BufferedReader reader2 = new BufferedReader(new FileReader(inFile2));
			     boolean found2 = false;
			     writer.write("<!--     -->" + xMSet.xU.EOL);
			     writer.write("<!-- " + xDateTime.DateTimeNow(xMSet.DateFormat) + "   -->" + xMSet.xU.EOL);
			     while( (sLijn = reader2.readLine()) != null ) {
			    	if( sLijn.startsWith("<!-- CMDLINEPARAMS")) found2 = true;
			    	if( found2 == false ) continue;
			       	writer.write(sLijn + xMSet.xU.EOL);
			     }
			     // write the end consolidated tag
			     writer.write(EndConsolidatedTag+xMSet.xU.EOL);
			     reader2.close();
		  }
		  reader.close();
		  writer.close();
		  xMSet.xU.VerwijderBestand(FBckNaam);
		  return true;
	  }
	  catch( Exception e)
	  {
		  Error("Reading file [" + FBckNaam+ "]");
		  Error(xMSet.xU.LogStackTrace(e));
		  return false;
	  }
	  
	}
	//
	//---------------------------------------------------------------------------------
	String KeepFile()
	//---------------------------------------------------------------------------------
	{
		String sUniq = "";
		if( this.cmdLineParams.size() == 0 ) sUniq = "-nil";
		for(int i=0;i<this.cmdLineParams.size();i++) { sUniq = sUniq + "-" + cmdLineParams.get(i); }	 
		String FDest = sDIR + "\\Out\\Ascii\\" + baseOutputFileName + sUniq + ".txt";
		try {
			xMSet.xU.copyFile( this.HTMLResultFileName, FDest);
		}
		catch ( Exception e) {
			Error("ERROR copying " + this.HTMLResultFileName + " to " + FDest);
			return null;
		}
		return FDest;
	}
	//
	//---------------------------------------------------------------------------------
	boolean gotVarsDefined()
	//---------------------------------------------------------------------------------
	{
	  //LogIt(0,"-----> VARS" + this.iParseSourceCodeFile.gotVarLinks() );
      return this.iParseSourceCodeFile.gotVarLinks();
	}
	//
	//---------------------------------------------------------------------------------
	String VervangSuffix(String sN , String sF)
	//---------------------------------------------------------------------------------
	{
		String sTemp = sN;
		String ssuf = xMSet.xU.GetSuffix(sTemp).toLowerCase();
		sTemp = xMSet.xU.RemplaceerNEW(sTemp,"."+ssuf,"."+sF);
		if( sN.compareToIgnoreCase(sTemp)==0) return sN + "." + sF;
		return sTemp;
	}
	//
	//---------------------------------------------------------------------------------
	void process_params()
	//---------------------------------------------------------------------------------
	{
	    // "p1 p2 p3" -> p1+p2+p3
		String sLijn = "";
		for(int i=0;i<this.cmdLineParams.size();i++) sLijn = sLijn + " " + cmdLineParams.get(i);
		sLijn = sLijn.trim();
		if( sLijn.length() < 1) return;
		if( sLijn.indexOf('"')<0) return;
		sLijn = xMSet.xU.vervangtussenDubbelQuotes(sLijn,' ','+');
		sLijn = xMSet.xU.OntdubbelKarakter(sLijn,' ');
		for(int i=0;i<cmdLineParams.size();i++) LogIt(9,cmdLineParams.get(i));
		// 
		int aantal = cmdLineParams.size();
		for(int i=0;i<aantal;i++) {
			for(int j=0;j<cmdLineParams.size();j++) cmdLineParams.remove(0);
		}
		cmdLineParams=null;
		cmdLineParams=new ArrayList<String>();
		//
		sLijn = sLijn + " ";
		aantal = xMSet.xU.TelDelims(sLijn,' ');
		for(int i=0;i<aantal;i++)
		{
			String sTemp = xMSet.xU.GetVeld(sLijn,(i+1),' ');
			sTemp = xMSet.xU.verwijderEnclosingQuotes(sTemp).trim();
			if( sTemp.length() < 1) continue;
			sTemp = xMSet.xU.RemplaceerNEW(sTemp,"\\\"","\"");
			cmdLineParams.add(sTemp);
		}
		for(int i=0;i<cmdLineParams.size();i++) LogIt(9,cmdLineParams.get(i));
	}
	//
	//
	void setLogLevel(int i)
	{
		this.LogLevel = i;
	}
	void setIsSimulation(boolean b)
	{
		this.isSimulation = b;
	}
	void setUseProxy(boolean b)
	{
		this.bUseProxy = b;
	}
	void setProxyName(String s)
	{
		this.sProxyName = s;
	}
	int getLogLevel()
	{
		 return this.LogLevel;
	}
	boolean getSimulation()
	{
		 return isSimulation;
	}
	boolean getUseProxy()
	{
		 return this.bUseProxy;
	}
	String getProxyName()
	{
		 return this.sProxyName;
	}
	String getMainUrl()
	{
		 return this.sMainUrl;
	}
	String getCmdLineParams()
	{
		 String sOut="";
		 for(int i=0;i<this.cmdLineParams.size();i++) {
			 if (i ==0 ) sOut = ""+(i+1)+"="+cmdLineParams.get(i); else sOut = sOut + " , " +(i+1) +"=" + cmdLineParams.get(i);
		 }	 
	     return sOut;		 
	}
	String getCmdLineParameter(int idx)
	{
		 if( idx < 1) return null;
		 if( idx > cmdLineParams.size() ) return null;
		 return cmdLineParams.get(idx-1);
	}
	
	parseInstruction getHTMLForm(parseFunction iFunc)
	{
		for(int i=0;i<iFunc.lstInstructies.size();i++)
		{
			if( iFunc.lstInstructies.get(i).Tipe == parseInstruction.isFORM) return iFunc.lstInstructies.get(i);
		}
		return null;
	}
	void setRobots(boolean ib)
	{
        //LogIt(0, ">>>>>>>>>>>>>>>>>>>>>>IN" + ib + " XMSET"  + xMSet.getAssessRobotsTxt()) ;
		useRobots=ib;
		if( xMSet.getAssessRobotsTxt() != useRobots ) {
			this.LogIt(5,"Overruling RobotsExclusion from [" + xMSet.getAssessRobotsTxt() + " to " + useRobots + "]");
			xMSet.overruleRobots(useRobots);
		}
	}
	void setUserAgent(String sBrowser)
	{
		if( xMSet.browserExists(sBrowser) )	{
			Browser = sBrowser;
			xMSet.setUserAgent(Browser);
		}
		else {
			Browser = "UNKNOWN BROWSER";
			xMSet.setUserAgent(null);
		}
		
	}
	String getUserAgent()
	{
		return xMSet.getUserAgent();
	}
}
