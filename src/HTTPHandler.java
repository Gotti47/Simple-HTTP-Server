import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This classes handles all the logic in HTTP protocol but not all of it
 *
 * @author Newton Bujiku
 * @since Dec, 2014
 */
public class HTTPHandler {

    /*
    HTTP constants
    Read the protocol specification itself to understand them
     */
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_HEAD= "HEAD";
    public static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_VERSION = "HTTP/1.1";
    public static final int HTTP_STATUS_CODE_OK = 200;
    public static final int HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR=500;
    public static final int HTTP_STATUS_CODE_FILE_NOT_FOUND=404;
    public static final String HTTP_STATUS_MESSAGE_OK = "OK";
    public static final String HTTP_STATUS_MESSAGE_INTERNAL_SERVER_ERROR="Internal Server Error";
    public static final String HTTP_STATUS_MESSAGE_FILE_NOT_FOUND="File Not Found";

    public static final String HTTP_CRLF = "\r\n";

    private SimpleDateFormat dateFormat;
    private String[] requestLine;//holds the the first line sent by the client in the request
    private Map<String, String> requestHeadersMap;//holds a key pair value of the HTTP request for several uses
    private Socket socket;//holds a reference to the opened socket for communication with the client
    private PrintWriter writer;//for writing messages to the client

    /**
     * Initialises the handler object
     *
     * @param socket socket object for communication
     */
    public HTTPHandler(Socket socket) {
        this.socket = socket;
        String pattern = "EEE, dd MMM yyyy HH:mm:ss zzz";//standardized date pattern
        dateFormat = new SimpleDateFormat(pattern);
        requestHeadersMap = new HashMap<String, String>();
        openSocketOutputStream(socket);//open the stream so that no NullPointerException is thrown later on
    }

    private void openSocketOutputStream(Socket socket) {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
            //a stream could not be opened
            //why???dunno
        }
    }

    /**
     * Handles a request with POST method
     *
     * @param requestedFileName the URI to the file to handle this POST request
     */

    public void doHTTPPOST(String requestedFileName) {

        //TODO
    }

    /**
     * Handles a request with GET method
     *
     * @param requestedFileName URI to the file to be sent back to the user
     */
    public void doHTTPGET(String requestedFileName) {
        if (requestedFileName.equals("/")) {//request to the document root,should return an index file
            requestedFileName = "/index.html";
        }
        BufferedReader fileReader = null;//for reading a file
        File file = new File(HTTPServer.DOCUMENT_ROOT + requestedFileName);//careful,an exception could be thrown here??
        if (file.exists()) {//check if the requested file exists
            String contentType = getContentType(requestedFileName);//get the content type of the request file
            writeResponseHeaders(contentType, file.length());
            writer.print(HTTP_CRLF);//end the headers, double \r\n
            writer.print(HTTP_CRLF);

            try {
                fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = null;
                while ((line = fileReader.readLine()) != null) {//read line by line until the end of the file
                    writer.print(line);//send the line to the client
                }

            } catch (FileNotFoundException e) {
                // e.printStackTrace();
                //let the client know that the server encountered an error
                sendResponseServerError();
                HTTPGUI.updateDisplay("The server encountered an error");//log this

            } catch (IOException e) {
                //e.printStackTrace();
                //let the client know that the server encountered an error
                sendResponseServerError();
                HTTPGUI.updateDisplay("Requested url "+requestedFileName+" does not exist");//log this

            }


        } else {
            //send 404 error
            //file  not be found
           // HTTPGUI.updateDisplay("File does not exist");
            //fix for safari
                //support safari by not sending error 404 message when it asks for .ico files
                sendResponseFileNotFound(requestedFileName);


        }


        closeResources(fileReader);//close open resources

    }

    /**
     * Checks if any the opened resources i.e socket,fileReader,writer
     * are still open and closes them
     *
     * @param fileReader BufferedReader object for reading the file
     */
    private void closeResources(BufferedReader fileReader) {
        try {
            if (fileReader != null) {
                fileReader.close();
            }

            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the content type of the file
     * At the moment only few MIME types are supported
     *
     * @param requestedFileName URI of the requested file
     * @return MIME type of the requested file
     */
    private String getContentType(String requestedFileName) {
        if (requestedFileName.endsWith("html")) {
            return "text/html";
        } else if (requestedFileName.toLowerCase().endsWith("js")) {
            return "text/javascript";
        } else if (requestedFileName.toLowerCase().endsWith("txt")) {
            return "text/plain";
        } else if (requestedFileName.toLowerCase().endsWith("png")) {
            return "image/png";
        } else if (requestedFileName.toLowerCase().endsWith("jpeg") || requestedFileName.endsWith("jpg")) {
            return "image/jpeg";
        } else if (requestedFileName.toLowerCase().endsWith("css")) {
            return "text/css";
        }else if(requestedFileName.toLowerCase().endsWith("mp3")){
            return "audio/mpeg";
        }else if(requestedFileName.toLowerCase().endsWith("ico")){
            //safari keeps asking for this kind of files
            return "image/x-icon";
        }


        return "";
    }

    /**
     * Handles HEAD method by only sending the HTTP response headers
     *
     * @param requestedFileName URI of the file
     */
    public void doHTTPHEAD(String requestedFileName) {

        // send headers of a file only
        //don't send the file
        if (requestedFileName.equals("/")) {//request to the document root,should return an index file
            requestedFileName = "/index.html";
        }
        File file = new File(HTTPServer.DOCUMENT_ROOT+requestedFileName);

        if(file.exists()){
            //apparently calling the doHTTPGET method is the only thing that works here
            //and it does return only the headers???how does it work??still learning HTTP
            //writing the headers explicitly does not work
            doHTTPGET(requestedFileName);
            //file exists send the headers
            //writeResponseHeaders(getContentType(requestedFileName),file.length());
            //writer.print("Connection : close");
            writer.print(HTTP_CRLF);//end the headers, double \r\n
            writer.print(HTTP_CRLF);

        }else{
            //the file is not on this server
            sendResponseFileNotFound(requestedFileName);
        }
    }

    /**
     * Write the headers to the HTTP response before sending the message body
     *
     * @param contentType   MIME type of the file to be sent back to the client
     * @param contentLength length of the file in bytes(octets)
     */
    public void writeResponseHeaders(String contentType, long contentLength) {
        //get a standardized Date format like Sun, 12 Oct 2015 21:45:06 GMT


        HTTPGUI.updateDisplay("\t\tRESPONSE HEADERS\t\t");//get ready to start writing the headers
        writer.print(HTTP_VERSION + " " + HTTP_STATUS_CODE_OK + " " + HTTP_STATUS_MESSAGE_OK+HTTP_CRLF);//status line
        HTTPGUI.updateDisplay(HTTP_VERSION + " " + HTTP_STATUS_CODE_OK + " " + HTTP_STATUS_MESSAGE_OK);//log

        writer.print("Date: " + dateFormat.format(new Date())+HTTP_CRLF);//Date
        HTTPGUI.updateDisplay("Date: " + dateFormat.format(new Date()));//log

        writer.print("Content-Length: " + contentLength+HTTP_CRLF);//Content-length
        HTTPGUI.updateDisplay("Content-Length: " + contentLength);//log

        writer.print("Server : " + HTTPServer.SERVER+HTTP_CRLF);//Server
        HTTPGUI.updateDisplay("Server " + HTTPServer.SERVER);//log

        writer.print("Content-Type: " + contentType + "; charset=UTF-8");//Content-type
        HTTPGUI.updateDisplay("Content-Type: " + contentType);//log


    }

    /**
     * To be invoked when an internal error has been encountered
     */

    public void sendResponseServerError(){

        writer.print(HTTP_VERSION+ HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR+
                HTTP_STATUS_MESSAGE_INTERNAL_SERVER_ERROR+HTTP_CRLF);

        writeErrorHeaders();


        writeErrorMessageBody(HTTP_STATUS_MESSAGE_INTERNAL_SERVER_ERROR,
                "The server at "+HTTPServer.HTTP_SERVER_ADDRESS+" encountered internal error ",
                HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR);


    }

    /**
     * Sends the error message body to the client
     *
     * @param httpMessage the specific error message
     * @param bodyMessage the body of the response
     * @param httpCode HTTP error code
     */

    private void writeErrorMessageBody(String httpMessage,String bodyMessage,int httpCode) {
        writer.print(
              "<html>" +
              "<head>" +
                      "<title>"+httpCode+" "+httpMessage+"</title>" +
                      "<style type=\"text/css\">"+
                      "body {margin:100px 0px; padding:0px;text-align:center;}"+
                      "p,h1{margin :0px auto; width : 500px}"+
                      "</style>"+
               "</head>"+

              "<body>" +

               "<h1>"+httpMessage+"</h1>"+
               "<p><br/><br/>"+bodyMessage+"</p>"+

              "</body>" +
               "</html>"
        );


    }


    /**
     * Writes the error headers
     */
    private void writeErrorHeaders() {
        writer.print("Date: "+dateFormat.format(new Date())+HTTP_CRLF);

        writer.print("Content-Type: "+"text/html"+"; charset=UTF-8"+HTTP_CRLF);

        writer.print("Server: "+HTTPServer.SERVER+HTTP_CRLF);

        writer.print(HTTP_CRLF);
        writer.print(HTTP_CRLF);
    }

    /**
     * Send an error message when a client has requested a file that does not exist on this server
     * @param url URL of non existing file
     */

    public void sendResponseFileNotFound(String url){

        writer.print(HTTP_VERSION+" "+HTTP_STATUS_CODE_FILE_NOT_FOUND+" "+HTTP_STATUS_MESSAGE_FILE_NOT_FOUND);

        writeErrorHeaders();

        writeErrorMessageBody(HTTP_STATUS_MESSAGE_FILE_NOT_FOUND,
                "The requested URL "+url+" was not found on this server",
                HTTP_STATUS_CODE_FILE_NOT_FOUND);
    }

    /**
     * Determine the method in the request line and invoke appropriate method
     */
    public void handleRequest() {
        if (requestLine[0].trim().equals(HTTP_METHOD_GET)) {
            doHTTPGET(requestLine[1].trim());
        } else if (requestLine[0].trim().equals(HTTP_METHOD_POST)) {
            doHTTPPOST(requestLine[1].trim());
        } else if (requestLine[0].trim().equals(HTTP_METHOD_HEAD)){
            doHTTPHEAD(requestLine[1].trim());
        }else{
            //send error report telling the client the server could not understand the request
        }

    }

    /**
     * Parse a header into a key value pair and put it in
     * a map
     *
     * @param headerLine a header to be parsed
     */
    public void setRequestHeader(String headerLine) {
        for (int i = 0; i < headerLine.length(); ++i) {
            if (headerLine.charAt(i) == ':') {
                //get the element before the : as key
                //and after : as value
                requestHeadersMap.put(headerLine.substring(0, i), headerLine.substring(i + 1));
                break;
            }

        }

    }

    /**
     * Set the provided request line
     * @param requestLine a request line
     */

    public void setRequestLine(String[] requestLine) {
        this.requestLine = requestLine;
    }

    /**
     *Set the socket
     * @param socket socket object for communication
     */

    public void setSocket(Socket socket) {
        this.socket = socket;
        if (writer == null) {
            openSocketOutputStream(socket);
        }
    }
}
