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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;

public class webStraktorParseForm {

	webStraktorSettings xMSet = null;
	webStraktorLogger iLogger=null;
	private int FormTeller=1;
	private boolean parsedOk=false;
	private int lastProbability=-1;
	
	class HTMLInput
	{
		String Name = "";
		String Type = "";
		String Id = "";
		String Value = "";
	}
	class HTMLForm
	{
		String Name   = "";
		String Action = "";
		String Method = "";
		String Id     = "";
		Boolean inUse = false;
		Boolean isGlyph = true;  // proxy detectie
		int     glyphProbability=0;
		ArrayList<HTMLInput> InputList = null;
		HTMLForm()
		{
			Name = ""+(FormTeller++);
			inUse = false;
			InputList = new ArrayList<HTMLInput>();
		}
	}
	ArrayList<HTMLForm> formlist = null;
	
	
	
	//
	//---------------------------------------------------------------------------------
	webStraktorParseForm(String FNaam,webStraktorSettings xM,webStraktorLogger i)
	//---------------------------------------------------------------------------------
	{
		xMSet = xM;
		iLogger = i;
		formlist = new ArrayList<HTMLForm>();
		parsedOk = ParseForm(FNaam);
	}
	//
	//---------------------------------------------------------------------------------
	public boolean parsedOK()
	//---------------------------------------------------------------------------------
	{
		return parsedOk;
	}
	//
	//---------------------------------------------------------------------------------
	void logit(int level , String sIn)
	//---------------------------------------------------------------------------------
	{
		if( iLogger != null ) iLogger.Logit(level, "FRM - "  + sIn);
	}
	//
	//---------------------------------------------------------------------------------
	void Error( String sIn)
	//---------------------------------------------------------------------------------
	{
		logit(0,sIn);
	}
	//
	//---------------------------------------------------------------------------------
	private boolean ParseForm(String FNaam)
	//---------------------------------------------------------------------------------
	{
		// lees bestand en knip de stukken eruit en zet die op een stack
		if( xMSet.xU.IsBestand(FNaam) == false ) {
			Error("File [" + FNaam + "] not found");
			return false;
		}
	    //	Knip tusssen form en /form en plaats een N voor iedere > en space op die wijze krijg je aparte lijnen
		BufferedWriter writer = null;
		try {
			  String TempName = FNaam+".txt";
			  File inFile  = new File(FNaam);  // File to read from.
	       	  BufferedReader reader = new BufferedReader(new FileReader(inFile));
	       	  
	       	  String sLijn = null;
	       	  Boolean iFound = false;
	       	  int teller = 0;
	       	  boolean inner=false;
	       	  while ((sLijn=reader.readLine()) != null) {
	       		 sLijn = xMSet.xU.RemplaceerNEW(sLijn,"\"/>","\" />");     //  <href a= ..  iets="aa"/>  maak daar een spaatie /> van
	       		 if( sLijn.toLowerCase().indexOf("<form") >= 0) iFound = true;   
		         if( iFound == true ) {
		        	if( writer == null ) { 
		        	 writer = new BufferedWriter(new FileWriter(TempName));
		        	} 
		        	char[] SChar = sLijn.toCharArray();
		     	    for(int ii=0;ii<SChar.length;ii++)
		     	    {
		     	    	if( SChar[ii] == '\t') continue;
		     	    	if( SChar[ii] == '"') inner = !inner;
		     	    	if( SChar[ii] == '>') { writer.write(xMSet.xU.EOL + "<!--EINDE" + xMSet.xU.EOL); continue; } 
		     	    	if( SChar[ii] == '<') continue;
		     	    	writer.write(SChar[ii]);
		     	    	if( (SChar[ii] == ' ') && (inner==false)) writer.write(""+xMSet.xU.EOL); 
		     	    }
		            writer.write(""+xMSet.xU.EOL);
		            teller++;
		         }
		         if( sLijn.toLowerCase().indexOf("</form>") >= 0) iFound = false;   
	          }
	       	  if( writer != null ) writer.close();
	       	  reader.close();
	       	  if( teller == 0) {
	       		  Error("No form found in main url");
	       		  return false;
	       	  }
	       	  // lees nu de gegeknipte form
	       	 inFile  = new File(FNaam);  // File to read from.
	       	 reader = new BufferedReader(new FileReader(TempName));
	       	 iFound = false;
	       	 int inputteller=0;
	       	 int idx = -1;
	       	 int pdx = -1;
	       	 inner = false;
	       	 while ((sLijn=reader.readLine()) != null) {
	       		 sLijn = sLijn.trim();
	       		 if( sLijn.trim().length() == 0 ) continue;
	       		 sLijn = xMSet.xU.VervangKarakter(sLijn,'\'','\"');
	       	//logit(1,sLijn);
	       		 idx = formlist.size()-1;
	       		if( sLijn.toLowerCase().indexOf("<!--EINDE")==0) {
	       			inner = false;
	       		}
	       		 if( sLijn.toLowerCase().indexOf("form")==0) {
	       			 inputteller=0;
	       			 inner = true;
	       			 HTMLForm x = new HTMLForm();
	       			 formlist.add(x);
	       			 continue;
	       		 }
	       		 if( idx < 0 ) continue;  // indien geen form geen zin om verder te doen
	       		 if( (sLijn.toLowerCase().indexOf("input")==0) || (sLijn.toLowerCase().indexOf("select")==0) ) {  // SELECT added
	       			 inputteller++;
	       			 inner = true;
	       			 HTMLInput x = new HTMLInput();
	       			 if(sLijn.toLowerCase().indexOf("select")==0 ) x.Type = "select";
	       			 formlist.get(idx).InputList.add(x);
	       	         continue;
	       		 }
	       		 if( inner == false ) continue;
	       		 if( sLijn.toLowerCase().indexOf("action=")==0) {
	       			 sLijn = xMSet.xU.Remplaceer(sLijn,"action=",""); // beter dan getveld want soms = in de action
	       		     sLijn = xMSet.xU.verwijderEnclosingQuotes(sLijn);
	       		     if (formlist.get(idx).Action.length()==0) formlist.get(idx).Action = sLijn.trim();
	       			 continue;
	       		 }
	       		 if( sLijn.toLowerCase().indexOf("method=")==0) {
	       			 sLijn = xMSet.xU.Remplaceer(sLijn,"method=","");
	       		     sLijn = xMSet.xU.verwijderEnclosingQuotes(sLijn);
	       		     if (formlist.get(idx).Method.length()==0) formlist.get(idx).Method = sLijn.trim().toUpperCase();
	       			 continue;
	       		 }
	       		 if( (sLijn.toLowerCase().indexOf("id=")==0) && (inputteller==0)) {
	       			 sLijn = xMSet.xU.GetVeld(sLijn,2,'=');
	       		     sLijn = xMSet.xU.verwijderEnclosingQuotes(sLijn);
	       		     if (formlist.get(idx).Id.length()==0) formlist.get(idx).Id = sLijn.trim();
	       			 continue;
	       		 }
	       		 if( (sLijn.toLowerCase().indexOf("name=")==0) && (inputteller==0)) {
	       			 sLijn = xMSet.xU.GetVeld(sLijn,2,'=');
	       		     sLijn = xMSet.xU.verwijderEnclosingQuotes(sLijn);
	       		     if (formlist.get(idx).inUse == false ) {
	       		    	 formlist.get(idx).Name = sLijn.trim();
	       		    	formlist.get(idx).inUse = true;
	       		     }
	       			 continue;
	       		 }
	       		 pdx = formlist.get(idx).InputList.size()-1;
	       		 if( pdx < 0 ) continue;
	       		 if( (sLijn.toLowerCase().indexOf("name=")==0) && (inputteller>0)) {
	       			 sLijn = xMSet.xU.GetVeld(sLijn,2,'=');
	       		     sLijn = xMSet.xU.verwijderEnclosingQuotes(sLijn);
	       		     if( formlist.get(idx).InputList.get(pdx).Name.length() == 0) formlist.get(idx).InputList.get(pdx).Name = sLijn.trim();
	       			 continue;
	       		 }
	       		 if( (sLijn.toLowerCase().indexOf("id=")==0) && (inputteller>0)) {
	       			 sLijn = xMSet.xU.GetVeld(sLijn,2,'=');
	       		     sLijn = xMSet.xU.verwijderEnclosingQuotes(sLijn);
	       		     if( formlist.get(idx).InputList.get(pdx).Id.length() == 0) formlist.get(idx).InputList.get(pdx).Id = sLijn.trim();
	       			 continue;
	       		 }
	       		 if( (sLijn.toLowerCase().indexOf("type=")==0) && (inputteller>0)) {
	       			 sLijn = xMSet.xU.GetVeld(sLijn,2,'=');
	       		     sLijn = xMSet.xU.verwijderEnclosingQuotes(sLijn);
	       		     if( formlist.get(idx).InputList.get(pdx).Type.length() == 0) formlist.get(idx).InputList.get(pdx).Type = sLijn.trim();
	       			 continue;
	       		 }
	       		if( (sLijn.toLowerCase().indexOf("value=")==0) && (inputteller>0)) {
	       			 sLijn = xMSet.xU.GetVeld(sLijn,2,'=');
	       		     sLijn = xMSet.xU.verwijderEnclosingQuotes(sLijn);
	       		     sLijn = xMSet.xU.TerugUitInternFormaat(sLijn);
	       		     if( formlist.get(idx).InputList.get(pdx).Value.length() == 0) formlist.get(idx).InputList.get(pdx).Value = sLijn.trim();
	       			 continue;
	       		 }
	       	 }
	       	 reader.close();
	       	 xMSet.xU.VerwijderBestand(TempName);
	       	 if( formlist.size() == 0 ) {
	       		 Error("NO form data parsed");
	       		 return false;
	       	 }
	       	 for(int i=0;i<formlist.size();i++)
	       	 {
	       		 logit(5, "FORM Name="   + formlist.get(i).Name + " " +
	       				  "Id="     + formlist.get(i).Id + " " +
	       				  "Method=" + formlist.get(i).Method + " " +
	       				  "Action=" + formlist.get(i).Action );
	       		 for(int j=0;j<formlist.get(i).InputList.size();j++) 
	       		 {
	       			 logit(5, "    INPUT   Name=" + formlist.get(i).InputList.get(j).Name + " " +
	       					  "Id=" + formlist.get(i).InputList.get(j).Id + " " +
	       					  "Type=" + formlist.get(i).InputList.get(j).Type + " " +
	       					  "Value=" + formlist.get(i).InputList.get(j).Value + " ");
 	       		 }
	       	 }
		}   	  
	    catch(Exception e) {
	    	Error("Parsing form [" + FNaam +"] " + e.getMessage());
			Error(xMSet.xU.LogStackTrace(e));	
			return false;
	    }
		return true;
	}
	//
	//---------------------------------------------------------------------------------
	public int getLastProbability()
	//---------------------------------------------------------------------------------
	{
		return this.lastProbability;
	}
	//
	//---------------------------------------------------------------------------------
	public String getFirstFormName()
	//---------------------------------------------------------------------------------
	{
		if( formlist.size() == 0 ) return "";
		return formlist.get(0).Name.trim();
	}
	//
	//---------------------------------------------------------------------------------
	public boolean doesFormexist(String Fin)
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<formlist.size();i++) 
		{
		   if( formlist.get(i).Name.trim().compareToIgnoreCase(Fin.trim()) == 0) return true;	
		}
		return false;
	}
	//
	//---------------------------------------------------------------------------------
	public boolean isMethodPost(String Fin)
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<formlist.size();i++) 
		{
		   if( formlist.get(i).Name.trim().compareToIgnoreCase(Fin.trim()) == 0) {
			   if( formlist.get(i).Method.trim().compareToIgnoreCase("POST")==0) return true;
			   return false;
		   }
		}
		return false;
	}
	//
	//---------------------------------------------------------------------------------
	public boolean isMethodGet(String Fin)
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<formlist.size();i++) 
		{
		   if( formlist.get(i).Name.trim().compareToIgnoreCase(Fin.trim()) == 0) {
			   if( formlist.get(i).Method.trim().compareToIgnoreCase("GET")==0) return true;
			   return false;
		   }
		}
		return false;
	}
	//
	//---------------------------------------------------------------------------------
	public String getSingleTextInput(String Fin)
	//---------------------------------------------------------------------------------
	{
		String sInput = null;
		for(int i=0;i<formlist.size();i++) 
		{
		   if( formlist.get(i).Name.trim().compareToIgnoreCase(Fin.trim()) == 0)  {
			   int textTeller=0;
			   for(int j=0;j<formlist.get(i).InputList.size();j++)
			   {
				   if( formlist.get(i).InputList.get(j).Type.trim().compareToIgnoreCase("TEXT")==0) {
					   textTeller++;
					   sInput = formlist.get(i).InputList.get(j).Name.trim();
				   }
			   }
			   if( textTeller == 1 ) return sInput; else return null;
		   }	   
		}
		return null;   
	}
	//
	//---------------------------------------------------------------------------------
	public String getAction(String Fin)
	//---------------------------------------------------------------------------------
	{
	    String sAction = null;
		for(int i=0;i<formlist.size();i++) 
		{
		   if( formlist.get(i).Name.trim().compareToIgnoreCase(Fin.trim()) == 0)  {
			   sAction = formlist.get(i).Action.trim();
			   break;
		   }	   
		}
		return sAction;   
	}
	//
	//---------------------------------------------------------------------------------
	public String getExtendedAction(String Fin,String sURLin)
	//---------------------------------------------------------------------------------
	{
		 String sDestUrl = null;
	    
		 //Error( Fin + "  " + sURLin );
		 
	    // precede met protocol indien missing
	    String sURL = sURLin.trim();
	    if( sURL.indexOf("http") != 0 ) sURL = "http://" + sURLin;
	    //
		for(int i=0;i<formlist.size();i++) 
		{
		   if( formlist.get(i).Name.trim().compareToIgnoreCase(Fin.trim()) == 0)  {
			   String sAction = formlist.get(i).Action.trim();
			   //  indien acton == #  moet je vervangen door sURL
			   if( sAction.trim().compareToIgnoreCase("#") == 0 ) return sURL + "/";   // 
			   // indien de action niet met / start plak die ervoor
			   if( (sAction.indexOf("/") != 0) && (sAction.indexOf("http") != 0) ) sAction = "/" + sAction;
			   
			   //  indien niet met http begonnen dan maken
			   if( sAction.length()> 4 ) {
					if( sAction.substring(0,4).compareToIgnoreCase("HTTP")==0)  sDestUrl = sAction; 
			   }
			   if( sDestUrl == null ) sDestUrl = sURL + sAction; 
			   if( xMSet.xU.isValidURL(sDestUrl) == false ) {
				   Error("Not a valid URL [" + sDestUrl + "]");
				   return null;
			   }
			   return sDestUrl; 
		   }
		}
		return null;
	}
	//
	//---------------------------------------------------------------------------------
	public boolean doesFieldExist(String Fin, String sField)
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<formlist.size();i++) 
		{
		   if( formlist.get(i).Name.trim().compareToIgnoreCase(Fin.trim()) == 0) {
			   for(int j=0;j<formlist.get(i).InputList.size();j++) {
				   if( formlist.get(i).InputList.get(j).Name.trim().compareToIgnoreCase(sField.trim())==0) return true;
			   }
		   }
		}
		return false;
	}
	//
	//---------------------------------------------------------------------------------
	public String getHiddenValues(String Fin)
	//---------------------------------------------------------------------------------
	{
		String sRet = "";
		int teller=0;
		for(int i=0;i<formlist.size();i++) 
		{
		   if( formlist.get(i).Name.trim().compareToIgnoreCase(Fin.trim()) == 0) {
			   for(int j=0;j<formlist.get(i).InputList.size();j++) {
				   String sVal = formlist.get(i).InputList.get(j).Value;
				   if( sVal == null ) sVal = "";  //  hidden met value = niets moet je ook meenemen
				   if( formlist.get(i).InputList.get(j).Type.trim().compareToIgnoreCase("HIDDEN") != 0 ) continue;
				   if (teller == 0 ) sRet = formlist.get(i).InputList.get(j).Name.trim();
				               else  sRet = sRet + "&" + formlist.get(i).InputList.get(j).Name.trim();
				   sRet = sRet + "=" + sVal.trim();
				   teller++;
			   }
		   }
		}
		return sRet;
	}
	
	
	public String MergeInputs(String sInputLijst , String Fin)
	{
		String sRet = "";
		int teller=0;
		for(int i=0;i<formlist.size();i++) 
		{
		   if( formlist.get(i).Name.trim().compareToIgnoreCase(Fin.trim()) == 0) {
			   for(int j=0;j<formlist.get(i).InputList.size();j++) {
				   
				   if( (formlist.get(i).InputList.get(j).Type.trim().compareToIgnoreCase("HIDDEN") != 0 ) &&
					   (formlist.get(i).InputList.get(j).Type.trim().compareToIgnoreCase("SELECT") != 0 ) &&
					   (formlist.get(i).InputList.get(j).Type.trim().compareToIgnoreCase("SEARCH") != 0 ) && // raar
					   (formlist.get(i).InputList.get(j).Type.trim().compareToIgnoreCase("TEXT") != 0 ) ) continue;
				   
				   // hebben we een value in de inputlijst ?
				   String sVal = null;
				   int aantal = xMSet.xU.TelDelims(sInputLijst,'&') + 1;
			       for(int k=0;k<=aantal;k++)
			       {
			         String ss = xMSet.xU.GetVeld(sInputLijst,(k+1),'&');
			         if( ss == null ) continue;
			         if( ss.length() == 0 ) continue;
			         if( ss.indexOf("=") < 0 ) continue;
			         String sEen  = xMSet.xU.GetVeld(ss,1,'=').trim();
			         if( sEen == null ) continue;
			         if( sEen.length() <= 0) continue;
			         String sTwee  = xMSet.xU.GetVeld(ss,2,'=').trim();
			         //
			         if( sEen.trim().compareToIgnoreCase(formlist.get(i).InputList.get(j).Name.trim()) == 0 ) {
			        	 if (sTwee == null ) sTwee="";
				         sVal = sTwee;
			        	 break;
			         }
			       }
			       
				   if( sVal == null ) sVal = formlist.get(i).InputList.get(j).Value;
				   if( sVal == null ) sVal = "";  //  hidden met value = niets moet je ook meenemen
				  
				   if (teller == 0 ) sRet = formlist.get(i).InputList.get(j).Name.trim();
				               else  sRet = sRet + "&" + formlist.get(i).InputList.get(j).Name.trim();
				   sRet = sRet + "=" + sVal.trim();
				   teller++;
			   }
		   }
		}
		return sRet;
	}
	
	//
	//---------------------------------------------------------------------------------
	public String findProxyForm()
	//---------------------------------------------------------------------------------
	{
		// Init
		for(int i=0;i<formlist.size();i++) {
			formlist.get(i).isGlyph = true;
			formlist.get(i).glyphProbability = 0;
		}
		// Excludeer de niet POST
		for(int i=0;i<formlist.size();i++) 
		{
		 if( formlist.get(i).isGlyph == false ) continue;
		 if( formlist.get(i).Method.trim().compareToIgnoreCase("POST") != 0 ) {
			 logit(5,"Excluding form [" + formlist.get(i).Name +"] : RULE method is not POST" );
			 formlist.get(i).isGlyph = false;
			 formlist.get(i).glyphProbability = -1000;
		 }
		}
		// Excludeer de forms indien niet voldaan :  slechts 1 TEXT input veld
		for(int i=0;i<formlist.size();i++) 
		{
			int textTeller=0;
			for( int j=0;j<formlist.get(i).InputList.size();j++)
			{
				if( formlist.get(i).InputList.get(j).Type.compareToIgnoreCase("TEXT") == 0 ) textTeller++;
			}
			if( textTeller != 1) {
				logit(5,"Excluding form [" + formlist.get(i).Name +"] : RULE no of more than one text field" );
				formlist.get(i).isGlyph = false;
				formlist.get(i).glyphProbability = -1000;
			}
		}
		// alleen includes/process.php?action=update schijnt nog te werken
		for(int i=0;i<formlist.size();i++) 
		{
		 if( formlist.get(i).isGlyph == false ) continue;
		 if( formlist.get(i).Action.indexOf("includes/process.php?action=update") >= 0 ) {
			 formlist.get(i).glyphProbability += 200;
		 }
		 else {
			 logit(5,"Excluding form [" + formlist.get(i).Name +"] : RULE action different from [includes/process.php?action=update]");
				formlist.get(i).isGlyph = false;
				formlist.get(i).glyphProbability = -1000; 
		 }
		}
		/*
		// indien php 25% kans
		for(int i=0;i<formlist.size();i++) 
		{
		 if( formlist.get(i).isGlyph == false ) continue;
		 if( formlist.get(i).Action.indexOf(".php") >= 0 ) formlist.get(i).glyphProbability += 25;
		}
		// indien php? 50% kans
		for(int i=0;i<formlist.size();i++) 
		{
		 if( formlist.get(i).isGlyph == false ) continue;
		 if( formlist.get(i).Action.indexOf(".php?") >= 0 ) formlist.get(i).glyphProbability += 50;
		}
		// indien process.php? 100% kans
		for(int i=0;i<formlist.size();i++) 
		{
		 if( formlist.get(i).isGlyph == false ) continue;
		 if( formlist.get(i).Action.indexOf("process.php?") >= 0 ) formlist.get(i).glyphProbability += 100;
		}
		*/
		// rapport
		for(int i=0;i<formlist.size();i++) 
		{
			String sLijn = "FORM [" + formlist.get(i).Name + "] Glyph Probability [" + formlist.get(i).glyphProbability + "]";
			logit(5,sLijn);
		}
		int max=0;
		int idx=-100000;
		for(int i=0;i<formlist.size();i++) 
		{
			 if( formlist.get(i).isGlyph == false ) continue;
			 if( formlist.get(i).glyphProbability > max ) {
				 max = formlist.get(i).glyphProbability;
				 idx = i;
			 }
		}
		if( idx < 0) {
			Error("Could not find a Glyph proxy");
			return null;
		}
		this.lastProbability = max;
		return formlist.get(idx).Name;
	}
}
