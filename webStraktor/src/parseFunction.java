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

import java.util.ArrayList;

public class parseFunction {

	int    OID;
	String Name;
	String URL;
	String Label;
	String startPattern;
	String endPattern;
	String passPhrase;
	String xpath;
	int    MaxIterations;
	ArrayList<Integer> lstOccurs;
	ArrayList<Integer> lstSkip;
	ArrayList<parseInstruction> lstInstructies;
	long traceID=0L;
    
	parseFunction(String iName,int iO)
	{
		OID = iO;
	    Name = iName;
	    Label = iName;
	    startPattern = null;
	    endPattern = null;
	    passPhrase = null;
	    xpath=null;
		lstInstructies = new ArrayList<parseInstruction>();
		URL = null;
		MaxIterations = -1;
		lstOccurs = new ArrayList<Integer>();
		lstSkip = new ArrayList<Integer>();
		traceID=0L;
	}
	
	String sho()
	{
		  String sT = "";
		  for(int j=0;j<lstOccurs.size();j++) {
			  if( j==0 ) sT = "OCCURS="+ lstOccurs.get(j);
			        else sT = sT + "," + lstOccurs.get(j);
		  }
		  for(int j=0;j<lstSkip.size();j++) {
			  if( j==0 ) sT = " SKIP="+lstSkip.get(j);
		        else sT = sT + "," + lstSkip.get(j);
	      }
		  String sXp = ""; if( xpath != null) sXp = "Xpath="+xpath;
		  return 
		       "FUNCTION (" + Name + ") " + 
			   "Start=" + startPattern + " " +
			   "End=" + endPattern + " " +
			   "Passphrase=" + passPhrase + " " +
			   "URL=" + URL + " " +
			   "MAXITERATIONS=" + MaxIterations + " " + sXp + " " +
			   sT.trim() + " " +
			   "TRACE=" + traceID;
	}
}
