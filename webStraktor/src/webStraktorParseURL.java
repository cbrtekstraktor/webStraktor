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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedWriter;
import java.io.FileWriter;



public class webStraktorParseURL {

	
	
	webStraktorSettings xMSet = null;
	webStraktorParseController iController = null;
	webStraktorParseModel iParseSourceCodeFile=null;
	webStraktorPrintStream resWriter=null;
	webStraktorHTTPClient httpclient=null;
	webStraktorDateTime xDateTime=null;
	webStraktorLogger iLogger=null;
	
	
	private int indentatie=0;
	private boolean gotContent=false;
	private String lastCharset=null;
	private long starttijd=0L;
	private String lastErrorMsg="";
	private int URLLogWaterMark=-1;
	
	
	
	class URLLog {
		String URL;
		String FNaam;
		long   lStarttime;
		long   lStoptime;
		String sStarttime;       
		long   starttimenano;
		long   stoptimenano;
		long   nbytes;
		int    status;
		URLLog(String sU) 
		{
			URL = sU;
			FNaam="";
			if( xDateTime == null ) {sStarttime = ""; lStarttime=0L; }
			                   else {sStarttime = xDateTime.DateTimeNow(xMSet.DateFormat); lStarttime=System.currentTimeMillis();}
			starttimenano = System.nanoTime();
			stoptimenano  = System.nanoTime();
			nbytes        = 0L;
			status        = 0;
		}
		void closeOK(long iL)
		{
			stoptimenano = System.nanoTime();
			lStoptime    = System.currentTimeMillis();
			nbytes = iL;
			status = 0;
		}
		void closeNOK()
		{
			stoptimenano = System.nanoTime();
			status = -1;
		}
		void setFile(String FIn)
		{
			FNaam = FIn;
		}
	}
	public ArrayList<URLLog> lstURL = null;
	
	
	//
	//---------------------------------------------------------------------------------
	webStraktorParseURL(webStraktorSettings iM , webStraktorParseController iC , webStraktorLogger iL)
	//---------------------------------------------------------------------------------
	{
		starttijd=System.currentTimeMillis();
		xMSet = iM;
		iController = iC;
		iLogger = iL;
		xDateTime = new webStraktorDateTime(xMSet.TimeZone);
		lstURL    = new ArrayList<URLLog>();
	}
	//
	//---------------------------------------------------------------------------------
	String naarInternFormaat(String sIn)
	//---------------------------------------------------------------------------------
	{
		String sTemp="";
		char[] sChar = sIn.toCharArray();
		char cc=(char)'\0';
		byte b=(byte)0;
		for(int i=0;i<sChar.length;i++)
		{
			cc=sChar[i];
			//byte ib = (byte)((int)cc & 0xff);
			//if( ib > (byte)0x80 ) {
			if( cc > (char)0x80 ) {
				//this.Error("CHANGING " + cc + " " + ((int)cc&0xff) + " "  + sIn);
				b = (byte)((int)cc & 0xff);
				// byte to hex en dan naar UTF8
				String sUTF = xMSet.xU.Latin1HexToUtf8Hex(xMSet.xU.ByteToHex(b).trim()).trim();
				if( sUTF.length() != 4) {
					Error("converting latin to utf8)");
					sTemp = sTemp + cc;
					continue; // doe maar verder
				}
				sTemp = sTemp + "&#0x" + sUTF.substring(0,2) + ";" + "&#0x" + sUTF.substring(2,4) + ";";
				continue;
			}
			sTemp=sTemp+cc;
		}
		return sTemp;
	}
	//
	//---------------------------------------------------------------------------------
	void Exporteer(String sIn)
	//---------------------------------------------------------------------------------
	{
		if( resWriter == null) {
			String FNaam = iController.getResultFileName();
			resWriter = new webStraktorPrintStream(FNaam);
			// header
			resWriter.println(naarInternFormaat("<?xml version=\"1.0\" encoding=\"" + xMSet.getTargetCodePageString() + "\"?>"));
			resWriter.println(naarInternFormaat("<!-- CMDLINEPARAMS  [" + this.iController.getCmdLineParams() + "] -->"));
			resWriter.println(naarInternFormaat("<!-- URL            [" + this.iController.getMainUrl() + "] -->"));
			resWriter.println(naarInternFormaat("<!-- Charset        [" + this.lastCharset + "] -->"));
			resWriter.println(naarInternFormaat("<!-- Started at     [" + xDateTime.DateTimeNow(xMSet.DateFormat) + "] -->")); 
			resWriter.println("");
		}
		String sOut = sIn;
		for(int i=0;i<indentatie;i++) sOut = "  " + sOut;
		//
		String sPrev = sOut;
		sOut = naarInternFormaat(sOut);
		if( sOut.compareTo(sPrev) !=0 ) logit(9,"Format change [" + sPrev + "] - [" + sOut + "]");
		//
		resWriter.println(sOut);
		logit(9,"WRITER->"+sOut);
	}
	//
	//---------------------------------------------------------------------------------
	void CloseExporteer()
	//---------------------------------------------------------------------------------
	{
		resWriter.println("");
		resWriter.println(naarInternFormaat("<!-- HTTP CLIENT stats -->"));
		for(int i=0;i<this.lstURL.size();i++)
		{
		  String sL = lstURL.get(i).sStarttime + " (" +
		              (lstURL.get(i).stoptimenano - lstURL.get(i).starttimenano) / 1000000L + " msec) (" +
		              lstURL.get(i).nbytes + " bytes) [" + 
		              lstURL.get(i).URL + "]";
		  resWriter.println(naarInternFormaat("<!-- " + sL  + " -->")); 
		}
		resWriter.println("");
		long elapsed = (System.currentTimeMillis() - starttijd)/1000;
		resWriter.println(naarInternFormaat("<!-- Stopped at     [" + xDateTime.DateTimeNow(xMSet.DateFormat) + "] " + elapsed + " sec -->")); 
		if( resWriter != null ) resWriter.close();
	}
	//
	//---------------------------------------------------------------------------------
	void logit(int level , String sIn)
	//---------------------------------------------------------------------------------
	{
		if( iLogger != null ) iLogger.Logit(level, "PRS - "  + sIn);
	}
	//
	//---------------------------------------------------------------------------------
	void Error( String sIn)
	//---------------------------------------------------------------------------------
	{
		logit(0,sIn);
		lastErrorMsg = sIn;
	}
	// returns a file
	//---------------------------------------------------------------------------------
	String FetchAndStoreUrl(String sUrl , webStraktorSettings.fileMIMEType iTipe)
	//---------------------------------------------------------------------------------
	{
		URLLog  x = new URLLog(sUrl);
		
		String FNaam=null;
		if( iTipe == webStraktorSettings.fileMIMEType.TEXT ) FNaam=iController.getNewDumpFileName();
		if( iTipe == webStraktorSettings.fileMIMEType.BLOB ) FNaam=iController.getNewBLOBFileName();
		
		// If in Simulation mode ,just see whether the file is there and return
		if( this.iController.getSimulation() == true ) {
			if( xMSet.xU.IsBestand(FNaam) == true ) {
				x.setFile(FNaam);
				x.closeOK(0L);
				this.lstURL.add(x);
				return FNaam;
			}
			Error("ERROR - Running in SIMULATION mode and the file [" + FNaam + "] is not present");
			x.closeNOK();
			this.lstURL.add(x);
			return null;
		}
		//
		// Initialize is not already
		if( httpclient == null ) {
		 httpclient = new webStraktorHTTPClient(xMSet,iController.getProxyName(),iController.getUseProxy(),this.iLogger, xMSet.getCrawlerDelay() );
		 if( httpclient.isActive == false ) {
			Error("HttpClient module did not correctly initialize");
			return null;
		 }
		}
		//
		if( httpclient.FetchThisURL(sUrl,FNaam,iTipe) == false ) {
			Error("HttpClient fetch failed for [" + sUrl + "]");
			x.closeNOK();
			this.lstURL.add(x);
			return null;
		}
		x.closeOK(httpclient.getLastFileSize());
		x.setFile(FNaam);
		this.lstURL.add(x);
		this.lastCharset = httpclient.getLastCharSet();
		return FNaam;
	}
	// returns a filename
	//---------------------------------------------------------------------------------
	String PerformHttpClientForm(String sUrl, String FNaam , String FormName , String sInput )
	//---------------------------------------------------------------------------------
	{
		URLLog  x = new URLLog(sUrl);
		String FDestNaam = iController.getNewDumpFileName();
		
		// If in Simulation mode ,just see whether the file is there and return
		if( this.iController.getSimulation() == true ) {
			if( xMSet.xU.IsBestand(FDestNaam) == true ) {
				x.setFile(FDestNaam);
				x.closeOK(0L);
				this.lstURL.add(x);
				return FNaam;
			}
			Error("ERROR - Running in SIMULATION mode and the file [" + FDestNaam + "] is not present");
			x.closeNOK();
			this.lstURL.add(x);
			return null;
		}
		//
		if( httpclient.PerformFORM(sUrl,FNaam,FormName,sInput,FDestNaam) == false ) {
			Error("HttpClient PerformFORM failed for [" + sUrl + "]");
			x.closeNOK();
			this.lstURL.add(x);
			return null;
		}
		x.closeOK(httpclient.getLastFileSize());
		x.setFile(FNaam);
		this.lstURL.add(x);
		this.lastCharset = httpclient.getLastCharSet();
		return FDestNaam;
	}
	//
	//---------------------------------------------------------------------------------
	private String getTraceHTMLLogLine(int i)
	//---------------------------------------------------------------------------------
	{
		return  "" + 
		        String.format("%15d", (lstURL.get(i).starttimenano/1000L)) + "|" + 
		        String.format("%15d", (lstURL.get(i).stoptimenano/1000L)) + "|" + 
		        String.format("%10d", lstURL.get(i).nbytes) + "|" + 
		        lstURL.get(i).URL + "|" + lstURL.get(i).FNaam;
	}
	//
	//---------------------------------------------------------------------------------
	private void traceHTML(int iOID )
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<lstURL.size();i++)
		{
			if( i <= this.URLLogWaterMark ) continue;
			this.URLLogWaterMark++;	
			if (lstURL.get(i).status != 0) xMSet.xTrc.traceInstruction("H" , iOID , webStraktorTrace.TraceStatus.Error , getTraceHTMLLogLine(i));	
			                               xMSet.xTrc.traceInstruction("H" , iOID , webStraktorTrace.TraceStatus.Ok , getTraceHTMLLogLine(i));	
		}
	}
	//
	//---------------------------------------------------------------------------------
	private void traceInstructionHTML(parseInstruction iStruc)
	//---------------------------------------------------------------------------------
	{
		int UrlOID = iStruc.OID;
		traceHTML( UrlOID );
	}
	//
	//---------------------------------------------------------------------------------
	private void traceFunctionHTML(parseFunction iFunc)
	//---------------------------------------------------------------------------------
	{
		int UrlOID = xMSet.xTrc.getURLOID( iFunc.OID );
		traceHTML( UrlOID );
	}
	//
	//---------------------------------------------------------------------------------
	boolean processURL(String sMainUrl, parseInstruction xS)
	//---------------------------------------------------------------------------------
	{
		// open trace file en NIO
		xMSet.xTrc.openTrace();
		//
		// haal de MAIN
		parseFunction iFunc = iController.getForeach("main");
		if( iFunc == null ) {
			Error("Could not find main in parser program");
			xMSet.xTrc.closeTrace();
			return false;
		}
		xMSet.xTrc.traceFunction(iFunc.OID,webStraktorTrace.TraceStatus.Start,"");  // OOK HIRNA AANPASSEN
		if( xS == null )
		{
		  xMSet.xTrc.traceFunction(xMSet.xTrc.getURLOID( iFunc.OID ),webStraktorTrace.TraceStatus.Start,"");
		  // Fetch the URL defined in main
		  String sPagina = FetchAndStoreUrl(sMainUrl, webStraktorSettings.fileMIMEType.TEXT );
		  if( sPagina == null ) {
			String sMsg ="Could not fetch [" + sMainUrl + "]";
			xMSet.xTrc.traceFunction(xMSet.xTrc.getURLOID( iFunc.OID ),webStraktorTrace.TraceStatus.Error,"");
			xMSet.xTrc.traceFunction(iFunc.OID,webStraktorTrace.TraceStatus.Error,sMsg);
			Error(sMsg);
			xMSet.xTrc.closeTrace();
			return false;
		  }
		  traceFunctionHTML(iFunc);
		  xMSet.xTrc.traceFunction(xMSet.xTrc.getURLOID( iFunc.OID ),webStraktorTrace.TraceStatus.Stop,"");
		}
		else {  // een HTMLFORM
			xMSet.xTrc.traceInstruction(xS.OID,webStraktorTrace.TraceStatus.Start,"");
			String FormNaam = xS.Form.FormName.trim();
			String sInput = "";
			for(int i=0;i<xS.Form.lstInput.size();i++)
			{
				if( i==0 ) sInput = xS.Form.lstInput.get(i).trim(); else sInput = sInput + "&" + xS.Form.lstInput.get(i).trim();
			}
			sInput = this.iController.substituteParamsOnString(sInput);
			logit(5,"FORM=" + FormNaam + " INPUT=" + sInput);
		    // process the HTMLForm - deel 1 fetch de page
			String sPagina = FetchAndStoreUrl(sMainUrl , webStraktorSettings.fileMIMEType.TEXT);
			traceInstructionHTML(xS);
			xMSet.xTrc.traceInstruction(xS.OID,webStraktorTrace.TraceStatus.Stop,"");
			if( sPagina == null ) {
				String sMsg = "Could not fetch [" + sMainUrl + "]";
				xMSet.xTrc.traceInstruction(xS.OID,webStraktorTrace.TraceStatus.Error,sMsg);
				xMSet.xTrc.traceFunction(iFunc.OID,webStraktorTrace.TraceStatus.Error,sMsg);
				Error(sMsg);
				xMSet.xTrc.closeTrace();
				return false;
			 }
			 // deel 2 - analyseer de page, haal de form params eruit en exec de form
			 sPagina = PerformHttpClientForm(sMainUrl , sPagina , FormNaam , sInput);
			 traceInstructionHTML(xS);
			 if( sPagina == null ) {
				    String sMsg = "Could not perform HTML form [" + sMainUrl + "]";
				    xMSet.xTrc.traceInstruction(xS.OID,webStraktorTrace.TraceStatus.Error,sMsg);
				    xMSet.xTrc.traceFunction(iFunc.OID,webStraktorTrace.TraceStatus.Error,sMsg);
					Error(sMsg);
					xMSet.xTrc.closeTrace();
					return false;
			 }
			 xMSet.xTrc.traceInstruction(xS.OID,webStraktorTrace.TraceStatus.Stop,"FORM fetched");
		}
		//
		// Process the instructions of main
		if( xS != null ) xMSet.xTrc.traceInstruction(xS.OID,webStraktorTrace.TraceStatus.Start,"");
		String sNorma = iController.getNormalizedName(iFunc.Label).trim();  // you can set a label on main
		Exporteer( "<" + sNorma + ">" );
		boolean isOK = do_foreach( iController.getCurrentDumpFileName() , iFunc );
		Exporteer( "</" + sNorma + ">" );
		//
		if( isOK == false ) Exporteer("<!-- ERROR : parseURL has errors -->");
		//
		// content ?
		if( gotContent == false ) Exporteer("<!-- WARNING : No content found -->");
		//
		// sluit de output
		CloseExporteer();
		
		// shutdown the httpclient
		if( httpclient != null ) httpclient.Afsluiten();
		//
		// no content is not an error if there are no VAR or LINKs defined;
		if( gotContent == false  ) {
			if( this.iController.gotVarsDefined() == false ) {
				logit(5,"There are no vars defined and there is no content. so overruling the return state to TRUE");
				gotContent = true;
			}
		}
		if( (isOK == true) && (gotContent == false)) isOK = false;
		if( xS != null ) xMSet.xTrc.traceInstruction(xS.OID,webStraktorTrace.TraceStatus.Stop,"");
		//closeTrace();
		return isOK;
	}
	
	//
	//---------------------------------------------------------------------------------
	void VerwijderBestand(String FNaam)
	//---------------------------------------------------------------------------------
	{
		if( this.iController.getLogLevel() < 9 ) xMSet.xU.VerwijderBestand(FNaam);
		else logit(9,"KEPT TEMP FILE [" + FNaam + "]");
	}
	//
	//---------------------------------------------------------------------------------
	boolean do_foreach( String sURLin , parseFunction iFunc )
	//---------------------------------------------------------------------------------
	{
		String FBestand = sURLin;
		indentatie++;
		//
		logit(5,"FOREACH=" + iFunc.Name + " " + iFunc.Label + " file[" + FBestand + "]" );
		//
		xMSet.xTrc.traceFunction(iFunc.OID,webStraktorTrace.TraceStatus.Start,"");
		// if a pass phrase has been specified then look for it
		if( iFunc.passPhrase != null ) {
			xMSet.xTrc.traceInstruction(xMSet.xTrc.getPassPhraseOID(iFunc.OID),webStraktorTrace.TraceStatus.Start,"");
			if( iFunc.passPhrase.length() > 0) {
				if( this.doesFileComprise(FBestand,iFunc.passPhrase) == false ) {
					String sMsg = "Could not find pass phrase [" + iFunc.passPhrase + "] in file [" + FBestand + "]";
					xMSet.xTrc.traceInstruction(xMSet.xTrc.getPassPhraseOID(iFunc.OID),webStraktorTrace.TraceStatus.Error,iFunc.passPhrase);
					xMSet.xTrc.traceFunction(iFunc.OID,webStraktorTrace.TraceStatus.Error,sMsg);
					Error(sMsg);
					return false;
				}
				else {
					xMSet.xTrc.traceInstruction(xMSet.xTrc.getPassPhraseOID(iFunc.OID),webStraktorTrace.TraceStatus.Stop,iFunc.passPhrase);
					logit(5,"Found PASSPHRASE [" + iFunc.passPhrase + "] in file [" + FBestand + "]");
				}
			}
		}
		//
		// CUT out the specified part
		for(int j=0;j<iFunc.lstInstructies.size();j++)
		{
		    if( iFunc.lstInstructies.get(j).Tipe != parseInstruction.isCUT ) continue;
		    xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Start,"");
		    logit(5,"CUTTING [" + FBestand + "] between ["+iFunc.lstInstructies.get(j).startPattern + "] and [" + iFunc.lstInstructies.get(j).endPattern +"]" );
		    ArrayList<String> lstX = new ArrayList<String>();
		    boolean isOk=false;
		    if( iFunc.lstInstructies.get(j).xpath == null ) {
		    	// reuse dissect and use the first one
		    	isOk = dissect_file ( FBestand , iFunc.lstInstructies.get(j).Name , iFunc.lstInstructies.get(j).startPattern , iFunc.lstInstructies.get(j).endPattern , lstX );
		    }
		    else  {  // cut via xpath
		    	isOk = iController.xHTML.parseQuery(iFunc.lstInstructies.get(j).xpath);
		    	if (isOk==true) isOk = iController.xHTML.parseHTML(FBestand,lstX);
		    }
	        // neem nu gewoon de eerste file
		    if( (lstX.size() > 0) && (isOk) ) {
	    		String FN = lstX.get(0);
	    		if( xMSet.xU.IsBestand(FN)==true) {
	    			logit(5,"Replacing [" + FBestand + "] name by cut-up file name [" + FN + "]");
	    			FBestand = FN;  // Replace the original file with cut-up file
	    			xMSet.xTrc.traceInstructionFile(iFunc.lstInstructies.get(j).OID,FN);
	    		}
	    		else isOk = false; 
	    	}
		    if( isOk == false ) {
		    	String sMsg = "CUT operation failed";
		    	Error(sMsg);
		    	xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Error,sMsg);
		    	// return false  // OK ignore and continue
		    }
		    xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Stop,"");
		}  // CUT  
		//
		// Cut up the file in chunks as defined by the FOREACH 
		ArrayList<String> lstChunk = new ArrayList<String>();
		boolean isOK = true;
		if( iFunc.xpath == null ) {
		  isOK = dissect_file ( FBestand , iFunc.Name , iFunc.startPattern , iFunc.endPattern , lstChunk );
		}
		else {
		  isOK = iController.xHTML.parseQuery(iFunc.xpath);
		  if( isOK == true) isOK = iController.xHTML.parseHTML(FBestand,lstChunk);
		}
		//
		// loop through the chunks and perform the instructions
		for(int zz=0;zz<lstChunk.size(); zz++)
		{
			// SKIP logic
			if( iFunc.lstSkip.size()>0) {
				boolean skip=false;
				for(int bb=0;bb<iFunc.lstSkip.size();bb++)
				{
					if( iFunc.lstSkip.get(bb) == (zz+1) ) {
						logit(6,"SKIPPING [" + (zz+1) + "]");
						skip=true;
						break;
					}
				}
				if( skip ) continue;  // next chunk
			}
			// Occurs logic (invers skip)
			if( iFunc.lstOccurs.size()>0) {
				boolean skip=true;
				for(int bb=0;bb<iFunc.lstOccurs.size();bb++)
				{
					if( iFunc.lstOccurs.get(bb) == (zz+1) ) {
						logit(6,"Found OCCURS match [" + (zz+1) + "]" );
						skip=false;
						break;
					}
				}
				if( skip ) continue;  // next chunk
			}
			// MAXITERATIONS logic
			if( (zz >= iFunc.MaxIterations) && (iFunc.MaxIterations>0) ) {
			   logit(6,"Max Iterations reached [" + zz + "]" );
			   break;	
			}
			//
			String sChunkLabel = iFunc.Name;
			if( iFunc.Label != null ) {
				if (iFunc.Label.length()>1) sChunkLabel = iFunc.Label;
			}
			Exporteer( "<" + sChunkLabel + ">" );
			//			
			for(int j=0;j<iFunc.lstInstructies.size();j++)
			{
				// CUT is niet per iteratie
			    if( iFunc.lstInstructies.get(j).Tipe == parseInstruction.isCUT ) continue;
			    //
			    // indien FOREACH do_foreach
				if( iFunc.lstInstructies.get(j).Tipe == parseInstruction.isFOREACH ) {
					xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Start,"");
					xMSet.xTrc.traceInstructionFile( iFunc.lstInstructies.get(j).OID , lstChunk.get(zz) );
				    parseFunction nxtFunc = iController.getForeach( iFunc.lstInstructies.get(j).Name );
					if( nxtFunc == null ) {
						String sMsg = "Could not find FOREACH referenced by [" + iFunc.lstInstructies.get(j).Name  + "]";
						Error(sMsg);
						isOK=false;
						xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Error,sMsg);
					    continue;
					}
					do_foreach( lstChunk.get(zz) , nxtFunc );
					xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Stop,"");
				}
				// VAR en BLOB
                if( (iFunc.lstInstructies.get(j).Tipe == parseInstruction.isVARIABLE) || (iFunc.lstInstructies.get(j).Tipe == parseInstruction.isBLOB) ) {
                	xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Start,"");
                	xMSet.xTrc.traceInstructionFile( iFunc.lstInstructies.get(j).OID , lstChunk.get(zz) );
				    String sVar = do_variable( lstChunk.get(zz) , iFunc.lstInstructies.get(j) );
					String sVarBlob = "VARIABLE";
					if( iFunc.lstInstructies.get(j).Tipe == parseInstruction.isBLOB )sVarBlob = "BLOB";
					logit(5,sVarBlob + " [" + iFunc.lstInstructies.get(j).Name + "] = [" + sVar + "]");
					String sLabel = "";
					if( iFunc.lstInstructies.get(j).ignore == false ) {
						indentatie++;
						sVar = xMSet.xU.naarSaveXML(sVar);  // < > etc naar &lt; &gt; etc
						sLabel = iFunc.lstInstructies.get(j).Label;
						if( sLabel == null ) sLabel = iFunc.lstInstructies.get(j).Name;
						if( sLabel.length() <=0 ) sLabel = iFunc.lstInstructies.get(j).Name;
						Exporteer( "<" +  sLabel + ">" + sVar + "</" +  sLabel + ">");
						indentatie--;
						xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Stop,sVar);
					}
					//  Fetch the BLOB
					if( iFunc.lstInstructies.get(j).Tipe == parseInstruction.isBLOB ) {
						String sFullLink = iController.constructFullUrlFromPart(sVar.trim());
						logit(5,"Fetching image -> [" + sFullLink + "]");
						String sBestand = FetchAndStoreUrl(sFullLink , webStraktorSettings.fileMIMEType.BLOB);
						traceInstructionHTML(iFunc.lstInstructies.get(j));
						if( sBestand == null ) {
							xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Error,"");
						    Error("Could not fetch [" + sFullLink + "]");
							isOK=false;
							continue;
						}
						// XML uitbreiden met de filenaam
					    if( iFunc.lstInstructies.get(j).ignore == false ) {
					     indentatie++;
					     sLabel = sLabel + "FileName";
					     String BlobNaam = xMSet.xU.GetFileName(httpclient.getLastFileName().trim());
					     Exporteer( "<" +  sLabel + ">" + BlobNaam + "</" +  sLabel + ">");
					     indentatie--;
					     xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Stop,BlobNaam);
						}
		             }
				}
                // LINK
                if( iFunc.lstInstructies.get(j).Tipe == parseInstruction.isLINK ) {
                	xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Start,"");
                	xMSet.xTrc.traceInstructionFile( iFunc.lstInstructies.get(j).OID , lstChunk.get(zz) );
				    String sLink = do_variable( lstChunk.get(zz) , iFunc.lstInstructies.get(j) );
					String sFullLink = iController.constructFullUrlFromPart(sLink);
					logit(5,"Following link [" + sLink + "] -> [" + sFullLink + "]");
					//
					// Fetch the URL defined in the link
					String sBestand = FetchAndStoreUrl(sFullLink , webStraktorSettings.fileMIMEType.TEXT);
		            //logit(5,"LINK->"+sBestand);
					traceInstructionHTML(iFunc.lstInstructies.get(j));
					if( sBestand == null ) {
						String sMsg = "Could not fetch [" + sFullLink + "]";
						xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Error,sMsg);
						Error(sMsg);
						isOK=false;
					    continue;
					}
					//
					parseFunction nxtFunc = iController.getForeach( iFunc.lstInstructies.get(j).referencedForEach );
					if( nxtFunc == null ) {
						String sMsg = "Could not find FOREACH referenced by [" + iFunc.lstInstructies.get(j).referencedForEach  + "]";
						xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Error,sMsg);
						Error(sMsg);
						isOK=false;
						continue;
					}
					isOK=do_foreach( sBestand , nxtFunc );
					xMSet.xTrc.traceInstruction(iFunc.lstInstructies.get(j).OID,webStraktorTrace.TraceStatus.Stop,"");
				}
                
        	}
			//
			VerwijderBestand(lstChunk.get(zz) );
			//
			Exporteer( "</" + sChunkLabel + ">" );
		}
		//
		if( !isOK ) xMSet.xTrc.traceFunction(iFunc.OID,webStraktorTrace.TraceStatus.Error,lastErrorMsg);
		       else xMSet.xTrc.traceFunction(iFunc.OID,webStraktorTrace.TraceStatus.Stop,"");
		//
		// je hoeft de foreach niet te verwijderen want het is een chunk van de bovenliggende
		indentatie--;
		return isOK;
	}
	//
	//---------------------------------------------------------------------------------
	boolean dissect_file( String sURL , String iName , String istartPattern , String iendPattern , ArrayList<String> lstChunk)
	//---------------------------------------------------------------------------------
	{
		// lees bestand en knip de stukken eruit en zet die op een stack
		if( xMSet.xU.IsBestand(sURL) == false ) {
			Error("File [" + sURL + "] not found");
			return false;
		}
		logit(5,"Dissecting Name=[" + iName +"] File=[" + sURL + "] Start=["+istartPattern + "] End=["+ iendPattern + "]");
	    //	
		BufferedWriter writer = null;
		int teller=0;
		int nIter=0;
		try {
			  File inFile  = new File(sURL);  // File to read from.
	       	  BufferedReader reader = new BufferedReader(new FileReader(inFile));
	       	  
	       	  String sLijn = null;
	       	  int iStart=0;
	       	  Boolean iFound = false;
	       	  if( istartPattern == null ) iFound = true;
	          while ((sLijn=reader.readLine()) != null) {
	        	teller++;
	        	      	
	        	if( istartPattern != null ) {
	        	 if( (iStart=sLijn.indexOf(istartPattern)) >=0 ) {
	        		iFound = true;
	        		sLijn = sLijn.substring(iStart +istartPattern.length());
	        	 }
	        	}
	        	if( iFound == false ) continue;
	        	if( iendPattern != null ) {
		        	 if( (iStart=sLijn.indexOf(iendPattern)) >= 0) {
		        		sLijn = sLijn.substring(0,iStart);
		        		iFound = false;
		        	 }
		        }
	        	// uitsturen
	        	if( writer == null ) {
	        		// nieuwe chunk
	        		nIter++;
	        		String XBestandNaam = iController.getTempFileName();
	        		lstChunk.add(XBestandNaam);
	        		//
	        		writer = new BufferedWriter(new FileWriter(XBestandNaam));
	        		writer.write( "<!--" + xMSet.xU.EOL );
	        		writer.write( "FOREACH      = " + xMSet.xU.StripHTML(iName) + xMSet.xU.EOL );
	        		writer.write( "startPattern = " + xMSet.xU.StripHTML(istartPattern) + xMSet.xU.EOL);
	        		writer.write( "endPattern   = " + xMSet.xU.StripHTML(iendPattern) + xMSet.xU.EOL);
	        		writer.write( "=============================================================" + xMSet.xU.EOL);
	        		writer.write( "-->" + xMSet.xU.EOL );
	        	}
	        	writer.write(sLijn + xMSet.xU.EOL );
	        	if( iFound == false ) {
	        		writer.close();
	        		writer = null;
	        	}
	          }
	          reader.close();
	          if( writer != null ) writer.close();
	        }
			catch (Exception e) {
				Error("Dissecting file [" + sURL +"] " + e.getMessage());
				Error(xMSet.xU.LogStackTrace(e));
		    }
			
	    if( nIter == 0) {
	    	Error("Could not find a match between [" + istartPattern +"] and [" + iendPattern + "] in [" + sURL + "]");
	    	return false;
	    }
		return true;
	}
	
	//
	//---------------------------------------------------------------------------------
	String do_variable( String FBestand , parseInstruction iInstruction)
	//---------------------------------------------------------------------------------
	{
		//logit( "Variable [" + iInstruction.Name + "] on file [" + FBestand + "]" );
		String sVar = null;
		
		// is dit GetInputParam
		boolean isGetInputParam = false;
		if( iInstruction.lstCommands.size() == 1 ) {
			if( iInstruction.lstCommands.get(0).CommandType == parseCommand.cmdGETINPUTPARAMETER ) {
				isGetInputParam = true;
				sVar = "DUMMY";
			}
		}
		if( isGetInputParam == false )
		{
		 // if REGEX
		 if (iInstruction.regex != null ) sVar = RegexVarContent( FBestand , iInstruction.regex );
		 else {
			if (iInstruction.xpath != null ) sVar = xPathFetchVarContent( FBestand, iInstruction.xpath );
			                            else sVar = FetchVarContent( FBestand, iInstruction.startPattern , iInstruction.endPattern );
		 }
		 if( sVar == null ) {
			Error("Variable [" + iInstruction.Name + "] NOT FOUND on file [" + FBestand + "]");
			return null;
		 }
		}
		//
		sVar = do_commands( sVar , iInstruction.lstCommands );
		if( sVar == null ) {
			Error("Variable [" + iInstruction.Name + "] NOT FOUND on file [" + FBestand + "]");
			return null;
		}
		if( sVar.length() > 0 ) gotContent = true;
		return sVar;
	}
	//
	//---------------------------------------------------------------------------------
	String xPathFetchVarContent(String FBestand , String xpath)
	//---------------------------------------------------------------------------------
	{
		String sRet = null;
		if( iController.xHTML.parseQuery(xpath) == false )return null;
		ArrayList<String> filelst = new ArrayList<String>();
		if( iController.xHTML.parseHTML(FBestand, filelst) == false )return null;
		if( filelst.size() == 0) return null;
		sRet = xMSet.xU.ReadContentFromFile(filelst.get(0), 10000);
		return sRet;
	}
	//
	//---------------------------------------------------------------------------------
	String RegexVarContent(String FBestand , String sRegex)
	//---------------------------------------------------------------------------------
	{
		Pattern myPattern = Pattern.compile(sRegex);
		String sRet = null;
		try {
			  File inFile  = new File(FBestand);  // File to read from.
	       	  BufferedReader reader = new BufferedReader(new FileReader(inFile));
	       	  
	       	  String sLijn = null;
	       	  while ((sLijn=reader.readLine()) != null) {
	        	  Matcher iMatcher = myPattern.matcher(sLijn);
	        	  while (iMatcher.find()) {
	        		logit( 5,"REGEX match [" + iMatcher.group() + "] from " + iMatcher.start() + " to " + iMatcher.end());
	        		sRet =  iMatcher.group();
	        		break;
	        	  }
	          }
	          reader.close();
	        }
			catch (Exception e) {
				Error ("RegeVxarContent [" + FBestand + "] [" + sRegex + "] " + e.getMessage());
				return null;
		    }
		return sRet;
	}
	//
	//---------------------------------------------------------------------------------
	String FetchVarContent(String FBestand , String startPattern , String endPattern)
	//---------------------------------------------------------------------------------
	{
		String sRet = "";
		try {
			  File inFile  = new File(FBestand);  // File to read from.
	       	  BufferedReader reader = new BufferedReader(new FileReader(inFile));
	       	  
	       	  String sLijn = null;
	       	  int iStart=0;
	       	  Boolean iFound = false;
	       	  if( startPattern == null ) return null;
	          while ((sLijn=reader.readLine()) != null) {
	    
	        	if( startPattern != null ) {
	        	 if( (iStart=sLijn.indexOf(startPattern)) >=0 ) {
	        		iFound = true;
	        		sLijn = sLijn.substring(iStart + startPattern.length());
	        	 }
	        	}
	      
	        	if( iFound == false ) continue;
	        	if( endPattern != null ) {
		        	 if( (iStart=sLijn.indexOf( endPattern)) >= 0) {
		        		sLijn = sLijn.substring(0,iStart);
		        		iFound = false;
		        		sRet = sRet + sLijn;
			        	reader.close();
			        	return sRet;
		        	 }
		        	
		        }
	        	sRet = sRet + sLijn;
	          }
	          reader.close();
	        }
			catch (Exception e) {
				Error ("FetchVarContent [" + FBestand + "] [" + startPattern + "] [" + endPattern + "] " + e.getMessage());
				return null;
		    }
			
		return null;
	}
	
	//
	//---------------------------------------------------------------------------------
	boolean doesFileComprise(String FBestand , String passPhrase)
	//---------------------------------------------------------------------------------
	{
		Boolean iFound = false;
		try {
			  File inFile  = new File(FBestand);  // File to read from.
	       	  BufferedReader reader = new BufferedReader(new FileReader(inFile));
	       	  String sLijn = null;
	       	  while ((sLijn=reader.readLine()) != null) {
	        	if( sLijn.indexOf(passPhrase)>=0) {
	        		iFound = true;
	        		break;
	        	}
	          }
	          reader.close();
	        }
			catch (Exception e) {
				Error("doesFileComprise " + e.getMessage());
				return iFound;
		    }
		return iFound;
	}
	
	//  UPPER - LOWER - TRIM - SUBSTR - GET <sss> - GET sss - GETFIELDDELIM - REMOVE sss
	//
	//---------------------------------------------------------------------------------
	String do_commands(String sIn, ArrayList<parseCommand> CmdLst)
	//---------------------------------------------------------------------------------
	{
		String sRet = sIn;
		if( sRet == null ) return null;
		for(int i=0;i<CmdLst.size();i++)
		{
			parseCommand Cmd = CmdLst.get(i);    
			if( Cmd.CommandType == parseCommand.cmdTRIM  )  { sRet=xMSet.xU.VervangKarakter(sRet,'\t',' '); sRet = sRet.trim(); }
			if( Cmd.CommandType == parseCommand.cmdUPPER )  { sRet = sRet.toUpperCase();  }
			if( Cmd.CommandType == parseCommand.cmdLOWER )  { sRet = sRet.toLowerCase();  }
			if( Cmd.CommandType == parseCommand.cmdTITLECASE ){ sRet = xMSet.xU.toTitleCaseSimple(sRet);  }
			if( Cmd.CommandType == parseCommand.cmdGET )    { sRet = GetValueFromTag( sRet , Cmd.params ); }
			if( Cmd.CommandType == parseCommand.cmdREMOVE ) { sRet = xMSet.xU.RemplaceerIgnoreCase(sRet,Cmd.params,""); }
			if( Cmd.CommandType == parseCommand.cmdGETFIELD ) { sRet = getField(sRet,Cmd.params); }
			if( Cmd.CommandType == parseCommand.cmdGETHTMLCONTENT ) { sRet = getHTMLContent(sRet); }
			if( Cmd.CommandType == parseCommand.cmdGETINPUTPARAMETER ) { sRet = getInputParameter(Cmd.params); }
			if( Cmd.CommandType == parseCommand.cmdKEEPNUMBER ) { if( sRet==null ) sRet=""; else sRet = xMSet.xU.keepDigits(sRet).trim(); }
			if( Cmd.CommandType == parseCommand.cmdKEEPFLOAT ) { if( sRet==null ) sRet=""; else sRet = xMSet.xU.keepDecimals(sRet).trim(); }
		}
		return sRet;
	}
	//
	//---------------------------------------------------------------------------------
    String GetValueFromTag( String sIn , String sTag)
    //---------------------------------------------------------------------------------
    {
    	String sRet = sIn;

    	//
    	if ( sTag == null ) return sRet;
    	// 
    	if( sIn.indexOf(sTag) < 0 ) return sRet;
    	// <tag> </tag>
    	if( sIn.indexOf( "<"+sTag+">") >= 0) {
    		int istart = sIn.indexOf( "<"+sTag+">") + sTag.length() + 2;
    		int istop  = sIn.indexOf( "</"+sTag+">");
    		if( (istart <  istop) && (istop>=0) ) {
    			try {
    			 sRet = sIn.substring(istart,istop);
    			}
    			catch( Exception e) { sRet=sIn; };
    		}
    		return sRet;
    	}
    	
        //  href=""   -> lees alles dat na = komt en tussen quotes
    	int istart=-1;
    	if( (istart=sIn.indexOf(" " + sTag + "=")) >= 0 ) {
    		 String sTemp = sIn.substring(istart);   //  je hebt nu  tag="iets" , knip dus 2de value met delim "
    		 sRet = xMSet.xU.GetVeld(sTemp,2,'"');  
    	}
    	return sRet;
    }
    //
	//---------------------------------------------------------------------------------
    String getHTMLContent( String sIn )
    //---------------------------------------------------------------------------------
    {
      if( sIn == null ) return null;
      String sRet = "";
      char[] buf = sIn.toCharArray();
      boolean inBetweenTag=false;
      for(int i=0;i<buf.length;i++)
      {
    	  if( buf[i] == '<' ) { inBetweenTag = true; continue; }
    	  if( buf[i] == '>' ) { inBetweenTag = false; continue; }
     	  if( inBetweenTag ) continue;
     	  sRet = sRet + buf[i];
      }
      return sRet;
    }
    //
	//---------------------------------------------------------------------------------
    String getField( String sIn , String sParams)
    //---------------------------------------------------------------------------------
    {
      if( sIn == null ) return null;
      
      int idx = xMSet.xU.NaarInt(xMSet.xU.GetVeld(sParams,1,',').trim());
      if( idx <= 0) {
    	  Error("GetField - First field must be a number (" + sParams + ")");
    	  return sIn;
      }
      String ss = xMSet.xU.GetVeld(sParams,2,',').trim();
      if( ss.length() != 1) {
    	  Error("GetField - 2nd field must be a single character (" + sParams + ")");
    	  return sIn;
      }
      char firstLetter = ss.charAt(0);
      //  een komma wordt gesubsitueerd door ù; een ; wordt doo ç gesubsitueerd
      if( firstLetter == 'ù') firstLetter = ',';
      if( firstLetter == 'ç') firstLetter = ';';
      // Freak case : de delimiter voor de get is een van de interne formaten &#0xnn;
      if( (sIn.indexOf("&#0x") >= 0) && ((firstLetter==';')||(firstLetter=='&')||(firstLetter=='#')) ) {
          // vervang de firsLetter door een dummy indien dit niet een inter formaat is
    	  String sOld = sIn;
    	  sIn = xMSet.xU.VervangBuitenInternFormaat(sIn,firstLetter,'§');
    	  firstLetter = '§';
    	  Error("Freak case [" + sOld + "] -> [" + sIn + "]");
      }
      return xMSet.xU.GetVeld(sIn,idx,firstLetter);
    }
    //
	//---------------------------------------------------------------------------------
    String getInputParameter(String sParams)
    //---------------------------------------------------------------------------------
    {
      if( sParams == null ) return null;
      String sP = sParams.trim();	
      if( sP.length() <= 0) return null;
      if (xMSet.xU.IsNumeriek(sP) == false ) return null;
      int idx = xMSet.xU.NaarInt(sP);
      return iController.getCmdLineParameter(idx);
    }
}
