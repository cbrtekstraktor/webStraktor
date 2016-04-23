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

import java.util.ArrayList;


public class webStraktorTrace {

	public enum TraceStatus { Start, Stop, Error, Ok };
	private int OIDSeed = (int)System.currentTimeMillis();
	
	webStraktorSettings xMSet = null;
	ArrayList<parseFunction> lstFuncties = null;
	private int diepte = 0;
	private long trace_num = 100; //System.currentTimeMillis();
	nioClient nio_out = null;
	webStraktorPrintStream trace_out=null;
	webStraktorParseController iController=null;
	webStraktorDateTime xDateTime=null;
	private int lijnteller=0;
	
	private long nuu = System.nanoTime()/1000L;
	private long num = System.currentTimeMillis();
	
	public ArrayList<String> traceList=null;
	
	enum  ComponentType { Unknown, Main, Foreach , Procedure , Variable, Link , Blob, Cut , Form , Call , PassPhrase , Url };
	
	class trace 
	{
		int           RefOID=-1;
		String        Naam="";
		long          TraceId=0L;
		long          ParentTraceId=0L;
		int           diepte=0;
		boolean       isFunc=false;
		ComponentType cmpTipe;
	}
	ArrayList<trace> tlist = null;
	
	//---------------------------------------------------------------------------------
	webStraktorTrace(webStraktorSettings iM)
	//---------------------------------------------------------------------------------
	{
	   xMSet = iM;	
	   nuu = System.nanoTime()/1000L;
	   num = System.currentTimeMillis();
	   traceList = new ArrayList<String>();
	}
	//---------------------------------------------------------------------------------
	private void LogIt(int level , String sIn)
	//---------------------------------------------------------------------------------
	{
		xMSet.LogIt(level, sIn);
	}
	//---------------------------------------------------------------------------------
	private void Error(String sIn)
	//---------------------------------------------------------------------------------
	{
		LogIt(0,sIn);
	}
	//---------------------------------------------------------------------------------
	public void maakTraceModel(webStraktorParseController iC)
	//---------------------------------------------------------------------------------
	{
		iController = iC;
	    lstFuncties = iC.lstFuncties;
	    xDateTime = new webStraktorDateTime(xMSet.TimeZone);
	    tlist = new ArrayList<trace>();
	    
	    // get main and walk the tree of functions
		int idx =1;
		for(int i=0;i<lstFuncties.size();i++) {
			if( "main".compareToIgnoreCase(lstFuncties.get(i).Name) == 0 ) idx=i;
		}
		if ( idx < 0 ) {
			Error("There is no main function");
			return;
		}
		descend( lstFuncties.get(idx) , -1);
		//
		for(int i=0;i<tlist.size();i++)
		{
			String sIndent = "";
			for(int j=0;j<tlist.get(i).diepte;j++) sIndent = sIndent + "  ";
			String sLijn = sIndent + 
			               tlist.get(i).Naam + " " +
			               tlist.get(i).cmpTipe + " " +
			               tlist.get(i).RefOID + " " +
			               tlist.get(i).TraceId + " " + tlist.get(i).ParentTraceId + " " + 
			               tlist.get(i).isFunc;
			LogIt(5, sLijn );
		}
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
	private void descend(parseFunction x , long prntid)
	//---------------------------------------------------------------------------------
	{
		diepte++;

		//
		trace tr = new trace();
		tr.RefOID  = x.OID;
		tr.Naam    = x.Label;
		tr.TraceId = x.traceID;
		if( x.traceID <=0 ) tr.TraceId = trace_num++;
		tr.ParentTraceId = prntid;
		tr.diepte = diepte;
		tr.isFunc = true;
		if( prntid < 0 ) tr.cmpTipe = ComponentType.Main;
		else {
			if( x.Name.toUpperCase().startsWith("FOREACH")==true) tr.cmpTipe = ComponentType.Foreach; 
			                                                 else tr.cmpTipe = ComponentType.Procedure;
		}
		//
		tlist.add(tr);
		// add a dummy passphrase to each procedure
		trace tr2 = new trace();
		tr2.RefOID  = OIDSeed++;
		tr2.Naam    = x.Label + "-PassPhrase";
		tr2.TraceId = trace_num++;
		tr2.ParentTraceId = tr.TraceId;
		tr2.diepte = diepte;
		tr2.isFunc = false;
		tr2.cmpTipe = ComponentType.PassPhrase;
		//
		tlist.add(tr2);
		// add a dummy URL to each procedure
		trace tr3 = new trace();
		tr3.RefOID  = OIDSeed++;
		tr3.Naam    = x.Label + "-URL";
		tr3.TraceId =  trace_num++;
		tr3.ParentTraceId = tr.TraceId;
		tr3.diepte = diepte;
		tr3.isFunc = false;
		tr3.cmpTipe = ComponentType.Url;
		//
		tlist.add(tr3);
		//
		for(int i=0;i<x.lstInstructies.size();i++)
		{
		  trace tz = new trace();
		  tz.RefOID  = x.lstInstructies.get(i).OID;
		  tz.Naam    = x.lstInstructies.get(i).Label;
		  tz.TraceId = x.lstInstructies.get(i).traceID;
		  if( tz.TraceId <=0 ) tz.TraceId = trace_num++;
		  tz.ParentTraceId = tr.TraceId;
		  tz.diepte = diepte;
		  tz.isFunc = false;
		  switch( x.lstInstructies.get(i).Tipe )
		  {
		  case parseInstruction.isVARIABLE : { tz.cmpTipe = ComponentType.Variable; break; }
		  case parseInstruction.isBLOB : { tz.cmpTipe = ComponentType.Blob; break; }
		  case parseInstruction.isFORM : { tz.cmpTipe = ComponentType.Form; break; }
		  case parseInstruction.isCUT : { tz.cmpTipe = ComponentType.Cut; break; }
		  case parseInstruction.isFOREACH : 
		       { 
		         if( x.lstInstructies.get(i).Name.trim().toUpperCase().startsWith("FOREACH") == true ) tz.cmpTipe = ComponentType.Foreach;
		         else  tz.cmpTipe = ComponentType.Call;
		         break; 
		       }
		  case parseInstruction.isLINK : { tz.cmpTipe = ComponentType.Link; break; }
		  default : {  tz.cmpTipe = ComponentType.Unknown; break; }
		  }
		
		  //
		  tlist.add(tz);
		  //
		  if( x.lstInstructies.get(i).Tipe == parseInstruction.isFOREACH )	{
			  parseFunction y =  getForeach( x.lstInstructies.get(i).Name );
			  if( y == null ) continue;
			  descend(y , tr.TraceId);
		  }
		  if( x.lstInstructies.get(i).Tipe == parseInstruction.isLINK )	{
			  parseFunction y =  getForeach( x.lstInstructies.get(i).referencedForEach );
			  if( y == null ) continue;
			  descend( y , tr.TraceId);
		  }
		  
		}
		diepte--;	
	}
	
	//
	//---------------------------------------------------------------------------------
	public void openTrace()
	//---------------------------------------------------------------------------------
	{
		trace_out = new webStraktorPrintStream(iController.getTraceFileName());
		nio_out = new nioClient(xMSet.TracePort);
		//
		String sLijn = String.format("%-33s","--HEADER") + "webStraktor";
		trace_out.println("");
		trace_out.println("START");
		trace_out.println(sLijn);
		//
		nio_out.writeClient("");
		nio_out.writeClient("START");
		nio_out.writeClient(sLijn);
		// 
		sLijn = String.format("%-33s","--@FLEX-PID") + xMSet.xU.getProcessId("?");
		trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
		// 
		sLijn = String.format("%-33s","--@FLEX-VERSION") + xMSet.version;
		trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
		// 
		sLijn = String.format("%-33s","--@FLEX-BUILD") + xMSet.build;
		trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
		// 
		sLijn = String.format("%-33s","--@FLEX-EXECTIME") + xDateTime.DateTimeNow(xMSet.LoggingDateFormaat);
		trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
		// 
		sLijn = String.format("%-33s","--@FLEX-SCRIPTNAME") + iController.getSourceCodeFileName().trim();
		trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
		//
		sLijn = String.format("%-33s","--@FLEX-CMDLINEPARAMS") + iController.getCmdLineParams();
		trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
		//
		sLijn = String.format("%-33s","--@FLEX-LOGFILE") + iController.getLogFileName();
		trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
		//
		sLijn = String.format("%-33s","--@FLEX-LOGLEVEL") + iController.getLogLevel();
		trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
		//
		sLijn = String.format("%-33s","--@FLEX-PROXY") + iController.getProxyName();
		trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
		//
		sLijn = String.format("%-33s","--@FLEX-KEEPFILE") + iController.KeepFile();
		trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
		//
		sLijn = String.format("%-33s","--@FLEX-USERAGENT") + iController.getUserAgent();
		trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
		//
		sLijn = "--@FLEX-MODELITEMINFO " + String.format("%16s", "Trace ID  ") + String.format("%18s", "Parent Trace ID ") + String.format("%-41s", "Component Name") + "Component Type";
        trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
		//
		for(int i=0;i<tlist.size();i++)
		{
			       sLijn = "--@FLEX-MODELITEM " +
			               String.format("%18d", tlist.get(i).TraceId) + " " +
			               String.format("%18d", tlist.get(i).ParentTraceId) + " " +
			               String.format("%-40s", tlist.get(i).Naam) + " " +
			               (""+tlist.get(i).cmpTipe).toLowerCase();
			trace_out.println(sLijn);
			nio_out.writeClient(sLijn);
		}
		//
		trace_out.println("--END HEADER");
		nio_out.writeClient("--END HEADER");
	}
	//
	//---------------------------------------------------------------------------------
	public void closeTrace()
	//--------------------------------------------------------------------------------
	{
		trace_out.println("END");
		nio_out.writeClient("END");
		if( trace_out != null ) trace_out.close();
		nio_out.close();
	}
	//
	//---------------------------------------------------------------------------------
	private int getTraceIdViaOID(int iOID)
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<tlist.size();i++)
		{
			if( tlist.get(i).RefOID == iOID ) return i;
		}
		return -1;
	}
	//
	//---------------------------------------------------------------------------------
	public void writeTrace(String tipe , String sName , long traceID , TraceStatus iStat , String sMsg)
	//---------------------------------------------------------------------------------
	{
		lijnteller++;
		String sLijn = String.format("%35s", sName ) + "|" +
		               tipe + "|" +
		               String.format("%18d",traceID) + "|" +
		               String.format("%10s", ""+iStat ) + "|" + 
		               String.format("%15d",System.nanoTime()/1000L) + "|" +
				       sMsg;
        LogIt(5,sLijn);
		if( trace_out == null ) return;
		if( trace_out.isActive() == false ) return;
		if( lijnteller == 1 ) {
			 String oLijn = 
				      "--@FLEX-SYNCHRO milli" + String.format("%15d",num) + 
				      " micro" +  String.format("%15d",nuu); 	
			 trace_out.println(oLijn);
			 nio_out.writeClient(oLijn);
		}
		trace_out.println(sLijn);
		nio_out.writeClient(sLijn);
	}
	//
	//---------------------------------------------------------------------------------
	private void traceFuncInstr(String tipe , int iOID , webStraktorTrace.TraceStatus iStat , String sMsg)
	//---------------------------------------------------------------------------------
	{
		int idx = getTraceIdViaOID( iOID );
		if( idx < 0 ) return;
		writeTrace( tipe , tlist.get(idx).Naam , tlist.get(idx).TraceId , iStat , sMsg);
	}
	//
	//---------------------------------------------------------------------------------
	public void traceInstruction(int iOID , TraceStatus iStat , String sMsg)
	//---------------------------------------------------------------------------------
	{
		traceInstruction( "C" , iOID , iStat , sMsg);
	}
	//
	//---------------------------------------------------------------------------------
	public void traceFunction(int iOID , webStraktorTrace.TraceStatus iStat , String sMsg)
	//---------------------------------------------------------------------------------
	{
		traceFunction( "C" , iOID , iStat , sMsg );
	}
	//
	//---------------------------------------------------------------------------------
	public void traceInstruction(String tipe , int iOID , webStraktorTrace.TraceStatus iStat , String sMsg)
	//---------------------------------------------------------------------------------
	{
		traceFuncInstr( tipe , iOID , iStat , sMsg);
	}
	//
	//---------------------------------------------------------------------------------
	public void traceFunction(String tipe, int iOID , webStraktorTrace.TraceStatus iStat , String sMsg)
	//---------------------------------------------------------------------------------
	{
		traceFuncInstr( tipe , iOID , iStat , sMsg );
	}
	//
	//---------------------------------------------------------------------------------
	public void traceInstructionFile(int iOID,String FNaam)
	//---------------------------------------------------------------------------------
	{
		traceInstruction( "F" , iOID , webStraktorTrace.TraceStatus.Ok , FNaam );
	}
	//
	//---------------------------------------------------------------------------------
	public void traceFunctionFile(int iOID ,String FNaam)
	//---------------------------------------------------------------------------------
	{
		traceFunction( "F" , iOID , webStraktorTrace.TraceStatus.Ok , FNaam );
	}
	//
	//---------------------------------------------------------------------------------
	public int getURLOID(int iOID)
	//---------------------------------------------------------------------------------
	{
		int idx = getTraceIdViaOID( iOID );
		if( idx < 0 ) return -1;
		long prntTraceId = tlist.get(idx).TraceId;
		for(int i=0;i<tlist.size();i++)
		{
			if( tlist.get(i).ParentTraceId != prntTraceId ) continue;
			if( tlist.get(i).cmpTipe == webStraktorTrace.ComponentType.Url ) return tlist.get(i).RefOID;
		}
		return -1;
	}
	//
	//---------------------------------------------------------------------------------
	public int getPassPhraseOID(int iOID)
	//---------------------------------------------------------------------------------
	{
		int idx = getTraceIdViaOID( iOID );
		if( idx < 0 ) return -1;
		long prntTraceId = tlist.get(idx).TraceId;
		for(int i=0;i<tlist.size();i++)
		{
			if( tlist.get(i).ParentTraceId != prntTraceId ) continue;
			if( tlist.get(i).cmpTipe == webStraktorTrace.ComponentType.PassPhrase ) return tlist.get(i).RefOID;
		}
		return -1;
	}
}
