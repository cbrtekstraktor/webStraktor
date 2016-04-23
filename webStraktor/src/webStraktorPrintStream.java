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
import java.io.FileOutputStream;
import java.io.PrintStream;


public class webStraktorPrintStream {

	 PrintStream writer=null;
	 //
	 //---------------------------------------------------------------------------------
	 webStraktorPrintStream(String FNaam)
	 //---------------------------------------------------------------------------------
	 {
		 writer = OpenWriteFile(FNaam);
	 }
	 //
	 //---------------------------------------------------------------------------------
	 boolean isActive()
	 //---------------------------------------------------------------------------------
	 {
		 if( writer == null ) return false;
		 return true;
	 }
	 //
	 //---------------------------------------------------------------------------------
	 void logit(String sL)
	 //---------------------------------------------------------------------------------
	 {
		 System.out.println(sL);
	 }
	 //
	 //---------------------------------------------------------------------------------
	 private PrintStream OpenWriteFile(String FNaam)
	 //---------------------------------------------------------------------------------
	 {
	    	try {
	    	   writer = new PrintStream(new FileOutputStream(FNaam,false));
	    	   return writer;
	    	}
	    	catch (Exception e )
	    	{
	    		logit("Could not open [" + FNaam + "] for writing" + e.getMessage());
	    		return null;
	    	}
	  }
	 //
	 //---------------------------------------------------------------------------------
	 void close()
	 //---------------------------------------------------------------------------------
	 {
	    	try {
	    	   if( writer != null ) writer.close();
	    	   writer = null;
	     	   return;
	     	}
	     	catch (Exception e )
	     	{
	     		logit("Could not close file writer" + e.getMessage());
	     		return;
	     	}
	 }
	 //
	 //---------------------------------------------------------------------------------
	 void println(String sLijn)
	 //---------------------------------------------------------------------------------
	 {
		    if( writer == null ) {
		    	System.out.println(sLijn);
		    	return;
		    }
	    	try {
	    	   writer.println(sLijn);
	     	   return;
	     	}
	     	catch (Exception e )
	     	{
	     		logit("Could not write to file " + e.getMessage());
	     		return;
	     	}
	 }
	 
}
