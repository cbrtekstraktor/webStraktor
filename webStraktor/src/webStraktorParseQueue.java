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
	  * Contact details for copyright holder:  webstraktor@gmail.com
	  * GNU General Public License : www.gnu.org/copyleft/gpl.html
	  *
	  * 
	 */

import java.io.FileOutputStream;
import java.util.ArrayList;



//import com.sun.corba.se.spi.orbutil.fsm.Action;


public class webStraktorParseQueue {

	ArrayList<String> cmdLst = null;
	private String[] cmdArgs;
	private boolean initOK = true;
	private String flexRootDir = "";
	private boolean ProxyTest = false;
	private boolean AddProxies=false;
	private String ProxyInputFileNaam=null; 
	webStraktorSettings xMSet = null;
	webStraktorDateTime xdatetime = null;
	private String FMarkerFile = null;
	
	
	class proxytest
	{
		int idx;
		String SourceCodeFile=null;
		String Host=null;
		boolean isFunctioning=false;
		String LastError="";
	}
	private ArrayList<proxytest> proxytestLst = null;
	
	
	class glyphProxy
	{
		String Url=null;
		String Action=null;
		String Input=null;
		String Form=null;
		int Probability=0;
		long responsTime=0L;
	}
	ArrayList<glyphProxy> glyphProxyList = null;
	//
	//---------------------------------------------------------------------------------
	webStraktorParseQueue(webStraktorSettings iX , String[] args)
	//---------------------------------------------------------------------------------
	{
		xMSet = iX;
		cmdArgs = args;
		xdatetime = new webStraktorDateTime(xMSet.TimeZone);
		cmdLst = new ArrayList<String>();
		// -D <directory> -Q <queue file>
		// -D <directory> -P <sourcecode> p1 p2 p3
		// -D <directory> -PROXYADD <file>
		// -D <directory> -PROXYTEST
		evaluateArgs(args);
		
	}
	//
	//---------------------------------------------------------------------------------
	void logit(int level , String sIn)
	//---------------------------------------------------------------------------------
	{
		xMSet.LogIt(level, "QUE - " + sIn);
	}
	//
	//---------------------------------------------------------------------------------
	boolean executeQueu()
	//---------------------------------------------------------------------------------
	{
		if( initOK == false ) {
			logit(0,"crawler queue did not initialize correctly");
			return false;
		}
		if( xMSet.xU.IsDir(this.flexRootDir) == false ) {
			logit(0,"Could not open webStraktor basedir [" + flexRootDir +"]");
			return false;
		}
		//
		ZetMarkerFile();
		//
		if( ProxyTest == true ) {
			do_proxytest();
	        if( cmdLst.size() == 0 ) {UnzetMarkerFile(); return true; };  // else  doorgaan zodat de queue verwerkt wordt
		}
		if( AddProxies == true ) {
			do_addProxies();
	        if( cmdLst.size() == 0 ) {UnzetMarkerFile(); return true; };  // else  doorgaan zodat de queue verwerkt wordt
		}
		// if there is a queue run the queue, otherwise just run the controller
		if( cmdLst.size() == 0 ) {
			webStraktorParseController x = new webStraktorParseController(xMSet , cmdArgs );
			boolean ib = x.execController();
			UnzetMarkerFile();
			return ib;
		}
		// iterate through the q
		for(int i=0;i<cmdLst.size();i++)
		{
			// File presence check
			{
			 String sTemp = flexRootDir + xMSet.xU.ctSlash + "Src" + xMSet.xU.ctSlash + cmdLst.get(i);
			 sTemp = xMSet.xU.RemplaceerNEW(sTemp," ","|");
		     sTemp = xMSet.xU.GetVeld(sTemp,1,'|');
			 if( xMSet.xU.IsBestand(sTemp) == false ) {
				logit(0,"Source code [" + cmdLst.get(i) + "] not found [" + sTemp + "]");
				continue;
			 }
			}
			// -D <dir> -P <source> p1 p2 p3
			String sL = cmdLst.get(i).trim();
			sL = xMSet.xU.OntdubbelKarakter(sL,' ');
			int aantal = xMSet.xU.TelDelims(sL,' ');
			String[] args = new String[aantal+4];
			args[0] = "-D";
			args[1] = flexRootDir;
			args[2] = "-P";
			for(int j=0;j<=aantal;j++)
			{
				args[3+j] = xMSet.xU.GetVeld(sL,(j+1),' ');
			}
			String sT="";
			for(int j=0;j<args.length;j++) sT = sT + " " + args[j];
			logit(5,"Calling controller : " + sT);
			webStraktorParseController x = new webStraktorParseController(xMSet , args );
			boolean isOK = x.execController();
			if( isOK == false ) {
				logit(0,"Error running parseController : " + sT);
			}
			// post processing when proxy testing
			if( this.ProxyTest == true ) {
				this.proxytestLst.get(i).isFunctioning = isOK;
				this.proxytestLst.get(i).LastError = x.getErrorList();
				if( xMSet.xU.IsBestand(this.proxytestLst.get(i).SourceCodeFile) == true ) {
					xMSet.xU.VerwijderBestand(this.proxytestLst.get(i).SourceCodeFile);
				}
			}
			x = null;
		}
		if( this.ProxyTest == true )  ProxyReport();
		if( this.AddProxies == true ) MakeProxyConfigFile();
		UnzetMarkerFile();
		return true;
	}
	//
	//---------------------------------------------------------------------------------
	void evaluateArgs(String[] args)
	//---------------------------------------------------------------------------------
	{
		int idx = -1;
		int pdx = -1;
		int zdx = -1;
		for(int i=0;i<args.length;i++)
		{
			if( args[i].trim().compareToIgnoreCase("-Q") == 0) {idx = i; continue;}
			if( args[i].trim().compareToIgnoreCase("-D") == 0) {pdx = i; continue;}
			if( args[i].trim().compareToIgnoreCase("-PROXYTEST") == 0) {ProxyTest = true; continue;}
			if( args[i].trim().compareToIgnoreCase("-ADDPROXY") == 0) {zdx=i; continue;}
		}
		//
		if( pdx < 0 ) return;
		pdx++;
		if( pdx >= args.length ) {
			logit(0,"No workingdirectory file specified");
			return;
		}
		flexRootDir = args[pdx];
		if( xMSet.xU.IsDir( flexRootDir ) == false ) {
			logit(0,"Cannot access directory [" + flexRootDir + "]");
			return;
		}
		else logit(0,"RootDir [" + flexRootDir +"]");
		// 
		// Addproxy ??
		if( zdx >= 0 ) {
			zdx++;
			if( zdx >= args.length ) {
				logit(0,"There is no ProxyList specified for adding");
				initOK=false;
				return;
			}
			ProxyInputFileNaam = flexRootDir + xMSet.xU.ctSlash + args[zdx];
			if( xMSet.xU.IsBestand(ProxyInputFileNaam) == false ) {
				logit(0,"The file with proxies to be added [" + ProxyInputFileNaam + "] cannot be accessed");
				initOK=false;
				return;
			}
			AddProxies = true;
			return;
		}
		if( idx < 0 ) return;
		idx++;
		if( idx >= args.length ) {
			logit(0,"No queue file specified");
			initOK=false;
			return;
		}
		String FNaam = flexRootDir + xMSet.xU.ctSlash + "Src" + xMSet.xU.ctSlash + args[idx];
		if( xMSet.xU.IsBestand(FNaam) == false ) {
			logit(0,"Cannot access queuefile [" + FNaam + "]");
			initOK = false;
			return;
		}
		//
		String sQ = xMSet.xU.ReadContentFromFile(FNaam,5000) + "\n\n";
		int aantal = xMSet.xU.TelDelims(sQ, '\n');
		for(int i=0;i<aantal;i++) {
			String sL = xMSet.xU.GetVeld(sQ,(i+1),'\n').trim();
			if( sL.length() <= 2) continue;
			if( sL.startsWith("--") == true) continue;
			if( sL.startsWith("//") == true) continue;
			if( sL.toUpperCase().startsWith("REM ") == true) continue;
			cmdLst.add(sL);
			logit(5,"Queuing --> " + sL);
		}
		if( cmdLst.size() < 1) {
			initOK = false;
			logit(0,"Empty queue file [" + FNaam + "]");
			return;
		}
		//
	}
	//
	//---------------------------------------------------------------------------------
	void do_proxytest()
	//---------------------------------------------------------------------------------
	{
	  // reinit de queue
	  cmdLst = null;
 	  cmdLst = new ArrayList<String>();
 	  proxytestLst = new ArrayList<proxytest>();
 	  //
	  logit(0,"Running in Proxy test mode");
	  for(int i=0;i<xMSet.lstProxyConfig.size();i++)
	  {
		  if( xMSet.lstProxyConfig.get(i).enabled == true ) {
			  // maak een source code file en zet op de queue
			  String sBase = "flex-Proxy-Test-" + i + ".txt";
			  String FCode = flexRootDir + xMSet.xU.ctSlash + "Src" + xMSet.xU.ctSlash + sBase ;
			  if( xMSet.xU.IsBestand(FCode) == true) xMSet.xU.VerwijderBestand(FCode);
			  if( xMSet.xU.IsBestand(FCode) == true) {
				  logit(0,"Cannot remove [" + FCode + "]");
				  continue;
			  }
			  webStraktorPrintStream writer = new webStraktorPrintStream(FCode);
			  writer.println("REM Generated Script to test proxies");
			  writer.println("PROXY " + xMSet.lstProxyConfig.get(i).host);
			  writer.println("LOGLEVEL 9");
			  writer.println("KEEPOUTPUTFILE NO");
			  writer.println("CONSOLIDATE NO");
			  writer.println("SIMULATION NO");
			  writer.println("OUTPUTFILENAME flexProxyTest");
			  writer.println("main");
			  writer.println("URL " + xMSet.getProxyTestUrl() ); 
			  writer.println("PASSPHRASE \"" + xMSet.getProxyPassPhrase() + "\""); 
			  writer.println("end main");
			  writer.close();
			  // zet op de queue
			  cmdLst.add(sBase);
			  proxytest x = new proxytest();
			  x.idx = i;
			  x.Host = xMSet.lstProxyConfig.get(i).host;
			  x.SourceCodeFile = FCode;
			  proxytestLst.add(x);
		  }
		  else {
			  logit(5,"Proxy [" + xMSet.lstProxyConfig.get(i).host + "] is not enabled");
		  }
	  }
	}
	//
	//---------------------------------------------------------------------------------
	void ProxyReport()
	//---------------------------------------------------------------------------------
	{
		  String FNaam = flexRootDir + xMSet.xU.ctSlash + "active-proxies.xml";
		  webStraktorPrintStream uit = new webStraktorPrintStream(FNaam);
		  logit(5,"Writing report [" + FNaam + "]");
		  uit.println("<?xml version=\"1.0\" encoding=\"" + xMSet.getTargetCodePageString() + "\"?>");
		  uit.println("<!-- webStraktor active proxy report -->");
		  uit.println("<!-- " + xdatetime.DateTimeNow(xMSet.DateFormat) + "-->");
		  uit.println("<ActiveproxyReport>");
		  uit.println("<Timestamp>" + xdatetime.Now() + "</Timestamp>");
		  for(int i=0;i<xMSet.lstProxyConfig.size();i++)
		  {
			  String sLijn= "Proxy [" + xMSet.lstProxyConfig.get(i).host.trim() + "]";
			  uit.println(" <proxy>");
			  uit.println("  <host>" + xMSet.lstProxyConfig.get(i).host.trim() + "</host>");
			  sLijn = xMSet.xU.RPad(sLijn,40);
			  if( xMSet.lstProxyConfig.get(i).enabled == false ) {
				   sLijn = "-------- " +sLijn + " is not enabled";
				   uit.println("<status>disabled</status>");
			  }
			  else {
				  int idx = -1;
				  for(int j=0;j<proxytestLst.size();j++) {
					  if( proxytestLst.get(j).idx == i ) { idx = j; break; }
				  }
				  if( idx >= 0 ) {
					  if( proxytestLst.get(idx).isFunctioning ) {  
						  sLijn = "---OK--- " + sLijn + " is functioning ok";   
						  uit.println("<status>active</status>");
					  }
					  else { 
						  sLijn = "-ERROR-- " + sLijn + " " + proxytestLst.get(idx).LastError;  
						  uit.println("<status>inactive</status>");
						  uit.println("<error>" + proxytestLst.get(idx).LastError + "</error>");
					  }
				  }
			  }
			  logit(0,sLijn);
			  uit.println(" </proxy>");
		  }
		  uit.println("</ActiveproxyReport>");
		  uit.close();
	}
	//
	//---------------------------------------------------------------------------------
	void CheckProxyTime()
	//---------------------------------------------------------------------------------
	{
	  Long elapsed = xdatetime.Now() - xMSet.ProxyLastAssessed;
	  if( elapsed > (1000L * 60L * 60L * 24L * 7L) )  // 7 days
	 	  logit(0,"Proxies have not been assessed in 7 days. Consider to run webStraktor in proxytest mode");
	}
	//
	//---------------------------------------------------------------------------------
	void ZetMarkerFile()
	//---------------------------------------------------------------------------------
	{
		FMarkerFile = this.flexRootDir + xMSet.xU.ctSlash + "webStraktor.BUSY";
		webStraktorDateTime xt = new webStraktorDateTime(xMSet.TimeZone);
		webStraktorPrintStream fd = new webStraktorPrintStream(FMarkerFile);
		fd.println("WebStraktor : " + xt.DateTimeNow(xMSet.DateFormat));
		fd.close();
	}
	//
	//---------------------------------------------------------------------------------
	void UnzetMarkerFile()
	//---------------------------------------------------------------------------------
	{
		
		if( xMSet.xU.IsBestand(FMarkerFile)== true ) {
			boolean ib = xMSet.xU.VerwijderBestand(FMarkerFile);
			if( ib == false ) System.out.println("Cannot remove markerfile [" + FMarkerFile + "] ");
		}
	}
	//
	//---------------------------------------------------------------------------------
	private boolean do_addProxies()
	//---------------------------------------------------------------------------------
	{
		// lijst
		ArrayList<String> inlijst = new ArrayList<String>();
		String sLijn = xMSet.xU.ReadContentFromFile(this.ProxyInputFileNaam,1000);
		int aantal = xMSet.xU.TelDelims(sLijn,'\n');
		for(int i=0;i<=aantal;i++)
		{
		  String sHost = xMSet.xU.GetVeld(sLijn,(i+1),'\n').trim();
		  if( sHost == null ) continue;
		  if( sHost.trim().length() < 1 ) continue;
		  if( sHost.startsWith("--") == true ) continue;  // comment
		  if( sHost.startsWith("http://") == true ) sHost = xMSet.xU.Remplaceer(sHost,"http://","");
		  sHost = xMSet.xU.StripTrailingSlash(sHost);
		  inlijst.add(sHost);
		}
		// bestaande
		ArrayList<String> exlijst = new ArrayList<String>();
		for(int i=0;i<xMSet.lstProxyConfig.size();i++)
		{
		  String sHost = xMSet.lstProxyConfig.get(i).host.trim();
		  if( sHost == null ) continue;
		  if( sHost.trim().length() < 1 ) continue;
		  exlijst.add(sHost);
		}
		// verwijder bestaande
		for(int i=0;i<exlijst.size();i++)
		{
			for(int j=0;j<inlijst.size();j++)
			{
				if (inlijst.get(j) == null ) continue;
				if( exlijst.get(i).compareToIgnoreCase(inlijst.get(j))==0) {
					logit(5,"proxy [" + exlijst.get(i) + "] already covered");
					inlijst.set(j, null);
					break;
				}
			}
		}
		// logger
		String LogFileNaam = this.flexRootDir + xMSet.xU.ctSlash + "Log" +  xMSet.xU.ctSlash + "Add-Proxy.log";
		webStraktorLogger proxyLogger = new webStraktorLogger(9,LogFileNaam,xMSet.TimeZone,xMSet.LoggingDateFormaat);
		
		//
		glyphProxyList = new ArrayList<glyphProxy>();
		// kijk of de proxies antwoorden; lees de forms uit en zoek naar mogelijke glyphs
		for(int i=0;i<inlijst.size();i++)
		{
			if( inlijst.get(i) == null ) continue;
			if( inlijst.get(i).length() < 1 ) continue;
			webStraktorHTTPClient httpproxyclient = new webStraktorHTTPClient( xMSet , null, false, proxyLogger , 1);
			if( httpproxyclient.isActive == false ) return false;
			httpproxyclient.setRobotmode(false);
			boolean ibb = test_proxy( httpproxyclient , inlijst.get(i) , proxyLogger );
			
			// debug 
			if( i >= 2 ) break;
		}
		
		// Remove all proxies from xMSet and replace
		//xMSet.clearProxyConfig();
		for(int i=0;i<glyphProxyList.size();i++)
		{
		  sLijn = "Adding PROXY=[" + glyphProxyList.get(i).Url + "] Form=[" + glyphProxyList.get(i).Form + "] Action=[" + 	glyphProxyList.get(i).Action + "]  Input=[" + glyphProxyList.get(i).Input + "] Probalility=[" + glyphProxyList.get(i).Probability + "]";
		  proxyLogger.Logit(0,sLijn);
		  //
		  xMSet.addToProxyConfig(glyphProxyList.get(i).Url,glyphProxyList.get(i).Action,glyphProxyList.get(i).Input );
		}
		
		// run proxytest
		do_proxytest();
		ProxyTest=true;
		
		proxyLogger.CloseLogs();
		return true;
	}
	//
	//---------------------------------------------------------------------------------
	private boolean test_proxy(webStraktorHTTPClient http , String sUrl , webStraktorLogger proxyLogger)
	//---------------------------------------------------------------------------------
	{
		
		String sHost = xMSet.xU.getHostNameFromURL(sUrl);
		sHost = xMSet.xU.RemplaceerNEW(sHost,".","-").trim().toLowerCase();
		String TempFileNaam = this.flexRootDir + xMSet.xU.ctSlash + "Temp" +  xMSet.xU.ctSlash + sHost + ".txt";
		proxyLogger.Logit(5,"===========================================================================");
		proxyLogger.Logit(5,"-->> Testing proxy [" + sUrl + "] sending output to [" + TempFileNaam + "]");
		//
		if( xMSet.xU.IsBestand(TempFileNaam) == true ) {
			xMSet.xU.VerwijderBestand(TempFileNaam);
			if( xMSet.xU.IsBestand(TempFileNaam) == true ) {
				proxyLogger.Logit(0,"cannot remove [" + TempFileNaam + "]");
				return false;
			}
		}
		//
		http.verwijderCookies();
		if( sUrl.startsWith("http:") == false ) sUrl = "http://" + sUrl;
		boolean ibb = http.FetchThisURL(sUrl,TempFileNaam, webStraktorSettings.fileMIMEType.TEXT );
		if( ibb == false ) {
			proxyLogger.Logit(0,"URL [" + sUrl + "] cannot be accessed");
			return false;
		}
		//
		webStraktorParseForm x = new webStraktorParseForm( TempFileNaam , xMSet , proxyLogger);
	    ibb = x.parsedOK();
	    if( ibb == false ) {
	    	proxyLogger.Logit(0,"FORM parser failed on [" + sUrl + "] ");
	    	return false;
	    }
	    // Zoek de FORM die URL input voor de proxy uitvoert.
	    String formName = x.findProxyForm();
	    if( formName == null ) formName="";
	    if( formName.length() == 0 ) {
	    	  proxyLogger.Logit(0,"Formname is [" + formName + "] is empty");
	    	  return false;
	    }
	    // action
	    String sAction = x.getAction(formName); if (sAction == null ) sAction = "";
	    if( sAction.length() == 0 ) {
	    	  proxyLogger.Logit(0,"Formname is [" + formName + "] Action is empty");
	    	  return false;
	    }
	    // input veld
	    String sInput = x.getSingleTextInput(formName); if( sInput == null ) sInput = "";
	    if( sInput.length() == 0 ) {
	    	  proxyLogger.Logit(0,"Formname is [" + formName + "] no input veld");
	    	  return false;
	    }
	    //
	    glyphProxy y = new glyphProxy();
	    y.Url = xMSet.xU.Remplaceer(sUrl,"http://","").trim();
	    y.Form = formName.trim();
	    y.Action = sAction.trim();
	    y.Input = sInput.trim();
	    y.Probability = x.getLastProbability();
	    glyphProxyList.add(y);
	    //
		return true;
	}
	//
	//---------------------------------------------------------------------------------
	void MakeProxyConfigFile()
	//---------------------------------------------------------------------------------
	{
		  String FNaam = flexRootDir + xMSet.xU.ctSlash + xMSet.PROXIESCONFIGNAME;
		  String FCopyNaam = FNaam + "-" + (xdatetime.DateTimeNow("YYMMDDHHMISS")).trim() + ".xml";
		  FNaam = FNaam + ".xml";
		  try {
		      xMSet.xU.copyFile(FNaam,FCopyNaam);
		  }
		  catch(Exception e) {
			  logit(0,"Cannot copy [" + FNaam + "] to [" + FCopyNaam +"]");
			  return;
		  }
		  webStraktorPrintStream uit = new webStraktorPrintStream(FNaam);
		  logit(5,"Writing new ProxyConfigFile [" + FNaam + "]");
		  uit.println("<?xml version=\"1.0\" encoding=\"" + xMSet.getTargetCodePageString() + "\"?>");
		  uit.println("<!-- webStraktor generated Proxy Config File -->");
		  uit.println("<!-- " + xdatetime.DateTimeNow(xMSet.DateFormat) + "-->");
		
		  uit.println("<ProxyList>");
		  uit.println("<Timestamp>" + xdatetime.Now() + "</Timestamp>");
		  for(int i=0;i<xMSet.lstProxyConfig.size();i++)
		  {
			  int AddIdx=-1;
			  for (int j=0;j<this.glyphProxyList.size();j++)
			  {
				  if( glyphProxyList.get(j).Url.trim().compareToIgnoreCase(xMSet.lstProxyConfig.get(i).host.trim())==0) {
					
					  AddIdx = j;
					  break;
				  }
			  }
			  int TestIdx = -1;
			  for(int j=0;j<proxytestLst.size();j++) {
				  if( proxytestLst.get(j).idx == i ) { TestIdx = j; break; }
			  }
			  if( (AddIdx >= 0) && (proxytestLst.get(TestIdx).isFunctioning  == false)) continue; // nodeloos eennieuwe niet werkende toe te voegen
			  
			  uit.println(" <Proxy>");
			  uit.println("   <Host>"   + xMSet.lstProxyConfig.get(i).host.trim()   + "</Host>");
			  uit.println("   <Action>" + xMSet.lstProxyConfig.get(i).action.trim() + "</Action>");
			  uit.println("   <Input>"  + xMSet.lstProxyConfig.get(i).input.trim()  + "</Input>");
		 
			  //
			  if( xMSet.lstProxyConfig.get(i).enabled == false ) {
				   uit.println("   <Active>no</Active>");
			  }
			  else {
				  if( TestIdx >= 0 ) {
					  if( proxytestLst.get(TestIdx).isFunctioning ) { uit.println("   <Active>yes</Active>"); }
					                                           else { uit.println("   <Active>no</Active>");  }
				  }
			  }
			  // prio
			  int prio = 0;
			  if( AddIdx >= 0) prio =  glyphProxyList.get(AddIdx).Probability;
			  uit.println("   <Priority>"+ prio + "</Priority>");
			  uit.println("   <Cookiepolicy>browser</Cookiepolicy>");
			  uit.println("   <CookieEnabled>no</CookieEnabled>");
			  //
			  		
			  uit.println(" </Proxy>");
		  }
		  uit.println("</ProxyList>");
		  uit.close();
	}
}
