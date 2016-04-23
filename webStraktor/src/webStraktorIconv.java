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

import java.io.*;
import java.util.ArrayList;

public class webStraktorIconv {
	
	webStraktorLogger iLogger=null;
	webStraktorSettings xMSet=null;
	private byte[] convbuffer = new byte[7];  // &#0xnn;
	
	// combined character and diacritical  eg  e accent acute  61 CC 81
	//  omschrijving UTF8 Latin
	private String[][] arCombinedCharacter = new String [][] {
				{"aacute" ,"61CC81" ,"E1"},
				{"Aacute" ,"41CC81" ,"C1"},
				{"acirc"  ,"61CC82" ,"E2"},
				{"Acirc"  ,"41CC82" ,"C2"},
				{"agrave" ,"61CC80" ,"E0"},
				{"Agrave" ,"41CC80" ,"C0"},
				{"aring"  ,"61CC8A" ,"E5"},
				{"Aring"  ,"41CC8A" ,"C5"},
				{"atilde" ,"61CC83" ,"E3"},
				{"Atilde" ,"41CC83" ,"C3"},
				{"auml"   ,"61CC88" ,"E4"},
				{"Auml"   ,"41CC88" ,"C4"},
				{"ccedil" ,"63CCA7" ,"E7"},
				{"Ccedil" ,"43CCA7" ,"C7"},
				{"eacute" ,"65CC81" ,"E9"},
				{"Eacute" ,"45CC81" ,"C9"},
				{"ecirc"  ,"65CC82" ,"EA"},
				{"Ecirc"  ,"45CC82" ,"CA"},
				{"egrave" ,"65CC80" ,"E8"},
				{"Egrave" ,"45CC80" ,"C8"},
				{"euml"   ,"65CC88" ,"EB"},
				{"Euml"   ,"45CC88" ,"CB"},
				{"iacute" ,"69CC81" ,"ED"},
				{"Iacute" ,"49CC81" ,"CD"},
				{"icirc"  ,"69CC82" ,"EE"},
				{"Icirc"  ,"49CC82" ,"CE"},
				{"igrave" ,"69CC80" ,"EC"},
				{"Igrave" ,"49CC80" ,"CC"},
				{"iuml"   ,"69CC88" ,"EF"},
				{"Iuml"   ,"49CC88" ,"CF"},
				{"ntilde" ,"6ECC83" ,"F1"},
				{"Ntilde" ,"4ECC83" ,"D1"},
				{"oacute" ,"6FCC81" ,"F3"},
				{"Oacute" ,"4FCC81" ,"D3"},
				{"ocirc"  ,"6FCC82" ,"F4"},
				{"Ocirc"  ,"4FCC82" ,"D4"},
				{"ograve" ,"6FCC80" ,"F2"},
				{"Ograve" ,"4FCC80" ,"D2"},
				{"oslash" ,"6FCCB7" ,"F8"},
				{"Oslash" ,"4FCCB7" ,"D8"},
				{"otilde" ,"6FCC83" ,"F5"},
				{"Otilde" ,"4FCC83" ,"D5"},
				{"ouml"   ,"6FCC88" ,"F6"},
				{"Ouml"   ,"4FCC88" ,"D6"},
				{"uacute" ,"75CC81" ,"FA"},
				{"Uacute" ,"55CC81" ,"DA"},
				{"ucirc"  ,"75CC82" ,"FB"},
				{"Ucirc"  ,"55CC82" ,"DB"},
				{"ugrave" ,"75CC80" ,"F9"},
				{"Ugrave" ,"55CC80" ,"D9"},
				{"uuml"   ,"75CC88" ,"FC"},
				{"Uuml"   ,"55CC88" ,"DC"},
				{"yacute" ,"79CC81" ,"FD"},
				{"Yacute" ,"59CC81" ,"DD"},
				//{"Notsign" ,"65CCC2" ,"AC"},
				{"yuml"   ,"79CC88" ,"FF"} };
		
	class CombinedDiacritical
	{
		String utf8_3;
		String utf8_2;
		String latin1;
		CombinedDiacritical(String i, String j , String k)
		{
			utf8_3=i;
			utf8_2=j;
			latin1 =k;
		}
	}
	ArrayList<CombinedDiacritical> diaLst = null;
	               
	//
    // ---------------------------------------------------------------------------------
	webStraktorIconv(webStraktorSettings iS, webStraktorLogger iL)
	// ---------------------------------------------------------------------------------
	{
		xMSet = iS;
		iLogger = iL;
	}
	//
    // ---------------------------------------------------------------------------------
	private void LogIt(int level , String sIn)
	// ---------------------------------------------------------------------------------
	{
		if( iLogger != null ) iLogger.Logit(level, "iCV - "  + sIn);
	}
	// ---------------------------------------------------------------------------------
	private void Error(String sIn)
	// ---------------------------------------------------------------------------------
	{
		LogIt(0,sIn);
	}
	//
    // ---------------------------------------------------------------------------------
	boolean iconv(String FNameIn, String FNameOut , boolean naarLatin1)
	// ---------------------------------------------------------------------------------
	{
		int totalRead = 0;
		int totalWritten=0;
		boolean CombinedDiacritical=false;
		FileOutputStream outputStream = null;
		// Output
		try {
             outputStream =  new FileOutputStream(FNameOut);
        }
        catch(Exception e) {
        	Error("ERROR Writing File [" + FNameOut + "] " + e.getMessage() );
			Error(xMSet.xU.LogStackTrace(e));
			return false; 
        }
		//
		int convbufferdiepte=0;
		boolean isOk=false;
		try {
            byte[] buffer = new byte[1024];

            FileInputStream inputStream = new FileInputStream(FNameIn);
           
            int nRead = 0;
            boolean flushit=false;
            byte b='\0';
            while((nRead = inputStream.read(buffer)) != -1) {
                totalRead = totalRead + nRead;
                //   &#0xnn;    
                for(int i=0;i<nRead;i++)
                {
                   b=(byte)buffer[i];
                   convbufferdiepte++;
                   convbuffer[convbufferdiepte-1] = b;
                   switch ( convbufferdiepte )
                   {
                   case 1 : { if( b == (byte)'&' ) continue; flushit = true; break;}
                   case 2 : { if( b == (byte)'#' ) continue; flushit = true; break;}
                   case 3 : { if( b == (byte)'0' ) continue; flushit = true; break; }
                   case 4 : { if((b == (byte)'x')||(b == (byte)'X')) continue; flushit = true; break; }
                   case 5 : { break; }
                   case 6 : { break; }
                   case 7 : { if(b == (byte)';') {
                	             //convbuffer[0] = (byte)'{';
                	             //convbuffer[1] = convbuffer[4];
                	             //convbuffer[2] = convbuffer[5];
                	             //convbuffer[3] = (byte)'}';
                	             //convbufferdiepte=4;  // debug
                	             String sTemp = (""+(char)convbuffer[4]+(char)convbuffer[5]).toUpperCase();  // je kan tolower in de script gebruikt hebben
                	             int ib = xMSet.xU.HexNaarInt(sTemp);
                	             convbuffer[0] = (byte)(ib & 0xff);
                	             convbufferdiepte=1;
                	             if( (ib==204) || (ib==205) || (ib==206) || (ib==206) ) { //  CC - CD - CE - CF -
                	            	 if( CombinedDiacritical == false ) {
                	            	   CombinedDiacritical=true;
                	            	   LogIt(2,"Appears tot be encoded in Combined Diacritical. got [" + sTemp + "]");
                	            	 }
                	             }
                	           }
                               else {
                	             LogIt(1,"Bizar");  // haast onmogelijk om geen; te hebben op positie 7
                               }
                               flushit = true; 
                               break; }
                   default : { Error("Error in inconv"); flushit=true; break; }
                   }
                   if( flushit == true ) {
                   	isOk = do_flush(FNameOut,outputStream , convbufferdiepte);
                   	if( isOk == false ) break;
                   	totalWritten += convbufferdiepte;
                   	convbufferdiepte=0;
                   	flushit=false;
                   	continue;
                   }
                }
                if( isOk == false ) break;
            }
            // rest
        	totalWritten += convbufferdiepte;
            do_flush(FNameOut,outputStream , convbufferdiepte);
            //
            inputStream.close();		
            LogIt(5,"-->    Read [" + totalRead + "] bytes on " + FNameIn);
            LogIt(5,"--> Written [" + totalWritten + "] bytes on " + FNameOut);
            //
            if( outputStream != null ) outputStream.close();
            if( CombinedDiacritical == true ) inconvCombinedDiacriticalToUTF8(FNameOut);
            if( naarLatin1 == true )inconvNaarLatin1(FNameOut);
            return isOk;
        }
		catch( Exception e ) {
			Error("ERROR Reading File [" + FNameIn + "] " + e.getMessage() );
			Error(xMSet.xU.LogStackTrace(e));
			return false;
		}
	}
	//
    // ---------------------------------------------------------------------------------
	private boolean do_flush(String FN , FileOutputStream io , int diepte)
	// ---------------------------------------------------------------------------------
	{
	  //for(int i=0;i<diepte;i++)  System.out.print(""+(char)convbuffer[i]);
	  try {
	  	  io.write(convbuffer,0,diepte);
	  }
	  catch( Exception e) {
		  Error("ERROR writing File [" + FN + "] " + e.getMessage() );
		  Error(xMSet.xU.LogStackTrace(e));
		  return false;
	  }
	  return true;
	}
	
	//
    // ---------------------------------------------------------------------------------
	void initialiseerDiaConvTabel()
    // ---------------------------------------------------------------------------------
	{
		diaLst = new ArrayList<CombinedDiacritical>();
		for(int i=0;i<arCombinedCharacter.length;i++) {
			String naam = arCombinedCharacter[i][0];
			String utf83 = arCombinedCharacter[i][1];
			String lat1 = arCombinedCharacter[i][2];
			String utf82 = xMSet.xU.Latin1HexToUtf8Hex(lat1);
			//Error( naam + " " + utf83 + " " + lat1 + "->" + utf82);
			CombinedDiacritical  x = new CombinedDiacritical(utf83,utf82,lat1);
			diaLst.add(x);
		}
		//for(int i=0;i<diaLst.size();i++) LogIt(9, diaLst.get(i).utf8_3 + "=" + diaLst.get(i).latin1 + "->" + diaLst.get(i).utf8_2 );
	}
	//
    // ---------------------------------------------------------------------------------
	private boolean inconvCombinedDiacriticalToUTF8(String Fnaam)
    // ---------------------------------------------------------------------------------
	{
		// http://www.utf8-chartable.de/unicode-utf8-table.pl?start=768&unicodeinhtml=hex
		
		//   character + CCnn
		//   accent grave CC80; accent acute CC81; circonflx CC82 ; tilde CC83; umluat CC88
		initialiseerDiaConvTabel();
		//
		// kopieer naar backup
		String FBck = Fnaam + "_BCK.txt";
		try {
			xMSet.xU.copyFile(Fnaam,FBck);
		}
		catch(Exception e) {
			Error("Copying [" + Fnaam + "] to [" + FBck + "] " + e.getMessage());
			Error(xMSet.xU.LogStackTrace(e));
			return false;
		}
		//
		int totalRead = 0;
		int totalWritten=0;
		FileOutputStream outputStream = null;
		// Output
		try {
             outputStream =  new FileOutputStream(Fnaam);
        }
        catch(Exception e) {
        	Error("ERROR Writing File [" + Fnaam + "] " + e.getMessage() );
			Error(xMSet.xU.LogStackTrace(e));
			return false; 
        }
		//
        boolean isOk=true;
        try {
            byte[] buffer = new byte[1024];
            FileInputStream inputStream = new FileInputStream(FBck);
            int nRead = 0;
            byte b='\0';
            byte p='\0';
            byte c='\0';
            boolean found = false;
            String sRet=null;
            while((nRead = inputStream.read(buffer)) != -1) {
                totalRead += nRead;
                for(int i=0;i<nRead;i++)
                {
                   b=(byte)buffer[i];
                   if( totalRead == 1 ) { p=b; continue; }
                   if( found == true ) {
                	   sRet = ZoekCombined(p,c,b);
                	   if( sRet == null ) {
                		   outputStream.write(p);
                    	   totalWritten++;
                	   }
                	   else {
                           int een  = xMSet.xU.HexNaarInt(sRet.substring(0,2));
                           int twee = xMSet.xU.HexNaarInt(sRet.substring(2,4));
                           //Error( sRet + " " + een + " " + twee );
                           p = (byte)(een & 0xff);
                           outputStream.write(p);
                           p = (byte)(twee & 0xff);
                           outputStream.write(p);
                           totalWritten += 2;
                	   }
                	   p='\0';
                	   found = false;
                	   continue;
                   }
                   if( (b != (byte)0xcc) && (b != (byte)0xcd) && (b != (byte)0xce) && (b != (byte)0xcf) ) {
                	   if( p != '\0' ) {
                		   outputStream.write(p);
                	       totalWritten++;
                	   }
                	   p=b;
                	   continue;
                   }
                   found = true;
                   c=b;
                }   
            }
            // rest
        	//
            if( p != '\0' ) {
               outputStream.write(p);
               totalWritten++;
            }
            //
            inputStream.close();		
            LogIt(5,"-->    Read [" + totalRead + "] bytes on " + FBck);
            LogIt(5,"--> Written [" + totalWritten + "] bytes on " + Fnaam);
            //
            if( outputStream != null ) outputStream.close();
            //
            xMSet.xU.VerwijderBestand(FBck);
            return isOk;
        }
		catch( Exception e ) {
			Error("ERROR Reading File [" + FBck + "] " + e.getMessage() );
			Error(xMSet.xU.LogStackTrace(e));
			return false;
		}
   }
   //
   // ---------------------------------------------------------------------------------
   String ZoekCombined( byte een , byte twee ,byte drie)
   // ---------------------------------------------------------------------------------
   {
	   String vier = (xMSet.xU.IntToHex(een&0xff) + xMSet.xU.IntToHex(twee&0xff) + xMSet.xU.IntToHex(drie&0xff)).trim();
	   for(int i=0;i<diaLst.size();i++)
	   {
		   if( diaLst.get(i).utf8_3.compareToIgnoreCase(vier) == 0 ) {
			   //Error(" GOT " + drie + " -> " + diaLst.get(i).utf8_2 );
			   return diaLst.get(i).utf8_2;
		   }
	   }
	   LogIt(5,"Diacritical combined :" + vier + " not found +[" + (een&0xff) + " " + (twee&0xff) + " " + (drie&0xff) + "]");
	   return null;
   }
   //
   // ---------------------------------------------------------------------------------
   private boolean inconvNaarLatin1(String FNaam )
   // ---------------------------------------------------------------------------------
   {
	   LogIt(5,"Latin1 Conversion");
	   String FOut = xMSet.xU.RemplaceerNEW(FNaam, "\\Out\\utf8\\" , "\\Out\\latin1\\");
	   if( FOut.compareToIgnoreCase(FNaam) == 0 )  {
		   Error( "Output filename and input filename are the same");
		   return false;
	   }
	   
	   int totalRead = 0;
	   int totalWritten=0;
	   FileOutputStream outputStream = null;
	   // Output
	   try {
            outputStream =  new FileOutputStream(FOut);
       }
       catch(Exception e) {
       	Error("ERROR Writing File [" + FOut + "] " + e.getMessage() );
			Error(xMSet.xU.LogStackTrace(e));
			return false; 
       }
		//
       boolean isOk=true;
       int lijnteller=0;
       try {
           byte[] buffer = new byte[1024];
           FileInputStream inputStream = new FileInputStream(FNaam);
           int nRead = 0;
           byte b='\0';
           byte p='\0';
           byte bs;
           byte utf1 = '\0';
           byte utf2 = '\0';
           while((nRead = inputStream.read(buffer)) != -1) {
        	   //  110xxx87 10654321  -> 110xxxx 10xxxxxx dus C0 en 80
               totalRead += nRead;
               for(int i=0;i<nRead;i++)
               {
                  b=(byte)buffer[i];
                  if( b == '\n') {
                	  lijnteller++;
                	  if( lijnteller == 1) {
                		  String sHeader = " <?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>" + xMSet.xU.EOL + "<!-- Converted [" + FNaam + "] to Latin1 -->" + xMSet.xU.EOL;
                		  byte[] theByteArray = sHeader.getBytes();
                		  outputStream.write(theByteArray);
                	  }
                  }
                  if( lijnteller == 0 ) continue;
                  if( p == '\0' ) {
                	bs=b;
               	    utf1 = (byte)(bs & 0xc0); 
               	    if( utf1 == (byte)0xc0 ) {
               	       p = b;
               	    }
                    else {
                    	outputStream.write(b);
                        totalWritten++;
                    }
                    continue;
                  }
                  //
                  bs=b;
                  utf2 = (byte)(bs & 0x80);
                  if( utf2 == (byte)0x80 ) {
                	  byte lat1 = (byte)((p & 0x03)<<6);
                	  byte lat2 = (byte)(b & 0x3f);
                	  byte lat3 = (byte)(lat1 | lat2);
                	  //Error("--->" + (p&0xff) + " " + (bs&0xff) + " " + (lat3&0xff) + " " + (char)lat3);
                	  outputStream.write(lat3);
                	  totalWritten++;;
                  }
                  else {
                	  outputStream.write(p);
                	  outputStream.write(b);
                      totalWritten +=2;
                  }
                  p = '\0';
               } // for   
           } // while
           // rest
       	   //
           if( p != '\0' ) {
        	   outputStream.write(b);
               totalWritten++; 
           }
           //
           inputStream.close();		
           LogIt(5,"-->    Read [" + totalRead + "] bytes on " + FNaam);
           LogIt(5,"--> Written [" + totalWritten + "] bytes on " + FOut);
           //
           if( outputStream != null ) outputStream.close();
           return isOk;
       }
		catch( Exception e ) {
			Error("ERROR Reading File [" + FNaam + "] " + e.getMessage() );
			Error(xMSet.xU.LogStackTrace(e));
			return false;
		}
   }
}
