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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class webStraktorHTML {

	webStraktorSettings xMSet=null;
	webStraktorPrintStream repWriter=null;
	webStraktorParseController iController=null;
	private FileInputStream fromHTML=null;
	private int BUFLEN = 10000;
	private int lijnteller=0;
	boolean isReport=false;
	private BufferedWriter writer=null;
	private boolean exporting=false;
	private boolean isvalidquery=false;
	ArrayList<String> filelst=null;
	
	class Attrib
	{
		String attrib=null;
		String value=null;
		Attrib(String sa, String sv)
		{
			attrib=sa;
			value=sv;
		}
	}
	class HTMLNode
	{
		String Tag;
		String Content;
		ArrayList<Attrib> attriblst=null;
		int Occurence=0;
		HTMLNode(String sIn)
		{
			Tag=sIn;
			Content=null;
			attriblst=new ArrayList<Attrib>();
			Occurence=0;
		}
		void addAttrib(String sa, String sv)
		{
			Attrib x = new Attrib(sa,sv);
			attriblst.add(x);
		}
	}
	ArrayList<HTMLNode> nodelst = null;
	ArrayList<String> pathstack=null;
	ArrayList<HTMLNode> querylst = null;
	
	//
	//---------------------------------------------------------------------------------
	webStraktorHTML(webStraktorSettings iM,boolean iRep,webStraktorParseController iC)
	//---------------------------------------------------------------------------------
	{
		xMSet = iM;
		iController = iC;
		isReport=iRep;
		nodelst = new ArrayList<HTMLNode>();
		pathstack = new ArrayList<String>();
		querylst = null;
		if( isReport ) LogIt(1,"Running in Full HTML Report mode");
	}
	//
	//---------------------------------------------------------------------------------
	boolean parseQuery(String sQuery)
	//---------------------------------------------------------------------------------
	{
		//  /node/node/node
		//  /node/node[n]/node
		//  /node/node[attribute::name="value"]/node
		//  /node/node[@name="value"]/node
		//  /node/node[@name="value"][n]/node
	    //  /node/node[@name="value"][n][@name="value"]/node
		isvalidquery=false;
		querylst = null;
		querylst = new ArrayList<HTMLNode>();
		//
		if( sQuery == null ) {
			Error("XQuery is null");
			return false;
		}
		sQuery = sQuery.trim();
		if( sQuery.length() == 0 ) {
			Error("XQuery is empty - this is valid");
			isvalidquery=true;
			return true;  // valid query
		}
		if( sQuery.startsWith("/")==false) {
			Error("XQuery [" + sQuery + "] does not start by /");
			return false;
		}
		if( sQuery.endsWith("/")==true) {
			Error("XQuery [" + sQuery + "] cannot end on /");
			return false;
		}
		int aantal = xMSet.xU.TelDelims(sQuery,'/');
		for(int i=0;i<=aantal;i++)
		{
			String sDeel = xMSet.xU.GetVeld(sQuery,(i+1),'/').trim();
			if( sDeel.length() == 0) continue;
			String sEen  = xMSet.xU.GetVeld(sDeel,1,'[').trim();
			HTMLNode a = new HTMLNode(sEen);
			int iRest = xMSet.xU.TelDelims(sDeel,'[');
			for(int j=2;j<=(iRest+1);j++)
			{
			 String sTmp =  xMSet.xU.GetVeld(sDeel,j,'[').trim();
			 if( sTmp.length() < 1) continue;
			 if( sTmp.endsWith("]")==false) {
					Error(sQuery + " - No ending ] on (" + sDeel + ")");
					return false;
			  }
			  sTmp = xMSet.xU.RemplaceerNEW(sTmp,"]","");
			  if( xMSet.xU.IsNumeriek(sTmp) == false ) {
			   sTmp = xMSet.xU.RemplaceerNEW(sTmp,"attribute::","");
			   sTmp = xMSet.xU.RemplaceerNEW(sTmp,"@","");
			  
			   if (sTmp.indexOf("=") < 0 ) {
					Error(sQuery + " - Illegal attribute (" + sDeel + ")");
					return false;
			   }
			   String s1 = xMSet.xU.GetVeld(sTmp,1,'=').trim();
			   String s2 = xMSet.xU.GetVeld(sTmp,2,'=').trim();
			   //if( (s2.startsWith("\"")==false) || (s2.endsWith("\"")==false)) {
			   //		Error(sQuery + " - No enclosing quotes (" + sDeel + ")" + s1 + " " + s2);
			   //		return false;
			   //}
			   s2 = xMSet.xU.verwijderEnclosingQuotes(s2);
			   if( (s1.length()==0) || (s2.length()==0) ) {
					Error(sQuery + " - Empty attribute (" + sDeel + ")");
					return false;
			   }
			   Attrib x = new Attrib(s1,s2);
			   a.attriblst.add(x);
			 }
			 else {
			   int nn = xMSet.xU.NaarInt(sTmp);
			   if( nn < 0) {
				   Error("Not a numeric value " + sTmp + " in " + sDeel);
			   }
			   else a.Occurence = nn;
			 }	  
			}
			querylst.add(a);
		}
		LogIt(5,"QUERY : " + printStack(querylst));		
		
		isvalidquery=true;
		return isvalidquery;
	}
	//
	//---------------------------------------------------------------------------------
	boolean parseHTML(String FNaam,ArrayList<String> filelstIn)
	//---------------------------------------------------------------------------------
	{
		//
		fromHTML=null;
		lijnteller=0;
		writer=null;
		exporting=false;
		filelst=null;
		//
		this.filelst = filelstIn;
		if( querylst == null ) {
			Error("There appears to be no query list. Please run parseHTML() prior to parseHTML()");
			return false;
		}
		if( isvalidquery == false ) {
			Error("Not a valid query");
			return false;
		}
		boolean isOK = OpenHTMLFile(FNaam);
		if( isOK == false ) return false;
		if( isReport ) {
			String RepNaam = FNaam + "-report.txt";
			repWriter = new webStraktorPrintStream(RepNaam);
			Error("Detailed report available in" + RepNaam);
		}
		ParseIt(FNaam);
		isOK = CloseHTMLFile(FNaam);
		if( isOK == false ) return false;
		if( repWriter != null ) repWriter.close();
		return true;
	}
	//
	//---------------------------------------------------------------------------------
	void LogIt(int level, String sIn)
	//---------------------------------------------------------------------------------
	{
		System.out.println(sIn);
	}
	//
	//---------------------------------------------------------------------------------
	void Error(String sIn)
	//---------------------------------------------------------------------------------
	{
		LogIt(0,sIn);
	}
	//
	//---------------------------------------------------------------------------------
	private boolean OpenHTMLFile(String FNaam)
	//---------------------------------------------------------------------------------
	{
		try {
			fromHTML = new FileInputStream(FNaam);
			return true;
		}
		catch( Exception e)
		{
			Error("Error opening [" + FNaam + "]");
			Error(xMSet.xU.LogStackTrace(e));
			return false;
		}
	}
	//
	//---------------------------------------------------------------------------------
	private boolean CloseHTMLFile(String FNaam)
	//---------------------------------------------------------------------------------
	{
    try {
			if( fromHTML != null ) fromHTML.close();
			return true;
		}
		catch( Exception e)
		{
			Error("Error opening [" + FNaam + "]");
			Error(xMSet.xU.LogStackTrace(e));
			return false;
		}
	}
	//
	//---------------------------------------------------------------------------------
	private boolean ParseIt(String FNaam)
	//---------------------------------------------------------------------------------
	{
		byte[] buffer = new byte[BUFLEN];
		int bytes_read=0;
		byte b='\0';
		boolean isTag=false;
		String sTag="";
		String sContent="";
		boolean isScript=false;
		boolean isComment=false;
		String sComment="";
		String sScript="";
		try {
			while( (bytes_read=fromHTML.read(buffer)) != -1 ) {
	//System.out.println("READ" + bytes_read);
			 for(int ibc=0;ibc<bytes_read;ibc++)
			 {
	//System.out.print((char)buffer[ibc]);
			   if( exporting ) writeByte(buffer[ibc]);
			   //
			   if( buffer[ibc] == 10 ) {lijnteller++; continue; }
			   if( buffer[ibc] == 13 ) continue;
			   if( buffer[ibc] == '\t' ) continue;
			    b = buffer[ibc];
			   if( isScript ) {
				   sScript = sScript + (char)b;
				   if( sScript.toUpperCase().endsWith("</SCRIPT>")==true) {
					isScript=false;
				    PopTag("script");
				  	//Error("script->" + sScript);
				   }
				   continue; 
			   }
			   if( isComment ) {
				   sComment = sComment + (char)b;
				   if( sComment.toUpperCase().endsWith("-->")==true) {
						isComment=false;
				   }
				   continue;
			   }
			   //
			   if( b == '<') {
				   isTag=true;
				   sTag = "";
				   continue;
			   }
			   if( (b == '>') && (isTag==true) ) {
				   isTag=false;
				   if ( sTag.length() < 1) {
					   Error("Empty tag");
					   continue;
				   }
				   // comments <!--  dan moet einde --> zijn
				   //if( sTag.startsWith("!--")) {
				   //	   if( sTag.endsWith("--") == false) {  continue;  }
				   //}
				   // </    afsluitende tag
				   if( sTag.startsWith("/")) {
					  sTag = sTag.substring(1);
					  // CONTENT
					  AddContent(sTag,sContent);
					  sContent = "";
					  // POP
					  PopTag(sTag);
					  continue; 
				   }
				   // <   />  afsluitende tag
				   if( sTag.endsWith("/")) {
					  sTag = sTag.substring(0,sTag.length()-1);  
					  // PUSH
					  PushTag(sTag,"/>");
					  // CONTENT
					  AddContent(sTag,"");
					  sContent="";
					  // POP immediately after the push
					  PopTag(sTag);
					  continue;
				   }
				   //PUSH
				   PushTag(sTag,">");
				   isTag=false;
				   if( sTag.compareToIgnoreCase("SCRIPT")==0) { isScript=true; sScript=""; }
				   continue;
			   }
			   if( isTag ) {
				 sTag = sTag + (char)b;
				 if( sTag.startsWith("!--") ){ isComment=true; sComment=""; }
			   }
			   else {
				 sContent = sContent + (char)b;  
			   }
			 }
			}
		}
		catch( Exception e)
		{
			Error("Error reading [" + FNaam + "] " + e.getMessage());
			Error( xMSet.xU.LogStackTrace(e));
			return false;
		}
		return true;
	}
	//
	//---------------------------------------------------------------------------------
	private void PushTag(String sIn,String sEnd)
	//---------------------------------------------------------------------------------
	{
	 //Error("PUSHED in->" + sIn);
		String sTag=sIn;
		//
		if( sTag == null ) return;
		if( sTag.length() == 0 ) return;
		if( sTag.startsWith("!")) return;
		if( sTag.startsWith("?")) return;
		//
		int idx=-1;
		// <tag attrib="iets"   >
		if( (idx=sIn.indexOf(" ")) > 0 ) {
			sTag = sIn.substring(0,idx).trim();
		}
		//
	 	PushStack(sTag);
	//Error("PUSHED 02->" + sIn + " stack=" + this.printStack(nodelst));
		// attribs?
        if( sIn.compareToIgnoreCase(sTag) != 0 ) {
          String sAttrib = sIn.substring(idx).trim();
          sAttrib = xMSet.xU.vervangtussenDubbelQuotes(sAttrib,' ','$');
          sAttrib = xMSet.xU.vervangtussenDubbelQuotes(sAttrib,'"','µ');
          sAttrib = xMSet.xU.OntdubbelKarakter(sAttrib,' ').trim();
          sAttrib = xMSet.xU.VervangKarakter(sAttrib,' ','\n');
          sAttrib = xMSet.xU.RemplaceerNEW(sAttrib,"\"","");
          sAttrib = xMSet.xU.VervangKarakter(sAttrib,'$',' ');
          sAttrib = xMSet.xU.VervangKarakter(sAttrib,'µ','"');
          sAttrib = sAttrib + "\n";
          int aantal = xMSet.xU.TelDelims(sAttrib,'\n');
          for(int i=0;i<=aantal;i++)
          {
           String sEen = xMSet.xU.GetVeld(sAttrib,(i+1),'\n');
           if( sEen.length() < 1) continue;
           if( sEen.indexOf("=") < 0 ) continue;
           String sTwee = xMSet.xU.GetVeld(sEen,2,'=');
           sEen = xMSet.xU.GetVeld(sEen,1,'=').trim();
           AddAttrib(sEen,sTwee);    	
          }
        }
        // Check of er een match is
        if( CheckMatch() ) {
        	LogIt(9,"   MATCH " + showNodeLst());
        	if( exporting == false ) {
        		exporting = true;
        		openWriter();
        		printWriter("<"+sIn+sEnd);
        	}
        }
        //else {
        //	Error("NO MATCH " + shostack());
        //	if( exporting == true ) {
        //		exporting = false;
        //		closeWriter();
        //	}
        //}
    }
	
	//
	//---------------------------------------------------------------------------------
	private void PopTag(String sIn)
	//---------------------------------------------------------------------------------
	{
		String sTag=sIn;
		int idx=-1;
		if( (idx=sIn.indexOf(" ")) > 0 ) {
			sTag = sIn.substring(0,idx).trim();
		}
		PopStack(sTag);
		// einde van de match
	    if( CheckMatch() == false ) {
	        	LogIt(9,"NO MATCH " + showNodeLst());
	        	if( exporting == true ) {
	        		exporting = false;
	        		closeWriter();
	        	}
	    }
	}
	//
	//---------------------------------------------------------------------------------
	private void PushStack(String sT)
	//---------------------------------------------------------------------------------
	{
		HTMLNode x = new HTMLNode(sT);
		nodelst.add(x);
		DetermineAndSetOccurence();
	}
	//
	//---------------------------------------------------------------------------------
	private void PopStack(String sT)
	//---------------------------------------------------------------------------------
	{
		if( isReport ) {
			if( repWriter != null ) repWriter.println(showNodeLst()); else LogIt(5,showNodeLst());
		}
		if( nodelst.size() <= 0) return;
		HTMLNode x = nodelst.get(nodelst.size()-1);
		if( x.Tag.compareToIgnoreCase(sT)==0) {
			nodelst.remove(nodelst.size()-1);
			return;
		}
		Error("-- line=" + lijnteller + " ---> POPPING incorrect node [" + sT +"] last is [" + x.Tag +"]");
		Error(showNodeLst());
		// case 1 :  een pop van een tag die niet op laatste steekt maar ervoor
		// restore is door de laatste occurernce van de tag te zoeken en alles daarachter te verwijderen
		// case 2 : een pop van een tag die nooit op de stack is gezet
		// restore is onmogelijk, doch hoe detecteren ? Oplossing = ignore, doch hoe detecteer je dit geval?  je zal steeds te weinig op stack hebben
		
		int idx=-1;
		for(int i=nodelst.size()-1;i>=0;i--)
		{
			if( nodelst.get(i).Tag.compareToIgnoreCase(sT) == 0) {
				idx=i;
				break;
			}
		}
		if( idx>=0) {
			int aantal = nodelst.size();
			for(int i=0;i<aantal;i++) {
				for(int j=idx;j<nodelst.size();j++) {
					nodelst.remove(j);
				}
			}
		}
		LogIt(9,showNodeLst());
	}
	//
	//---------------------------------------------------------------------------------
	private String showNodeLst()
	//---------------------------------------------------------------------------------
	{
		return xMSet.xU.LPad(""+lijnteller,6) + " " + printStack(nodelst);
	}
	//
	//---------------------------------------------------------------------------------
	private String printStack(ArrayList<HTMLNode> lst)
	//---------------------------------------------------------------------------------
	{
		String sL="";
		for(int i=0;i<lst.size();i++) {
			if( i != 0) sL = sL + "->";
			sL = sL + "(" + lst.get(i).Occurence + ") " + lst.get(i).Tag;
			for(int j=0;j<lst.get(i).attriblst.size();j++)
			{
				sL = sL + "[" + lst.get(i).attriblst.get(j).attrib + "=" + lst.get(i).attriblst.get(j).value + "]";
			}
			if( lst.get(i).Content == null) continue;
			if( lst.get(i).Content.length() == 0 ) continue;
			sL = sL + "[CONTENT=" + lst.get(i).Content + "]";
		}
		return sL;
	}
	//
	//---------------------------------------------------------------------------------
	void AddAttrib(String sE, String sT)
	//---------------------------------------------------------------------------------
	{
		int idx=this.nodelst.size()-1;
		if( idx < 0) return;
		nodelst.get(idx).addAttrib(sE,sT);
	}
	//
	//---------------------------------------------------------------------------------
	void AddContent(String sTag,String sContent)
	//---------------------------------------------------------------------------------
	{
		int idx=this.nodelst.size()-1;
		if( idx < 0) return;
		nodelst.get(idx).Content = sContent.trim();
	}
	//
	//---------------------------------------------------------------------------------
	void DetermineAndSetOccurence()
	//---------------------------------------------------------------------------------
	{
		String sPath="";
		for(int i=0;i<this.nodelst.size();i++) {
			sPath = sPath + "+" + nodelst.get(i).Tag;
		}
		pathstack.add(sPath);
		// Look for similar path on stack and count occurences and set this to the last node
		int teller=0;
		for(int i=0;i<pathstack.size();i++) {
			if( pathstack.get(i).compareTo(sPath)==0) teller++;
		}
		int idx=this.nodelst.size()-1;
		if( idx < 0) return;
		nodelst.get(idx).Occurence = teller;
	}
	//
	//---------------------------------------------------------------------------------
	boolean CheckMatch()
	//---------------------------------------------------------------------------------
	{
	  try  {
		if( querylst == null ) return false;
		int matchCount = this.querylst.size();
		if (matchCount == 0) return true;
		
		//Error("NODE STACK" + this.printStack(nodelst));
		//Error("QUER STACK" + this.printStack(querylst));
		//Error("----------------------");
		
		
		// match op de nodenames
		int teller=0;
		for(int i=0;i<matchCount;i++)
		{
		  if( nodelst.get(i).Tag.compareTo(querylst.get(i).Tag)!=0) break;   // indien nodelst niet bestaat -> exception
		  // occurence match takes precedence
		  if( querylst.get(i).Occurence != 0 ) {
			  if( nodelst.get(i).Occurence != querylst.get(i).Occurence ) return false;
		  }
		  // match op de attribs
		  int attribCount = querylst.get(i).attriblst.size();
		  if( attribCount > 0) {
			  int itel=0;
			  for(int j=0;j<querylst.get(i).attriblst.size();j++) {
				  String sAttrib=querylst.get(i).attriblst.get(j).attrib;
				  String sValue=querylst.get(i).attriblst.get(j).value;
				  for(int k=0;k<nodelst.get(i).attriblst.size();k++) {
					  if( (nodelst.get(i).attriblst.get(k).attrib.compareTo(sAttrib)==0) &&
						  (nodelst.get(i).attriblst.get(k).value.compareTo(sValue)==0) ) {
						  itel++;
						  break;
					  }
				  }
			  }
			  if( itel != attribCount ) return false;
		  }
		  // een match
		  teller++;
		}
		if( teller == matchCount ) return true;
	  }
	  catch( Exception e) {	return false; }
	  return false;
	}
	//
	//---------------------------------------------------------------------------------
	boolean openWriter()
	//---------------------------------------------------------------------------------
	{
		if( writer != null ) closeWriter();
		try {
			    String FNaam = iController.getNewDumpFileName();
				writer = new BufferedWriter(new FileWriter(FNaam));
				LogIt(0,"Opened [" + iController.getCurrentDumpFileName() + "] for XPATH");
				filelst.add(FNaam);
				return true;
		}
		catch (Exception e) {
				Error("Error opening [" + iController.getCurrentDumpFileName() + "] " + e.getMessage());
				Error(xMSet.xU.LogStackTrace(e));
				return false;
		}
	}
	//
	//---------------------------------------------------------------------------------
	boolean closeWriter()
	//---------------------------------------------------------------------------------
	{
		if( writer == null ) return true;
		try {
			writer.close();
			return true;
		}
		catch (Exception e) {
			Error("Error closing [" + iController.getCurrentDumpFileName() + "] " + e.getMessage());
			Error(xMSet.xU.LogStackTrace(e));
			return false;
		}
    }
	//
	//---------------------------------------------------------------------------------
	boolean printWriter(String sIn)
	//---------------------------------------------------------------------------------
	{
		if( writer == null ) return true;
		try {
			writer.write(sIn);
			return true;
		}
		catch (Exception e) {
			Error("Error writing [" + iController.getCurrentDumpFileName() + "] " + e.getMessage());
			Error(xMSet.xU.LogStackTrace(e));
			return false;
		}
    }
	//
	//---------------------------------------------------------------------------------
	boolean writeByte(byte b)
	//---------------------------------------------------------------------------------
	{
		if( writer == null ) return true;
		try {
			writer.write(b);
			return true;
		}
		catch (Exception e) {
			Error("Error writing [" + iController.getCurrentDumpFileName() + "] " + e.getMessage());
			Error(xMSet.xU.LogStackTrace(e));
			return false;
		}
    }
}



