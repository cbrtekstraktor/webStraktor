/*
	  *
	  * Copyright 2014 - webStraktor
	  *
	  * uses Apache HTTPClient
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import java.net.URL;

import org.apache.http.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.Header;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.client.ResponseHandler;

import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;

import java.util.ArrayList;
import java.util.List;



public class webStraktorHTTPClient {

	webStraktorSettings xMSet = null;
	webStraktorLogger iLogger=null;
	
	DefaultHttpClient httpclient = null;
	boolean  ViaProxy =false;
	String   sProxyHost="";
    String   sProxyAction="";
    String   sProxyInput="";
    boolean  isActive=false;
    //String   sBrowser       = "Mozilla Firefox 2.0";
    String   sKeepAliveTijd = "300";
    boolean  CookieEnabled  = false; 
    private  boolean ProxyHasBeenInitialzed = false;
    private  long  MAXBUFSIZE = 30000000L;
    static final String HEXES = "0123456789ABCDEF";
    private  long lastSize=0L;
    private boolean robotmode = true;
    private int UrlRequestCounter = 0;
    private int crawlerDelay = 5;
    private String lastCharSet = null;
    private String lastFileName = null;
    String LastLocation = "";
    
    class MijnKookie
    {
    	String name;
    	String value;
    	String expires;
    	String domain;
    	String path;
    	boolean startbladzijde;
    	String cookietype;
    	long  created;
    	MijnKookie()
    	{
    		name="";
    		value="";
    		expires="";
    		domain="";
    		path="";
    		startbladzijde=true;
    		cookietype="";
    		created = System.currentTimeMillis();
    	}
    }
    ArrayList<MijnKookie> KookieLijst = new ArrayList<MijnKookie>();

    private static final int tpUNKNOWN  = 100;
    private static final int tpALLOW    = 101;
    private static final int tpDISALLOW = 102;
    private static final int tpFOLDER   = 200;
    private static final int tpFILE     = 201;
    
    class RobotSpec
    {
    	int access   = tpUNKNOWN;
    	int tipe     = tpFOLDER;
    	String value = null;
    	String sho()
    	{
    		String een = "Disallow"; if( access == tpALLOW ) een = "Allow";
    		String twee = "Folder"; if ( tipe == tpFILE ) twee = "File";
    		return een + " " + twee + " : " + value;
    	}
    }
    ArrayList<RobotSpec> robotspecLst = new ArrayList<RobotSpec>();
    
    //
	//---------------------------------------------------------------------------------
	webStraktorHTTPClient( webStraktorSettings iM , String iPreferredProxy , boolean iUseProxy , webStraktorLogger iL , int idel)
	//---------------------------------------------------------------------------------
	{
		isActive = false;
	    xMSet = iM;
	    iLogger = iL;
		ViaProxy = iUseProxy;
		crawlerDelay = idel;
		robotmode = xMSet.getAssessRobotsTxt();
			
        //		
		httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,xMSet.getUserAgent());
		httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpclient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
		//
		CookieEnabled = true;
		//
		isActive = InitialiseerProxy(iPreferredProxy);
	}
	//
	//---------------------------------------------------------------------------------
	void LogIt(int level , String sIn)
	//---------------------------------------------------------------------------------
	{
		if( iLogger != null) iLogger.Logit(level, "CLN - " + sIn);
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
	boolean InitialiseerProxy(String iPreferredProxy )
	//---------------------------------------------------------------------------------
	{
		if( !ViaProxy ) return true;
		if( xMSet.lstProxyConfig.size() == 0 ) {
			LogIt(6,"Empty proxy list - switching to NO proxy");
			ViaProxy = false;
			return true;
		}
		// selecteer een proxy
		int idx = -1;
		if( iPreferredProxy == null ) iPreferredProxy = "";
        if( iPreferredProxy.length() > 0) {
			   for(int i=0;i<xMSet.lstProxyConfig.size();i++) {
				 if( iPreferredProxy.trim().compareToIgnoreCase(xMSet.lstProxyConfig.get(i).host)==0)  {
					 idx=i;
					 break;
				 }
			   }
		   }
        if( idx == -1 )
        {
     	   for(int i=0;i<1000;i++)
     	   {
     		   long ldx = System.nanoTime() % (long)(xMSet.lstProxyConfig.size());
         	   idx = (int)ldx;
         	   if( xMSet.lstProxyConfig.get(idx).enabled == true ) break;
     	   }
     	   if( xMSet.lstProxyConfig.get(idx).enabled == false ) {
     		   LogIt(6,"Could not find an active proxy in list - switching to NO proxy");
     		   ViaProxy = false;
     		   return true;
     	   }
        }
        //
    	LogIt(1,"Selected proxy number ("+idx+") proxyhost [" + sProxyHost + "]");
        //
		sProxyHost    = xMSet.lstProxyConfig.get(idx).host;
		sProxyAction  = xMSet.lstProxyConfig.get(idx).action;
		sProxyInput   = xMSet.lstProxyConfig.get(idx).input;
		CookieEnabled = xMSet.lstProxyConfig.get(idx).cookieEnabled;
		//
		ZetCookiePolicy(xMSet.lstProxyConfig.get(idx).cookiepolicy);
		//   
		return true;
	}
	// 
	//---------------------------------------------------------------------------------
	void ZetCookiePolicy(String cookiepolicy)
	//---------------------------------------------------------------------------------
	{
		
		if ( cookiepolicy.compareToIgnoreCase("RFC_2965")==0 ) {
		 httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2965);
		 return;
		}
		if ( cookiepolicy.compareToIgnoreCase("BEST_MATCH")==0 ) {
		 httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
		 return;
		}
		if ( cookiepolicy.compareToIgnoreCase("NETSCAPE")==0 ) {
		 httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.NETSCAPE);
		 return;
		}
		if ( cookiepolicy.compareToIgnoreCase("RFC_2109")==0 ) {
		 httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2109);
		 return;
		}
		if ( cookiepolicy.compareToIgnoreCase("BROWSER_COMPATIBILITY")==0 ) {
			 httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
			 return;
		}
		LogIt( 1 , "Unknown CookiePolicy [" + cookiepolicy + "] on [" + sProxyHost + "] switching to BROWSER_COMPATIBILITY");
		// default -  werkt trouwens het best
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
	}
	//
	//---------------------------------------------------------------------------------
	String getProxyName()
	//---------------------------------------------------------------------------------
	{
		return sProxyHost;
	}
	//
	//
	//---------------------------------------------------------------------------------
	boolean Afsluiten()
	//---------------------------------------------------------------------------------
	{
		 httpclient.getConnectionManager().shutdown(); 
		 LogIt(6,"Shutdown httpclient");
		 return true;
	}
	//
	//
	//---------------------------------------------------------------------------------
	void StoreKookies()
	//---------------------------------------------------------------------------------
	{
		List<Cookie> cookies = httpclient.getCookieStore().getCookies();
        if ( cookies.isEmpty()) return;
        boolean startbladzijde;
        if ( KookieLijst.size() == 0 ) startbladzijde=true; else startbladzijde= false;
        LogIt(6,"== Cookie report =======================================================");
        for (int i = 0; i < cookies.size(); i++) 
        {
                LogIt(6," (httpclient) " + cookies.get(i).toString());
                boolean found=false;
                for(int jj=0;jj<KookieLijst.size();jj++)
                {
                  if ( (KookieLijst.get(jj).domain.compareToIgnoreCase(cookies.get(i).getDomain())==0) && 	
                	   (KookieLijst.get(jj).name.compareToIgnoreCase(cookies.get(i).getName())==0) &&
                	   (KookieLijst.get(jj).path.compareToIgnoreCase(cookies.get(i).getPath())==0) )
                  {
                	  KookieLijst.get(jj).value   = cookies.get(i).getValue();
                	  KookieLijst.get(jj).expires = ""+cookies.get(i).getExpiryDate();
                	  found = true;
                	  break;
                  }
                }  
                if( !found )
                {
                	  MijnKookie x = new MijnKookie();
                	  x.domain         = cookies.get(i).getDomain().trim();
                	  x.name           = cookies.get(i).getName().trim();
                	  x.value          = cookies.get(i).getValue().trim();
                	  x.path           = cookies.get(i).getPath().trim();
                	  x.expires        = (""+cookies.get(i).getExpiryDate()).trim();
                	  x.startbladzijde = startbladzijde;
                	  x.cookietype     = (""+cookies.get(i).getVersion()).trim();
                	  KookieLijst.add(x);
                }
        } 
        /* overbodig want hierboven
        for(int jj=0;jj<KookieLijst.size();jj++) {
        	LogIt(6," (crawler)    ["+KookieLijst.get(jj).name + "] [" + 
        			KookieLijst.get(jj).value + "] [" +
        			KookieLijst.get(jj).domain + "] [" +
        			KookieLijst.get(jj).startbladzijde+"] [" +
        			KookieLijst.get(jj).cookietype + "] [" +
        			KookieLijst.get(jj).expires + "]" );
        }
        */
	}
	//
	//
	//---------------------------------------------------------------------------------
	public void verwijderCookies()
	//---------------------------------------------------------------------------------
	{
		int aantal = this.KookieLijst.size();
		for (int i = 0; i < aantal; i++)
		{
				KookieLijst.remove(0);
				break;
		}
	}
	//
	//
	//---------------------------------------------------------------------------------
    String construeerKookie(String iUrl, boolean WEGDOEN )
    //---------------------------------------------------------------------------------
    {
      String sTemp="";
      //  
      
      if( this.ViaProxy == true ) {
    	  LogIt( 5 , "Proxy is used so Cookies will not be set.  TEMPORARILY DISABLED");
    	  //return sTemp;    
      }
      if( CookieEnabled == false  ) {
    	  LogIt( 5 , "Cookies are disabled");
    	  return sTemp;    
      }
     
      //
      String sHost = xMSet.xU.getHostNameFromURL(iUrl).trim();
      if( sHost == null ) return "";
      if( sHost.length() == 0 ) return "";
      String sDomain = sHost.startsWith("www.") ? sHost.substring(4) : sHost;
      //
      int j=0;
      for(int i=0;i<KookieLijst.size();i++) //  domain kan zowel  amazon.com als .amazon.com zijn op de cookie
      {
        if( (KookieLijst.get(i).domain.compareToIgnoreCase(sDomain)!=0) &&
        	(KookieLijst.get(i).domain.compareToIgnoreCase("www." + sDomain)!=0) &&		
        	(KookieLijst.get(i).domain.compareToIgnoreCase("."+sDomain)!=0) ) continue;
        if( j != 0) sTemp = sTemp + "; ";
        j++;
        sTemp = sTemp + KookieLijst.get(i).name.trim() + "=" + KookieLijst.get(i).value.trim();
      }
      
      LogIt(6,"-> Cookie for domain [" + sDomain + "] will be set to [" + sTemp + "]");
      return sTemp;
    }
    //
	//
	//---------------------------------------------------------------------------------
	void ToonHeader(Header[] hIn , String sRichting)
	//---------------------------------------------------------------------------------
	{
		LogIt(6,"== Header report =======================================================");
		if( hIn != null ) {
	 	  for(int jj=0;jj<hIn.length;jj++) {
	 		  LogIt(6, "    " + sRichting + "  ["+hIn[jj].toString()+"]");
	 		  if( hIn[jj].getName().compareToIgnoreCase("LOCATION")==0) {
              	LastLocation = hIn[jj].getValue();
              }
	 	  }
	 	  LogIt(6,"Lastlocation =" + LastLocation);
        }
		else {
		  Error("Bizar: No header info found");	
		}
	}
	//
	//
	//---------------------------------------------------------------------------------
	private boolean FetchViaProxy(String sDestUrl, String FNaam , webStraktorSettings.fileMIMEType iMimeTipe )
	//---------------------------------------------------------------------------------
	{
		// Veiligheid
		// indien niet als proxy geconfigureer doe dan gewoon
		if( ViaProxy == false ) return FetchUrl( sDestUrl , FNaam, iMimeTipe );
		LogIt(1,"====> FETCHING (VIA PROXY): " + sDestUrl);
		//
		// Je bent reeds doorheen de proxy gegaan
		if( ProxyHasBeenInitialzed == true ) {
			// sommige proxies hebben geen http://hostnaam staan.  Indien dat het geval is plak die ervoor
			if( sDestUrl.substring(0,4).compareToIgnoreCase("HTTP")!=0) {
				// soms staat er ?q= etc  dat moet dan  url /+ action + worden 
				String sAction = "";
				if( sDestUrl.substring(0,1).compareToIgnoreCase("?")==0) sAction = "/" + sProxyAction;
				if( sDestUrl.substring(0,1).compareToIgnoreCase("/")==0) sDestUrl = "http://" + sProxyHost + sAction  + sDestUrl;
				                                                    else sDestUrl = "http://" + sProxyHost + sAction + "/" + sDestUrl;
			}
			return FetchUrl(sDestUrl , FNaam , iMimeTipe);
		}
		ProxyHasBeenInitialzed=true;
		//
		//
		String sProxyHomeUrl = "http://" + sProxyHost + "/";
	    // Haal de Home page binnen 
        String FAnalyseer = FNaam + "_proxy_.txt";
	    boolean haalPageOne = FetchUrl( sProxyHomeUrl , FAnalyseer , iMimeTipe );
        if( haalPageOne == false ) return false;  
	    // Analayseer de form
        webStraktorParseForm x = new webStraktorParseForm(FAnalyseer,xMSet,iLogger);
        
        // 
        // TODO - analyseer nu type proxy
        // http://www.glype.com/  GLYPE Proxy
        // dit soort proxies hebben alle een FORM als volgt
        //  action = /includes/process.php?action=update
        //  text voor addrss veld  is  u
        //   <form method="post" action="/index.php">		
        //	 <input id="address_box" name="q" type="text" class="bar" onfocus="this.select()" value="" /></td>
        // bepaal domain (alleen maar de host)
        String sProxyActionUrl = sProxyAction;
        Error("ACTION RAW[" + sProxyAction + "] ");
        // sommig proxies //proxfree.nl/php etc
        if( sProxyActionUrl.startsWith("//")==true) sProxyActionUrl = "http:" + sProxyActionUrl; 
        if( sProxyActionUrl.startsWith("http://")==false) {
        	if( sProxyAction.startsWith("/")==false) sProxyActionUrl = "/" + sProxyAction;
        	sProxyActionUrl = sProxyHomeUrl + sProxyAction;
        }
        Error("ACTION PROCESSED[" + sProxyActionUrl + "] ");
        String sProxyInputParams = sProxyInput.trim() + "£" + sDestUrl.trim();
        return sendPost( FNaam, sProxyActionUrl ,  sProxyInputParams , '§' , '£');
        
	}
	//
	//
	//---------------------------------------------------------------------------------
	public boolean PerformFORM(String pUrl , String FNaam , String FormNaam , String sInputLijst , String FDestNaam)
	//---------------------------------------------------------------------------------
	{
		LogIt(1,"====> FORM : " + pUrl);
		
		// Extract FORM details : name, action, method en inputs
		webStraktorParseForm xForm = new webStraktorParseForm(FNaam,xMSet,this.iLogger);
		if( FormNaam == null ) FormNaam = "";
		if( FormNaam.length() == 0 ) xForm.getFirstFormName();
		boolean ibb = xForm.doesFormexist(FormNaam);
		if( ibb == false ) {
			Error("There is no matching form [" + FormNaam +"] on URL [" + pUrl + "]");
			return false;
		}
		// Kijk of de input lijst wel voorkomt op de form
		// input  a=b&c=d&etc
		int aantal = xMSet.xU.TelDelims(sInputLijst,'&') + 1;
        for(int i=0;i<aantal;i++)
        {
         String ss = xMSet.xU.GetVeld(sInputLijst,(i+1),'&');
         if( ss.indexOf("=") < 0 ) continue;
         String sEen  = xMSet.xU.GetVeld(ss,1,'=').trim();
         if( sEen.length() <= 0) continue;
         if( xForm.doesFieldExist( FormNaam , sEen) == false ) {
        	 Error("Field [" + sEen + "] does not feature as an inputfield on form [" + FormNaam + "]");
        	 return false;
         }
        }
		// maak nu query string door de input lisjt te mergen met alle textvelden hidden en niet hidden
        sInputLijst = xForm.MergeInputs( sInputLijst , FormNaam );
        /*
		// De input lijst uitbreiden met de hidden values
		String sHiddenValues = xForm.getHiddenValues(FormNaam);
		if( sHiddenValues == null ) sHiddenValues = "";
		if( sHiddenValues.length()>0) {   // eigenlijk moet je mergen want je kan de hiddens zelf meegegeven hebben
			String sOld = sInputLijst;
			sInputLijst = mergeHTMLParams( sInputLijst , sHiddenValues );
			LogIt(5,"Input list [" + sOld + "] and hidden values [" + sHiddenValues +"] have been merged to [" + sInputLijst +"]");
		}
		*/
		
		// Action
		String sURL=pUrl;
		if( this.ViaProxy ) {  // maak self de URL , ttz. protocol en de host (zonder de paden)
			String sHost = xMSet.xU.getHostNameFromURL(this.sProxyHost);
			sURL = "http://" + sHost;
			// Indien de action op de host geen 
			
		}
		String sURLAction = xForm.getExtendedAction(FormNaam,sURL);
		if( sURLAction == null ) {
		    	Error("There is no action defined on [" + FormNaam +"] on URL [" + pUrl + "]");
				return false;
		}
	    LogIt(5,"ACTION=" + sURLAction);
	    
		// Form needs to be processed by a POST
		if( xForm.isMethodPost(FormNaam) == true )
		{
			return sendPost( FDestNaam , sURLAction , sInputLijst , '&' , '=');
		}
		// GET
		if( xForm.isMethodGet(FormNaam) == false ) {
			Error("Neither POST nor GET on [" + pUrl + "] assuming GET");
			//return false;
		}
		String sGetUrl = sURLAction + "?" + sInputLijst;
		sGetUrl = xMSet.xU.Remplaceer(sGetUrl, "//?", "/?");
		
		// sommige sites hebben %3A voor : en %2F voor / staan
		if( sGetUrl.indexOf("%")>=0) {
			String sUO = sGetUrl;
			sGetUrl = xMSet.xU.RemplaceerNEW(sGetUrl,"%3A",":");
			sGetUrl = xMSet.xU.RemplaceerNEW(sGetUrl,"%3a",":");
			sGetUrl = xMSet.xU.RemplaceerNEW(sGetUrl,"%2F","/");
			sGetUrl = xMSet.xU.RemplaceerNEW(sGetUrl,"%2f","/");
			Error( "Changed [" + sUO + "] -> [" + sGetUrl + "]");
		}
		return FetchViaProxy( sGetUrl , FDestNaam , webStraktorSettings.fileMIMEType.TEXT );
	}
	//
	//
	//---------------------------------------------------------------------------------
	private boolean sendPost( String FNaam , String sUrlAction, String sInputLijst , char ampersand , char gelijkteken) 
	//---------------------------------------------------------------------------------
	{
		    // ampersand en gelijkteken is omdat de proxies soms ze als input voor de URL hebben, bvb.  http://www.worldcat.org/search?q=4554&qt=results_page hebben
		    // en dus om te vermijden dat dit 2 valuepairs worden http://www.worldcat.org/search?q=4554  en qt=results_page wordt
		    LogIt(6,"====> POST [" + sUrlAction + "] params [" + sInputLijst + "]");
		 	List <NameValuePair> nvps = new ArrayList <NameValuePair>();
	        HttpPost httpost = null;
	        HttpResponse response=null;
	       	        
	        // URL met de action
	        String sUrlSubmitted = xMSet.xU.CleanseURL(sUrlAction);
	        httpost = new HttpPost(sUrlSubmitted);
	       
	        // input  a=b&c=d&etc
	        String sLijst = sInputLijst + ampersand;
	        int aantal = xMSet.xU.TelDelims(sLijst,ampersand) + 1;
	        for(int i=0;i<aantal;i++)
	        {
	          String ss = xMSet.xU.GetVeld(sLijst,(i+1),ampersand);
	          if( ss == null ) continue;
	          if( ss.length() < 1) continue;
	          if( ss.indexOf(gelijkteken) < 0 ) continue;
	          // zet § naar & terug
	          String sEen  = xMSet.xU.GetVeld(ss,1,gelijkteken).trim();
	          String sTwee = xMSet.xU.GetVeld(ss,2,gelijkteken).trim();
	          if( sEen.length() <= 0) continue;
	          nvps.add(new BasicNameValuePair(sEen,sTwee));
	          //Error("adding NameValuePair [" + sEen + "] = [" + sTwee + "]");
	        }
	        //
	        LogIt(6,"POST Input Parameters " + nvps.toString() );
	        if( nvps.size() == 0 ) {
	        	Error("Bizar no POST input parameters. InputList = [" + sInputLijst + "]");
	        	return false;
	        }
	        //
	        //
	        int postResp=-1;
	        try {
	          httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
	          
	          //  ZELF de header construeren
	          //-----------------------------------------------------
	          httpost.setHeader("User-Agent"      , xMSet.getUserAgent());
	          //httpost.setHeader("Browser"         , sBrowser);
	          httpost.setHeader("Keep-Alive"      , sKeepAliveTijd);
	          httpost.setHeader("Connection"      , "keep-alive");
	          httpost.setHeader("Accept-Encoding" , "*");
	          httpost.setHeader("Accept"          , "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	          httpost.setHeader("Accept-Language" , "en-us,en;q=0.58");
	          httpost.setHeader("Accept-Charset"  , "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
	        
	          // Essentieel voor de post
	          httpost.setHeader("Content-Type", "application/x-www-form-urlencoded");  // EXPLICIET ZETTEN
	          String mijnkookie = construeerKookie(""+httpost.getURI(),false);
	          if( mijnkookie.length() > 0 ) {
	        	  httpost.setHeader("Cookie", mijnkookie);
	           }
	          //
	          LogIt(5 , "POST created URI- > " + httpost.getURI());
	          LogIt(5 , "POST - Expect continue [" + httpost.expectContinue() +"]");
	          //
	          // betere header repport leest alles uit
	          ToonHeader(httpost.getAllHeaders(),"SND");
	          //  EXEC
	          //
	          response = httpclient.execute(httpost);
	          //
		      // Cookie rapport
	          StoreKookies(); 
		      //
	          ToonHeader(response.getAllHeaders(),"RCV");
	          //
	          HttpEntity entity = response.getEntity();
	          postResp = response.getStatusLine().getStatusCode();
	          LogIt(6,"POST returned http error code -> (" + postResp +")");  // HTTP 200 of iets anders
	          if (entity != null) {  // je moet de entity om de een of andere manier consumen voor je verder kan
	     	      
	        	  if( (postResp != 200) && (postResp != 302)  ) {
	     		     LogIt(6,"POST did not return a redirect nor a 200. Result stored in [" + FNaam + "]");
	       	         DumpBytes( entity , FNaam , false);
		             EntityUtils.consume(entity);
		     	     return false;  // report as an error
		          }
	     	     if( postResp == 200 )
	             {
	        	     LogIt(6,"POST did return a 200. Result stored in [" + FNaam + "]");
	     	         boolean isOK = DumpBytes( entity , FNaam , false);  
	     	         EntityUtils.consume(entity);
	     	         return isOK;
	             }
	          
	             if( (postResp == 302) ) {
	            	 // 301 REDIRECT PERMANENT  
	            	 // 302 REDIRECT TEMPORARILY
	            	 String redirectLok="";
	                 Header[] headers = response.getAllHeaders();
	                 for (int i = 0; i < headers.length; i++) {
	                 	 //LogIt(6,"  REDIR " + headers[i].getName() + "   [" + headers[i].getValue() + "]");
	                     if( headers[i].getName().compareToIgnoreCase("LOCATION")==0) {
	                     	redirectLok = headers[i].getValue();
	                     }
	                 }
	                 EntityUtils.consume(entity);
	                 if( redirectLok.length() > 0)  {
	                 	LogIt(6,"Redirecting to [" + redirectLok + "]");
	                 	if( redirectLok.length() == 0 ) {
	                 		Error("Could not determine redirection location");
	                 		return false;
	                 	}
	                 	if( redirectLok.indexOf("http") != 0 ) {
	                 		String sHost = xMSet.xU.getHostNameFromURL(this.sProxyHost);
	                 		if( redirectLok.indexOf("/") != 0 ) redirectLok = "/" + redirectLok;
	            			redirectLok = "http://" + sHost + redirectLok;
	            			LogIt(6,"Redirection location has been transformed to [" + redirectLok + "]");
	                 	}
	                 	return FetchUrl( redirectLok, FNaam , webStraktorSettings.fileMIMEType.TEXT);
	                 }
	                 return false;
	             }
	             //
	          }
	          Error("POST - Entity null");
	        }
	        catch (Exception e ) {
	        	Error("Fout bij POST execute"+ e.getMessage() );
	        	Error(xMSet.xU.LogStackTrace(e));
	        }
	        return false;
	        
	        
	}
	//
	//
	//---------------------------------------------------------------------------------
	private boolean FetchUrl(String sUrl, String FNaam , webStraktorSettings.fileMIMEType iMimeTipe )
	//---------------------------------------------------------------------------------
	{
		LogIt(1,"====> FETCHING : " + sUrl);
		HttpGet httpget = null;
		String sUrlSubmitted = xMSet.xU.CleanseURL(sUrl);
		//
		try {
		      httpget = new HttpGet(sUrlSubmitted); 
	          LogIt(6,"Fetching URL [" + httpget.getURI() +"]");
		}
		 catch (Exception e ) {
	     	  Error("Error on new HttpGet " + sUrlSubmitted + " " + e.getMessage() );
	     	  Error(xMSet.xU.LogStackTrace(e));
	     	  return false;
	    }
		// 
	    try {
	      HttpResponse response=null;
	      
	    
	      //-----------------------------------------------------
          httpget.setHeader("User-Agent"      , xMSet.getUserAgent());
          //httpget.setHeader("Browser"         , sBrowser);
          httpget.setHeader("Keep-Alive"      , sKeepAliveTijd);
          httpget.setHeader("Connection"      , "keep-alive");
          //httpget.setHeader("Accept-Encoding" , "*");   // potentially resuts in a Chunked encodig and GZIP encoding
          httpget.setHeader("Accept"          , "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
          httpget.setHeader("Accept-Language" , "en-us,en;q=0.58");
          httpget.setHeader("Accept-Charset"  , "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
          // Hier zeker geen content-type zetten
          String mijnkookie = construeerKookie(""+httpget.getURI(),false);
          if( mijnkookie.length() > 0 ) {
        	  httpget.setHeader("Cookie", mijnkookie);
           }
          //
          // beter header rapport
          ToonHeader(httpget.getAllHeaders(),"SND");
          // Bewaar de laatste locatie
          //
          //-----------------------------------------------------
	      //
          // EXEC
          LogIt(6,"EXECUTE GET");
	      response = httpclient.execute(httpget);
	      //
	      // Cookie rapport
          StoreKookies(); 
	      //
	      //
          ToonHeader(response.getAllHeaders(),"RCV");
          //
          HttpEntity entity = response.getEntity();
	      int serverResp = response.getStatusLine().getStatusCode();
          LogIt(6,"Fetch form: " + serverResp);
          if (entity != null) 
          {
             //
        	 String contentEncoding = null;
             Header encoding = entity.getContentEncoding();
             if (encoding != null) {contentEncoding = encoding.getValue();}
             String contentMimeTipe = null;
 			 Header type = entity.getContentType();
             if (type != null) { contentMimeTipe = type.getValue();}
             LogIt(5,"Content Encoding [" + contentEncoding + "] Content MimeType [" + contentMimeTipe +"]");
        	 //
        	  
        	 if( iMimeTipe == webStraktorSettings.fileMIMEType.TEXT) {
        	     if( DumpBytes( entity , FNaam , false ) == false ) return false; 
        	 }
        	 else {
        		 if( iMimeTipe == webStraktorSettings.fileMIMEType.BLOB) { 	
        			
        			 // We need to set the suffix of the file based on the mime type
        			 String sSuffix = "jpg";
                     if( contentMimeTipe == null ) contentMimeTipe = "image/jpg";
                     if( contentMimeTipe.trim().toLowerCase().indexOf("jpg")>=0)  sSuffix = "jpg";
                     if( contentMimeTipe.trim().toLowerCase().indexOf("gif")>=0)  sSuffix = "gif";
                     if( contentMimeTipe.trim().toLowerCase().indexOf("jpeg")>=0) sSuffix = "jpeg";
                     if( contentMimeTipe.trim().toLowerCase().indexOf("png")>=0)  sSuffix = "png";
                     if( contentMimeTipe.trim().toLowerCase().indexOf("tiff")>=0) sSuffix = "tiff";
                     FNaam = FNaam + "." + sSuffix;
                     if( xMSet.xU.IsBestand(FNaam)==true) { xMSet.xU.VerwijderBestand(FNaam); }
                     //
        		     if( DumpBytes( entity , FNaam , true ) == false ) return false;  // dump in raw mode 
        		 }
        		 else {
        		  Error("Unknown filemimetype");
        		  return false;
        		 }
        	 }
        	 EntityUtils.consume(entity);
     	  }
          //
          if( serverResp != 200 ) {
      	    Error("URL [" + sUrlSubmitted + "] did not return a 200");
      	    return false;
          }
	    }
	    catch (Exception e ) {
     	  Error("Error fetching " + sUrlSubmitted + " " + e.getMessage() );
     	  Error(xMSet.xU.LogStackTrace(e));
     	  return false;
        }
        return true;
    }
	//
	//
	//---------------------------------------------------------------------------------
	void DumpText( HttpEntity entity  , String FNaam)
	//---------------------------------------------------------------------------------
	{
		   String sAntwoord = "";
		   try {
			   lastCharSet = EntityUtils.getContentCharSet(entity);
			   LogIt(6,"Content charset -> " + lastCharSet);
		       sAntwoord = EntityUtils.toString(entity);
		   }
		   catch (Exception e) {
		        Error("Error opening/writing [" + FNaam + "] (Byte)");
		        Error(xMSet.xU.LogStackTrace(e));
		        return;
		   }
		   //
		
		   FileWriter outFile=null;
		   PrintWriter outPrinter=null;
		   try {
		     outFile    = new FileWriter(FNaam);
		     outPrinter = new PrintWriter(outFile);
		     if( outPrinter != null ) {
	        	  outPrinter.print(sAntwoord);
	        	  outPrinter.close();
		     } 	  
		   }
		   catch (Exception e) {
			  Error("Error opening/writing [" + FNaam + "] (Byte)");
		      Error(xMSet.xU.LogStackTrace(e));
		   }
		   finally {
			   if( outPrinter != null ) outPrinter.close();
		   }
		   LogIt(6,"Dumped " + sAntwoord.length() + " bytes onto " + FNaam + "]");
		   return;
	}
	//
	//---------------------------------------------------------------------------------
	boolean DumpBytes( HttpEntity entity , String FNaam , boolean isRaw)
	//---------------------------------------------------------------------------------
	{
		  //Error("Dumping" + FNaam + isRaw);
		
		  lastSize=0L;
		  lastFileName = FNaam;
		  FileOutputStream tof=null;
		  try 
		  {
		     byte[] buffer = EntityUtils.toByteArray(entity);
//System.out.println("buffer ok");
		     lastCharSet = EntityUtils.getContentCharSet(entity);
//System.out.println(lastCharSet);

		     tof  = new FileOutputStream(FNaam);
		     long lengte = buffer.length;
		     if( lengte > MAXBUFSIZE) {
		    	 Error("MAXBUFSIZE [" + MAXBUFSIZE + "] has been reached");
		    	 return false;
		     }
		     byte b='\0';
		     for( long l=0L; l<lengte ; l=l+1L)
		     {
//System.out.print(""+l);
		    	 b=buffer[(int)l];
		    	 if ( isRaw == true ) {
		    		 tof.write(buffer[(int)l]); 
		    	 }
		    	 else {
		    	    int kk = (int)b & 0xff;
		    	    if( kk >= 0x80 ) {
		    	      tof.write('&');
		    	      tof.write('#');
		    	      tof.write('0');
		    	      tof.write('x');
		    	      tof.write( HEXES.charAt((b & 0xF0) >> 4) );
		    		  tof.write( HEXES.charAt((b & 0x0F)) );
		    		  tof.write(';');
		    	    }
		    	    else tof.write(buffer[(int)l]);
		    	 }
		    	 lastSize = lastSize + 1L;
		     }
//System.out.print("einde");   
		  }    
		  catch (Exception e) {
	        Error("Error opening/writing [" + FNaam + "] (Byte)" + e.getMessage());
	        Error(xMSet.xU.LogStackTrace(e));
	        return false;
	      }
		  finally {
			   if( tof != null ) try { tof.close(); } 
			   catch (Exception e) {
				   Error("Close writer " + FNaam + " " + e.getMessage()); 
				   return false; }
		  }
		  LogIt(5,"Dumped " + lastSize + " bytes onto " + FNaam + "] in Charset [" + lastCharSet + "]");
		  return true;
	}
	
	//
	//---------------------------------------------------------------------------------
	long getLastFileSize()
	//---------------------------------------------------------------------------------
	{
		return lastSize;
	}
	//
	//---------------------------------------------------------------------------------
	String getLastCharSet()
	//---------------------------------------------------------------------------------
	{
		return lastCharSet;
	}
	//
	//---------------------------------------------------------------------------------
	String getLastFileName()
	//---------------------------------------------------------------------------------
	{
		return lastFileName;
	}
	//
	//
	//---------------------------------------------------------------------------------
	boolean FetchThisURL(String sUrlIn, String FNaam , webStraktorSettings.fileMIMEType iMimeTipe)
	//---------------------------------------------------------------------------------
	{
		UrlRequestCounter++;
		//
		String sHost = null;
		String sPath = null;
		String sDestUrl = sUrlIn;
		if( ViaProxy ) {
			if( sDestUrl.indexOf("http") != 0 ) {
		     String sTmpHost = sProxyHost;
		     if( sTmpHost.indexOf("http") != 0 ) sTmpHost = "http://" + sTmpHost;
		     sTmpHost = xMSet.xU.getHostNameFromURL(sTmpHost);
		     sDestUrl = "http://" + sTmpHost + sDestUrl;
		   }
		}
		try
	    {
	         URL url = new URL(sDestUrl);
	         sHost = url.getHost();
	         sPath = url.getPath();
	    }catch(Exception e)
	    {
	    	 Error("Analyzing URL " + sDestUrl);
	         Error(xMSet.xU.LogStackTrace(e));
	         return false;
	    }
	    //
		if( (robotmode == true) && (UrlRequestCounter == 1) ) {
			      //  subsitute Path on the URL by  /robots.txt
				  String sUrlRobot = "http://" + sHost + "/robots.txt";
			      String FRobotNaam = xMSet.xU.GetSavePathName(FNaam) + xMSet.xU.ctSlash + sHost + "-robots";
			      FRobotNaam = (xMSet.xU.VervangKarakter(FRobotNaam,'.','-')).trim()+".txt";
			      LogIt(1, "robot.txt will be stored in [" + FRobotNaam + "]");
			      boolean isOK = FetchViaProxy(sUrlRobot,FRobotNaam , webStraktorSettings.fileMIMEType.TEXT );   // get /robots.txt
			      if( isOK == false ) return false;
			      // verwerk de file
			      isOK = ProcessRobotsTxt(FRobotNaam);
			      if( isOK == false ) return false;
			      // en nogmaals initialiseren van de proxy voor de echte bladzijde
			      this.ProxyHasBeenInitialzed = false;
		}
		if( (robotmode == true) && (sPath != null) ) {
			if( PathCanBeAccessed(sPath,sHost) == false ) {
				Error( "Host " + sHost + " does not allow to access [" + sDestUrl + "]");
				return false;
			}
		}
		if( UrlRequestCounter > 1) {
			LogIt(1,"Crawler will pauze for [" + this.crawlerDelay + "] seconds");
		    MyPauze(this.crawlerDelay);
		}
		return FetchViaProxy(sDestUrl,FNaam,iMimeTipe);
	}
	//
	//---------------------------------------------------------------------------------
	boolean ProcessRobotsTxt(String FNaam)
	//---------------------------------------------------------------------------------
	{
		ArrayList<String> lijnen = new ArrayList<String>();
		String sX = xMSet.xU.ReadContentFromFile(FNaam,50000);
	    if ( sX.length() < 1 ) {
	    	Error("Empty robots.txt [" + FNaam + "]");
	    	return false;
	    }
	    sX = sX + "\n\n";
	    int aantal = xMSet.xU.TelDelims(sX,'\n');
	    // nnn #  zzz    hou alles voor de #
	    // hou ook alles tussen User-agent: * en een andere User-agent: definitie
	    boolean found = false;
	    for(int i=0;i<=aantal;i++)
	    {
	      String sLijn = xMSet.xU.GetVeld(sX,(i+1),'\n')+ "#";
	      sLijn = xMSet.xU.GetVeld(sLijn,1,'#').trim();
	      if( sLijn.length() < 1 ) continue;
	      sLijn = xMSet.xU.RemplaceerNEW(sLijn,"\t"," ");
	      sLijn = xMSet.xU.RemplaceerNEW(sLijn," ","");
	      //LogIt(9," - robot -->" + sLijn);    
	      if( sLijn.compareToIgnoreCase("User-agent:*") == 0 ) {
	    	  found = true;
	    	  lijnen.add(sLijn);
	    	  continue;
	      }
	      if( (sLijn.toLowerCase().startsWith("user-agent:")==true) && (found==true) ) break;
	      if( found ) lijnen.add(sLijn);
	    }
	    if( found == false ) {
	    	lijnen = null;
	    	Error("User-agent: * not found in the [" + FNaam  + "]");
	    	return false;
	    }
	   for( int i=0;i<lijnen.size();i++) {
	       //Error( "---> " + lijnen.get(i));
		   if( lijnen.get(i).compareToIgnoreCase("Disallow:/") == 0) {
			   Error("Host does not allow  robots. Exiting");
			   if( xMSet.OverRuleRobotSpec() == false ) return false;
			   Error("Overruling");
		   }
		   String een  = xMSet.xU.GetVeld(lijnen.get(i),1,':');
		   String twee = xMSet.xU.GetVeld(lijnen.get(i),2,':');
		   if( (een.length() == 0) || (twee.length() == 0) ) continue;
		   if( een.compareToIgnoreCase("Sitemap") == 0 ) continue;  // ignore
		   if( een.compareToIgnoreCase("Crawl-delay") == 0 ) {
			   int idel = xMSet.xU.NaarInt(twee);
			   if( idel < 0 ) {
				   LogIt(6,"Not a valid number " + lijnen.get(i));
				   continue;
			   }
			   if( idel != 0 ) crawlerDelay = idel;
			   LogIt(1,"CRAWLER DELAY will be set to [" + crawlerDelay + "]");
			   continue;
		   }
	       //Error( "-----> " + lijnen.get(i) + " " + een + " " + twee);
		   if( (een.compareToIgnoreCase("Allow") == 0) || (een.compareToIgnoreCase("Disallow") == 0) ) {
			   RobotSpec x = new RobotSpec();
			   if( een.compareToIgnoreCase("Allow") == 0) x.access = tpALLOW; else x.access = tpDISALLOW;
			   if( twee.endsWith("/") == true )  x.tipe = tpFOLDER; else x.tipe = tpFILE;
			   x.value   = twee;
			   robotspecLst.add(x);
		   }
	   }
	   LogIt(5,"ROBOT SPECs");
	   for(int i=0;i<robotspecLst.size();i++) LogIt(5, "  - " + robotspecLst.get(i).sho() );
	   return true;
	}
	//
	//---------------------------------------------------------------------------------
	void MyPauze(int iPeriode)
	//---------------------------------------------------------------------------------
	{
	 	 try 
		 {
	       Thread.sleep(iPeriode*1000);        
	     }
		 catch (InterruptedException e)
	     {
	        Error(e.getMessage());
	     }
	}
	//
	//---------------------------------------------------------------------------------
	boolean PathCanBeAccessed(String sP, String sH)
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<robotspecLst.size();i++)
		{
		  String sPat = robotspecLst.get(i).value;
		  boolean isRegex = false;
		  // regex instructions - not supported yet
		  if( (sPat.indexOf("*") >= 0) || (sPat.indexOf("$") >= 0) || (sPat.indexOf("^") >= 0) ||
		      (sPat.indexOf("[") >= 0) || (sPat.indexOf("]") >= 0) || (sPat.indexOf("?") >= 0) ||
		      (sPat.indexOf("{") >= 0) || (sPat.indexOf("}") >= 0) || (sPat.indexOf(",") >= 0) ) isRegex = true;
		  if( isRegex )  continue;
		  //
		  if ((sP.startsWith( sPat) == true) && (robotspecLst.get(i).access == tpDISALLOW)) {
			  Error( "Host [" + sH + "] excludes [" + sPat +"] to be accessed bu robots");
			  if( xMSet.OverRuleRobotSpec() == false ) return false;
			  Error( "Overruling");
			  break;
		  }
		}
		LogIt(5,"PATH [" + sP + "] is allowed to be accessed by host " + sH);
		return true;
	}
	//
	//---------------------------------------------------------------------------------
	String mergeHTMLParams( String sEen , String sTwee )
	//---------------------------------------------------------------------------------
	{
	   String sDest = "";
	   if( sTwee == null ) return sEen;
	   if( sTwee.length() == 0) return sEen;
	   String sIn =  sEen + "&" + sTwee;
	   int aantal = xMSet.xU.TelDelims(sIn+"&",'&');
	   int teller = 0;
	   for(int i=0;i<=aantal;i++)
	   {
		   String s1  = xMSet.xU.GetVeld(sIn,i,'&');
		   if( s1 == null ) continue;
		   if( s1.length() == 0 ) continue;
		   String s11 = xMSet.xU.GetVeld(s1,1,'=');
	   //Error(">>>>>>" + s1 + " " + s11 + "---- Dest=" + sDest );
		   int len = xMSet.xU.TelDelims(sDest+"&",'&');
		   boolean found = false;
		   for(int j=0;j<=len;j++)
		   {
			  String s2  = xMSet.xU.GetVeld(sDest,j,'&');
			  if( s2 == null ) continue;
			  if( s2.length() == 0 ) continue;
			  String s21 = xMSet.xU.GetVeld(s2,1,'=');
		//Error(">>>>>>" + s1 + " " + s11 + " -- " + s2 + " " + s21 + " Dest=" + sDest);
			  if( s21.trim().compareTo(s11.trim()) == 0 ) { found = true; break; }
		   }
		   if( found == false ) {
			   if( teller == 0 ) sDest = s1; else sDest = sDest + "&" + s1;
			   teller++;
		   }
	   }
	   return sDest;	
	}
	//
	//---------------------------------------------------------------------------------
	void setRobotmode(boolean ib)
	//---------------------------------------------------------------------------------
	{
		this.robotmode = ib;
	}
}
