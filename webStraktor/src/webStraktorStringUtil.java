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

public class webStraktorStringUtil {
	
	webStraktorStringUtil()
	{
		
	}
	//
	//---------------------------------------------------------------------------------
	int TelDelims ( String sIn , char ctKar )
	//---------------------------------------------------------------------------------
	{   
	    char[] SChar = sIn.toCharArray();
	    int aantal=0; 		
		for(int ii=0;ii<SChar.length;ii++) 
		{	
			if ( (SChar[ii] == ctKar) ) aantal++;
		}		
		return aantal;
	}
	//
	//---------------------------------------------------------------------------------
	String GetVeld( String sIn , int idx , char delim )
	//---------------------------------------------------------------------------------
	{ String sTemp="";
	  char[] sChar;
	  int ii;
	  int hit = 0;
	  
	  sTemp = delim + sIn.trim() + delim;
	  sChar = sTemp.toCharArray();
	  sTemp = "";
	  for(ii=0;ii<sChar.length;ii++)
	  {
		  if ( sChar[ii] == delim ) { hit++; continue; }
		  if ( hit == idx ) {
			  sTemp = sTemp + sChar[ii];
		  }
	  }
	  return sTemp;
	}
	//
	//---------------------------------------------------------------------------------
	String Remplaceer( String sIn , String sPattern , String sReplace )
	// ---------------------------------------------------------------------------------
	{
		        int e = 0;
		        int teller=0;
		        String sOut = sIn;
		        while ((e = sOut.indexOf(sPattern)) >= 0) {
		         String sPre="";
		         String sPost="";
		         String sTemp="";
		         e = sIn.indexOf(sPattern);
		         if( e >= 0) {
		        	sPre = sIn.substring(0,e);
		            sTemp = sPre + sReplace;
		            e += sPattern.length();
		            if( e < sIn.length() ) {
		                sPost = sIn.substring(e);
		                sTemp = sTemp + sPost;
		            }
		         }
		         sOut = sTemp;
		         teller++; if( teller>10) break;
		        } 
		        //System.out.println(sIn + "->" + sOut);
		        return sOut;
		
	}
	//
	//---------------------------------------------------------------------------------
	String RemplaceerIgnoreCase( String sIn , String sPattern , String sReplace )
	// ---------------------------------------------------------------------------------
	{
		        int e = 0;
		        int teller=0;
		        String sOut = sIn;
		        String sUpper = sIn.toUpperCase();
		        while ((e = sUpper.indexOf(sPattern.toUpperCase())) >= 0) {
		         String sPre="";
		         String sPost="";
		         String sTemp="";
		         e = sUpper.indexOf(sPattern.toUpperCase());
		         if( e >= 0) {
		        	sPre = sOut.substring(0,e);
		            sTemp = sPre + sReplace;
		            e += sPattern.length();
		            if( e < sOut.length() ) {
		                sPost = sOut.substring(e);
		                sTemp = sTemp + sPost;
		            }
		         }
		         sOut = sTemp;
		         sUpper = sTemp.toUpperCase();
		         teller++; if( teller>10) break;
		        } 
		        //System.out.println(sIn + "->" + sOut);
		        return sOut;
		
	}
	//
	//---------------------------------------------------------------------------------
	String RemplaceerNEW( String sIn , String sPattern , String sReplace )
	// ---------------------------------------------------------------------------------
	{   // Remplaceer vervangt slechts eerste occurence
		String sOut = sIn;
		for(int i=0;i<100;i++)
		{
		  if( sOut.indexOf(sPattern)<0) break;
		  sOut = this.Remplaceer(sOut, sPattern, sReplace);
		}
		return sOut;
	}
}
