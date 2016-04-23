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

public class parseCommand {

	public static final int cmdUNKNOWN   = 301;
	public static final int cmdUPPER     = 301;
	public static final int cmdLOWER     = 302;
	public static final int cmdTITLECASE = 303;
	public static final int cmdTRIM      = 304;
	public static final int cmdGET       = 305;
	public static final int cmdREMOVE    = 306;
	public static final int cmdREGEX     = 307;
	public static final int cmdGETFIELD          = 308;
	public static final int cmdGETHTMLCONTENT    = 309;
	public static final int cmdGETINPUTPARAMETER = 310;
	public static final int cmdKEEPNUMBER = 311;
	public static final int cmdKEEPFLOAT = 312;
	
	int OID=1;
	int CommandType;
	String params;
	
	//
	//---------------------------------------------------------------------------------
	parseCommand(int io)
	//---------------------------------------------------------------------------------
	{
		OID = io;
		CommandType = cmdUNKNOWN;
		params      = null;
	}
	//
	//---------------------------------------------------------------------------------
	int getCommandToken(String sI)
	//---------------------------------------------------------------------------------
	{
		String ss = sI.trim();
		if( ss.compareToIgnoreCase("UPPER") == 0 ) return cmdUPPER;
		if( ss.compareToIgnoreCase("LOWER") == 0 ) return cmdLOWER;
		if( ss.compareToIgnoreCase("TITLECASE") == 0 ) return cmdTITLECASE;
		if( ss.compareToIgnoreCase("TRIM") == 0 ) return cmdTRIM;
		if( ss.compareToIgnoreCase("GET") == 0 ) return cmdGET;
		if( ss.compareToIgnoreCase("REMOVE") == 0 ) return cmdREMOVE;
		if( ss.compareToIgnoreCase("REGEX") == 0 ) return cmdREGEX;
		if( ss.compareToIgnoreCase("GETHTMLCONTENT") == 0 ) return cmdGETHTMLCONTENT;
		if( ss.compareToIgnoreCase("GETFIELD") == 0 ) return cmdGETFIELD;
		if( ss.compareToIgnoreCase("STRIPHTMLTAGS") == 0 ) return cmdGETHTMLCONTENT;   // same as getcontent
		if( ss.compareToIgnoreCase("GETINPUTPARAMETER") == 0 ) return cmdGETINPUTPARAMETER;
		if( ss.compareToIgnoreCase("KEEPNUMBERS") == 0 ) return cmdKEEPNUMBER;
		if( ss.compareToIgnoreCase("KEEPNUMBER") == 0 ) return cmdKEEPNUMBER;
		if( ss.compareToIgnoreCase("KEEPDIGITS") == 0 ) return cmdKEEPNUMBER;
		if( ss.compareToIgnoreCase("KEEPDIGIT") == 0 ) return cmdKEEPNUMBER;
		if( ss.compareToIgnoreCase("KEEPFLOAT") == 0 ) return cmdKEEPFLOAT;
		if( ss.compareToIgnoreCase("KEEPDECIMAL") == 0 ) return cmdKEEPFLOAT;
		if( ss.compareToIgnoreCase("KEEPDECIMALS") == 0 ) return cmdKEEPFLOAT;
		return -1;
	}
	//
	//---------------------------------------------------------------------------------
	String sho()
	//---------------------------------------------------------------------------------
	{
	  String sRet = "";
	  switch( this.CommandType )
	  {
	   case cmdUPPER     : { sRet = "UPPER"; break; }
	   case cmdLOWER     : { sRet = "LOWER"; break; }
	   case cmdTITLECASE : { sRet = "TitleCase"; break; }
	   case cmdTRIM      : { sRet = "TRIM"; break; }
	   case cmdGET       : { sRet = "GET"; break; }
	   case cmdREMOVE    : { sRet = "REMOVE"; break; }
	   case cmdREGEX     : { sRet = "REGEX"; break; }
	   case cmdGETFIELD  : { sRet = "GetFIELD"; break; }
	   case cmdGETHTMLCONTENT    : { sRet = "GetHTMLContent"; break; }
	   case cmdGETINPUTPARAMETER : { sRet = "GetInputParameter"; break; }
	   case cmdKEEPNUMBER : { sRet = "KeepNumber"; break; }
	   default           : { sRet = "UNKNOWN"; break; }
	  }
	  if( this.params != null ) {
		  sRet = sRet + "[" + this.params + "]";
	  }
	  return sRet;
	}
	
}
