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

public class parseInstruction {

	public static final int isVARIABLE    = 100;
	public static final int isLINK        = 101;
	public static final int isFOREACH     = 102;
	public static final int isCUT         = 103;
	public static final int isFORM        = 104;
	public static final int isBLOB        = 105;
	
	
	class HTMLForm
	{
		String FormURL = null;
		String FormName = null;
		ArrayList<String> lstInput = null;
		HTMLForm()
		{
			FormURL  = null;
			FormName = null;
			lstInput = new ArrayList<String>();
		}
	}
	int    OID=-1;
	String Name;
	String Label;
	String startPattern;
	String endPattern;
	String regex;
	String xpath;
	String referencedForEach;
	Boolean ignore;
	int    Tipe;
	ArrayList <parseCommand> lstCommands;
	String codestring;
	HTMLForm Form;
	long traceID;
		
	parseInstruction(String iName,int io)
	{
		OID   = io;
		Name  = iName;
		Label = iName;
		Tipe  = isVARIABLE;
	    startPattern = null;
		endPattern = null;
		xpath = null;
		regex = null;
		referencedForEach=null;
		lstCommands = new ArrayList<parseCommand>();
		ignore = false;
		codestring = null;
		Form = new HTMLForm();
		traceID=0L;
	}
	private String shoTipe()
	{
		if( Tipe == isVARIABLE ) return "VAR";
		if( Tipe == isLINK )     return "LINK";
		if( Tipe == isFOREACH )  return "FOREACH";
		if( Tipe == isCUT )      return "CUT";
		if( Tipe == isFORM )     return "FORM";
		if( Tipe == isBLOB )     return "BLOB";
		return ""+Tipe;
	}
	private String shoName()
	{
		if( Label.compareToIgnoreCase(Name)!=0) return Label; else return Name;
	}
	String sho()
	{
		if( Tipe != isFORM )
			return
			" INSTRUCTION (" + shoName() + ") Label=" + Label + " " +
			"Tipe=" + shoTipe() + " " +
			"Start=" + startPattern + " " +
			"End=" + endPattern + " " +
			"Xpath=" + xpath + " " +
			"Regex=" + regex + " " + 
			"Call=" + referencedForEach + " " +
			"CMD=" + codestring + " " +
			"TRC=" + traceID + " " +
			"RfrncdFE=" + referencedForEach;
		else
			return
			" INSTRUCTION (" + shoName() + ") Label=" + Label + " " +
			"Tipe=" + shoTipe() + " " +
	        "FormName=" + Form.FormName + " " +
	        "FormURL=" + Form.FormURL + " " +
	        "Fields=" + Form.lstInput.toString() + " " +
	    	"TRC=" + traceID;
	}
}
