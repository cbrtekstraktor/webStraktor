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
import java.io.FileReader;
import java.util.ArrayList;

public class webStraktorParseModel {
	
	private int OIDSeed=500;
	
	webStraktorSettings xMSet = null;
	webStraktorLogger iLogger = null;
	webStraktorHTML xHTML = null;
	
	public static final int tkERROR          = -1;
    public static final int tkUNKNOWN        = 200;
    public static final int tkPROCEDURE      = 201;
    public static final int tkURL            = 202;
    public static final int tkFOREACH        = 203;
    public static final int tkSTARTPATTERN   = 204;
    public static final int tkENDPATTERN     = 205;
    public static final int tkREGEX          = 206;
    //public static final int tkSTORE          = 207;  zelfde als label
    public static final int tkCODE           = 208;
    public static final int tkENDVAR         = 209;
    public static final int tkENDFOREACH     = 210;
    public static final int tkENDPROCEDURE   = 211;
    public static final int tkCALL           = 212;
    public static final int tkLABEL          = 213;
    public static final int tkVAR            = 214;
    public static final int tkLINK           = 215;
    public static final int tkEND            = 216;
    public static final int tkPASSPHRASE     = 217;
    public static final int tkMAIN           = 218;
    public static final int tkENDMAIN        = 219;
    public static final int tkENDLINK        = 220;
    public static final int tkMAXITERATIONS  = 221;
    public static final int tkOCCURS         = 222;
    public static final int tkSKIP           = 223;
    public static final int tkIGNORE         = 224;
    public static final int tkSIMULATION     = 225;
    public static final int tkPROXY          = 226;
    public static final int tkLOGLEVEL       = 227;
    public static final int tkOUTPUTFILENAME = 228;
    public static final int tkCONSOLIDATE    = 229;
    public static final int tkKEEPOUTPUTFILE = 230;
    public static final int tkCUT            = 231;
    public static final int tkENDCUT         = 232;
    public static final int tkXPATH          = 233;
    public static final int tkFORM           = 234;
    public static final int tkENDFORM        = 235;
    public static final int tkFORMNAME       = 236;
    public static final int tkFORMFIELD      = 237;
    public static final int tkBLOB           = 238;
    public static final int tkENDBLOB        = 239;
    public static final int tkTRACEID        = 240;
    public static final int tkBROWSER        = 241;
    public static final int tkROBOT          = 242;
   
    
    enum commandStatus {UNKNOWN, EXPECTCODE , EXPECTNEXTCODE , EXPECTVALUE };
	
    commandStatus flexStatus = commandStatus.EXPECTCODE;
    int prevCommand = tkUNKNOWN;
	private int LogLevel = 5;
	private String sProxyName = "";
	private boolean useProxy = false;
	private boolean isSimulation = false;
	private boolean consolidate=true;
    private boolean keepOutputFile=true;
    private String BaseOutputFileName=null;
    private String Browser="default";  // will read default browser from file
    private boolean useRobots=false;   // default will be set to global setting in init
    private int VarTeller=0;
    private int LinkTeller=0;
    
    class TokenizedCommand
    {
    	int CmdToken;
    	String params;
    	int lijn;
    	TokenizedCommand(int i,String s, int il)
    	{
    		CmdToken = i;
    		params =s;
    		lijn = il;
    	}
    }
	ArrayList<TokenizedCommand> lstCommand = null;
	
	//
	enum parseObjectTipe {UNKNOWN , ROOT, MAIN, PROCEDURE , FOREACH , VAR , LINK , CUT , FORM , BLOB};  
	class parseObject
	{
		Object xo;
		parseObjectTipe tipe=parseObjectTipe.UNKNOWN;
		parseObject(parseObjectTipe i,Object o)
		{
			tipe = i;
			xo = o;
		}
	}
	ArrayList<parseObject> parseStack = null;
	//
	
	//
	//---------------------------------------------------------------------------------
	void LogIt(int level, String sIn)
	//---------------------------------------------------------------------------------
	{
		if( iLogger != null ) iLogger.Logit(level, "SRC - " + sIn);
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
	webStraktorParseModel(webStraktorSettings xI,webStraktorLogger iL, webStraktorHTML iH)
	//---------------------------------------------------------------------------------
	{
		xMSet = xI;
		iLogger = iL;
		xHTML = iH;
		useRobots = xMSet.getAssessRobotsTxt();
	}
	// GETTERS
	//---------------------------------------------------------------------------------
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
		 return this.useProxy;
	}
	String getProxyName()
	{
		 return this.sProxyName;
	}
	String getBaseOutputFileName()
	{
		 return this.BaseOutputFileName;
	}
	String getBrowser()
	{
		return Browser;
	}
	boolean getRobots()
	{
		return useRobots;
	}
	boolean getConsolidate()
	{
		 return this.consolidate;
	}
	boolean getKeepOutput()
	{
		 return this.keepOutputFile;
	}
	//
	//---------------------------------------------------------------------------------
	boolean LoadAndParseSourceCodeFile(String FNaam , ArrayList<parseFunction> lstFuncties )
	//---------------------------------------------------------------------------------
	{
	//  read file and maak de command stack
		lstCommand = new ArrayList<TokenizedCommand>();
	    Boolean isOK = tokenizeSourceCodeFile( FNaam );
		if( isOK == false ) return false;
		// interpreteer de command stack
		parseStack = new ArrayList<parseObject>();
		isOK = transformToFunction(lstFuncties);
		if( isOK == false ) return false;
		isOK = checkConsistency(lstFuncties);
		return isOK;
	}
	
	 // reduces spaces to a single space, unless spaces are enclosed in " "
	 // removes " unless preceded by \
	 //---------------------------------------------------------------------------------
	 Boolean tokenizeSourceCodeFile(String FBestand)
	 //---------------------------------------------------------------------------------
	 {
		 Boolean inBetweenQuotes=false;
		 Boolean isSpace=false;
		 Boolean inBetweenAccolades=false;
		 char cPrev='?';
		 String sRet = "";
		 int lijnTeller = 0;
		 
		 try {
			  File inFile  = new File(FBestand);  // File to read from.
	       	  BufferedReader reader = new BufferedReader(new FileReader(inFile));
	       	  String sLijn = null;
	       	  while ((sLijn=reader.readLine()) != null) {
	             lijnTeller++;    
	             // append space om als delim te dienen bij 2 lijnen, dit triggert de verwerking bij einde lijn
	             sLijn = sLijn + ' ';
	       		 // Negeer de lijn indien start met // of REM of --
	       		 if( sLijn.trim().length() == 0 ) continue; 
	       		 // 24-SEP-2013     FANOUT en END FANOUT negeren   - zetten van de traceid
	       		 if( sLijn.trim().length() > 5 ) {
	       			 if( sLijn.trim().toUpperCase().indexOf("FANOUT") >= 0 ) continue;
	       			 if( sLijn.trim().toUpperCase().startsWith("--@FLEX-CREATEDAT") == true ) {   // De traceid is EQUIVALNET aan startdate van iedere component
	       				 String sL = xMSet.xU.Remplaceer(sLijn,"--@FLEX-CREATEDAT","").trim();
	       				 //this.LogIt(5,"TRACE ID spotted " + sL );
	       				 if( pushTokenOnStack("TRACEID",lijnTeller) == false ) {
	       					reader.close();
	       					return false;
	       				 }
	       				 if( pushTokenOnStack(sL,lijnTeller) == false ) {
	       					reader.close();
	       					return false;
	       				 }
	       				 continue;
	       			 }
	       			 if( sLijn.trim().toUpperCase().startsWith("--@FLEX-MODELITEM") == true ) { 
	       				 String sM = sLijn;
	       				 // NOP
	       			 }
	       		 }
	       		 //
	       		 if( sLijn.trim().length()>= 2) {
	       			 if (sLijn.trim().indexOf("//") ==0) continue;
	       			 if (sLijn.trim().indexOf("--") ==0) continue;
	       			 if (sLijn.trim().indexOf("REM")==0) continue;
	       		 }
	       		 //
	       		 char[] buf = sLijn.toCharArray();
	       		 inBetweenQuotes = false;
	       		 for(int i=0;i<buf.length; i++)
	       		 {
	       			char cc = buf[i];
	       			if( cc == '\t') cc = ' ';
	       			if( cc < ' ') { cPrev = cc; continue; }  // negeer alle niet printables
	       		    
	       			// { en }
	       			if( (cc == '{') && (inBetweenQuotes==false) ) {
	       				inBetweenAccolades = true;
	       				cPrev = cc;
	       				continue;
	       			}
	       			if( (cc == '}') && (inBetweenQuotes==false)) {
	       				inBetweenAccolades = false;
	       				cPrev = cc;
	       				continue;
	       			}
	       			if( inBetweenAccolades == true) {
	       				sRet = sRet + cc;
	       				cPrev = cc;
	       				continue;
	       			}
	       			// quote
	       			if( cc == '"' ) 
	       			{
	       				if( (cPrev == '\\') ) {
	       					cPrev = cc;
	       					sRet = sRet + cc;
	       					continue;
	       			    }		
	       				inBetweenQuotes = !inBetweenQuotes;
	       				continue;
	       			}
	       			// space
	       			if( cc == ' ') {
	       				if( inBetweenQuotes ) { sRet = sRet + cc; cPrev = cc; continue; }
	       				if( cPrev == ' ') continue;
	       				cPrev = cc;
	                    //  Push deze string op de stack
	       				if( pushTokenOnStack(sRet,lijnTeller) == false ) {
	       					reader.close();
	       					return false;
	       				}
	       				sRet="";
	       				continue;
	       			}
	       			sRet = sRet + cc;
	       			cPrev = cc;
	       			//
	     
	       		 }
	          }
	          reader.close();
	          if( sRet.trim().length()>0) {
	        	  if( pushTokenOnStack(sRet,lijnTeller) == false ) {
     					reader.close();
     					return false;
     				}
	          }
	        }
			catch (Exception e) {
				Error("TokenizeSourceCodFile " + e.getMessage());
				Error(xMSet.xU.LogStackTrace(e));
				return false;
		    }
			return true;
	 }
	 //
	 //---------------------------------------------------------------------------------
	 Boolean pushTokenOnStack( String sIn , int lijnTeller )
	 //---------------------------------------------------------------------------------
	 {
	     //Error("PushTokenOnStack " + sIn);
		 if( flexStatus == commandStatus.EXPECTCODE ) {
			 flexStatus = commandStatus.EXPECTVALUE;
			 int iToken = getToken( sIn );
			 if( iToken == tkERROR ) {
				 Error("(1)UNKNOWN COMMAND [" + sIn + "] on line " + lijnTeller);
				 return false;
			 }
			 prevCommand = iToken;
			 
			 // FOREACH en MAIN verwacht geen value
			 if( (iToken == tkFOREACH) || (iToken == tkMAIN) ) {
				     String sName = "FOREACH-"+lijnTeller;
				     if( iToken == tkMAIN ) sName = "main";
					 createStackEntry( iToken , sName , lijnTeller);
					 flexStatus = commandStatus.EXPECTCODE;
			 }
			 //  CUT verwacht ook geen value
			 if( (iToken == tkCUT) ) {
			     String sName = "CUT-"+lijnTeller;
			     createStackEntry( iToken , sName , lijnTeller);
				 flexStatus = commandStatus.EXPECTCODE;
		     }
			//  FORM verwacht ook geen value
			 if( (iToken == tkFORM) ) {
			     String sName = "FORM-"+lijnTeller;
			     createStackEntry( iToken , sName , lijnTeller);
				 flexStatus = commandStatus.EXPECTCODE;
		     }
			 // verwachten geen value
			 if( (iToken == tkENDVAR) || (iToken== tkENDLINK) || (iToken== tkENDPROCEDURE) || (iToken == tkENDMAIN) || 
				 (iToken == tkIGNORE) || (iToken == tkENDFOREACH) || (iToken== tkENDCUT) || (iToken== tkENDFORM) || (iToken== tkENDBLOB)) {
				 createStackEntry( iToken , null , lijnTeller);
				 flexStatus = commandStatus.EXPECTCODE;
			 }
			 // END -> verwacht iets meer
			 if( iToken == tkEND ) {
				 flexStatus = commandStatus.EXPECTNEXTCODE;
			 }
			 return true;
		 }
		 if( flexStatus == commandStatus.EXPECTVALUE ) {
			 int iToken = getToken( sIn );
			 flexStatus = commandStatus.EXPECTCODE;
			 if( iToken != tkERROR ) {
				 Error("VALUE [" + sIn + "] is a reserved word on line " + lijnTeller);
				 return false;
			 }
			 String sCommand = sIn;
			 if( prevCommand != tkCODE) sCommand = xMSet.xU.RemplaceerNEW(sIn, "\\\"", "\"");    //  \" wordt "
			 createStackEntry( prevCommand , sCommand , lijnTeller);
		 }
		 if( flexStatus == commandStatus.EXPECTNEXTCODE ) {
			 // moet nu PROCEDURE, VAR, MAIN , FOREACH of FORM zijn
			 int iToken = getToken( "END" + sIn );
			 if( iToken == tkERROR ) {
				 Error("(2)UNKNOWN COMMAND [" + sIn + "] on line " + lijnTeller);
				 return false;
			 }
			 prevCommand = iToken;
			 flexStatus = commandStatus.EXPECTCODE;
			 createStackEntry( iToken , null , lijnTeller);
		 }
		 return true;
	 }
	 //---------------------------------------------------------------------------------
	 int getToken(String sIn)
	 //---------------------------------------------------------------------------------
	 {
		  if( sIn.compareToIgnoreCase("PROCEDURE")==0) return tkPROCEDURE;
		  if( sIn.compareToIgnoreCase("URL")==0) return tkURL;
		  if( sIn.compareToIgnoreCase("FOREACH")==0) return tkFOREACH;
		  if( sIn.compareToIgnoreCase("STARTPATTERN")==0) return tkSTARTPATTERN;
		  if( sIn.compareToIgnoreCase("ENDPATTERN")==0) return tkENDPATTERN;
		  if( sIn.compareToIgnoreCase("REGEX")==0) return tkREGEX;
		  if( sIn.compareToIgnoreCase("STORE")==0) return tkLABEL;
		  if( sIn.compareToIgnoreCase("CODE")==0) return tkCODE;
		  if( sIn.compareToIgnoreCase("ENDVAR")==0) return tkENDVAR;
		  if( sIn.compareToIgnoreCase("ENDFOREACH")==0) return tkENDFOREACH;
		  if( sIn.compareToIgnoreCase("ENDPROCEDURE")==0) return tkENDPROCEDURE;
		  if( sIn.compareToIgnoreCase("CALL")==0) return tkCALL;
		  if( sIn.compareToIgnoreCase("LABEL")==0) return tkLABEL;
		  if( sIn.compareToIgnoreCase("VAR")==0) return tkVAR;
		  if( sIn.compareToIgnoreCase("LINK")==0) return tkLINK;
		  if( sIn.compareToIgnoreCase("END")==0) return tkEND;
		  if( sIn.compareToIgnoreCase("PASSPHRASE")==0) return tkPASSPHRASE;
		  if( sIn.compareToIgnoreCase("MAIN")==0) return tkMAIN;
		  if( sIn.compareToIgnoreCase("ENDMAIN")==0) return tkENDMAIN;
		  if( sIn.compareToIgnoreCase("ENDLINK")==0) return tkENDLINK;
		  if( sIn.compareToIgnoreCase("IGNORE")==0) return tkIGNORE;
		  if( sIn.compareToIgnoreCase("MAXITERATIONS")==0) return tkMAXITERATIONS;
		  if( sIn.compareToIgnoreCase("OCCURS")==0) return tkOCCURS;
		  if( sIn.compareToIgnoreCase("SKIP")==0) return tkSKIP;
		  //
		  if( sIn.compareToIgnoreCase("LOGLEVEL")==0) return tkLOGLEVEL;
		  if( sIn.compareToIgnoreCase("PROXY")==0) return tkPROXY;
		  if( sIn.compareToIgnoreCase("SIMULATION")==0) return tkSIMULATION;
		  if( sIn.compareToIgnoreCase("CONSOLIDATE")==0) return tkCONSOLIDATE;
		  if( sIn.compareToIgnoreCase("OUTPUTFILENAME")==0) return tkOUTPUTFILENAME;
		  if( sIn.compareToIgnoreCase("KEEPOUTPUTFILE")==0) return tkKEEPOUTPUTFILE;
		  if( sIn.compareToIgnoreCase("BROWSER")==0) return tkBROWSER;
		  if( sIn.compareToIgnoreCase("ROBOT")==0) return tkROBOT;
		  if( sIn.compareToIgnoreCase("ROBOTS")==0) return tkROBOT;
		  
		  if( sIn.compareToIgnoreCase("CUT")==0) return tkCUT;
		  if( sIn.compareToIgnoreCase("ENDCUT")==0) return tkENDCUT;
		  if( sIn.compareToIgnoreCase("XPATH")==0) return tkXPATH;
		  //
		  if( sIn.compareToIgnoreCase("FORM")==0) return tkFORM;
		  if( sIn.compareToIgnoreCase("ENDFORM")==0) return tkENDFORM;
		  if( sIn.compareToIgnoreCase("FORMNAME")==0) return tkFORMNAME;
		  if( sIn.compareToIgnoreCase("INPUT")==0) return tkFORMFIELD;
		  //
		  if( sIn.compareToIgnoreCase("BLOB")==0) return tkBLOB;
		  if( sIn.compareToIgnoreCase("ENDBLOB")==0) return tkENDBLOB;
		  if( sIn.compareToIgnoreCase("IMAGE")==0) return tkBLOB;   // zelfde als BLOB
		  if( sIn.compareToIgnoreCase("ENDIMAGE")==0) return tkENDBLOB;  // zelfde als ENDBLOB
		  //
		  if( sIn.compareToIgnoreCase("TRACEID")==0) return tkTRACEID;  // zelfde als ENDBLOB
		  //
		  return tkERROR;
	 }
	 //
	 //---------------------------------------------------------------------------------
	 void createStackEntry(int iToken, String sParam , int iLijn)
	 //---------------------------------------------------------------------------------
	 {
		 TokenizedCommand x = new TokenizedCommand(iToken,sParam,iLijn);
		 lstCommand.add(x);
	 }
	 //
	 //---------------------------------------------------------------------------------
	 Boolean transformToFunction(ArrayList<parseFunction> lstFuncties)
	 //---------------------------------------------------------------------------------
	 {
	    parseFunction iFunc=new parseFunction("root",OIDSeed++);
	    parseInstruction iInstruc=null;
	    
		// initialiseer de stack
		parseObjectTipe iCurrentTipe = parseObjectTipe.ROOT;
		parseObject iCurrentObject = new parseObject(iCurrentTipe,iFunc);
		//
		
		// show
		// zoek log level
		for(int i=0;i<lstCommand.size();i++) 
		{
			 if( lstCommand.get(i).CmdToken == this.tkLOGLEVEL ) {
				  this.LogLevel = xMSet.xU.NaarInt(lstCommand.get(i).params.trim());
				  if( this.LogLevel < 0) this.LogLevel = 0;
				  if( this.LogLevel > 9) this.LogLevel = 9;
				  this.iLogger.setLogLevel(this.LogLevel);
				  break;
			  }
		}
		for(int i=0;i<lstCommand.size();i++) 
		{
			LogIt(9,CommandToString(lstCommand.get(i).CmdToken) + " " + lstCommand.get(i).params );
		}
		//
		for(int i=0;i<lstCommand.size();i++) 
		{
		 	  
		  TokenizedCommand xCommand = lstCommand.get(i);
		  //  Global commands
		  if( xCommand.CmdToken == tkLOGLEVEL ) {  // feitelijk niet meer nodig want hierjuist boven al uitgevoerd
			  this.LogLevel = xMSet.xU.NaarInt(xCommand.params.trim());
			  if( this.LogLevel < 0) this.LogLevel = 0;
			  if( this.LogLevel > 9) this.LogLevel = 9;
			  continue;
		  }
		  if( xCommand.CmdToken == tkPROXY ) {
			  this.useProxy = false;
			  String sProxy = xCommand.params.trim();
			  if( (sProxy.compareToIgnoreCase("TRUE")==0) || (sProxy.compareToIgnoreCase("ON")==0) || (sProxy.compareToIgnoreCase("1")==0)|| (sProxy.compareToIgnoreCase("YES")==0)) {
				 this.useProxy = true;
				 continue;
			  }
			  if( (sProxy.compareToIgnoreCase("FALSE")==0) || (sProxy.compareToIgnoreCase("OFF")==0) || (sProxy.compareToIgnoreCase("0")==0)|| (sProxy.compareToIgnoreCase("NO")==0) ) {
				 this.useProxy = false;
				 continue;
			  }
			  if( sProxy.length() > 5 ) {
				  this.useProxy = true;
				  this.sProxyName = sProxy;
			  }
			  continue;
		  }
		  if( xCommand.CmdToken == tkSIMULATION ) {
			  String sSim = xCommand.params.trim();
			  this.isSimulation = false;
			  if( (sSim.compareToIgnoreCase("TRUE")==0) || (sSim.compareToIgnoreCase("ON")==0) || (sSim.compareToIgnoreCase("1")==0) || (sSim.compareToIgnoreCase("YES")==0) ) {
				  this.isSimulation = true;
				  continue;
			  }
			  continue;
		  }
		  if( xCommand.CmdToken == tkCONSOLIDATE ) {
			  String sCon = xCommand.params.trim();
			  this.consolidate = true;
			  if( (sCon.compareToIgnoreCase("FALSE")==0) || (sCon.compareToIgnoreCase("OFF")==0) || (sCon.compareToIgnoreCase("0")==0) || (sCon.compareToIgnoreCase("NO")==0) ) {
				  this.consolidate = true;
				  continue;
			  }
			  continue;
		  }
		  if( xCommand.CmdToken == tkKEEPOUTPUTFILE ) {
			  String sSim = xCommand.params.trim();
			  this.keepOutputFile = true;
			  if( (sSim.compareToIgnoreCase("FALSE")==0) || (sSim.compareToIgnoreCase("OFF")==0) || (sSim.compareToIgnoreCase("0")==0) || (sSim.compareToIgnoreCase("NO")==0) ) {
				  this.keepOutputFile = true;
				  continue;
			  }
			  continue;
		  }
		  if( xCommand.CmdToken == tkOUTPUTFILENAME ) {
			  this.BaseOutputFileName = xCommand.params.trim();
			  continue;
		  }
		  if( xCommand.CmdToken == tkBROWSER ) {
			  this.Browser = xCommand.params.trim();
			  continue;
		  }
		  if( xCommand.CmdToken == tkROBOT ) {
			  String sSim = xCommand.params.trim();
			  if( (sSim.compareToIgnoreCase("FALSE")==0) || (sSim.compareToIgnoreCase("OFF")==0) || (sSim.compareToIgnoreCase("0")==0) || (sSim.compareToIgnoreCase("NO")==0) ) {
				  this.useRobots = false;
				  continue;
			  }
			  if( (sSim.compareToIgnoreCase("TRUE")==0) || (sSim.compareToIgnoreCase("ON")==0) || (sSim.compareToIgnoreCase("1")==0) || (sSim.compareToIgnoreCase("YES")==0) ) {
				  this.useRobots = true;
				  continue;
			  }
			  continue;
		  }
		  // end of globals
		  //
		  //
		  // begin van een nieuwe functie
		  if( (xCommand.CmdToken == tkMAIN) || (xCommand.CmdToken == tkPROCEDURE) || (xCommand.CmdToken == tkFOREACH) ||
		      (xCommand.CmdToken == tkVAR) || (xCommand.CmdToken == tkLINK) || (xCommand.CmdToken == tkCUT) || 
		      (xCommand.CmdToken == tkFORM)|| (xCommand.CmdToken == tkBLOB)) {
		  
			  // push de oude op de stack
			  pushOnStack( iCurrentObject );
			  
			  // maak alvast nieuw object
			  iCurrentTipe = parseObjectTipe.UNKNOWN;
			  if( xCommand.CmdToken == tkMAIN ) iCurrentTipe = parseObjectTipe.MAIN;
			  if( xCommand.CmdToken == tkPROCEDURE ) iCurrentTipe = parseObjectTipe.PROCEDURE;
			  if( xCommand.CmdToken == tkFOREACH ) iCurrentTipe = parseObjectTipe.FOREACH;
			  if( xCommand.CmdToken == tkVAR )  { iCurrentTipe = parseObjectTipe.VAR; VarTeller++; }
			  if( xCommand.CmdToken == tkLINK ) { iCurrentTipe = parseObjectTipe.LINK; LinkTeller++; } 
			  if( xCommand.CmdToken == tkCUT ) iCurrentTipe = parseObjectTipe.CUT;
			  if( xCommand.CmdToken == tkFORM ) iCurrentTipe = parseObjectTipe.FORM;
			  if( xCommand.CmdToken == tkBLOB ) iCurrentTipe = parseObjectTipe.BLOB;
			  if( iCurrentTipe == parseObjectTipe.UNKNOWN )  {
				  Error("PARSER ERROR 102 - Token unsupported");
				  return false;
			  }
			  if( (iCurrentTipe == parseObjectTipe.VAR) || (iCurrentTipe == parseObjectTipe.LINK) || 
				  (iCurrentTipe == parseObjectTipe.CUT) || (iCurrentTipe == parseObjectTipe.FORM) || (iCurrentTipe == parseObjectTipe.BLOB) )
			  {
				  iInstruc = new parseInstruction(xCommand.params,OIDSeed++);
				  if( iCurrentTipe == parseObjectTipe.VAR ) iInstruc.Tipe = iInstruc.isVARIABLE;
				  if( iCurrentTipe == parseObjectTipe.LINK ) iInstruc.Tipe = iInstruc.isLINK;
				  if( iCurrentTipe == parseObjectTipe.CUT ) iInstruc.Tipe = iInstruc.isCUT;
				  if( iCurrentTipe == parseObjectTipe.FORM ) iInstruc.Tipe = iInstruc.isFORM;
				  if( iCurrentTipe == parseObjectTipe.BLOB ) iInstruc.Tipe = iInstruc.isBLOB;
				  iCurrentObject = new parseObject(iCurrentTipe,iInstruc);
		          //Error(">>>ADDING " + iInstruc.Tipe + " " + iCurrentTipe );
			  }
			  else {   // Dit zijn de functies
				  iFunc = new parseFunction(xCommand.params,OIDSeed++);  
				  iCurrentObject = new parseObject(iCurrentTipe,iFunc);
			  }
			  continue;
		  }
		  //
		  // einde van een functie
		  if( (xCommand.CmdToken == this.tkENDMAIN) || (xCommand.CmdToken == this.tkENDPROCEDURE) || (xCommand.CmdToken == this.tkENDFOREACH) ||
			  (xCommand.CmdToken == this.tkENDVAR) || (xCommand.CmdToken == this.tkENDLINK) || (xCommand.CmdToken == this.tkENDCUT) || 
			  (xCommand.CmdToken == this.tkENDFORM) || (xCommand.CmdToken == this.tkENDBLOB)) {
			    	  
			  //Error(">>>CLOSING " + xCommand.CmdToken);
			  
			  // de end moet de start matchen
			  Boolean isOK = false;
			  if( (iCurrentObject.tipe == parseObjectTipe.MAIN ) && (xCommand.CmdToken == tkENDMAIN) ) isOK=true; 
			  if( (iCurrentObject.tipe == parseObjectTipe.PROCEDURE ) && (xCommand.CmdToken == tkENDPROCEDURE) ) isOK=true; 
			  if( (iCurrentObject.tipe == parseObjectTipe.FOREACH ) && (xCommand.CmdToken == tkENDFOREACH) ) isOK=true; 
			  if( (iCurrentObject.tipe == parseObjectTipe.VAR ) && (xCommand.CmdToken == tkENDVAR) ) isOK=true; 
			  if( (iCurrentObject.tipe == parseObjectTipe.LINK ) && (xCommand.CmdToken == tkENDLINK) ) isOK=true; 
			  if( (iCurrentObject.tipe == parseObjectTipe.CUT ) && (xCommand.CmdToken == tkENDCUT) ) isOK=true; 
			  if( (iCurrentObject.tipe == parseObjectTipe.FORM ) && (xCommand.CmdToken == tkENDFORM) ) isOK=true; 
			  if( (iCurrentObject.tipe == parseObjectTipe.BLOB ) && (xCommand.CmdToken == tkENDBLOB) ) isOK=true; 
			  if( isOK == false ) {
				  Error("Unmatching " + CommandToString(xCommand.CmdToken) + " at line " + xCommand.lijn);
				  return false;
			  }
			  
			  // Indien een VAR, LINK of CUT moeten de patterns gekend zijn
			  if( (iCurrentObject.tipe == parseObjectTipe.VAR) || (iCurrentObject.tipe == parseObjectTipe.LINK) || 
				  (iCurrentObject.tipe == parseObjectTipe.CUT) || (iCurrentObject.tipe == parseObjectTipe.BLOB) )
			  {
				  if( iInstruc == null ) {
					  Error(CommandToString(xCommand.CmdToken) + " line " + xCommand.lijn + " ERROR PARSER : empty iInstruc");
					  return false;
				  }
				  if( (iInstruc.startPattern == null) && (iInstruc.regex == null)  && (iInstruc.xpath == null)) {
					  // is toegestaan indien dit CODE{ GetInputParam() )
					  boolean isOk=false;
					  if( iInstruc.lstCommands.size() == 1 ) {
						  if ( iInstruc.lstCommands.get(0).CommandType == parseCommand.cmdGETINPUTPARAMETER ) isOk = true; 
					  }
					  if( isOk == false ) {
					   Error(CommandToString(xCommand.CmdToken) + " line " + xCommand.lijn + " ERROR : startpattern, regex and xpath empty");
					   return false;
					  }
				  }
				  if( (iCurrentObject.tipe == parseObjectTipe.CUT) && (iInstruc.regex != null) ) {
					  Error(CommandToString(xCommand.CmdToken) + " line " + xCommand.lijn + " ERROR : regex not supported for CUT");
					  return false;
				  }
				  // LINK moet een call hebben
				  if( iCurrentObject.tipe == parseObjectTipe.LINK ) {
					  if( iInstruc.referencedForEach == null ) {
						  Error(CommandToString(xCommand.CmdToken) + " line " + xCommand.lijn + " ERROR : NO CALL FUNCTION");
						  return false;
					  }
				  }
			  }
			  // FUNCs of forms
			  else {
				  if(  iCurrentObject.tipe == parseObjectTipe.FORM ) {
					  // URL , NAME en minstens 1 field
					  if( iInstruc == null ) {
						  Error(CommandToString(xCommand.CmdToken) + " line " + xCommand.lijn + " ERROR PARSER : empty iInstruc");
						  return false;
					  }
					  if( iInstruc.Form.FormName == null ) {
						  Error(CommandToString(xCommand.CmdToken) + " line " + xCommand.lijn + " ERROR : NO FORMNAME");
						  return false;
					  }
					  if( iInstruc.Form.FormURL == null ) {
						  Error(CommandToString(xCommand.CmdToken) + " line " + xCommand.lijn + " ERROR : NO FORMURL");
						  return false;
					  }
					  if( iInstruc.Form.lstInput.size() == 0 ) {
						  Error(CommandToString(xCommand.CmdToken) + " line " + xCommand.lijn + " ERROR : NO FORM INPUT fields defined");
						  return false;
					  }
				  }
				  else {
				    if( iFunc == null ) {
					  Error(CommandToString(xCommand.CmdToken) + " line " + xCommand.lijn + " ERROR PARSER : empty iFUNC");
					  return false;
				    } 
				    // TODO checks?
				  }
			  }
		
			  String sForeach=null;
			  // All tests passsed
			  if( (iCurrentObject.tipe == parseObjectTipe.VAR) || (iCurrentObject.tipe == parseObjectTipe.LINK)|| 
				  (iCurrentObject.tipe == parseObjectTipe.CUT) || (iCurrentObject.tipe == parseObjectTipe.FORM)|| (iCurrentObject.tipe == parseObjectTipe.BLOB) )
			  {
				  // add de instructie op de instructielijst van de huidge functie
				  if( iFunc == null ) {
					  Error(CommandToString(xCommand.CmdToken) + " line " + xCommand.lijn + " ERROR PARSER : no iFunc to add instruction to");
					  return false;
				  }
				  iFunc.lstInstructies.add( iInstruc );
			  }
			  // Funcs
			  else {
				lstFuncties.add(iFunc);
				// Indien een For Each moet je die koppelen aan de bovenliggende functie
				if( iCurrentObject.tipe == parseObjectTipe.FOREACH ) {
				  sForeach= iFunc.Name;
				}
			  }
			  
			  // pop het vorige object
			  iCurrentObject = popFromStack();
			  if( iCurrentObject == null ) {
				  Error(CommandToString(xCommand.CmdToken) + " line " + xCommand.lijn + " ERROR PARSER : stack empty");
				  return false;
			  }
			  iCurrentTipe = iCurrentObject.tipe;
			  if( (iCurrentObject.tipe == parseObjectTipe.VAR) || (iCurrentObject.tipe == parseObjectTipe.LINK) )
			  {
				  iInstruc = (parseInstruction)iCurrentObject.xo;
			  }
			  else {
				  iFunc = (parseFunction)iCurrentObject.xo;
				  if (sForeach != null ) { // koppel de foreach
					  parseInstruction xy = new parseInstruction(sForeach,OIDSeed++);
					  xy.Tipe = xy.isFOREACH;
					  iFunc.lstInstructies.add(xy);
				  }
			  }
			  //
			  continue;
		  }
		  //
		  // het object updaten
		  // Dit is het gedeelte voor de instructies
		  if( (iCurrentObject.tipe == parseObjectTipe.VAR) || (iCurrentObject.tipe == parseObjectTipe.LINK) || 
			  (iCurrentObject.tipe == parseObjectTipe.CUT) || (iCurrentObject.tipe == parseObjectTipe.FORM) || (iCurrentObject.tipe == parseObjectTipe.BLOB) ) {
			  if( iInstruc == null ) {
				  Error(CommandToString(xCommand.CmdToken) + " line " + xCommand.lijn + " ERROR parser : empty iInstruc");
				  return false;
			  }
			  if( xCommand.CmdToken == tkSTARTPATTERN ) iInstruc.startPattern = xCommand.params;
			  if( xCommand.CmdToken == tkENDPATTERN ) iInstruc.endPattern = xCommand.params;
			  if( xCommand.CmdToken == tkREGEX ) iInstruc.regex = xCommand.params;
			  if( xCommand.CmdToken == tkLABEL ) iInstruc.Label = xCommand.params;
			  if( xCommand.CmdToken == tkCALL ) iInstruc.referencedForEach = xCommand.params;
			  if( xCommand.CmdToken == tkIGNORE ) iInstruc.ignore = true;
			  if( xCommand.CmdToken == tkCODE ) {
				  iInstruc.codestring = xCommand.params;
				  ArrayList<parseCommand> z = null; 
				  if( xCommand.params != null ) {
					String sC = xCommand.params.trim();
					if( sC.length() > 0 ) {
				      z = parseCommands( xCommand.params , xCommand.lijn );
				      if ( z == null ) {
				    	  return false;
				      }
					}
				  }
				  if ( z != null ) iInstruc.lstCommands = z;
			  }
			  if( xCommand.CmdToken == tkXPATH ) {
				  if( xHTML.parseQuery(xCommand.params) == false ) return false;
				  iInstruc.xpath = xCommand.params;
			  }
			  // FORM attributen
			  if( xCommand.CmdToken == tkURL ) iInstruc.Form.FormURL = NormaliseerURL(xCommand.params);
			  if( xCommand.CmdToken == tkFORMNAME ) iInstruc.Form.FormName = xCommand.params;
			  if( xCommand.CmdToken == tkFORMFIELD ) {
				  String sInput= xCommand.params;
				  iInstruc.Form.lstInput.add(sInput);
			  }
			  // 24-SEP TRACE-IDs
			  if( xCommand.CmdToken == tkTRACEID ) {
				  if( iInstruc.traceID == 0L ) iInstruc.traceID = xMSet.xU.NaarLong(xCommand.params);
			  }
		  }
		  // gedeelte FUNCTIONS
		  else {
			  if( xCommand.CmdToken == tkSTARTPATTERN ) iFunc.startPattern = xCommand.params;
			  if( xCommand.CmdToken == tkENDPATTERN ) iFunc.endPattern = xCommand.params;
			  if( xCommand.CmdToken == tkLABEL ) iFunc.Label = xCommand.params;
			  if( xCommand.CmdToken == tkPASSPHRASE ) iFunc.passPhrase = xCommand.params;
			  if( xCommand.CmdToken == tkURL ) iFunc.URL = NormaliseerURL(xCommand.params);
			  if( xCommand.CmdToken == tkXPATH ) {
				  if( xHTML.parseQuery(xCommand.params) == false ) return false;
				  iFunc.xpath = xCommand.params;
			  }
			  // een call wordt opgevat als een instructie die een for each is
			  if( xCommand.CmdToken == tkCALL ) {
				  parseInstruction xy = new parseInstruction(xCommand.params,OIDSeed++);
				  xy.Tipe = xy.isFOREACH;
				  iFunc.lstInstructies.add(xy);
			  }
			  if( xCommand.CmdToken == tkMAXITERATIONS ) {
				  iFunc.MaxIterations = xMSet.xU.NaarInt(xCommand.params.trim());
			  }
			  // skip and occur lists
              if( (xCommand.CmdToken == tkOCCURS) || (xCommand.CmdToken == tkSKIP) ) {
				 String sLst = (xCommand.params + ",").trim();
				 sLst = xMSet.xU.RemplaceerNEW(sLst,";",",");
				 sLst = xMSet.xU.RemplaceerNEW(sLst,"-",",");
				 sLst = xMSet.xU.RemplaceerNEW(sLst," ","");
				 int aantal = xMSet.xU.TelDelims(sLst,',');
				 for(int k=0;k<(aantal+1);k++)
				 {
					 String sN = xMSet.xU.GetVeld(sLst,k+1,',');
					 int iN = xMSet.xU.NaarInt(sN);
					 if( iN >= 0) {
						 if( xCommand.CmdToken == tkOCCURS ) iFunc.lstOccurs.add(iN);
						                                else iFunc.lstSkip.add(iN);
					 }
				 }
			  }
              // 24-SEP TRACE-IDs
			  if( xCommand.CmdToken == tkTRACEID ) {
				  if ( (iFunc.Name.toUpperCase().startsWith("FOREACH") == true) || (xCommand.CmdToken == tkCALL) ) {
				     if( iFunc.traceID == 0L ) iFunc.traceID = xMSet.xU.NaarLong(xCommand.params);
				  }
			  }
             
		  }
		  
		} // i loop
		
		//
		// huidige moet een root zijn
		if( iCurrentObject.tipe != parseObjectTipe.ROOT) {
			Error("PARSER ERROR - no ROOT entry at end");
			return false;
		}
		// nu mag er niets meer op de stack steken
		parseObject xeinde = popFromStack();
		if( xeinde != null ) {
			Error("PARSER ERROR - stack not empty at end");
			return false;
		}
		
		return true;
	 }
	 //
	 //---------------------------------------------------------------------------------
	 void pushOnStack(parseObject io)
	 //---------------------------------------------------------------------------------
	 {
		 parseStack.add(io);
	 }
	 //
	 //---------------------------------------------------------------------------------
	 parseObject popFromStack()
	 //---------------------------------------------------------------------------------
	 {
		 int idx=-1;
		 if( (idx=parseStack.size()) < 1 ) return null;
		 parseObject ret = parseStack.get(idx-1);
		 parseStack.remove(idx-1);
		 return ret;
	 }
	 //
	 //---------------------------------------------------------------------------------
	 String CommandToString(int sIn)
	 //---------------------------------------------------------------------------------
	 {
		  if( sIn==tkPROCEDURE) return "PROCEDURE";
		  if( sIn==tkURL) return "URL";
		  if( sIn==tkFOREACH) return "FOREACH";
		  if( sIn==tkSTARTPATTERN) return "STARTPATTERN";
		  if( sIn==tkENDPATTERN) return "ENDPATTERN";
		  if( sIn==tkREGEX) return "REGEX";
		  //if( sIn==tkSTORE) return "STORE";
		  if( sIn==tkCODE) return "CODE";
		  if( sIn==tkENDVAR) return "ENDVAR";
		  if( sIn==tkENDFOREACH) return "ENDFOREACH";
		  if( sIn==tkENDPROCEDURE) return "ENDPROCEDURE";
		  if( sIn==tkCALL) return "CALL";
		  if( sIn==tkLABEL) return "LABEL";
		  if( sIn==tkVAR) return "VAR";
		  if( sIn==tkLINK) return "LINK";
		  if( sIn==tkEND) return "END";
		  if( sIn==tkPASSPHRASE) return "PASSPHRASE";
		  if( sIn==tkMAIN) return "MAIN";
		  if( sIn==tkENDMAIN) return "ENDMAIN";
		  if( sIn==tkENDLINK) return "ENDLINK";
		  if( sIn==tkIGNORE) return "IGNORE";
		  if( sIn==tkOCCURS) return "OCCURS";
		  if( sIn==tkMAXITERATIONS) return "MAXITERATIONS";
		  if( sIn==tkSKIP) return "SKIP";
		  if( sIn==tkCUT) return "CUT";
		  if( sIn==tkENDCUT) return "ENDCUT";
		  if( sIn==tkXPATH) return "XPATH";
		  // globals
		  if( sIn==tkLOGLEVEL) return "LOGLEVEL";
		  if( sIn==tkPROXY) return "PROXY";
		  if( sIn==tkSIMULATION) return "SIMULATION";
		  if( sIn==tkCONSOLIDATE) return "CONSOLIDATE";
		  if( sIn==tkKEEPOUTPUTFILE) return "KEEPOUTPUTFILE";
		  if( sIn==tkOUTPUTFILENAME) return "OUTPUTFILENAME";
		  if( sIn==tkBROWSER) return "BROWSER";
		  if( sIn==tkROBOT) return "ROBOT";
		  //
		  if( sIn==tkFORM) return "FORM";
		  if( sIn==tkENDFORM) return "ENDFORM";
		  if( sIn==tkFORMFIELD) return "INPUT";
		  if( sIn==tkFORMNAME) return "FORMNAME";
		  //
		  if( sIn==tkBLOB) return "BLOB";
		  if( sIn==tkENDBLOB) return "ENDBLOB";
		  //
		  if( sIn==tkTRACEID) return "TRACEID/CREATEDAT";
		  //
		  return("(3)UNKNOWN COMMAND [" + sIn +"]");
	 }
	 //
	 //---------------------------------------------------------------------------------
	 ArrayList<parseCommand> parseCommands( String sIn , int lijnteller)
	 //---------------------------------------------------------------------------------
	 {
		 ArrayList<parseCommand> lst = new ArrayList<parseCommand>();
		 
		 String sOrig= sIn;
		 sIn = xMSet.xU.vervangtussenCommandQuotes(sIn,';','ç');  // bvb. getfield(1,";") wordt getfield(1,"ç")
		 sIn = xMSet.xU.vervangtussenCommandQuotes(sIn,',','ù');  // bvb. getfield(1,",") wordt getfield(1,"ù")
		 //
	     sIn = xMSet.xU.vervangtussenHaakjes(sIn,';','µ');
		 sIn = xMSet.xU.vervangtussenHaakjes(sIn,',','µ');
			 
	     sIn = xMSet.xU.vervangtussenDubbelQuotes(sIn,',','§');
	     sIn = xMSet.xU.vervangtussenDubbelQuotes(sIn,';','£');
	     sIn = xMSet.xU.VervangKarakter(sIn,';',',') + ",";
	     int aantal = xMSet.xU.TelDelims(sIn,',');
         for(int i=0;i<(aantal+1);i++)
		 {
			 String sX = xMSet.xU.GetVeld(sIn,i,',').trim();
			 sX = xMSet.xU.VervangKarakter(sX,'£',';');
			 sX = xMSet.xU.VervangKarakter(sX,'§',',');
			 if ( sX.length() <= 0 ) continue;
			 // er moet ( en ) staan
			 if ( (sIn.indexOf('(') < 0) || (sIn.indexOf(')') < 0) ) {
				 Error("Error on [" + sIn + "] missing round parenthesis on [" + sOrig + "] on line " + lijnteller);
				 return null;
			 }
			 String sArg = xMSet.xU.GetVeld(sX,2,'(').trim();
			 sArg = xMSet.xU.RemplaceerNEW(sArg,")","");
			 
			 // indien begin en einde " verwijderen
			 sArg = xMSet.xU.verwijderEnclosingQuotes(sArg);
			 sArg = xMSet.xU.RemplaceerNEW(sArg,"\\\"","\"");  //  \" naar "
			 String sCmd = xMSet.xU.GetVeld(sX,1,'(').trim();
			 
			 // staan er µ in de params, tz   getfield(a,b)
			 String sPrm = xMSet.xU.GetVeld(sX,2,'(').trim();
			 if( sPrm.indexOf('µ')>=0) {
	             //Error("-->getfield " + sPrm);
	             //sPrm = xMSet.xU.RemplaceerNEW(sPrm,"\"µ\"","ù");
				 int ia = xMSet.xU.TelDelims(sPrm,'µ');
				 for(int k=0;k<=ia;k++) {
					 sPrm = xMSet.xU.RemplaceerNEW(sPrm,")","").trim();
					 String sss = xMSet.xU.GetVeld(sPrm,(k+1),'µ');
					 sss = xMSet.xU.verwijderEnclosingQuotes(sss).trim();
					 sss = xMSet.xU.RemplaceerNEW(sss,"\\\"","\"");
					 if( k==0) sArg = sss; else sArg = sArg + "," + sss;
				 }
				 if( sCmd.compareToIgnoreCase("GETFIELD")==0) {
					 if( xMSet.xU.TelDelims(sArg,',') != 1) {
						 Error("GETFIELD requires 2 parameters [" + sArg + "]");
						 return null;
					 }
					 String een = xMSet.xU.GetVeld(sArg,1,',').trim();
					 String twee = xMSet.xU.GetVeld(sArg,2,',');
					 if( twee.length() != 1 ) {
						 Error("GETFIELD 2nd parameter must be a single character [" + sArg + "]");
						 return null;
					 }
					 int oo = xMSet.xU.NaarInt(een);
					 if( oo <= 0) {
						 Error("GETFIELD 1st parameter must be a number [" + sArg + "]");
						 return null; 
					 }
				 }
			 }
		     String sParams = sArg;  if ( sParams.length() < 1) sParams = null;
		     parseCommand x = new parseCommand(OIDSeed++);
		     int iToken = x.getCommandToken(sCmd);
		     if( iToken < 0 ) {
		    	 Error("Unknown command [" + sCmd + "] on " + sOrig);
		    	 return null;
		     }
		     x.CommandType = iToken;
		     x.params = sParams;
		     lst.add( x );
		 }
		 return lst;
	 }
	 
	 
	 //
	 //---------------------------------------------------------------------------------
	 boolean checkConsistency(ArrayList<parseFunction> lstFunc)
	 //---------------------------------------------------------------------------------
	 {
		 boolean isOK=true;
		 // various kinds of consitency checks
		 
		 // unreferenced procedures
		 for(int i=0;i<lstFunc.size();i++)
		 {
			 for(int j=0;j<lstFunc.get(i).lstInstructies.size();j++)
			 {
				 String sCall = lstFunc.get(i).lstInstructies.get(j).referencedForEach;
				 if( sCall == null ) continue;
				 boolean found = false;
				 for(int k=0;k<lstFunc.size();k++)
				 {
					 String sName = lstFunc.get(k).Name.trim();
					 if( sName.compareTo(sCall) == 0 ) {
						 found = true;
						 break;
					 }
					 if( sName.compareToIgnoreCase(sCall) == 0 ) {
						 LogIt(0,"[" + sName + "] possible error in type case");
					 }
				 }
				 if( found == false ) {
					 Error("[CALL " + sCall + "] is calling an unreferenced procedure");
					 isOK = false;
				 }
			 }
		 }
		 return isOK;
	 }
	 //
	 //---------------------------------------------------------------------------------
	 boolean gotVarLinks()
	 //---------------------------------------------------------------------------------
	 {
	   int i = this.VarTeller + this.LinkTeller;
	   if( i <= 0 ) return false;
	   return true;
	 }
	 //
	 //---------------------------------------------------------------------------------
	 private String NormaliseerURL(String sIn)
	 //---------------------------------------------------------------------------------
	 {
	   String sRet = sIn.trim();
	   if( sRet.toLowerCase().indexOf("http://") != 0 ) sRet = "http://" + sRet;
	   // TODO indien eindigen op / verwijderen
	   return sRet;
	 }
}
