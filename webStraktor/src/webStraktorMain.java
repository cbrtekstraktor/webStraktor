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

public class webStraktorMain {

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	

		webStraktorSettings xMSet = new webStraktorSettings(args);
		System.out.println("webStraktor version [" + xMSet.version + "]  build [" + xMSet.build + "]");
		webStraktorParseQueue x = new webStraktorParseQueue(xMSet , args );
		boolean isOk = x.executeQueu();
		x.CheckProxyTime();
		if( isOk ) System.exit(0); else { System.out.println("exit != 0\n"); System.exit(1);};
	}

}
