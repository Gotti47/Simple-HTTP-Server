import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * A background thread class implementing the Runnable interface,
 * serves clients by invoking several methods of the HTTP handler class.
 *
 * @author Newton Bujiku
 * @since Dec,2014
 */
public class HTTPThread implements Runnable {

    private Socket socket;//holds a reference to the opened socket for communication with the client
    /**
     * Initialises the handler object
     * @param socket socket object for communication
     */
    public HTTPThread(Socket socket){
        this.socket = socket;
    }

    /**
     * The thread runs here
     */
    @Override
    public void run() {

        BufferedReader reader=null;
        try {
            reader= new BufferedReader(new InputStreamReader(socket.getInputStream()));//for reading from the socket
            String line =null;
            HTTPHandler handler = new HTTPHandler(socket);
            //System.out.println("\t\tREQUEST HEADERS\t\t");//start logging the request headers
            HTTPGUI.updateDisplay("\t\tREQUEST HEADERS\t\t");//start logging the request headers
            while(!(line=reader.readLine()).equals("")){//HTTP uses \r\n to separate,the body and headers,read until
                                                        //"" is reached
               HTTPGUI.updateDisplay(line);//log
                if(line.contains(":")){
                  handler.setRequestHeader(line);//we have a header here,key-value pair separated by :
                }else{
                    handler.setRequestLine(line.split(" "));//the request line containing method,uri,and HTTP version
                }

            }
            System.out.println("\t\tNOW HANDLING REQUEST\t\t");//log

            handler.handleRequest();//start serving the client

        } catch (IOException e) {
            //could not open stream from the socket
            //let the client know an error happened here
            //how????
        }finally {

            //close the reader we don't need any more
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }

            }
        }

        System.out.println("\t\tDONE SERVING..!\t\t");//the client has been served with a web page

    }



}
