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

import java.util.ArrayList;


public class webStraktorSettings {

	String version   =  "1.0";
	String build     =  "2014-04-20";
	
	enum flexCodePage {LATIN1,UTF8};
	enum fileMIMEType {TEXT,BLOB };
	
	webStraktorUtil xU    = null;
	String DateFormat     = "DD-MMM-YY HH:MI:SS.MIL";
	String TimeZone       = "GMT+1";
	private flexCodePage DefaultTargetCodePage = flexCodePage.UTF8;
	private flexCodePage TargetCodePage = DefaultTargetCodePage;
	private String ProxyTestUrl         = "http://www.hebdosregionaux.ca/monteregie/le-courrier-du-sud";
	private String ProxyPassPhrase      = "Votre journal";
	private boolean overrulerobotspec   = false;
	private int crawlerDelay            = 5;
	private boolean AssessRobotsSettings  = true;
	private boolean AssessRobotsTxt     = AssessRobotsSettings;
	private boolean FullReportHTMLMode  = false;
	String LoggingDateFormaat = "HH:MI:SS.MIL";
	boolean isProxyAssess = false;
	long ProxyLastAssessed = 0L;
	int TracePort = 8001;
	public String PROXIESCONFIGNAME = "webStraktor-Proxies";
	String   sDefaultUserAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6 (.NET CLR 3.5.30729)";
	String   sUserAgent = sDefaultUserAgent;
	boolean isUserAgentAssess = false;
	
	class ProxyConfig
    {
    	String  host;
    	String  action;
    	String  input;
    	boolean enabled;
    	boolean cookieEnabled;
    	String  cookiepolicy;
    	int     priority;
    	
    	ProxyConfig()
    	{
    		enabled=false;
    		cookieEnabled=false;
    		priority=-1;
    	}
    }
    ArrayList<ProxyConfig> lstProxyConfig = new ArrayList<ProxyConfig>();
    ArrayList<ProxyConfig> lstActiveProxies = new ArrayList<ProxyConfig>();
    
    webStraktorTrace xTrc = null;
    
    class userAgent
    {
    	String browsercode="";
    	String  userAgentSignature="";
    }
    ArrayList<userAgent> userAgentList = new ArrayList<userAgent>();
    
    private int LogLevel = 5;
    //
    // ---------------------------------------------------------------------------------
	webStraktorSettings(String[] args)
	// ---------------------------------------------------------------------------------
	{
	  xU = new webStraktorUtil(this);
	  xTrc = new webStraktorTrace(this);
	  
	  // look for -C <configfile.xml> and read it
	  String sDIR = "";
	  String sFLE= "";
	  for(int i=0;i<args.length;i++)
	  {
		  if( (args[i].compareTo("-C")==0) && ( (i+1) < args.length) ) sFLE = args[i+1];
		  if( (args[i].compareTo("-D")==0) && ( (i+1) < args.length) ) sDIR = args[i+1];
	  }
	  String sCfgName = sDIR + xU.ctSlash + sFLE;
	  if( xU.IsBestand(sCfgName)==false ) {
		  LogIt(0,"Configuration file could not be found");
		  System.exit(1);
	  }
	  //
	  readConfigFile (sCfgName );
	  readProxyAssessFile( sDIR );
	  readUserAgentFile( sDIR ); shoUserAgentList();
	  //
	  //System.out.println("LAST " + ProxyLastAssessed);
	  //for(int i=0;i<this.lstActiveProxies.size();i++)  logit("Proxy:" + this.lstActiveProxies.get(i).host + this.lstActiveProxies.get(i).enabled);
	  for(int i=0;i<lstActiveProxies.size();i++)
	  {
		 int idx=-1;
		 for(int j=0;j<lstProxyConfig.size();j++) 
		 {
		   if( lstProxyConfig.get(j).host.trim().compareTo(lstActiveProxies.get(i).host.trim()) == 0 ) { idx =j; break; } 
		 }
		 if( idx < 0) continue;
		 if( lstActiveProxies.get(i).enabled == true ) {  // ACTIVE in active-proxies
			 if( lstProxyConfig.get(idx).enabled == false ) {  // INACTIVE in config
				  //lstProxyConfig.get(idx).enabled = true;
				 LogIt(0,"proxy [" + lstActiveProxies.get(i).host + "] status is disabled but active");
			 }
		 }
		 else {   // INACTIVE in the active list
			 if( lstProxyConfig.get(idx).enabled ) {   // Active in the Config
			  lstProxyConfig.get(idx).enabled = false;  // disable the proxy
			  LogIt(0,"proxy [" + lstActiveProxies.get(i).host + "] status has been overruled and set to disabled");
			 }
		 }
	  }
	}
	//
    // ---------------------------------------------------------------------------------
	void setLogLevel(int i)
	// ---------------------------------------------------------------------------------
	{
		LogLevel = i;
	}
	//
    // ---------------------------------------------------------------------------------
	void LogIt(int level , String sLijn)
	// ---------------------------------------------------------------------------------
	{
		if( level > LogLevel ) return;
		System.out.println(sLijn);
	}
	// ---------------------------------------------------------------------------------
	void logit( String sLijn)
	// ---------------------------------------------------------------------------------
	{
		LogIt(0,sLijn);
	}
	
	//
    // ---------------------------------------------------------------------------------
	public void readConfigFile(String FConfigNaam)
	// ---------------------------------------------------------------------------------
	{
		  webStraktorSAX sp = new webStraktorSAX(this);
	  	  boolean isOk = sp.ParseXMLFile(FConfigNaam);
	  	  // Read proxies
	  	  String FNaam = xU.GetPathName(FConfigNaam) + xU.ctSlash + PROXIESCONFIGNAME + ".xml";
	  	  // April 2015 - avoid compatibility issues du to renaming of flexCrawler to webStraktor 
	  	  if( xU.IsBestand(FNaam) == false ) {  // try to use flexCrawler-Proxies instead of webStraktor-proxies
	  		 String OldConfigName = "flexCrawler-Proxies";
	  		 FNaam = xU.GetPathName(FConfigNaam) + xU.ctSlash + OldConfigName + ".xml";
	  	     LogIt(0,"[" + FNaam +"] cannot be found. Testing old name [" + FNaam + "]" );
	  	     if( xU.IsBestand(FNaam) == true )  {
	  	    	 PROXIESCONFIGNAME = OldConfigName;
	  	    	 LogIt(1,"Switching to [" + FNaam + "]");
	  	     }
	  	  }
	  	  webStraktorSAX sp2 = new webStraktorSAX(this);
	  	  isOk = sp.ParseXMLFile(FNaam);
	  	  for(int i=0;i<this.lstProxyConfig.size();i++)  logit("Proxy:" + this.lstProxyConfig.get(i).host);
	  	  if( isOk == false ) {
	  		  LogIt(0,"Error in readConfigFile");
	  		  System.exit(1);
	  	  }
	}
	//
    // Event Handlers
    // ---------------------------------------------------------------------------------
	public void startNode(String qName , String sContent, String sHier) 
	// ---------------------------------------------------------------------------------
	{
		if( isUserAgentAssess ) {
			if(qName.equalsIgnoreCase("useragent")) {
				   userAgent x = new userAgent();
				   userAgentList.add(x);
				}
			return;
		}
		if( isProxyAssess ) {
			if(qName.equalsIgnoreCase("Proxy")) {
				   ProxyConfig x = new ProxyConfig();
				   lstActiveProxies.add(x);
				}
			return;
		}
		if(qName.equalsIgnoreCase("Proxy")) {
		   ProxyConfig x = new ProxyConfig();
		   lstProxyConfig.add(x);
		}
	}
	//
    // ---------------------------------------------------------------------------------
	public void endNode(String qName , String sContent, String sHier)
	// ---------------------------------------------------------------------------------
	{
		if( isUserAgentAssess ) {
			endNode3( qName , sContent , sHier );
			return;
		}
		if (isProxyAssess ) {
			endNode2( qName , sContent , sHier);
			return;
		}
		int xPositie=-1;
		if(qName.equalsIgnoreCase("Host"))           { xPositie=100;}
		if(qName.equalsIgnoreCase("Action"))         { xPositie=101;}
		if(qName.equalsIgnoreCase("Input"))          { xPositie=102;}
		if(qName.equalsIgnoreCase("Active"))         { xPositie=103;}
		if(qName.equalsIgnoreCase("Priority"))       { xPositie=104;}
		if(qName.equalsIgnoreCase("CookiePolicy"))   { xPositie=105;}
		if(qName.equalsIgnoreCase("CookieEnabled"))  { xPositie=106;}	
		if(qName.equalsIgnoreCase("DateFormat"))     { xPositie=107;}	
		if(qName.equalsIgnoreCase("TargetCodePage")) { xPositie=108;}
		if(qName.equalsIgnoreCase("TimeZone"))       { xPositie=109;}
		if(qName.equalsIgnoreCase("ProxyTestURL"))   { xPositie=110;}
		if(qName.equalsIgnoreCase("ProxyPassPhrase")){ xPositie=111;}
		if(qName.equalsIgnoreCase("OverRuleRobotSpecification")){ xPositie=112;}
		if(qName.equalsIgnoreCase("CrawlerDelay"))   { xPositie=113;}
		if(qName.equalsIgnoreCase("AssessRobotsTxt")){ xPositie=114;}
		if(qName.equalsIgnoreCase("FullReportHTMLmode")){ xPositie=115;}
		if(qName.equalsIgnoreCase("LogDateFormat"))  { xPositie=116;}
		if(qName.equalsIgnoreCase("IPCPort"))  { xPositie=117;}
		
		my_characters( sContent , xPositie);
	}
	//
    // ---------------------------------------------------------------------------------
	public void my_characters( String sContent , int xPositie)
	// ---------------------------------------------------------------------------------
	{
    	String tempVal = sContent;
    	switch ( xPositie )
		{
    	 case 100 : { lstProxyConfig.get(lstProxyConfig.size()-1).host = tempVal.trim(); break; }
    	 case 101 : { lstProxyConfig.get(lstProxyConfig.size()-1).action = tempVal.trim(); break; }
    	 case 102 : { lstProxyConfig.get(lstProxyConfig.size()-1).input = tempVal.trim(); break; }
    	 case 103 : { lstProxyConfig.get(lstProxyConfig.size()-1).enabled = xU.ValueInBooleanValuePair("="+tempVal.trim()); break; }
    	 case 104 : { lstProxyConfig.get(lstProxyConfig.size()-1).priority = xU.NaarInt(tempVal.trim()); break; }
    	 case 105 : { lstProxyConfig.get(lstProxyConfig.size()-1).cookiepolicy = tempVal.trim(); break; }
    	 case 106 : { lstProxyConfig.get(lstProxyConfig.size()-1).cookieEnabled = xU.ValueInBooleanValuePair("="+tempVal.trim()); break; }
    	 
    	 case 107 : { this.DateFormat = tempVal.trim(); break; }
    	 case 108 : { this.TargetCodePage = getCodePageEnum(tempVal.trim()); break; }
    	 case 109 : { this.TimeZone = tempVal.trim(); break; }
    	 case 110 : { this.ProxyTestUrl = tempVal.trim(); if( this.ProxyTestUrl.startsWith("http://") == false) this.ProxyTestUrl = "http://" + this.ProxyTestUrl; break; }
    	 case 111 : { this.ProxyPassPhrase = tempVal.trim(); break; }
    	 case 112 : { this.overrulerobotspec = xU.ValueInBooleanValuePair("="+tempVal.trim()); break; }
    	 case 113 : { this.crawlerDelay = xU.NaarInt(tempVal.trim()); break; }
    	 case 114 : { this.AssessRobotsTxt = xU.ValueInBooleanValuePair("="+tempVal.trim()); AssessRobotsSettings=AssessRobotsTxt; break; }
    	 case 115 : { this.FullReportHTMLMode = xU.ValueInBooleanValuePair("="+tempVal.trim()); break; }
    	 case 116 : { this.LoggingDateFormaat = tempVal.trim(); break; }
    	 case 117 : { this.TracePort = xU.NaarInt(tempVal.trim()); break; }
    	 
		 default  : return;
		}
	}
	//
    // ---------------------------------------------------------------------------------
	public void endNode2(String qName , String sContent, String sHier)
	// ---------------------------------------------------------------------------------
	{
		//System.out.println(qName + " " + sContent);
		int xPositie=-1;
		if(qName.equalsIgnoreCase("Host"))      { xPositie=100;}
		if(qName.equalsIgnoreCase("Status"))    { xPositie=101;}
		if(qName.equalsIgnoreCase("TimeStamp")) { xPositie=102;}
		//
		switch ( xPositie)
		{
		 case 100 :  { lstActiveProxies.get(lstActiveProxies.size()-1).host = sContent.trim(); break; }
		 case 101 :  { if ( sContent.trim().compareToIgnoreCase("Active")==0) lstActiveProxies.get(lstActiveProxies.size()-1).enabled = true; 
		                                                                 else lstActiveProxies.get(lstActiveProxies.size()-1).enabled = false;
		               break;
		              }
		 case 102 : { ProxyLastAssessed = xU.NaarLong(sContent.trim()); break; }
		 default : return;
		}
	}
	//
    // ---------------------------------------------------------------------------------
	private flexCodePage getCodePageEnum(String sIn)
    // ---------------------------------------------------------------------------------
	{
		String sCp = xU.RemplaceerNEW(sIn,"-","");
		sCp = xU.RemplaceerNEW(sCp," ","");
		if( sCp.compareToIgnoreCase("LATIN1")==0) return flexCodePage.LATIN1;
		if( sCp.compareToIgnoreCase("ISO88591")==0) return flexCodePage.LATIN1;
		if( sCp.compareToIgnoreCase("UTF8")==0) return flexCodePage.UTF8;
		return DefaultTargetCodePage;
	}
	//
    // ---------------------------------------------------------------------------------
	private flexCodePage getTargetCodePage()
	// ---------------------------------------------------------------------------------
	{
		return this.TargetCodePage;
	}
	//
    // ---------------------------------------------------------------------------------
	String getTargetCodePageString()
    // ---------------------------------------------------------------------------------
	{
		if ( this.getTargetCodePage() == flexCodePage.LATIN1 ) return "ISO-8859-1";
		return "UTF-8";
	}
	//
    // ---------------------------------------------------------------------------------
	String getProxyTestUrl()
    // ---------------------------------------------------------------------------------
	{
		return this.ProxyTestUrl.trim();
	}
	//
    // ---------------------------------------------------------------------------------
	String getProxyPassPhrase()
    // ---------------------------------------------------------------------------------
	{
		return this.ProxyPassPhrase.trim();
	}
	//
    // ---------------------------------------------------------------------------------
	boolean OverRuleRobotSpec()
    // ---------------------------------------------------------------------------------
	{
		return this.overrulerobotspec;
	}
	//
    // ---------------------------------------------------------------------------------
	int getCrawlerDelay()
    // ---------------------------------------------------------------------------------
	{
		if( crawlerDelay < 1  ) return 5;
		if( crawlerDelay > 30 ) return 5;
		return crawlerDelay;
	}
	//
    // ---------------------------------------------------------------------------------
	boolean getAssessRobotsTxt()
	// ---------------------------------------------------------------------------------
	{
		return this.AssessRobotsTxt;
	}
	//
    // ---------------------------------------------------------------------------------
	void overruleRobots(boolean ib)
    // ---------------------------------------------------------------------------------
	{
		AssessRobotsTxt=ib;
	}
	//
    // ---------------------------------------------------------------------------------
	void restoreRobotsSettings()
	// ---------------------------------------------------------------------------------
	{
		AssessRobotsTxt=AssessRobotsSettings;
	}
	//
    // ---------------------------------------------------------------------------------
	boolean getFullReportHTMLMode()
	// ---------------------------------------------------------------------------------
	{
		return this.FullReportHTMLMode;
	}
	//
    // ---------------------------------------------------------------------------------
	void readProxyAssessFile( String inDir )
    // ---------------------------------------------------------------------------------
	{
		  isProxyAssess = true;
		  String FNaam = inDir + xU.ctSlash + "active-proxies.xml";
		  webStraktorSAX sp = new webStraktorSAX(this);
	  	  boolean isOk = sp.ParseXMLFile(FNaam);
	}
	//
    // ---------------------------------------------------------------------------------
	void clearProxyConfig()
    // ---------------------------------------------------------------------------------
	{
		int aantal = this.lstProxyConfig.size();
		for( int i=0;i<aantal;i++) this.lstProxyConfig.remove(0);
	}
	//
    // ---------------------------------------------------------------------------------
	void addToProxyConfig(String sUrl, String sAction , String sInput)
	// ---------------------------------------------------------------------------------
	{
		ProxyConfig x = new ProxyConfig();
		x.host = sUrl;
		x.action = sAction;
		x.input = sInput;
		x.enabled = true;
		x.cookieEnabled = false;
		x.cookiepolicy = "unknown";
		lstProxyConfig.add(x);
	}
	//
	// ---------------------------------------------------------------------------------
	private void readUserAgentFile(String sDir)
	// ---------------------------------------------------------------------------------
	{
	   userAgentList = null;
	   userAgentList = new ArrayList<userAgent>();
	   String FNaam = sDir + xU.ctSlash + "useragents.xml";
	   if( xU.IsBestand(FNaam) == false ) {
		   LogIt(0,"Could not open User Agent file [" + FNaam );
		   return;
	   }
	   isUserAgentAssess=true;
	   webStraktorSAX sp = new webStraktorSAX(this);
	   sp.ParseXMLFile(FNaam);
	}
	//
    // ---------------------------------------------------------------------------------
	public void endNode3(String qName , String sContent, String sHier)
	// ---------------------------------------------------------------------------------
	{
		//System.out.println(qName + " " + sContent);
		int xPositie=-1;
		if(qName.equalsIgnoreCase("browser"))      { xPositie=100;}
		if(qName.equalsIgnoreCase("signature"))    { xPositie=101;}
		switch ( xPositie)
		{
		 case 100 :  { userAgentList.get(userAgentList.size()-1).browsercode = sContent.trim(); break; }
		 case 101 :  { userAgentList.get(userAgentList.size()-1).userAgentSignature = sContent.trim(); break; }
		 default : return;
		}
		
	}
	//
	// ---------------------------------------------------------------------------------
	public String getUserAgent()
	// ---------------------------------------------------------------------------------
	{
		return sUserAgent;
	}
	//
	// ---------------------------------------------------------------------------------
	public boolean browserExists(String sBrowser)
	// ---------------------------------------------------------------------------------
	{
		if( sBrowser == null ) return false;
		for(int i=0;i<userAgentList.size();i++)
		{
			if( userAgentList.get(i).browsercode.trim().compareToIgnoreCase(sBrowser.trim())==0) return true;
		}
		return false;
	}
	//
	// ---------------------------------------------------------------------------------
	public void setUserAgent(String sBrowser)
	// ---------------------------------------------------------------------------------
	{
		sUserAgent = sDefaultUserAgent;  // go for default
		int idx=-1;
		if( sBrowser != null )
		{
		  for(int i=0;i<userAgentList.size();i++)
		  {
			if( userAgentList.get(i).browsercode.trim().compareToIgnoreCase(sBrowser.trim())==0) { idx =i; break; }
		  }
		}
		if( idx < 0 ) {
			LogIt(0,"Browser [" + sBrowser +"] could not be found. Switching to default User-Agent");
			return;
		}
		String sTemp = userAgentList.get(idx).userAgentSignature;
		if( sTemp == null ) return;
		if( sTemp.length() < 10 ) return;  //  signature must be at least 10
		sUserAgent = userAgentList.get(idx).userAgentSignature;
		LogIt(5,"[" + sBrowser + "] found -> [" + sUserAgent + "]");
	}
	//
	// ---------------------------------------------------------------------------------
	private void shoUserAgentList()
	// ---------------------------------------------------------------------------------
	{
		LogIt(5,"User Agent List");
		for(int i=0;i<userAgentList.size();i++)
		{
			LogIt(5,"[" + userAgentList.get(i).browsercode + "] [" + userAgentList.get(i).userAgentSignature + "]");
		}
	}
}
