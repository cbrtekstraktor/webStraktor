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

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
	
public class webStraktorUtil {

		

		webStraktorSettings xMSet=null;
		webStraktorStringUtil xstrutl=null;
		char ctDelimFile  = '|';
		char ctDelimWoord = '-';
		char ctSlash      = '\\';
		char ctWildcard   = '*';
		String ctTempFoto = "XXX";
		int  BatTeller    = 0;
		String sInbound   = "_V9wo2WOviGXdLDI[g1C7qnjP|k6Nusz%axR:h4 YZ8JfHAQc05ytpS3TKrlBeMEFUbm!-.";
	    String sOutbound  = "Q_kaO:eA7K!PDZL-cj C46lWY9EIzfiVG1.3ubMUyJXqF0gBN5mt8odTvR2HhrSnwp|xs[%";
	    String EOL = null;
	    static final String HEXES = "0123456789ABCDEF";
		//
		//---------------------------------------------------------------------------------
		webStraktorUtil(webStraktorSettings iS)
		//---------------------------------------------------------------------------------
		{
			xMSet=iS;
			xstrutl = new webStraktorStringUtil();
			ctSlash = System.getProperty("file.separator").toCharArray()[0];
			EOL = System.getProperty("line.separator");
		}
		//
		//---------------------------------------------------------------------------------
	    void logit(String sLijn)
	    //---------------------------------------------------------------------------------
	    {
	      if( xMSet != null ) xMSet.logit(sLijn);
	    }
		//
		//---------------------------------------------------------------------------------
		public String LogStackTrace(Exception e)
		//---------------------------------------------------------------------------------
		{
		      try {
		        StringWriter sw = new StringWriter();
		        PrintWriter pw = new PrintWriter(sw);
		        e.printStackTrace(pw);
		        return sw.toString();
		      }
		      catch(Exception e2) {
		    	e.printStackTrace();
		        return "";
		      }
		} 
	    //
		//---------------------------------------------------------------------------------
		void GetSpecs()
		//---------------------------------------------------------------------------------
		{
			try {
			 InetAddress thisHost = InetAddress.getLocalHost();
		     String thisHostName = thisHost.getHostName();
		     String thisHostIPAddress = thisHost.getHostAddress();
		     String thisUserName = System.getProperty("user.name");
		     String thisOSName = System.getProperty("os.name");
			}
			catch ( Exception e) {
			}
		}
		//
		//---------------------------------------------------------------------------------
		String LPad( String sIn , int lengte )
		//---------------------------------------------------------------------------------
		{   
		    	int ii;
		    	String sZero = "";
		    	for(ii=0;ii<lengte;ii++) sZero = sZero + "0";
		    	return sZero.substring(0,lengte-sIn.length()) + sIn;
		}
		//
		//---------------------------------------------------------------------------------
		String LPadSpace( String sIn , int lengte )
		//---------------------------------------------------------------------------------
		{   
		    	int ii;
		    	String sZero = "";
		    	for(ii=0;ii<lengte;ii++) sZero = sZero + " ";
		    	return sZero.substring(0,lengte-sIn.length()) + sIn;
		}
		//
		//
		//---------------------------------------------------------------------------------
	    String RPad(String sIn, int len)
	    //---------------------------------------------------------------------------------
	    {
	      if( sIn == null ) sIn ="";
	      int j=len-sIn.length();
	      for(int i=0;i<j;i++) sIn = sIn + " ";
	      return sIn.substring(0,len);
	    }
		//
		// Extraheert de suffix uit een bestandsnaam
		//
		//---------------------------------------------------------------------------------
		String GetSuffix( String FNaam )
		//---------------------------------------------------------------------------------
		{
		  int idx =	FNaam.lastIndexOf('.');
		  if( idx < 0 ) return "";
	 	  return FNaam.substring(  idx + 1 , FNaam.length() ).toUpperCase();
		}
		//
		//
		//---------------------------------------------------------------------------------
		boolean ValueInBooleanValuePair(String sIn)
		//---------------------------------------------------------------------------------
		{
		   String sWaarde = this.GetVeld(sIn, 2 , '=');
		   if( sWaarde.trim().toUpperCase().compareTo("Y")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("YES")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("1")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("J")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("TRUE")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("ON")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("JA")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("OUI")==0 ) return true;
		   return false;	
		}
		
		//
		//---------------------------------------------------------------------------------
		int NaarInt(String sIn)
		//---------------------------------------------------------------------------------
		{
			 int ii=-1;
				
			 try {
				  ii=Integer.parseInt( sIn );
				  return ii;
				 }
				 catch ( NumberFormatException e)
				 {
					 return -1;
				 }
		}
		//
		//---------------------------------------------------------------------------------
		long NaarLong(String sIn)
		//---------------------------------------------------------------------------------
		{
			 long ll=-1;
				
			 try {
				  ll=Long.parseLong( sIn );
				  return ll;
				 }
				 catch ( NumberFormatException e)
				 {
					 return -1;
				 }
		}
		//
		//---------------------------------------------------------------------------------
		boolean IsDir( String sDir )
		//---------------------------------------------------------------------------------
		{
			try {
			 File fObj = new File(sDir);
			 if ( fObj.exists() == true )
			 {
				if ( fObj.isDirectory() == true ) return true;
			 }
			 return false;
			} catch ( Exception e ) {
				e.printStackTrace();
				return false;
			}
			
		}
		//
		//
		//---------------------------------------------------------------------------------
		boolean IsGrafisch ( String sSuffix )
		//---------------------------------------------------------------------------------
		{
		  if( sSuffix.trim().compareToIgnoreCase("JPG") == 0 ) return true;
		  if( sSuffix.trim().compareToIgnoreCase("GIF") == 0 ) return true;
		  if( sSuffix.trim().compareToIgnoreCase("PNG") == 0 ) return true;
		  if( sSuffix.trim().compareToIgnoreCase("BMP") == 0 ) return true;
		  return false;
		}
		//
		//
		//
		//---------------------------------------------------------------------------------
		String GetPathName(String sIn)
		//---------------------------------------------------------------------------------
		{
			    File fObj = new File(sIn);
			    if ( fObj.exists() == true )
			    {
				 if ( fObj.isFile() == true ) return fObj.getParent();
			    }
			    return null;
		}
		//
		//
		//
		//---------------------------------------------------------------------------------
		boolean IsBestand( String sIn )
		//---------------------------------------------------------------------------------
		{
			if( sIn == null ) return false;
			try {
			 File fObj = new File(sIn);
			 if ( fObj.exists() == true )
			 {
				if ( fObj.isFile() == true ) return true;
			 } 
			 return false;
			} catch ( Exception e ) {
				e.printStackTrace();
				return false;
			}
		}
		//
		//
		//---------------------------------------------------------------------------------
		String GetFileSpecs(String sF)
		//---------------------------------------------------------------------------------
		{   String sTemp="";
			String sSize;
			
		    File fObj = new File(sF);
		    if ( fObj.exists() == true )
		    {
			 if ( fObj.isFile() == true ) {
		    
			 sSize = "" + fObj.length();
			 sTemp = GetSuffix(sF)         + ctDelimFile +
			         fObj.getName()        + ctDelimFile +
			         //LongToDate(fObj.lastModified())   + ctDelimFile +
			         fObj.lastModified()   + ctDelimFile +
			         LPad(sSize,12)        + ctDelimFile +
			         " "                   + ctDelimFile +
			         fObj.getParent()      + ctDelimFile +
			         "";
			 }
		    } 
			return sTemp;
		}
		
		//
		//
		//---------------------------------------------------------------------------------
		String ScrubTo7BitASCIIShort( String SIn )
		//---------------------------------------------------------------------------------
		{   String sTemp = "";
			char[] SChar = SIn.toUpperCase().toCharArray();
			
			int ii;
			for(ii=0;ii<SChar.length;ii++)
			{
			  switch ( SChar[ii ])
			  {
			   //  moet makkelijker 
			   case 'É'  : { SChar[ii] = 'E'; break; }
			   case 'È'  : { SChar[ii] = 'E'; break; }
			   case 'Â'  : { SChar[ii] = 'A'; break; }
			   case 'Î'  : { SChar[ii] = 'I'; break; }
			   case 'Ê'  : { SChar[ii] = 'E'; break; }
			   case 'Ï'  : { SChar[ii] = 'I'; break; }
			   
				   
			   default   : { SChar[ii] = SChar[ii]; break; }
			  }
			}
				
			// reassemble
			for(ii=0;ii<SChar.length;ii++) sTemp = sTemp + SChar[ii];
		
			return sTemp;
		}
		//
		//
		//---------------------------------------------------------------------------------
		String ScrubTo7BitASCIILong( String SIn )
		//---------------------------------------------------------------------------------
		{   String sTemp = "";
			char[] SChar = SIn.toUpperCase().toCharArray();
			
			int ii;
			for(ii=0;ii<SChar.length;ii++)
			{
			  switch ( SChar[ii ])
			  {
			   case '.'  : { SChar[ii] = ctDelimWoord; break; }
			   case '|'  : { SChar[ii] = ctDelimWoord; break; }
			   case '&'  : { SChar[ii] = ctDelimWoord; break; }
			   case '@'  : { SChar[ii] = ctDelimWoord; break; }
			   case '#'  : { SChar[ii] = ctDelimWoord; break; }
			   case '_'  : { SChar[ii] = ctDelimWoord; break; }
			   case '\'' : { SChar[ii] = ctDelimWoord; break; }
			   case '/'  : { SChar[ii] = ctDelimWoord; break; }
			   case '('  : { SChar[ii] = ctDelimWoord; break; }
			   case ')'  : { SChar[ii] = ctDelimWoord; break; }
			   case '['  : { SChar[ii] = ctDelimWoord; break; }
			   case ']'  : { SChar[ii] = ctDelimWoord; break; }
			   case ' '  : { SChar[ii] = ctDelimWoord; break; }
			   case '{'  : { SChar[ii] = ctDelimWoord; break; }
			   case '}'  : { SChar[ii] = ctDelimWoord; break; }
			   case ','  : { SChar[ii] = ctDelimWoord; break; }
			   case '+'  : { SChar[ii] = ctDelimWoord; break; }   
			   
			   default   : { SChar[ii] = SChar[ii]; break; }
			  }
			}
				
			// reassemble
			for(ii=0;ii<SChar.length;ii++) sTemp = sTemp + SChar[ii];
		
			return sTemp;
		}
		//
		//
		//
		//---------------------------------------------------------------------------------
		String ZetDuizendMarkerLong( long iL )
		//---------------------------------------------------------------------------------
		{
			try {
			return ZetDuizendMarker( "" + iL);
			}
			catch ( Exception e ) {
				return ("error");
			}
		}		
	    //
		//
		//---------------------------------------------------------------------------------
	    String ZetDuizendMarker( String sIn )
	    //---------------------------------------------------------------------------------
	    {
	        String sTemp;
			char[] sChar;
			int ii;
			int hit = 0;
			
			sChar = sIn.toCharArray();
			sTemp = "";
			hit = 0;
			for(ii=0;ii<sChar.length;ii++) {
				if( sChar[ii] != '0' ) hit++;
				if (hit>0) sTemp=sTemp+sChar[ii];
			}
			hit=0;
			sChar = sTemp.toCharArray();
			sTemp = "";
			for(ii=sChar.length-1;ii>=0;ii--)
			{
			  	  hit++;
			  	  sTemp = sChar[ii] + sTemp;
			  	  if ( hit == 3 ) {
			  		  hit = 0;
			  		  if( ii>0 ) sTemp = "." + sTemp;  
			  	  }
			}
			return sTemp;
				
		}
		//
		// verwijder de suffix uit een bestandsnaam
		//---------------------------------------------------------------------------------
		String RemoveSuffix( String FNaam )
		//---------------------------------------------------------------------------------
		{
			try{
	 	      return FNaam.substring( 0 , FNaam.lastIndexOf('.') );
			}
			catch( IndexOutOfBoundsException e) 
			{
				return FNaam;
			}
		}
		//	 Extraheer de directory naam
		//---------------------------------------------------------------------------------
		String GetDirName( String sIn )
		//---------------------------------------------------------------------------------
		{
			return sIn.substring( sIn.lastIndexOf(ctSlash , sIn.lastIndexOf(ctSlash) - 1 ) + 1 , sIn.lastIndexOf(ctSlash));
		}
		//
		//---------------------------------------------------------------------------------
		int TelDelims ( String sIn , char ctKar )
		//---------------------------------------------------------------------------------
		{   
		    return xstrutl.TelDelims( sIn , ctKar );
		}
		// Extraheer de filenaam
		//---------------------------------------------------------------------------------
		String GetFileName( String sIn )
		//---------------------------------------------------------------------------------
		{
			return sIn.substring( sIn.lastIndexOf(ctSlash)+1 );
		}
		// Extraheer de volledige dirnaam
		//---------------------------------------------------------------------------------
		String GetFullPathName( String sIn )
		//---------------------------------------------------------------------------------
		{
			try {
				return sIn.substring( 0 , sIn.lastIndexOf(ctSlash) );
			}
			catch ( Exception e)
			{
				return null;
			}
			
		}
		//
		//---------------------------------------------------------------------------------
		String OntdubbelKarakter ( String sIn , char ctK )
		//---------------------------------------------------------------------------------
		{   String sTemp = "";
		    char[] SChar = sIn.toCharArray();
		    
			// output en ontdubbel de dubbele spaces en/of delims
			char CPrev = '\t';  // tabs lijken mij een goeie
		    
			
			for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( (SChar[ii] != CPrev) || (CPrev != ctK) ) sTemp = sTemp + SChar[ii];
				CPrev = SChar[ii];
			}		
			return sTemp;
		}
		//
		//
		//---------------------------------------------------------------------------------
		String VervangKarakter ( String sIn , char ctSource , char ctTarget )
		//---------------------------------------------------------------------------------
		{   String sTemp = "";
		    char[] SChar = sIn.toCharArray();
		    		
			for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( (SChar[ii] == ctSource) ) sTemp = sTemp + ctTarget;
				else sTemp = sTemp + SChar[ii];
			}
	//logit(sIn + "->" + sTemp);
			return sTemp;
		}
		//
		//
		//---------------------------------------------------------------------------------
		boolean IsNumeriek(String sIn)
		//---------------------------------------------------------------------------------
		{  int ii;
		
			 try
	         {  
	            ii = Integer.parseInt(sIn);
	            ii++; // om de warning te verwijderen
	         }
	         catch (NumberFormatException e)
	         {  
	            	 return false;
	         }
	         return true;
	            
		}
		//
		//
		//---------------------------------------------------------------------------------
		void LijstBubbleSort( ArrayList<String> lijst , ArrayList<String> lijstSorted , int iKol , boolean richtingUp )
		//---------------------------------------------------------------------------------
		{
			String sMin, sVeld;
			int hit, MaxIter;
			int ii,jj;
	//logit("==============sort on " + iKol + richtingUp);	
	//for(int i=0;i<lijst.size();i++) logit(lijst.get(i));
			// sorteren - standaard BUBBLE
			MaxIter = lijst.size();
			
			for(ii=0;ii<MaxIter;ii++)
			{
				hit = 0;
				sMin = GetVeld(lijst.get(hit).toString(),iKol,'|');
				
				for(jj=0;jj<lijst.size();jj++) 
				{
				  sVeld = GetVeld(lijst.get(jj).toString(),iKol,'|');
				  if( richtingUp ) {
				   if( sVeld.compareTo( sMin ) < 0 ) {
					   sMin = sVeld;
					   hit=jj;
				   }
				  }
				  else {
					  if( sVeld.compareTo( sMin ) > 0 ) {
						   sMin = sVeld;
						   hit=jj;
					   }  
				  }
				}
				lijstSorted.add(lijst.get(hit));
				lijst.remove(hit);
			}
	//logit("==================sort on " + iKol + richtingUp);	
	//for(int i=0;i<lijstSorted.size();i++) logit(lijstSorted.get(i));
			return;
		}
		//
		//
		//---------------------------------------------------------------------------------
		String GetVeld( String sIn , int idx , char delim )
		//---------------------------------------------------------------------------------
		{ 
		  return xstrutl.GetVeld( sIn , idx , delim );
		}
		//
		//---------------------------------------------------------------------------------
		String Remplaceer( String sIn , String sPattern , String sReplace )
		// ---------------------------------------------------------------------------------
		{
			 return xstrutl.Remplaceer( sIn , sPattern , sReplace );
		}
		//
		//---------------------------------------------------------------------------------
		String RemplaceerIgnoreCase( String sIn , String sPattern , String sReplace )
		// ---------------------------------------------------------------------------------
		{
			return xstrutl.RemplaceerIgnoreCase( sIn , sPattern , sReplace );
		}
		//
		//---------------------------------------------------------------------------------
		String RemplaceerNEW( String sIn , String sPattern , String sReplace )
		// ---------------------------------------------------------------------------------
		{   
			return xstrutl.RemplaceerNEW( sIn , sPattern , sReplace );
		}
		//
		//---------------------------------------------------------------------------------
		String GetFirstWord( String sIn , char cDelim) // inclusief leading and trailing spaces
		// ---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] SChar = sIn.toCharArray();
		    	
		   
		    boolean woordstart=false;
		    int woordteller=0;
			for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( (SChar[ii] != cDelim) ) {
					if( woordstart == false ) {
						if( woordteller > 0) return sTemp;
						woordstart = true; woordteller++;
					}
				}
				if ( (SChar[ii] == cDelim) ) {
					if( woordstart = true ) woordstart = false; 
				}
				sTemp = sTemp + SChar[ii];
			}		
			return sTemp;
		}
		//
		//---------------------------------------------------------------------------------
		String RemoveLeadingSpaces( String sIn ) 
		// ---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] SChar = sIn.toCharArray();
		    	
		   
		    boolean woordstart=false;
		    char cDelim = ' ';
			for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( (SChar[ii] != cDelim) ) {
					woordstart = true; 
			    }
				if ( (SChar[ii] == cDelim) ) {
					if( woordstart = false ) continue; 
				}
				sTemp = sTemp + SChar[ii];
			}		
			return sTemp;
		}
		//
		//---------------------------------------------------------------------------------
	    boolean IsThisAnArchive(String FNaam)
	    //---------------------------------------------------------------------------------
	    {
	    	  String sSuf = this.GetSuffix(FNaam).trim().toUpperCase();
	    	  boolean isArchief=false;
	    	  if( sSuf.compareToIgnoreCase("RAR")==0) isArchief = true;
		      if( sSuf.compareToIgnoreCase("ZIP")==0) isArchief = true;
		      if( sSuf.compareToIgnoreCase("CBR")==0) isArchief = true;
		      if( sSuf.compareToIgnoreCase("CBZ")==0) isArchief = true;
		      return isArchief;
	    }
	   //
		//---------------------------------------------------------------------------------
		void copyFile(String sIn , String sOut) throws IOException 
		//---------------------------------------------------------------------------------
		{
			
			   InputStream in = null;
			   OutputStream out = null; 
			   byte[] buffer = new byte[16384];
			   try {
			      in = new FileInputStream(sIn);
			      out = new FileOutputStream(sOut);
			      while (true) {
			         synchronized (buffer) {
			            int amountRead = in.read(buffer);
			            if (amountRead == -1) {
			               break;
			            }
			            out.write(buffer, 0, amountRead); 
			         }
			      } 
			   } finally {
			      if (in != null) {
			         in.close();
			      }
			      if (out != null) {
			    	 out.flush();
			         out.close();
			      }
			   }
		    
		}
		//
		//---------------------------------------------------------------------------------
		boolean VerwijderBestand( String sIn)
		//---------------------------------------------------------------------------------
		{
	        File FObj = new File(sIn);
	        if ( FObj.isFile() != true ) {
	        	logit( "ERROR '" + sIn + ") -> file not found");
	        	return false;
	        }
	        if ( FObj.getAbsolutePath().length() < 10 ) {
	        	logit( sIn + "->lijkt mij geen goed idee om te schrappen");
	        	return false;  // domme veiligheid
	        }
	        FObj.delete();
	        File XObj = new File(sIn);
	        if ( XObj.isFile() == true ) {
	        	logit("ERROR" + sIn+ " -> could not be deleted");	
	        }
	        return true;
		}
		//
		//---------------------------------------------------------------------------------
		void LijstOpkuisen( ArrayList<String> lijst )
		//---------------------------------------------------------------------------------
		{
			  int iMax = lijst.size();
			  int ii;
		      for(ii=0;ii<iMax;ii++) lijst.remove(0);
		}
		//
		//---------------------------------------------------------------------------------
		String GetFileNameViaWildCard( String sIn )
		//---------------------------------------------------------------------------------
		{
			File fle = new File(sIn);
			String sDir = fle.getParent();
			String sFle = this.VerwijderLeestekens(fle.getName());
			File  dirObj = new File( sDir );
			{
				if ((dirObj.exists() == true)  ) {
					if (dirObj.isDirectory() == true) {
						File [] fileList = dirObj.listFiles();
						for (int i = 0; i < fileList.length; i++) {
							if (fileList[i].isDirectory()) continue;
							if (fileList[i].isFile()) {
								
								if ( fileList[i].getName().length() > ctTempFoto.length() ) {
								  // exclusief de files die ComixImag beginnen natuurlijk
								  if( fileList[i].getName().substring(0,ctTempFoto.length()).compareToIgnoreCase(ctTempFoto) == 0 ) {
									 continue;	
								  }
								}
								//System.out.println("->" + this.VerwijderLeestekens(fileList[i].getName()));
								if ( sFle.compareTo( this.VerwijderLeestekens(fileList[i].getName()))==0 ) 
									 return sDir + ctSlash + fileList[i].getName();
							}
						}
					}
				}
			}		
			//
			// Niets gevonden -> ga dan louter nog zoeken op nummers
			//
			{
				sFle = this.HoudCijfers(fle.getName());
				if ((dirObj.exists() == true)  ) {
					if (dirObj.isDirectory() == true) {
						File [] fileList = dirObj.listFiles();
						for (int i = 0; i < fileList.length; i++) {
							if (fileList[i].isDirectory()) continue;
							if (fileList[i].isFile()) {
								if ( fileList[i].getName().length() > ctTempFoto.length() ) {
									  if( fileList[i].getName().substring(0,ctTempFoto.length()).compareToIgnoreCase(ctTempFoto) == 0 ) {
										 continue;	
									  }
									}
								if ( sFle.compareTo( this.HoudCijfers(fileList[i].getName()))==0 ) 
									 return sDir + ctSlash + fileList[i].getName();
							}
						}
					}
				}
			}		
						
			logit("ERROR No match for " + sIn + " (" +sDir + ") (" + sFle + ")");
			return null;
		}
		
		//
		//---------------------------------------------------------------------------------
		String NaarWildCards(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] SChar = sIn.toCharArray();
		    for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( SChar[ii] > 'z' ) sTemp = sTemp + ctWildcard;
				else sTemp = sTemp + SChar[ii];
			}		
			return sTemp;
		    	
		}
		//
		//
		//---------------------------------------------------------------------------------
		String EersteLijn(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] SChar = sIn.toCharArray();
		    for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( (SChar[ii] == '\n') || (SChar[ii] == '\r') ) break;
				sTemp = sTemp + SChar[ii];
			}		
			return sTemp;
		}
		//
		//
		//---------------------------------------------------------------------------------
		String NaarCijferWildCards(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] SChar = sIn.toCharArray();
		    for(int ii=0;ii<SChar.length;ii++) 
			{	
		    	if ( ((SChar[ii] >= '0') && (SChar[ii] <= '9')) ||
						//((SChar[ii] == ' ') || (SChar[ii] == '_') || (SChar[ii] == '.' ) ||
						  ((SChar[ii] == '_') || (SChar[ii] == '.' ) ||
						  (SChar[ii] == '\\') ||(SChar[ii] == '/')) ) sTemp = sTemp + SChar[ii];
					else sTemp = sTemp + ctWildcard;
			}		
			return this.OntdubbelKarakter( sTemp , ctWildcard);
		    	
		}
		//
		//---------------------------------------------------------------------------------
		String VerwijderLeestekens(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] SChar = sIn.toCharArray();
		    for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( ((SChar[ii] >= '0') && (SChar[ii] <= '9')) ||
					 ((SChar[ii] >= 'A') && (SChar[ii] <= 'Z')) ||
					 ((SChar[ii] >= 'a') && (SChar[ii] <= 'z')) ||
					 ((SChar[ii] == ' ') || (SChar[ii] == '_') || (SChar[ii] == '.' ) ||
					  (SChar[ii] == '\\') ||(SChar[ii] == '/')) ) sTemp = sTemp + SChar[ii];
				else sTemp = sTemp + ctWildcard;
			}		
			return this.OntdubbelKarakter( sTemp , ctWildcard);
		    	
		}
		//
		//
		//---------------------------------------------------------------------------------
		String HoudCijfers(String sIn)  // opgepast dient alleen voor de getFiles
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] SChar = sIn.toCharArray();
		    for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( ((SChar[ii] >= '0') && (SChar[ii] <= '9')) ||
					//((SChar[ii] == ' ') || (SChar[ii] == '_') || (SChar[ii] == '.' ) ||
					  ((SChar[ii] == '_') || (SChar[ii] == '.' ) ||
					  (SChar[ii] == '\\') ||(SChar[ii] == '/')) ) sTemp = sTemp + SChar[ii];
				else sTemp = sTemp + ctWildcard;
			}		
			return this.OntdubbelKarakter( sTemp , ctWildcard);
		    	
		}
		//
		//
		//---------------------------------------------------------------------------------
		String keepDigits(String sIn)  
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] SChar = sIn.toCharArray();
		    for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( (SChar[ii] >= '0') && (SChar[ii] <= '9') ) sTemp = sTemp + SChar[ii];
			}		
			return sTemp;
		}
		//
		//
		//---------------------------------------------------------------------------------
		String keepDecimals(String sIn)  
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] SChar = sIn.toCharArray();
		    for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( ((SChar[ii] >= '0') && (SChar[ii] <= '9')) || (SChar[ii]=='.') ) sTemp = sTemp + SChar[ii];
			}		
			return sTemp;
		}
		//
		//---------------------------------------------------------------------------------
		String StripLetters(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] SChar = sIn.toCharArray();
		    for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( ((SChar[ii] >= '0') && (SChar[ii] <= '9')) ) sTemp = sTemp + SChar[ii];
			}		
			return sTemp;
		    	
		}
		//
		//---------------------------------------------------------------------------------
		boolean HasFrenchChars(String sIn)
		//---------------------------------------------------------------------------------
		{
			char[] SChar = sIn.toCharArray();
			for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( SChar[ii] > 'z' ) return true;
			}		
			return false;
		}
		//
		//
		//---------------------------------------------------------------------------------
		boolean HasWildCard(String sIn)
		//---------------------------------------------------------------------------------
		{
			char[] SChar = sIn.toCharArray();
		    for(int ii=0;ii<SChar.length;ii++) 
			{	
				if ( SChar[ii] == ctWildcard ) return true;
			}		
			return false;
		}
		
	    //
		//---------------------------------------------------------------------------------
		ArrayList<String> GetFilesInDir( String sDirName , String sPatroon)
		//---------------------------------------------------------------------------------
		{
			ArrayList<String> sLijst = new ArrayList<String>();
			File  dirObj = new File( sDirName );
			{
				if ((dirObj.exists() == true)  ) {
					if (dirObj.isDirectory() == true) {
						File [] fileList = dirObj.listFiles();
						for (int i = 0; i < fileList.length; i++) {
							if (fileList[i].isDirectory()) continue;
							if (fileList[i].isFile()) {
								if( sPatroon != null ) {
								  if ( fileList[i].getName().length() >= sPatroon.length() ) {
									if ( fileList[i].getName().substring(0,sPatroon.length()).compareToIgnoreCase(sPatroon)!=0) continue;
									sLijst.add(fileList[i].getName());
								  }
								} else {
									sLijst.add(fileList[i].getName());
								}
							}
						}
					}
				}
			}		
			return sLijst;
		}
		//
		//---------------------------------------------------------------------------------
		ArrayList<String> GetDirsInDir( String sDirName , String sPatroon)
		//---------------------------------------------------------------------------------
		{
			ArrayList<String> sLijst = new ArrayList<String>();
			File  dirObj = new File( sDirName );
			{
				if ((dirObj.exists() == true)  ) {
					if (dirObj.isDirectory() == true) {
						File [] fileList = dirObj.listFiles();
						for (int i = 0; i < fileList.length; i++) {
							if (fileList[i].isFile()) continue;
							if (fileList[i].isDirectory()) {
								if( sPatroon != null ) {
								  if ( fileList[i].getName().length() >= sPatroon.length() ) {
									if ( fileList[i].getName().substring(0,sPatroon.length()).compareToIgnoreCase(sPatroon)!=0) continue;
									sLijst.add(fileList[i].getName());
								  }
								} else {
									sLijst.add(fileList[i].getName());
								}
							}
						}
					}
				}
			}		
			return sLijst;
		}
		//
		//---------------------------------------------------------------------------------
		ArrayList<String> GetDirsInDir( String sDirName )
		//---------------------------------------------------------------------------------
		{
			return GetDirsInDir( sDirName , null);
		}
		//
		//---------------------------------------------------------------------------------
		ArrayList<String> GetFilesInDirRecursive( String sDirName , String sPatroon)
		//---------------------------------------------------------------------------------
		{
			ArrayList<String> sLijst = new ArrayList<String>();
			File  dirObj = new File( sDirName );
			{
				if ((dirObj.exists() == true)  ) {
					if (dirObj.isDirectory() == true) {
						File [] fileList = dirObj.listFiles();
						for (int i = 0; i < fileList.length; i++) {
							// Afdalen
							if (fileList[i].isDirectory()) {
								ArrayList<String> xL = GetFilesInDirRecursive( fileList[i].getAbsolutePath() , sPatroon);
								for(int k=0;k<xL.size();k++) sLijst.add(xL.get(k));
							}
							if (fileList[i].isFile()) {
								if( sPatroon != null ) {
								  if ( fileList[i].getName().length() >= sPatroon.length() ) {
									if ( fileList[i].getName().substring(0,sPatroon.length()).compareToIgnoreCase(sPatroon)!=0) continue;
									sLijst.add(fileList[i].getAbsolutePath());
								  }
								} else {
									sLijst.add(fileList[i].getAbsolutePath());
								}
							}
						}
					}
				}
			}		
			return sLijst;
		}
		//
		//---------------------------------------------------------------------------------
		ArrayList<String> GetFilesInDir( String sDirName )
		//---------------------------------------------------------------------------------
		{
			return GetFilesInDir( sDirName , null);
		}
		//
		//---------------------------------------------------------------------------------
		void GetOldestFilesInDir( String sDirName , int diepte , ArrayList<String> sTerug , int keep)
		//---------------------------------------------------------------------------------
		{
			ArrayList<String> sLijst = new ArrayList<String>();
			
			File  dirObj = new File( sDirName );
			{
				if ((dirObj.exists() == true)  ) {
					if (dirObj.isDirectory() == true) {
						File [] fileList = dirObj.listFiles();
						for (int i = 0; i < fileList.length; i++) {
							if (fileList[i].isDirectory()) continue;
							if (fileList[i].isFile()) {
								File fObj = new File(sDirName + this.ctSlash + fileList[i].getName());
								if( fObj.isFile() == true ) {
									sLijst.add(sDirName + this.ctSlash + fObj.getName()+ctDelimFile+fObj.lastModified());
								}
							}
						}
					}
				}
			}	
			//
		    // Sorteer
			ArrayList<String> sTemp = new ArrayList<String>();
			this.LijstBubbleSort(sLijst, sTemp, 2, false); // omgekeerd sorteren
			int verwijder = sTemp.size() - diepte;  // dit is ook wat er zal blijven aan bestanden
			if( verwijder < keep ) return;
			if( verwijder > sTemp.size()) verwijder = sTemp.size();
			for(int i=0;i<verwijder;i++) sTemp.remove(0);
			for(int i=0;i<sTemp.size();i++) sTerug.add(this.GetVeld(sTemp.get(i),1, ctDelimFile));
	    	return;
		}
		//---------------------------------------------------------------------------------
		void VerwijderOldestFilesInDir( String sDirName , int diepte , int keep)
		//---------------------------------------------------------------------------------
		{
			ArrayList<String> sLijst = new ArrayList<String>();
			GetOldestFilesInDir( sDirName , diepte , sLijst , keep);
			for(int i=0;i<sLijst.size();i++) {
				logit("Purging [" + sLijst.get(i) +"]");
				this.VerwijderBestand(sLijst.get(i));
			}
		}
		//
		//
		// ---------------------------------------------------------------------------------
		void KuisDirOp(String sDir , String sPatroon)
		// ---------------------------------------------------------------------------------
		{
			logit("Purging filepattern [" + sPatroon + "] from directory [" + sDir +"]");
			ArrayList<String> lstFiles = new ArrayList<String>();
			lstFiles = GetFilesInDir(sDir , sPatroon);
			for(int i=0;i<lstFiles.size();i++) {
				String FNaam = sDir + this.ctSlash + lstFiles.get(i);
				logit("Removing cached file [" + FNaam + "]");
				this.VerwijderBestand(FNaam);
			}
		}
		//
		//---------------------------------------------------------------------------------
		String BestaatGrafischBestand(String sPref)
		//---------------------------------------------------------------------------------
		{
			 String TestNaam;
			 TestNaam = sPref + ".jpg"; if ( this.IsBestand( TestNaam) == true ) return TestNaam; 
			 TestNaam = sPref + ".gif"; if ( this.IsBestand( TestNaam) == true ) return TestNaam; 
			 TestNaam = sPref + ".png"; if ( this.IsBestand( TestNaam) == true ) return TestNaam; 
			 return "";	
		}
		
		//
		//---------------------------------------------------------------------------------
		String StripTrailingSlash(String sIn)
		//---------------------------------------------------------------------------------
		{
		  String sTemp = sIn;	
		  String sLast = sIn.substring(sIn.length()-1);
		  if( sTemp.length()>1) {
		   if( sLast.compareToIgnoreCase("\\")==0) sTemp = sIn.substring(0,sIn.length()-1);
		   //logit("----->" + sIn + "  " + sTemp);
		  }
		  return sTemp;	
		}
		//
		//---------------------------------------------------------------------------------
		int GetNearestPowerTwo(int iIn)
		//---------------------------------------------------------------------------------
		{
		  int iRet=(1024*64);
		  int iNear = 1;
		  for(int i=0;i<17;i++)
		  {
			  iNear = iNear * 2;
			  if( iNear >= iIn ) return iNear;
		  }
		  return iRet;	
		}
		//
		//---------------------------------------------------------------------------------
		String SlashIt(String sIn)
		//---------------------------------------------------------------------------------
		{
		   String sOut = this.VervangKarakter(sIn, '\\', ctSlash);
		   sOut = this.VervangKarakter(sIn, '/', ctSlash);
		   sOut = this.OntdubbelKarakter(sOut, ctSlash);
		   //sOut = this.VervangKarakter(sIn, ctSlash , '/'); -- DEBUG
		   return sOut.trim();	
		}
		//
		//---------------------------------------------------------------------------------
		String CBNEncrypt(String sIn)
		//---------------------------------------------------------------------------------
		{
		   String sTemp="";
		   int jj;
		   int MaxLen=sInbound.length();
		   for(int ii=0;ii<sIn.length();ii++)
		   {
			   jj = sInbound.indexOf(sIn.charAt(ii) );
			   if( (jj<0)||(jj>=MaxLen)) {
				   sTemp = sTemp + sIn.charAt(ii);
			   }
			   else {
				   jj = ( jj + ii ) % MaxLen;
				   sTemp = sTemp + sOutbound.charAt(jj);
			   }
		   }
		   return sTemp;	
		}
		//
		//---------------------------------------------------------------------------------
		String CBNDecrypt(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp="";
			int jj;
			int MaxLen=sInbound.length();
			for(int ii=0;ii<sIn.length();ii++)
			{
			   jj = sOutbound.indexOf(sIn.charAt(ii) );
			   if( (jj<0)||(jj>=MaxLen)) {
				   sTemp = sTemp + sIn.charAt(ii);
			   }
			   else {
				   jj = ( jj - ii ) % MaxLen;
				   if (jj < 0) jj += MaxLen;
				   sTemp = sTemp + sInbound.charAt(jj);
			   }
			}
			return sTemp;	
		}   
		//
		//---------------------------------------------------------------------------------
		String HTMLEncode(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] sChar = sIn.toCharArray();
		    for(int ii=0;ii<sChar.length;ii++) 
			{	
		    	switch( sChar[ii])
		    	{
		    	   case ' '  : {sTemp = sTemp + "&#32;"; break; }
		    	   case '\\' : {sTemp = sTemp + "&#92;"; break; }
		    	   case '/'  : {sTemp = sTemp + "&#47;"; break; }
		           default    : {sTemp = sTemp + sChar[ii]; break; }
		    	}
		 	}		
		    return sTemp;    	
		}
		//---------------------------------------------------------------------------------
		String toTitleCase(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] sChar = sIn.toCharArray();
		    boolean Up=true;
		    for(int ii=0;ii<sChar.length;ii++) 
			{	
		    	if( sChar[ii] == ' ') { Up = true; sTemp = sTemp + " "; continue; }
		    	if( Up == true ) { sTemp = sTemp + (""+sChar[ii]).toUpperCase(); Up = false; continue;}
		    	sTemp = sTemp + (""+sChar[ii]).toLowerCase();
		 	}		
		    return sTemp;    	
		}
		//---------------------------------------------------------------------------------
		String toTitleCaseSimple(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] sChar = sIn.toCharArray();
		    int teller=0;
		    for(int ii=0;ii<sChar.length;ii++) 
			{	
		    	if( sChar[ii] != ' ') teller++;
		    	if( teller == 1 ) { sTemp = sTemp + (""+sChar[ii]).toUpperCase(); continue;}
		    	sTemp = sTemp + sChar[ii];
		 	}		
		    return sTemp;    	
		}
		//
		//---------------------------------------------------------------------------------
		String KortTekstIn(String sIn)
		//---------------------------------------------------------------------------------
		{
		   String sTemp = sIn;
		   if( sTemp == null ) return "";
		   if( sTemp.length() > 35 ) sTemp = sTemp.substring(0,10) + " ... " + sTemp.substring(sIn.length()-20);
		   return sTemp;	
		}
		//
		//---------------------------------------------------------------------------------
		void ShowAllThreads()
	    //---------------------------------------------------------------------------------
		{
			ThreadGroup current_thread_group;
			ThreadGroup root_thread_group;
			ThreadGroup parent;
			
			// Zoek de root
			current_thread_group = Thread.currentThread().getThreadGroup();
			root_thread_group = current_thread_group;
			parent = root_thread_group.getParent();
			while( parent != null )
			{
				root_thread_group = parent;
				parent = parent.getParent();
			}
			// toon nu recursief onderliggende
			logit("=============================================================================");
			printGroupInfo(root_thread_group,"-");
			logit("=============================================================================");
		}
		//
		//---------------------------------------------------------------------------------
		void printGroupInfo(ThreadGroup g, String indent)
		//---------------------------------------------------------------------------------
		{
			if( g == null ) return;
			int num_threads = g.activeCount();
			int num_groups = g.activeGroupCount();
			Thread[] threads = new Thread[num_threads];
			ThreadGroup[] groups = new ThreadGroup[num_groups];
			
			g.enumerate(threads,false);
			g.enumerate(groups,false);
			
			logit(indent + "Thread group: " + g.getName() + " Max priority: " + g.getMaxPriority() + (g.isDaemon()?" Deamon":""));
			for(int i=0;i<num_threads;i++) printThreadInfo(threads[i], indent + "--");
			for(int i=0;i<num_groups;i++) printGroupInfo(groups[i], indent + "--");
		}
		//
		//---------------------------------------------------------------------------------
		void printThreadInfo(Thread t, String indent)
		//---------------------------------------------------------------------------------
		{
			if( t == null ) return;
			logit(indent + "Thread: "+ t.getName() + " Priority: " + t.getPriority() + (t.isDaemon()?" Daemon":"") + (t.isAlive()?" Alive":"Not alive") );
		}
		//
		//---------------------------------------------------------------------------------
		String ReadContentFromFile(String FNaam, int MaxLines)
		//---------------------------------------------------------------------------------
		{
			String sRet= "";
			int teller=0;
			try {
			  File inFile  = new File(FNaam);  // File to read from.
	       	  BufferedReader reader = new BufferedReader(new FileReader(inFile));
	       	  //BufferedWriter writer = null;
	       	  String sLijn = null;
	          while ((sLijn=reader.readLine()) != null) {
	        	teller++; if( teller > 1) sRet = sRet + "\n";
	        	sRet = sRet + sLijn;
	        	if( teller > MaxLines ) {
	        		sRet = sRet + "\n\n --> Maximum  number [" + MaxLines + "] of display lines has been reached. \n --> Use alternative editor to view file [" + FNaam + "]";
	        		break;
	        	}
	          }
	          reader.close();
	          return ( sRet );
			}
			catch (Exception e) {
				return ("Error reading file [" + FNaam + "]");
		    }
		}
		//
		//---------------------------------------------------------------------------------
		boolean CreateDirectory(String sDirNaam)
		//---------------------------------------------------------------------------------
		{
			if( this.IsDir( sDirNaam ) ) return true; // bestaat
			boolean success = (new File(sDirNaam)).mkdir();
			if( success == true ) return this.IsDir( sDirNaam );
			return false;
		}
		//
		//---------------------------------------------------------------------------------
		String SpaceToNBSP(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] sChar = sIn.toCharArray();
		    for(int ii=0;ii<sChar.length;ii++) 
			{	
		    	if( sChar[ii] == ' ') sTemp = sTemp + "&nbsp;";
		    	else sTemp = sTemp + sChar[ii];
		 	}		
		    return sTemp;    	
		}
		//---------------------------------------------------------------------------------
		int CountDecimals(String sIn)
		//---------------------------------------------------------------------------------
		{
			char[] sChar = sIn.trim().toCharArray();
		    int ndecs=0;
		    boolean found=false;
		    for(int ii=0;ii<sChar.length;ii++) 
			{	
		    	if( sChar[ii] == '.') { found=true; continue;}
		    	if( found ) ndecs++;
		 	}		
		    return ndecs;
		}
		//
		//---------------------------------------------------------------------------------
		String ExpandQuote(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] sChar = sIn.toCharArray();
		    for(int ii=0;ii<sChar.length;ii++) 
			{	
		    	if( sChar[ii] == (char)'\'') sTemp = sTemp + "''";
		    	else sTemp = sTemp + sChar[ii];
		 	}		
		    return sTemp;    	
		}
		//
		//---------------------------------------------------------------------------------
		String ExpandSlash(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
		    char[] sChar = sIn.toCharArray();
		    for(int ii=0;ii<sChar.length;ii++) 
			{	
		    	if( sChar[ii] == (char)'\\') sTemp = sTemp + "\\\\";
		    	else sTemp = sTemp + sChar[ii];
		 	}		
		    return sTemp;    	
		}
		//
		//---------------------------------------------------------------------------------
		String HexNaarString(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp="";
		    char[] sChar = sIn.toCharArray();
		    
		    for(int ii=0;ii<sChar.length/2;ii++) 
			{	
		    	int een, twee;
		    	char cc = sChar[ii*2];
		    	if( (cc>='0')&&(cc<='9')) een = cc - '0'; else een = cc - 'A' + 10; 
		    	cc = sChar[(ii*2)+1];
		    	if( (cc>='0')&&(cc<='9')) twee = cc - '0'; else twee = cc - 'A' + 10; 
		    	int ord = (een*16)+twee;
		    	sTemp = sTemp + (char)ord;
		 	}		
		    return sTemp; 
		}
		//
		//---------------------------------------------------------------------------------
		int HexNaarInt(String sIn)
		//---------------------------------------------------------------------------------
		{
			    char[] sChar = sIn.toCharArray();
			    int ord=0;    
			    for(int ii=0;ii<sChar.length/2;ii++) 
				{	
			    	int een, twee;
			    	char cc = sChar[ii*2];
			    	if( (cc>='0')&&(cc<='9')) een = cc - '0'; else een = cc - 'A' + 10; 
			    	cc = sChar[(ii*2)+1];
			    	if( (cc>='0')&&(cc<='9')) twee = cc - '0'; else twee = cc - 'A' + 10; 
			    	ord = (een*16)+twee;
			   }		
			    return ord;  
		}
		
		//
		//---------------------------------------------------------------------------------
		String GetSavePathName(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp="";
		    char[] sChar = sIn.toCharArray();
		    int ll=-1;
		    for(int i=0;i<sChar.length;i++)
		    {
		    	if( sChar[i] == this.ctSlash) ll=i;
		    }
		    if( ll < 0 ) return null;
		    for(int i=0;i<ll;i++)
		    {
		    	sTemp = sTemp + sChar[i];
		    }
		    return sTemp;
		}
		//
		//---------------------------------------------------------------------------------
		String naarSaveXML(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp = "";
			if( sIn == null ) return sTemp;
			String sX = this.RemplaceerNEW(sIn,"&nbsp;"," ");  // nbsp is geen geldige XML
		    char[] sChar = sX.toCharArray();
		    for(int ii=0;ii<sChar.length;ii++) 
			{	
		    	if( sChar[ii] == '<')  { sTemp = sTemp + "&lt;"; continue; }
		    	if( sChar[ii] == '>')  { sTemp = sTemp + "&gt;"; continue; }
		    	//if( sChar[ii] == '&')  { sTemp = sTemp + "&amp;"; continue; }  // vewijderd er vervangen door ontzieInternFormaat
		    	if( sChar[ii] == '"')  { sTemp = sTemp + "&quot;"; continue; }
		    	if( sChar[ii] == '\'') { sTemp = sTemp + "&apos;"; continue; }
		    	sTemp = sTemp + sChar[ii];
		 	}	
		    // & naar &amp , doch niet de sekwenties &#0xnn;
		    sTemp = ontzieInternFormaat(sTemp);
		    return sTemp;    	
		}
		//
		//---------------------------------------------------------------------------------
		String ontzieInternFormaat(String sIn)
		//---------------------------------------------------------------------------------
		{
			String sTemp="";
			char[] sChar = sIn.toCharArray();
			int lengte=sChar.length;
			for(int ii=0;ii<lengte;ii++) 
			{	
			  if ( sChar[ii] != '&' ) { sTemp = sTemp + sChar[ii]; continue; }
			  if ( (ii+6) >= lengte ) { sTemp = sTemp + sChar[ii]; continue; }
			  if ( (sChar[ii+1]=='#') && (sChar[ii+2]=='0') && (sChar[ii+3]=='x') && (sChar[ii+6]==';') ) { sTemp = sTemp + sChar[ii]; continue; }
			  sTemp = sTemp + "&amp;";
			}
			return sTemp;
		}
		//
		//---------------------------------------------------------------------------------
		String vervangtussenDubbelQuotes(String sIn,char cSrc,char tSrc)
		//---------------------------------------------------------------------------------
		{
			 String sRet = "";
			 char[] buf = sIn.toCharArray();
			 int aantal = buf.length;
			 char prev='\0';
			 boolean dq=false;
			 for(int i=0;i<aantal;i++)
			 {
				 if( buf[i] == '"' ) {
					 // alleen maar togglen indien geen \"
					 if( dq == false ) {
						 if( prev != '\\' ) dq = true;
					 }
					 else { 
						if( prev != '\\' ) dq = false; 
					 }
					 //if( dq ) dq = false;
					 //	 else dq = true;
					 sRet = sRet + "\"";
					 prev = buf[i];
					 continue;
				 }
				 if( (dq==true) && (buf[i]==cSrc)) {
					 sRet = sRet + tSrc;
					 prev = buf[i];
					 continue;
				 }
				 sRet = sRet + buf[i];  
				 prev = buf[i];
			 }
			 return sRet;
		}
		//
		//---------------------------------------------------------------------------------
		String vervangtussenHaakjes(String sIn,char cSrc,char tSrc)
		//---------------------------------------------------------------------------------
		{
			 String sRet = "";
			 char[] buf = sIn.toCharArray();
			 int aantal = buf.length;
			 boolean dq=false;
			 for(int i=0;i<aantal;i++)
			 {
				 if( buf[i] == '(' ) {
					 dq = true;
					 sRet = sRet  + "(";
					 continue;
				 }
				 if( buf[i] == ')' ) {
					 dq = false;
					 sRet = sRet  + ")";
					 continue;
				 }
				 if( (dq==true) && (buf[i]==cSrc)) {
					 sRet = sRet + tSrc;
					 continue;
				 }
				 sRet = sRet + buf[i];  
			 }
			 return sRet;
		}
		//
		//---------------------------------------------------------------------------------
		String vervangtussenCommandQuotes(String sIn,char cSrc,char tSrc)
		//---------------------------------------------------------------------------------
		{    // tussen ( en )  vervang je tussen quotes x door y   vb  getfield(1,";") wordt getfield(1,"$")
			 String sRet = "";
			 char[] buf = sIn.toCharArray();
			 int aantal = buf.length;
			 boolean dq=false;
			 boolean inner=false;
			 char prev = '\0';
			 for(int i=0;i<aantal;i++)
			 {
				 if( buf[i] == '(' ) {
					 dq = true;
					 sRet = sRet  + "(";
					 prev = buf[i];
					 inner = false;
					 continue;
				 }
				 if( buf[i] == ')' ) {
					 dq = false;
					 sRet = sRet  + ")";
					 prev = buf[i];
					 continue;
				 }
				 if( (dq==true) && (inner==false) && (buf[i]== '"') && (prev != '\\') ) {
					 inner = true;
					 sRet = sRet + buf[i];
					 prev = buf[i];
					 continue;
				 }
				 if( (dq==true) && (inner==true) && (buf[i]== '"') && (prev != '\\') ) {
					 inner = false;
					 sRet = sRet + buf[i];
					 prev = buf[i];
					 continue;
				 }
				 if( (dq==true) && (inner==true) && (buf[i]==cSrc)) {
					 sRet = sRet + tSrc;
					 prev = buf[i];
					 continue;
				 }
				 sRet = sRet + buf[i];  
				 prev = buf[i];
			 }
	         //System.out.println("" + sIn + " -> " + sRet);
			 return sRet;
		}
		//
		//---------------------------------------------------------------------------------
		String VervangBuitenInternFormaat(String sIn , char cSrc, char cTgt)
		//---------------------------------------------------------------------------------
		{
	       String sRet = "";
	       char[] buf = sIn.toCharArray();
		   int aantal = buf.length;
		   for(int i=0;i<aantal;i++)
		   {
			   //
			   if( buf[i] == '&' ) {
				   if( (i+6) <= (aantal-1) ) {
					   if( (buf[i+1]=='#') && (buf[i+2] == '0') && (buf[i+3]=='x') && (buf[i+6]==';')) {
						   sRet = sRet + buf[i];
						   continue;
					   }
				   }
			   }
			   //
			   if( buf[i] == '#') {
				   if( (i>0) && ((i+5)<=(aantal-1)) ) {
					   if( (buf[i-1]=='&') && (buf[i+1] == '0') && (buf[i+2]=='x') && (buf[i+5]==';')) {
						   sRet = sRet + buf[i];
						   continue;
					   }
				   }
			   }
			   //
			   if( buf[i] == ';') {
				   if( ((i-6) >= 0) ) {
					   if( (buf[i-6]=='&') && (buf[i-5] == '#') && (buf[i-4]=='0') && (buf[i-3]=='x')) {
						   sRet = sRet + buf[i];
						   continue;
					   } 
				   }
			   }
			   //
			   if( buf[i] == cSrc ) {
				   sRet = sRet + cTgt;
				   continue;
			   }
			   //
			   sRet = sRet + buf[i];
		   }
	       return sRet;
		}
		//
		//---------------------------------------------------------------------------------
		String verwijderEnclosingQuotes(String sIn)
		//---------------------------------------------------------------------------------
		{
			 String sRet = "";
			 char[] buf = sIn.toCharArray();
			 int aantal = buf.length;
			 for(int i=0;i<aantal;i++)
			 {
				 if( buf[i] == '"' ) {
				     if( i == 0 ) continue;
				     if( i == (aantal-1)) continue;
				 }
				 sRet = sRet + buf[i];
			 }
			 return sRet;
		}
		//
		//---------------------------------------------------------------------------------
		String ByteToHex(byte b)
		//---------------------------------------------------------------------------------
		{
			return ""+(HEXES.charAt((b & 0xF0) >> 4) )+(HEXES.charAt((b & 0x0F)));
		}
		//
	    // ---------------------------------------------------------------------------------
		String IntToHex(int i)
	    // ---------------------------------------------------------------------------------
		{
			byte b = (byte)(i & 0xff);
			//return "" + HEXES.charAt((b & 0xF0) >> 4) + HEXES.charAt((b & 0x0F));
			return ByteToHex(b);
		}
		//
	    // ---------------------------------------------------------------------------------
		String Latin1HexToUtf8Hex(String sIn)
	    // ---------------------------------------------------------------------------------
		{
			//  AB  ->  110xxxxx 11xxxxxx
			//  110.000.87  10.654321
			int i = xMSet.xU.HexNaarInt(sIn);
			// 87000000 shift right OR 110.....
			int j = ((i & 0xc0)>>6)|0xc0;
			// 00654321 OR 10.......
			int k = ((i & 0x3f))|0xa0;
			//Error(sIn + "=" +i + " " + j + " " + k + " " + toHex(j) + " " + toHex(k));
			return ""+IntToHex(j)+IntToHex(k);
		}
		//
	    // ---------------------------------------------------------------------------------
		String StripHTML(String sIn)
	    // ---------------------------------------------------------------------------------
		{
			 String sRet = "";
			 if( sIn == null ) return "(null)";
			 char[] buf = sIn.toCharArray();
			 int aantal = buf.length;
			 for(int i=0;i<aantal;i++)
			 {
				 if( buf[i] == '>' ) { sRet = sRet + "-"; continue; }
				 if( buf[i] == '<' ) { sRet = sRet + "-"; continue; }
				 sRet = sRet + buf[i];
			 }
			 return sRet;
		}
		//
	    // ---------------------------------------------------------------------------------
		String TerugUitInternFormaat(String sIn)
		// ---------------------------------------------------------------------------------
		{
			String sRet=sIn;
			if( sIn.indexOf("&#0x") < 0) return sRet;
			
			// &#0xNN;
			sRet="";
			String sTemp="";
			char[] buf = sIn.toCharArray();
			int aantal = buf.length;
			int teller = 0;
			for(int i=0;i<aantal;i++)
			{
			  if( (teller==0) && (buf[i] != '&') ) { sRet = sRet + buf[i]; continue; }
			  switch(teller)
			  {
			  case 0 : { sTemp = "";
				         if( buf[i] != '&') {sRet = sRet + sTemp + buf[i]; teller=0; } else {sTemp = sTemp + buf[i]; teller++;} break; } 
			  case 1 : { if( buf[i] != '#') {sRet = sRet + sTemp + buf[i]; teller=0; } else {sTemp = sTemp + buf[i]; teller++;} break; }
			  case 2 : { if( buf[i] != '0') {sRet = sRet + sTemp + buf[i]; teller=0; } else {sTemp = sTemp + buf[i]; teller++;} break; }
			  case 3 : { if( buf[i] != 'x') {sRet = sRet + sTemp + buf[i]; teller=0; } else {sTemp = sTemp + buf[i]; teller++;} break; }
			  case 4 : { sTemp = sTemp + buf[i]; teller++; break; }
			  case 5 : { sTemp = sTemp + buf[i]; teller++; break; }
			  case 6 : { if( buf[i] != ';') {sRet = sRet + sTemp + buf[i]; teller=0; } 
			              else {
			  	             teller=0;
			  	             sTemp = "" + buf[i-2] + buf[i-1];
			  	             int ic = this.HexNaarInt(sTemp);
			  	             sRet = sRet +  (char)ic;
				          } 
			              break; }
			  default : break;
			  }
			}
			//logit(sIn + "->" + sRet);
			return sRet;
		}
		//
	    // ---------------------------------------------------------------------------------
		public boolean isValidURL(String sUrl)
		// ---------------------------------------------------------------------------------
		{
			try
		    {
		         URL url = new URL(sUrl);
		         return true;
		    }catch(Exception e)
		    {
		    	 //logit("Not a valid URL [" + sUrl + "]");
		         //logit(LogStackTrace(e));
		         return false;
		    }
		}
		//
	    // ---------------------------------------------------------------------------------
		public String getHostNameFromURL(String sUrl)
		// ---------------------------------------------------------------------------------
		{
			if( sUrl.indexOf("http")!=0) sUrl = "http://" + sUrl;
			try
		    {
		         URL url = new URL(sUrl);
		         return url.getHost();
		    }catch(Exception e)
		    {
		    	 //logit("Not a valid URL [" + sUrl + "]");
		         //logit(LogStackTrace(e));
		         return "";
		    }
		}
		//
	    // ---------------------------------------------------------------------------------
		public String CleanseURL(String sIn)
	    // ---------------------------------------------------------------------------------
		{
			String sRet = sIn.trim();
			if( sRet.indexOf("http://") != 0 ) sIn = "http://" + sRet;
			String sHost = getHostNameFromURL( sRet ); if( sHost.length() == 0 ) return null;
			// dubbele // verwijderen
			char[] buf = sIn.toCharArray();
			int aantal = buf.length;
			boolean proto = false;
			int dubbel = 0;
			sRet = "";
			for(int i=0;i<aantal;i++)
			{
				if ( buf[i] == '/' ) dubbel++; else dubbel = 0;
				if ( (dubbel >= 2) && (proto==true)) continue;
				if ( dubbel == 2 ) proto=true;
				sRet = sRet + buf[i];
			}
		    if( sIn.compareTo(sRet) != 0 ) {
		    	logit("URL has been cleansed [" + sIn + "] -> [" + sRet + "]");
		    }
			return sRet;
		}
		
		// ---------------------------------------------------------------------------------
		public String getProcessId(String fallback)
		// ---------------------------------------------------------------------------------
		{
		    // Note: may fail in some JVM implementations
		    // therefore fallback has to be provided

		    // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
		    final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		    final int index = jvmName.indexOf('@');

		    if (index < 1) {
		        // part before '@' empty (index = 0) / '@' not found (index = -1)
		        return fallback;
		    }

		    try {
		        return Long.toString(Long.parseLong(jvmName.substring(0, index)));
		    } catch (NumberFormatException e) {
		        // ignore
		    }
		    return fallback;
		}
		//
		//
		//---------------------------------------------------------------------------------
		//---------------------------------------------------------------------------------
		//---------------------------------------------------------------------------------
		
		
		
		
		
	

}
