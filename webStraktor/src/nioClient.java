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


import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;



public class nioClient {

	private int BUFSIZE = 2048;
	private SocketChannel client = null;
	private Selector selector = null;
	private ArrayList<String> msgList = null;
	private boolean initOK=false;
	private int teller=0;
	
	//------------------------------------------------------------
	nioClient(int iPort)
	//------------------------------------------------------------
	{
		msgList = new ArrayList<String>();
		initClient(iPort);
	}
	//------------------------------------------------------------
	private void logit(int level , String sIn)
	//------------------------------------------------------------
	{
		System.out.println( "CLIENT - " + sIn );
	}
	//------------------------------------------------------------
	private boolean initClient(int iPort)
	//------------------------------------------------------------
	{
		try {
		  client = SocketChannel.open();
		  client.configureBlocking(false);
		  client.connect( new java.net.InetSocketAddress("localhost",iPort));
		  selector = Selector.open();
		  client.register( selector , SelectionKey.OP_CONNECT );
		  initOK=true;
		  logit(5,"Selector registered for CONNECT on port [" + iPort + "]");
		}
		catch( Exception e ) {
			logit(0,"Open Error " + e.getMessage());
			logit(0,LogStackTrace(e));
			return false;
		}
		return true;
	}
	//------------------------------------------------------------
	public int writeClient(String sMsg)
	//------------------------------------------------------------
	{
		String sLijn = sMsg;
		msgList.add(sLijn);
		for(int i=0;i<10;i++) { if ( flush() == 0) return 0; }
	    return -1;
	}
	//------------------------------------------------------------
	public int flush()
	//------------------------------------------------------------
	{
		if( initOK == false ) return -1;
		int aantal = msgList.size();
		for(int i=0;i<aantal;i++)
		{
	       		if( write( msgList.get(0) ) != 0) return -1;
	       		msgList.remove(0);
	   }
	   return 0;
	}
	//------------------------------------------------------------
	private int write(String sMsg)
	//------------------------------------------------------------
	{
		try {
		   int nRet = selector.select(50);   // Timeout
		   if( nRet <= 0 ) return -1;
		   Set<SelectionKey> keys = selector.selectedKeys();
		   Iterator<SelectionKey> i = keys.iterator();
		   while( i.hasNext() )
		   {
			   SelectionKey key = i.next();
			   i.remove();
			   //
			   SocketChannel channel = (SocketChannel)key.channel();
			   //
			   if( key.isConnectable()) {
				   logit(5,"Server found");
				   // close pending connections
				   if( channel.isConnectionPending()) channel.finishConnect();
				   //
				   channel.register( selector , SelectionKey.OP_WRITE );
				   continue;
			   }
			   if( key.isWritable() ) {
				   if( writeBuffer( channel , sMsg ) != 0 ) return -1;
				   continue;
			   }
		   }
		}
		catch( Exception e ) {
			logit(0,"Write() " + e.getMessage());
			logit(0,LogStackTrace(e));
			initOK=false;
			return -1;
		}
		return 0;
	}
	//------------------------------------------------------------
	private int writeBuffer(SocketChannel channel , String sMsg)
	//------------------------------------------------------------
	{
		try {
			/*
		 ByteBuffer buffer = null;
		 buffer = ByteBuffer.wrap( new String(("<?xml version=\"1.0\"?><msg><msgid>"+(teller++)+"</msgid><![CDATA["+sMsg+"]]></msg>")).getBytes() );
		 int len = channel.write(buffer);
		 //logit( "len" + len + " " + buffer.capacity());
		 if( len == buffer.capacity() ) return 0;  else return -1;*/
			
			ByteBuffer buf = ByteBuffer.allocate(BUFSIZE);
			buf.clear();
			String sL = "<?xml version=\"1.0\"?><msg><msgid>"+(teller++)+"</msgid><![CDATA["+sMsg+"]]></msg>";
			buf.put(sL.getBytes());
			buf.flip();
			while(buf.hasRemaining()) {
			    channel.write(buf);
			}
			//Thread.sleep(10);  // blijkbaar nodig
			return 0;
		}
		catch( Exception e ) {
			logit(0,"writeBuffer " + e.getMessage());
			logit(0,LogStackTrace(e));
			return -1;
		}
	}
	//------------------------------------------------------------
	public void close()
	//------------------------------------------------------------
	{
		if( initOK == false ) return;
		try {
			Thread.sleep(500);
			this.write("END");flush();
			client.close();
		    logit(0,"close()");
		    if( msgList.size() > 0 )  logit(0,"Number of undelivered messages = " +  msgList.size() );
		}
		catch( Exception e ) {
			logit(0,"close() " + e.getMessage());
			logit(0,LogStackTrace(e));
		}
	}
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
}
