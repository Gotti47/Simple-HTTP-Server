import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * A simple multi-threaded HTTP server using TCP/IP sockets
 * that serves static files using the GET method.
 *
 * POST method not yet implemented
 *
 * @author Newton Bujiku
 * @since Dec,2014
 */
public class HTTPServer {
    public  static  String DOCUMENT_ROOT="";//A document root that the server will be serving files from

    private static ServerSocket server;//a server socket to listen for the incoming connections

    public static final String SERVER = "Java HTTP Server/ 1.0 ";
    public static  int PORT_NUMBER=0;
    public static String HTTP_SERVER_ADDRESS;
    public static boolean stopped=false;

    /**
     * Opens a new socket and start listening for requests
     */
    public static void startServer(){

        Socket client;

        try {
            server = new ServerSocket(PORT_NUMBER);//open a socket for listening on this port

            //send this info to the display area
            HTTPGUI.updateDisplay("Server started on "+new Date().toString()+" at "
                            +server.getInetAddress() +" on port "
                            + server.getLocalPort()
            );
             while (true){

                client = server.accept();//listen for incoming connections
                //serve this client on  a separate thread and go back to
                //listen of another one
                new Thread(new HTTPThread(client)).start();//serve this client on a separate thread
                System.out.println("Client connected : "+client.getInetAddress()+":"+PORT_NUMBER);//log

            }

        } catch (IOException e) {
            //unable to open socket
            //socket in use

            if(!stopped){
            	stopped=true;
                //server not running but was not stopped intentionally
                HTTPGUI.updateDisplay("Could not start server on port "+PORT_NUMBER);

            }



        }

    }

    /**
     * Stop the server
     */

    public  static void stopServer(){
        stopped=true;//we are stopping intentionally so we should be able to control the IOException
                    //when we close the socket abruptly
        if (server!=null){
            try {
                server.close();//close the socket to stop the server
                //send this info to the display area
                HTTPGUI.updateDisplay("Server stopped on "+new Date().toString());
            } catch (IOException e) {

            }
        }
    }


}
