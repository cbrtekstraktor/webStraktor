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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class webStraktorDateTime {
	
	public static final int FORMAT_UNKNOWN = -1;
	public static final int FORMAT_DATE = 100;
	public static final int FORMAT_TIME = 101;
	public static final int FORMAT_DATETIME = 102;
	
	
	String     sDefaultDateTime = "19000101000000";
	String     sDefaultDate = "19000101";
	String     sDefaultTime = "000000";
	String     sTimeZone = "GMT";
	
	    //
	    //---------------------------------------------------------------------------------
	    webStraktorDateTime(String iTimeZone)
	    //---------------------------------------------------------------------------------
	    {
		  sTimeZone = iTimeZone;
	    }
	    
	    //
		//---------------------------------------------------------------------------------
		String NaarInternDateTimeFormaat(String sIn,String DBDateTimeFormat)
		//---------------------------------------------------------------------------------
		{
		   String sOut = "ERROR";
			   
		   String sCC = "20";
		   String sYY = "00";
		   String sMM = "00";
		   String sDD = "00";
		   String sHH = "00";
		   String sMI = "00";
		   String sSS = "00";
		   String sMIL= "000";
		   int idx =  0;
		   
		   sIn = sIn + "000000000000000000000000000000000000000000000000000";
		   try {
			   idx =  DBDateTimeFormat.indexOf("CC");
			   if( idx >= 0 ) sCC = sIn.substring(idx,idx+2);
			   idx =  DBDateTimeFormat.indexOf("YY");
			   if( idx >= 0 ) sYY = sIn.substring(idx,idx+2);
			   //
			   idx =  DBDateTimeFormat.indexOf("MMM");
			   if( idx >= 0 ) {
				   String sMon = sIn.substring(idx,idx+3);
				   if( sMon.compareToIgnoreCase("JAN")==0) sMM = "01";
				   if( sMon.compareToIgnoreCase("FEB")==0) sMM = "02";
				   if( sMon.compareToIgnoreCase("MAR")==0) sMM = "03";
				   if( sMon.compareToIgnoreCase("APR")==0) sMM = "04";
				   if( sMon.compareToIgnoreCase("MAY")==0) sMM = "05";
				   if( sMon.compareToIgnoreCase("JUN")==0) sMM = "06";
				   if( sMon.compareToIgnoreCase("JUL")==0) sMM = "07";
				   if( sMon.compareToIgnoreCase("AUG")==0) sMM = "08";
				   if( sMon.compareToIgnoreCase("SEP")==0) sMM = "09";
				   if( sMon.compareToIgnoreCase("OCT")==0) sMM = "10";
				   if( sMon.compareToIgnoreCase("NOV")==0) sMM = "11";
				   if( sMon.compareToIgnoreCase("DEC")==0) sMM = "12";
			   }
			   else {
			      idx =  DBDateTimeFormat.indexOf("MM");
			      if( idx >= 0 ) sMM = sIn.substring(idx,idx+2);
			   }
			   idx =  DBDateTimeFormat.indexOf("DD");
			   if( idx >= 0 ) sDD = sIn.substring(idx,idx+2); 
			   idx =  DBDateTimeFormat.indexOf("HH");
			   if( idx >= 0 ) sHH = sIn.substring(idx,idx+2); 
			   idx =  DBDateTimeFormat.indexOf("MI");
			   if( idx >= 0 ) sMI = sIn.substring(idx,idx+2); 
			   idx =  DBDateTimeFormat.indexOf("SS");
			   if( idx >= 0 ) sSS = sIn.substring(idx,idx+2);
			   idx =  DBDateTimeFormat.indexOf("MIL");
			   if( idx >= 0 ) sMIL = sIn.substring(idx,idx+3);
			    
			   sOut = sCC+sYY+sMM+sDD+sHH+sMI+sSS+sMIL;
			   if( isNumeriekLong(sOut) == false ) {
				   //System.out.println("******************"+sOut);
				   sOut = sDefaultDateTime;  // anders , ttz. chrs check
			   }
		   }
		   catch (Exception e)
		   {
			   return "Error ("+sIn+") -> ("+sOut+")";
		   }
		   return sOut;
		}
		//
		//---------------------------------------------------------------------------------
		String NaarInternDateFormaat(String sIn, String DBDateFormat)
		//---------------------------------------------------------------------------------
		{
		   String sOut = "ERROR";
		   
		   String sCC = "20";
		   String sYY = "00";
		   String sMM = "00";
		   String sDD = "00";
		   int idx =  0;
		   
		   sIn = sIn + "000000000000000000000000000000000000000000000000000";
		   try {
			   idx =  DBDateFormat.indexOf("CC");
			   if( idx >= 0 ) sCC = sIn.substring(idx,idx+2);
			   idx =  DBDateFormat.indexOf("YY");
			   if( idx >= 0 ) sYY = sIn.substring(idx,idx+2);
			   idx =  DBDateFormat.indexOf("MM");
			   if( idx >= 0 ) sMM = sIn.substring(idx,idx+2);
			   idx =  DBDateFormat.indexOf("DD");
			   if( idx >= 0 ) sDD = sIn.substring(idx,idx+2); 
			    
			   sOut = sCC+sYY+sMM+sDD;
			   if( isNumeriekLong(sOut) == false ) sOut = sDefaultDate;  // anders , ttz. chrs check
		   }
		   catch (Exception e)
		   {
			   return "Error ("+sIn+") -> ("+sOut+")";
		   }
		   return sOut;
		}
		//
		//---------------------------------------------------------------------------------
		String NaarInternTimeFormaat(String sIn, String DBTimeFormat)
		//---------------------------------------------------------------------------------
		{
		   String sOut = "ERROR";
		   	  
		   String sHH = "00";
		   String sMI = "00";
		   String sSS = "00";
		   String sMIL = "000";
		   int idx =  0;
		   
		   sIn = sIn + "000000000000000000000000000000000000000000000000000";
		   try {
			   idx =  DBTimeFormat.indexOf("HH");
			   if( idx >= 0 ) sHH = sIn.substring(idx,idx+2); 
			   idx =  DBTimeFormat.indexOf("MI");
			   if( idx >= 0 ) sMI = sIn.substring(idx,idx+2); 
			   idx =  DBTimeFormat.indexOf("SS");
			   if( idx >= 0 ) sSS = sIn.substring(idx,idx+2);
			   idx =  DBTimeFormat.indexOf("MIL");
			   if( idx >= 0 ) sMIL = sIn.substring(idx,idx+3);
			    
			   sOut = sHH+sMI+sSS+sMIL;
			   if( isNumeriekLong(sOut) == false ) sOut = sDefaultTime;  // anders , ttz. chrs check
		   }
		   catch (Exception e)
		   {
			   return "Error ("+sIn+") -> ("+sOut+")";
		   }
		   return sOut;
		}
		//
		//---------------------------------------------------------------------------------
		boolean isNumeriekLong(String sIn)
		//---------------------------------------------------------------------------------
		{  long ii;
		
			 try
	         {  
	            ii = Long.parseLong(sIn);
	            ii++; // om de warning te verwijderen
	         }
	         catch (NumberFormatException e)
	         {  
	            	 return false;
	         }
	         return true;
	            
		}
		//
		//---------------------------------------------------------------------------------
		String FormateerTimeStamp(String sIn, String Formaat)
		//---------------------------------------------------------------------------------
		{
		   if( sIn.compareToIgnoreCase("NULL")==0) return "null";
		   String sOut = Formaat;
		   try {
			    
			    int i = 0;
			    String cc = sIn.substring(i,i+2); i=i+2;
			    String yy = sIn.substring(i,i+2); i=i+2;
			    String mm = sIn.substring(i,i+2); i=i+2;
			    String dd = sIn.substring(i,i+2); i=i+2;
			    String hh = sIn.substring(i,i+2); i=i+2;
			    String mi = sIn.substring(i,i+2); i=i+2;
			    String ss =  sIn.substring(i,i+2); i=i+2;
			    String mil =  sIn.substring(i,i+3); i=i+3;
			    String mon = mm;
			
				int imon = Integer.parseInt(mm);
				switch (imon )
				{
					case 1 : {mon = "Jan"; break; }
					case 2 : {mon = "Feb"; break; }
					case 3 : {mon = "Mar"; break; }
					case 4 : {mon = "Apr"; break; }
					case 5 : {mon = "May"; break; }
					case 6 : {mon = "Jun"; break; }
					case 7 : {mon = "Jul"; break; }
					case 8 : {mon = "Aug"; break; }
					case 9 : {mon = "Sep"; break; }
					case 10 : {mon = "Oct"; break; }
					case 11 : {mon = "Nov"; break; }
					case 12 : {mon = "Dec"; break; }
					default : {break;}
				}
			
			    sOut = this.Remplaceer(sOut, "MMM", mon);
			    sOut = this.Remplaceer(sOut, "DD", dd);
			    sOut = this.Remplaceer(sOut, "MM", mm);
			    sOut = this.Remplaceer(sOut, "YY", yy);
			    sOut = this.Remplaceer(sOut, "HH", hh);
			    sOut = this.Remplaceer(sOut, "MI", mi);
			    sOut = this.Remplaceer(sOut, "SS", ss);
			    sOut = this.Remplaceer(sOut, "CC", cc);
			    sOut = this.Remplaceer(sOut, "MIL", mil);
			}
			catch (Exception e) { ; }
			return sOut;
		}
		//
		//---------------------------------------------------------------------------------
		String FormateerDate(String sIn, String Formaat)
		//---------------------------------------------------------------------------------
		{
		   if( sIn.compareToIgnoreCase("NULL")==0) return "null";
		   String sOut = Formaat;
		   try {
			    
			    int i = 0;
			    String cc = sIn.substring(i,i+2); i=i+2;
			    String yy = sIn.substring(i,i+2); i=i+2;
			    String mm = sIn.substring(i,i+2); i=i+2;
			    String dd = sIn.substring(i,i+2); i=i+2;
			   
			    String mon = mm;
			
				int imon = Integer.parseInt(mm);
				switch (imon )
				{
					case 1 : {mon = "Jan"; break; }
					case 2 : {mon = "Feb"; break; }
					case 3 : {mon = "Mar"; break; }
					case 4 : {mon = "Apr"; break; }
					case 5 : {mon = "May"; break; }
					case 6 : {mon = "Jun"; break; }
					case 7 : {mon = "Jul"; break; }
					case 8 : {mon = "Aug"; break; }
					case 9 : {mon = "Sep"; break; }
					case 10 : {mon = "Oct"; break; }
					case 11 : {mon = "Nov"; break; }
					case 12 : {mon = "Dec"; break; }
					default : {break;}
				}
			
			    sOut = this.Remplaceer(sOut, "MMM", mon);
			    sOut = this.Remplaceer(sOut, "DD", dd);
			    sOut = this.Remplaceer(sOut, "MM", mm);
			    sOut = this.Remplaceer(sOut, "YY", yy);
			    sOut = this.Remplaceer(sOut, "CC", cc);
			}
			catch (Exception e) { ; }
			return sOut;
		}
		//
		//---------------------------------------------------------------------------------
		String FormateerTime(String sIn, String Formaat)
		//---------------------------------------------------------------------------------
		{
		   if( sIn.compareToIgnoreCase("NULL")==0) return "null";
		   String sOut = Formaat;
		   try {
			    
			    int i = 0;
			    String hh = sIn.substring(i,i+2); i=i+2;
			    String mi = sIn.substring(i,i+2); i=i+2;
			    String ss =  sIn.substring(i,i+2); i=i+2;
			    String mil =  sIn.substring(i,i+3); i=i+3;
			        
			    sOut = this.Remplaceer(sOut, "HH", hh);
			    sOut = this.Remplaceer(sOut, "MI", mi);
			    sOut = this.Remplaceer(sOut, "SS", ss);
			    sOut = this.Remplaceer(sOut, "MIL", mil);
			}
			catch (Exception e) { ; }
			return sOut;
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
		String LongToDate( long iL )
		//---------------------------------------------------------------------------------
		{  
			GregorianCalendar calendar = new GregorianCalendar();
			try {
			 calendar.setTimeInMillis(iL);
			 TimeZone timeZone = TimeZone.getTimeZone(sTimeZone);
			 calendar.setTimeZone(timeZone);
			}
			catch (Exception e ) {}
			
			String CCYY = (calendar.get(Calendar.YEAR)+"");
			String MM = ((calendar.get(Calendar.MONTH)+1)+"");
			String DD = (calendar.get(Calendar.DAY_OF_MONTH)+"");
			String HH = (calendar.get(Calendar.HOUR_OF_DAY)+"");
			String MI = (calendar.get(Calendar.MINUTE)+"");
			String SE = (calendar.get(Calendar.SECOND)+"");
			String ML = (calendar.get(Calendar.MILLISECOND)+"");
			
			if (MM.length() == 1) {	MM = "0"+MM; }
			if (DD.length() == 1) {	DD = "0"+DD; }
			if (HH.length() == 1) {	HH = "0"+HH; }
			if (MI.length() == 1) {	MI = "0"+MI; }
			if (SE.length() == 1) {	SE = "0"+SE; }
			if (ML.length() == 1) {	ML = "00"+SE; }
			if (ML.length() == 2) {	ML = "0"+SE; }
			
			return (CCYY + MM + DD + HH + MI + SE + ML);
		}
		//
		//---------------------------------------------------------------------------------
		long Now()
		//---------------------------------------------------------------------------------
		{
			return System.currentTimeMillis();
		}
		//
		//---------------------------------------------------------------------------------
		String DateTimeNow(String sFormat)
		//---------------------------------------------------------------------------------
		{
			return FormateerTimeStamp(LongToDate(Now()),sFormat);
		}
		//
		//---------------------------------------------------------------------------------
		String DateNow(String sFormat)
		//---------------------------------------------------------------------------------
		{
			return FormateerDate(LongToDate(Now()),sFormat);
		}
		//
		//---------------------------------------------------------------------------------
		String TimeNow(String sFormat)
		//---------------------------------------------------------------------------------
		{
			return FormateerTime( LongToDate(Now()).substring("CCYYMMDD".length()),sFormat);
		}
		//
		//---------------------------------------------------------------------------------
		String MakeADateTimeGuess(String sIn,String sJavaType)
		//---------------------------------------------------------------------------------
		{
			if( !this.isNumeriekLong(sIn)) return null;
			try {
			// indien alleen maar cijfer wordt er geprobeerd naar intern formaat te zetten
	        //	
			String nu =  LongToDate(Now());
			if( (sJavaType.compareToIgnoreCase("JAVA.SQL.DATE")==0) || (sJavaType.compareToIgnoreCase("JAVA.SQL.TIMESTAMP")==0)) {
				// DD
				if( sIn.length()==2) {  
                    if( isValidDate( sIn, "dd") ) return nu.substring(0,"CCYYMM".length()) + sIn + "000000000";		
                }
				// DDMM
				// MMDD
				if( sIn.length()==4) {  
                    if( isValidDate( sIn, "ddMM") ) {
                    	return nu.substring(0,"CCYY".length()) + sIn.substring(2,4) + sIn.substring(0,2) + "000000000";
                    }
                    if( isValidDate( sIn, "MMdd") ) return nu.substring(0,"CCYY".length()) + sIn + "000000000";	
				}
				// YYMMDD
				// DDMMYY
				if( sIn.length()==6) {  
                    if( isValidDate( sIn, "ddMMyy") ) {
                    	return nu.substring(0,"CC".length()) + sIn.substring(4,6) + sIn.substring(2,4) + sIn.substring(0,2) + "000000000";
                    }
                    if( isValidDate( sIn, "yyMMdd") ) return nu.substring(0,"CC".length()) + sIn + "000000000";		
				}
				// CCYYMMDD
				// DDMMYYCC
				if( sIn.length()==8) {  
                 	if( isValidDate( sIn, "ddMMyyyy") ) {
                    	return sIn.substring(4,8) + sIn.substring(2,4) + sIn.substring(0,2) + "000000000";
                    }
                    if( isValidDate( sIn, "yyyyMMdd") ) return sIn + "000000000";	
				}
			}
			//		
			if( sJavaType.compareToIgnoreCase("JAVA.SQL.TIME")==0) {
				// HH
				if( sIn.length()==2) {  
                    if( isValidDate( sIn, "HH") ) return "00000000" + sIn + "0000000";		
                }
				// HHMI
				if( sIn.length()==4) {  
                    if( isValidDate( sIn, "HHmm") ) return "00000000" + sIn + "00000";		
                }
				// HHMISS
				if( sIn.length()==6) {  
                    if( isValidDate( sIn, "HHmmss") ) return "00000000" + sIn + "000";		
                }
			    // HHMISSmmm
				if( sIn.length()==9) {  
                    if( isValidDate( sIn, "HHmmssSSS") ) return "00000000 " + sIn;		
            	}
			}
			if( sJavaType.compareToIgnoreCase("JAVA.SQL.TIMESTAMP")!=0) return null;
			// TIMESTAMP dus
			// CCYYMMDDHH
			if( sIn.length()==10) {  
				if( isValidDate( sIn, "ddMMyyyyHH") ) {
					return sIn.substring(4,8) + sIn.substring(2,4) + sIn.substring(0,2) + sIn.substring(8,10) + "0000000";		
				}
                if( isValidDate( sIn, "yyyyMMddHH") ) return sIn + "0000000";		
            }
			// CCYYMMDDHHMI
			if( sIn.length()==12) {  
				if( isValidDate( sIn, "ddMMyyyyHHmm") ) {
					return sIn.substring(4,8) + sIn.substring(2,4) + sIn.substring(0,2) + sIn.substring(8,12) + "00000";		
				}
                if( isValidDate( sIn, "yyyyMMddHHmm") ) return sIn + "00000";		
            }
			// CCYYMMDDHHMISS
			if( sIn.length()==14) {  
				if( isValidDate( sIn, "ddMMyyyyHHmmss") ) {
					return sIn.substring(4,8) + sIn.substring(2,4) + sIn.substring(0,2) + sIn.substring(8,14) + "000";		
				}
	            if( isValidDate( sIn, "yyyyMMddHHmmss") ) return sIn + "000";		
            }
			// CCYYMMDDHHMISSs
			if( sIn.length()==15) {  
				if( isValidDate( sIn, "ddMMyyyyHHmmssS") ) {
					return sIn.substring(4,8) + sIn.substring(2,4) + sIn.substring(0,2) + sIn.substring(8,15) + "00";		
				}
                if( isValidDate( sIn, "yyyyMMddHHmmssS") ) return sIn + "00";		
            }
			// CCYYMMDDHHMISSss
			if( sIn.length()==16) {  
				if( isValidDate( sIn, "ddMMyyyyHHmmssSS") ) {
					return sIn.substring(4,8) + sIn.substring(2,4) + sIn.substring(0,2) + sIn.substring(8,16) + "0";		
				}
                if( isValidDate( sIn, "yyyyMMddHHmmssSS") ) return sIn + "0";		
            }
			// CCYYMMDDHH
			if( sIn.length()==17) {  
				if( isValidDate( sIn, "ddMMyyyyHHmmssSSS") ) {
					return sIn.substring(4,8) + sIn.substring(2,4) + sIn.substring(0,2) + sIn.substring(8,17);		
				}
                if( isValidDate( sIn, "yyyyMMddHHmmssSSS") ) return sIn;		
            }
			return null;
			}
			catch( Exception e ) {
				return null;
			}
		}
		//
		//---------------------------------------------------------------------------------
		public boolean isValidDate(String inDate,String sFormaat) 
		//---------------------------------------------------------------------------------
		{
		    if (inDate == null) return false;

		    //set the format to use as a constructor argument
		    SimpleDateFormat dateFormat = new SimpleDateFormat(sFormaat);
		    
		    if (inDate.trim().length() != dateFormat.toPattern().length())  return false;

		    dateFormat.setLenient(false);
		    try {
		      //parse the inDate parameter
		      dateFormat.parse(inDate.trim());
		    }
		    catch (ParseException pe) { return false; }
		    return true;
		  }
		//
		//---------------------------------------------------------------------------------
		String MaakDbDateTimeValue(String sFormVeld,String sJavaType,String sDispFormaat , String sDBFormaat)
		//---------------------------------------------------------------------------------
		{
			String sDbValue=sFormVeld;
			// NOW(), sysdate(), today()
	        String sTemp = sFormVeld.trim() + "                      ";
	        //if( sTemp.substring(0,"NOW".length()).compareToIgnoreCase("NOW")==0) sFormVeld = "NOW";
	        //if( sTemp.substring(0,"SYSDATE".length()).compareToIgnoreCase("SYSDATE")==0) sFormVeld = "NOW";
	        //if( sTemp.substring(0,"TODAY".length()).compareToIgnoreCase("TODAY")==0) sFormVeld = "NOW";
	        if( sTemp.toUpperCase().indexOf("NOW")>=0 ) sFormVeld = "NOW";
	        if( sTemp.toUpperCase().indexOf("SYSDATE")>=0 ) sFormVeld = "NOW";
	        if( sTemp.toUpperCase().indexOf("TODAY")>=0 ) sFormVeld = "NOW";
	        //
	        String sIntern=null;
	        sIntern = this.MakeADateTimeGuess(sFormVeld,sJavaType);  // komt met intern formaat terug anders null
	        // scherm -> intern -> db
	        if( sJavaType.compareToIgnoreCase("JAVA.SQL.TIMESTAMP")==0) {
	          	 if( sFormVeld.compareToIgnoreCase("NOW")==0 ) sFormVeld = this.DateTimeNow(sDispFormaat);
	          	 if( sIntern == null ) sIntern = this.NaarInternDateTimeFormaat(sFormVeld,sDispFormaat);
	          	 sDbValue = this.FormateerTimeStamp(sIntern,sDBFormaat);
	        }
	        //
	        if( sJavaType.compareToIgnoreCase("JAVA.SQL.DATE")==0) {
	          	 if( sFormVeld.compareToIgnoreCase("NOW")==0 ) sFormVeld = this.DateNow(sDispFormaat);
	          	 if( sIntern == null ) sIntern = this.NaarInternDateTimeFormaat(sFormVeld,sDispFormaat);
	          	 sDbValue = this.FormateerTimeStamp(sIntern,sDBFormaat);
	        }
	        //
	        if( sJavaType.compareToIgnoreCase("JAVA.SQL.TIME")==0) {
	          	 if( sFormVeld.compareToIgnoreCase("NOW")==0 ) sFormVeld = this.TimeNow(sDispFormaat);
	          	 if( sIntern == null ) sIntern = this.NaarInternDateTimeFormaat(sFormVeld,sDispFormaat);
	          	 sDbValue = this.FormateerTimeStamp(sIntern,sDBFormaat);
	        }
	        
	        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
	        System.out.println("Scherm=(" + sFormVeld + ") Intern=(" + sIntern + ")" + sDispFormaat + "--" + sDBFormaat+"--"+sJavaType);
	        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
	        return sDbValue;
		}
		//
		//---------------------------------------------------------------------------------
		int BepaalDatumFormaat(String sFormaat)
		//---------------------------------------------------------------------------------
		{
			  String sTemp="";
			  char[] sChar;
			  
			  // Keep only chars
			  sChar = sFormaat.toUpperCase().trim().toCharArray();
			  sTemp = "";
			  for(int i=0;i<sChar.length;i++)
			  {
				if( (sChar[i] >= 'A')&&(sChar[i]<='Z') ) sTemp = sTemp + sChar[i];
			  }
			  //  valid   CCYYMMDDHHMISSMIL
			  sTemp = Remplaceer(sTemp,"MIL","");
			  sTemp = Remplaceer(sTemp,"CC","");
			  sTemp = Remplaceer(sTemp,"YY","");
			  sTemp = Remplaceer(sTemp,"MM","");
			  sTemp = Remplaceer(sTemp,"DD","");
			  sTemp = Remplaceer(sTemp,"HH","");
			  sTemp = Remplaceer(sTemp,"MI","");
			  sTemp = Remplaceer(sTemp,"SS","");
			  // nu mag er niets meer overschieten in de formaatstring	
			  if( sTemp.length() != 0)  return webStraktorDateTime.FORMAT_UNKNOWN;  // error
			  //
			  boolean isDate = false;
			  sTemp = sFormaat.toUpperCase().trim();
			  if( sTemp.indexOf("CC") >= 0 ) isDate = true;
			  if( sTemp.indexOf("YY") >= 0 ) isDate = true;
			  if( sTemp.indexOf("DD") >= 0 ) isDate = true;
			  if( sTemp.indexOf("MM") >= 0 ) isDate = true;
			  //
			  boolean isTime = false;
			  if( sTemp.indexOf("HH") >= 0 ) isTime = true;
			  if( sTemp.indexOf("MI") >= 0 ) isTime = true;
			  if( sTemp.indexOf("SS") >= 0 ) isTime = true;
			  if( sTemp.indexOf("MIL") >= 0 ) isTime = true;
			  // datetime
			  if( (isDate==true)&&(isTime== true)) return webStraktorDateTime.FORMAT_DATETIME;
			  if( isTime ) return webStraktorDateTime.FORMAT_TIME;
			  if( isDate ) return webStraktorDateTime.FORMAT_DATE;
			  return webStraktorDateTime.FORMAT_UNKNOWN;
		}
		//
		//---------------------------------------------------------------------------------
		Date isValidDateTime(String sIn , String iFormaat)
		//---------------------------------------------------------------------------------
		{
			/* http://javatechniques.com/blog/dateformat-and-simpledateformat-examples/
			 * yy year
			 * MM month
			 * dd day
			 * HH 24 uur
			 * mm minutes
			 * ss seconds
			 * SSS miliseconds
			 */
			String sFormaat = iFormaat;
			sFormaat = Remplaceer( sFormaat , "CC" , "yy");
			sFormaat = Remplaceer( sFormaat , "YY" , "yy");
			sFormaat = Remplaceer( sFormaat , "DD" , "dd");
			sFormaat = Remplaceer( sFormaat , "MI" , "mm");
			sFormaat = Remplaceer( sFormaat , "SS" , "ss");
			sFormaat = Remplaceer( sFormaat , "MIL" , "SSS");
			
			SimpleDateFormat format =  new SimpleDateFormat(sFormaat);

	        // See if we can parse the output of Date.toString()
	        try {
	            Date parsed = format.parse(sIn);
	            //System.out.println("-->"+parsed.toString());
	            return parsed;
	        }
	        catch(ParseException pe) {
	            System.out.println("ERROR: Cannot parse " + sIn + "-" + iFormaat);
	            return null;
	        }
	  }
	  //
	  //---------------------------------------------------------------------------------
	  Date isValidInternDateTime(String sIn)
	  //---------------------------------------------------------------------------------
	  {
			/* http://javatechniques.com/blog/dateformat-and-simpledateformat-examples/
			 * yy year
			 * MM month
			 * dd day
			 * HH 24 uur
			 * mm minutes
			 * ss seconds
			 * SSS miliseconds
			 */
			SimpleDateFormat format =  new SimpleDateFormat("yyyyMMddHHmmssSSS");

	        // See if we can parse the output of Date.toString()
	        try {
	            Date parsed = format.parse(sIn);
	  //System.out.println(sIn + "-->" + parsed.toString() + " " + format.toString() );
	            return parsed;
	        }
	        catch(ParseException pe) {
	            System.out.println("ERROR: Cannot parse " + sIn );
	            return null;
	        }
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
	  String MyDateTimeCheck(String sIn,String DBDateTimeFormat)
	  //---------------------------------------------------------------------------------
	  {
		   if( this.isNumeriekLong(sIn) == false ) return null;
		   //
		   String sCC = "20";
		   String sYY = "00";
		   String sMM = "00";
		   String sDD = "00";
		   String sHH = "00";
		   String sMI = "00";
		   String sSS = "00";
		   String sMIL= "000";
		   int idx =  0;
		   
		   sIn = sIn + "000000000000000000000000000000000000000000000000000";
		   try {
			   idx =  DBDateTimeFormat.indexOf("CC");
			   if( idx >= 0 ) sCC = sIn.substring(idx,idx+2);
			   idx =  DBDateTimeFormat.indexOf("YY");
			   if( idx >= 0 ) sYY = sIn.substring(idx,idx+2);
			   //
			   idx =  DBDateTimeFormat.indexOf("MMM");
			   if( idx >= 0 ) {
				   String sMon = sIn.substring(idx,idx+3);
				   if( sMon.compareToIgnoreCase("JAN")==0) sMM = "01";
				   if( sMon.compareToIgnoreCase("FEB")==0) sMM = "02";
				   if( sMon.compareToIgnoreCase("MAR")==0) sMM = "03";
				   if( sMon.compareToIgnoreCase("APR")==0) sMM = "04";
				   if( sMon.compareToIgnoreCase("MAY")==0) sMM = "05";
				   if( sMon.compareToIgnoreCase("JUN")==0) sMM = "06";
				   if( sMon.compareToIgnoreCase("JUL")==0) sMM = "07";
				   if( sMon.compareToIgnoreCase("AUG")==0) sMM = "08";
				   if( sMon.compareToIgnoreCase("SEP")==0) sMM = "09";
				   if( sMon.compareToIgnoreCase("OCT")==0) sMM = "10";
				   if( sMon.compareToIgnoreCase("NOV")==0) sMM = "11";
				   if( sMon.compareToIgnoreCase("DEC")==0) sMM = "12";
			   }
			   else {
			      idx =  DBDateTimeFormat.indexOf("MM");
			      if( idx >= 0 ) sMM = sIn.substring(idx,idx+2);
			   }
			   idx =  DBDateTimeFormat.indexOf("DD");
			   if( idx >= 0 ) sDD = sIn.substring(idx,idx+2); 
			   idx =  DBDateTimeFormat.indexOf("HH");
			   if( idx >= 0 ) sHH = sIn.substring(idx,idx+2); 
			   idx =  DBDateTimeFormat.indexOf("MI");
			   if( idx >= 0 ) sMI = sIn.substring(idx,idx+2); 
			   idx =  DBDateTimeFormat.indexOf("SS");
			   if( idx >= 0 ) sSS = sIn.substring(idx,idx+2);
			   idx =  DBDateTimeFormat.indexOf("MIL");
			   if( idx >= 0 ) sMIL = sIn.substring(idx,idx+3);
			  
		   }
		   catch (Exception e)  {  return null;   }
		   //
		   int iMaand = NaarInt(sMM); if( (iMaand<1) || (iMaand>12) ) return null;
		   int iUur   = NaarInt(sHH); if( (iUur<0) || (iUur>23) ) return null;
		   int iMin   = NaarInt(sMI); if( (iMin<0) || (iMin>59) ) return null;
		   int iSec   = NaarInt(sSS); if( (iSec<0) || (iSec>59) ) return null;
		   int iDag   = NaarInt(sDD); if( (iDag<1) || (iDag>31) ) return null;
		   if( ((iMaand==2) || (iMaand==4) || (iMaand==6) || (iMaand==9) || (iMaand==11)) && (iDag>30) ) return null;
		   if( iMaand == 2 ) {
		    int iJaar = NaarInt(sYY);
		    if ( ((iJaar % 4)!=0) && (iDag>28) ) return null;
		   }
		   return sIn;
	  }
	  //
	  //---------------------------------------------------------------------------------
	  String NaarInternDateTimeStrict(String sIn , String sFormaat)
	  //---------------------------------------------------------------------------------
	  {
		  //if( sIn.trim().length() != sFormaat.trim().length()) return null;
		  if( sIn.compareToIgnoreCase(this.sDefaultDateTime)==0 ) return this.sDefaultDateTime; //ok + op die manier is naderhand defaulttime een fout
		  String sIntern = NaarInternDateTimeFormaat(sIn,sFormaat);
		  if( sIntern.compareToIgnoreCase(this.sDefaultDateTime)==0 ) return null;
		  if( MyDateTimeCheck(sIntern,"CCYYMMDDHHMISSMIL") == null ) return null;
		  return sIntern;
	  }
	  //
	  //---------------------------------------------------------------------------------
	  String NaarInternDateStrict(String sIn , String sFormaat)
	  //---------------------------------------------------------------------------------
	  {
		  //if( sIn.trim().length() != sFormaat.trim().length()) return null;
		  if( sIn.compareToIgnoreCase(this.sDefaultDate)==0 ) return this.sDefaultDate; // ok
		  String sIntern = NaarInternDateFormaat(sIn,sFormaat);
		  if( sIntern.compareToIgnoreCase(this.sDefaultDate)==0 ) return null;
		  if( MyDateTimeCheck(sIntern+"0000000","CCYYMMDDHHMISSMIL") == null ) return null;
		  return sIntern;
	  }
	  //
	  //---------------------------------------------------------------------------------
	  String NaarInternTimeStrict(String sIn , String sFormaat)
	  //---------------------------------------------------------------------------------
	  {
		  //if( sIn.trim().length() != sFormaat.trim().length()) return null;
		  if( sIn.compareToIgnoreCase(this.sDefaultTime)==0 ) return this.sDefaultTime; // ok
		  String sIntern = NaarInternTimeFormaat(sIn,sFormaat);
		  if( sIntern.compareToIgnoreCase(this.sDefaultTime)==0 ) return null;
		  if( MyDateTimeCheck("20000101"+sIntern,"CCYYMMDDHHMISSMIL") == null ) return null;
		  return sIntern;
	  }
	  //
	  //---------------------------------------------------------------------------------
	  long naarLong(String il)
	  //---------------------------------------------------------------------------------
	  {
		  try {
		   long l = Long.parseLong(il);
		   return l;
		  }
		  catch (Exception e) { return -1L; }
	  }
}
