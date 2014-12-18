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
    public static boolean stop=false;

    public static void startServer(){
      /*  //Check to see if required arguments have been passed through
        verifyCLIArguments(args);
        //get the port number
        int portNumber = parsePortNumber(args);

        if(args[1].endsWith("/")){
            DOCUMENT_ROOT = args[1].substring(0,args[1].length());//remove the last /,it is provided by HTTP Request
        }else{
            DOCUMENT_ROOT =args[1];
        }
        */
        Socket client;

        try {
            server = new ServerSocket(PORT_NUMBER);//start the server
            HTTP_SERVER_ADDRESS=server.getInetAddress()+":"+server.getLocalPort();
            HTTPGUI.updateDisplay("Server started on " + new Date().toString() + " at "
                            + server.getInetAddress() + " on port "
                            + server.getLocalPort()
            );

            System.out.println("Server started on "+new Date().toString()+" at "
                            +server.getInetAddress() +" on port "
                            + server.getLocalPort()
            );
             while (true){


                client = server.accept();//listen for incoming connections
                //serve this client on  a separate thread and go back to
                //listen of another one
                new Thread(new HTTPThread(client)).start();
                System.out.println("Client connected : "+client.getInetAddress()+":"+PORT_NUMBER);//log
                HTTPGUI.updateDisplay("Client connected : " + client.getInetAddress() + ":" + PORT_NUMBER);//log



            }
           // stopServer();

        } catch (IOException e) {
            //unable to open socket
            //socket in use

            if(!stop){
                System.err.println("Could not start server on port "+PORT_NUMBER);

                //System.exit(1);
                HTTPGUI.updateDisplay("Could not start server on port " + PORT_NUMBER + "\n PORT IN USE");
            }

            HTTPGUI.updateDisplay("Server stopped on " + new Date().toString());


        }

    }

    public  static void stopServer(){
        stop=true;
        if (server!=null){
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Parses one of the CLI arguments to get a valid port number
     * @param args an array of CLI arguments
     * @return
     */
    private static int parsePortNumber(String[] args) {
        int portNumber = 0;
        try{
            portNumber = Integer.parseInt(args[0]);//get integer version of the port
        }catch (NumberFormatException e){
            //the port number was not valid
            System.err.println("Invalid port number !"+args[0]);
            System.out.println("Please provide a valid port number");
            System.exit(1);

        }
        return portNumber;
    }


    /**
     * Verifies to see if the server was started with proper
     * environmental variables
     *
     * @param args command line arguments
     */

    /*
    private static void verifyCLIArguments(String[] args) {
        if(args.length!=2){//require two arguments
            System.err.println("Unknown command!");
            System.out.println("Usage : java HTTPServer <port number> </path/to/document root/>");
            System.out.println("Make sure your document root has an index.html file");
            System.exit(1);

        }
    }
    */

}
