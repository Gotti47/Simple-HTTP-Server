import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

/**
 * A subclass of JFrame.
 * Holds all the Graphical User Interface components in place.
 * Manipulates these components in a special thread called
 * Event Dispatch Thread.
 *
 * @author Newton Bujiku
 * @since Dec,2014
 *
 */
public class HTTPGUI extends JFrame implements ActionListener,MouseListener {

    /*

    Define several class constants
    Names are self explanatory

     */
    private static  final int FRAME_HEIGHT = 500;
    private static final int FRAME_WIDTH = 900;
    private static  final int FRAME_LOCATION_X = 300;
    private static final int FRAME_LOCATION_Y = 150;
    private static  final String FRAME_TITLE = "HTTP Server";
    private static final String SERVER_STOPPED="SERVER STOPPED";
    private static final String SERVER_RUNNING="SERVER RUNNING...";
    private static  final String BUTTON_START="Start";
    private static  final String BUTTON_STOP="Stop ";
    private static  final  String ENTER_PORT_NUMBER="Enter port number ";
    private static final String DOCUMENT_ROOT_UNSET ="Document Root : Not set!!! ";

    /*
     *Define several instance variables
     * Declare the components here so that they can be referred globally in actionPerfomed()
     */
    private boolean documentIsSet;
    private static  JTextArea messageDisplay;//the screen where massages are displayed
    private JButton startStopButton,documentRootButton;
    private JFileChooser fileChooser;//document root selector
    private JLabel statusLabel,serverVersionLabel,documentRootLabel;
    private JTextField portNumberTextField;//where port number is entered




    public HTTPGUI(){


        createComponentsAndSetGUI();


    }


    /**
     * This method sends log messages to the display.
     * The action itself is done inside the run method of a runnable
     * ensuring that the thread that invoked this method does not touch the GUI components.
     * Only the Event Dispatch Thread is allowed to touch the views
     */
    public  static void updateDisplay(final String message){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                messageDisplay.setText(" " + messageDisplay.getText() + "\n" + " " + message + "\n");

            }
        });

    }

    @Override
    public void actionPerformed(ActionEvent event) {

        /*
        From the ActionListener interface.
        Runs  when an event occurs on the GUI components
         */

        if(event.getSource()==documentRootButton){
            //if the event was from document root button
            boolean indexFileExists=false;
            int returnVal = fileChooser.showOpenDialog(this);//open the dialog;returnVal holds the result from chooser
            if(returnVal==JFileChooser.APPROVE_OPTION){//check if a directory was selected

                File file = fileChooser.getSelectedFile();//get selected directory
                String[] files =file.list();//get all the files in the directory
                for(String subfile : files){//iterate over them
                    if(subfile.contains("index.html")){
                        //if the directory contains an index file then it's a valid document root
                        documentRootLabel.setText("Document Root : "+file.getAbsolutePath());//make the root visible
                        indexFileExists=true;//we have the index now
                        break;
                    }
                }

                if(indexFileExists){

                    HTTPServer.DOCUMENT_ROOT=file.getAbsolutePath();//make this directory a document root
                    documentIsSet=true;
                    if(HTTPServer.PORT_NUMBER>1024 &&HTTPServer.PORT_NUMBER<65536) {
                        //port number is valid
                        startStopButton.setEnabled(true);//enable the button so that the server can be started
                    }
                }else{
                    //the directory does not contain an index,don't accept it;let the user know
                    documentRootLabel.setText("Document Root : "+"No index.html in the selected directory");

                }


            }

        }else if(event.getSource()==startStopButton){

            //the event from the start/stop button
            if(startStopButton.getText().equals(BUTTON_START)){

                //the button labelled start
                new BackgroundThread().start();//start the server in another thread
                //we only want this thread to be dealing with GUI components

                documentRootButton.setEnabled(false);//should not change document root while the server is running
                portNumberTextField.setEnabled(false);//neither should the port number be changed
                statusLabel.setText(SERVER_RUNNING);//yes it is running
                statusLabel.setForeground(Color.BLUE);
                startStopButton.setText(BUTTON_STOP);//change the start/stop button test to stop

            }else if (startStopButton.getText().equals(BUTTON_STOP)){

                //the user has asked to stop the server
                startStopButton.setText(BUTTON_START);
                //stop the server
                BackgroundThread.stopServer();//stop it from this thread. No effect here
                statusLabel.setText(SERVER_STOPPED);
                statusLabel.setForeground(Color.RED);
                documentRootButton.setEnabled(true);//now document root and port number can be changed
                portNumberTextField.setEnabled(true);


            }

        }



    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if(event.getSource()==portNumberTextField){
            //if the text field was clicked
            if(portNumberTextField.getText().equals(ENTER_PORT_NUMBER)){
                //if it still contains the original text remove it
                portNumberTextField.setText("");
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent event) {

        if(event.getSource()==portNumberTextField){

            //the cursor has left the text field
            try{
                HTTPServer.PORT_NUMBER= Integer.parseInt(portNumberTextField.getText());//check if we have a valid number
                if (HTTPServer.PORT_NUMBER<1025 || HTTPServer.PORT_NUMBER>65535){
                    //if the number is valid but not  a valid port throw number exception and catch it
                    throw new NumberFormatException();
                }

                if(documentIsSet && !startStopButton.isEnabled()){
                    //all is okay,port number is a number and a valid port and start/stop button is not enabled
                    startStopButton.setEnabled(true);//enable the start/stop button
                }
            }catch (NumberFormatException e){

                //something went wrong,put original text
                portNumberTextField.setText(ENTER_PORT_NUMBER);
            }

        }
    }

    /**
     *
     * @return tips for the port number and the document root
     */
    public String getInstrunctions() {

        return "\n\n A port number should be an\n integer greater than 1024\n " +
                "and less than 65535" +
                "\n\n\n Your document root is\n where your website resides." +
                "\n Make sure that this directory\n contains an index.html file";
    }

    public static  void main(String[] args){

        /*
        Start the app by invoking SwingUtilities.invokeLater()
        this guarantees that the GUI components are controlled by
        only one thread called the Event Dispatch Thread,so that the UI
        is always responsive.
         */
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                HTTPGUI gui = new HTTPGUI();
            }
        });

    }
    /*
    The background thread that starts and stops the server
     */

    private  static class BackgroundThread extends  Thread{

        public void run(){
            HTTPServer.startServer();
        }

        public static void stopServer(){
           HTTPServer.stopServer();
        }

    }

    /**
     * Invoked in the constructor,sets all the component of the User Interface ready
     */

    private void createComponentsAndSetGUI() {
    /*
    Declare the panels locally because they don't need view updates
     */

        JPanel pane,topPanel, rightPanel,bottomPanel;
        JScrollPane  midPanel;


        /*
        Set up the message display area
         */
        messageDisplay = new JTextArea();
        messageDisplay.setEditable(false);//can not be edited
        messageDisplay.setBorder(BorderFactory.createLineBorder(Color.BLACK));//surround it with black border
        Font font = new Font("Verdana",Font.BOLD,12);
        messageDisplay.setFont(font);
        messageDisplay.setForeground(Color.RED);
        messageDisplay.setLineWrap(true);//words will be wrapped
        messageDisplay.setWrapStyleWord(true);//words will be wrapped between spaces

        /*
        Set up the port number text box

        */
        portNumberTextField = new JTextField(ENTER_PORT_NUMBER);
        portNumberTextField.setCursor(Cursor.getDefaultCursor());
        portNumberTextField.setFont(font);
        portNumberTextField.setForeground(Color.RED);
        portNumberTextField.addMouseListener(this);//listen for mouse events
        portNumberTextField.setBorder(BorderFactory.createLineBorder(Color.BLACK));//add a black border
        portNumberTextField.setHorizontalAlignment(JTextField.CENTER);//center the text

        /*
        Set up the button for starting and stopping the server
         */
        startStopButton = new JButton(BUTTON_START);
        startStopButton.addActionListener(this);
        startStopButton.setBounds(5,10,10,10);//make space around
        startStopButton.setEnabled(false);//disable until document root and port number have been set

         /*
        Set up the button for choosing the document root
         */
        documentRootButton = new JButton("Document Root");
        documentRootButton.addActionListener(this);//listen for events,click event
        documentRootButton.setBounds(5,10,10,10);//set spaces around

        /*
        Initialise the file chooser
         */
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//only pick directories
        //fileChooser.addActionListener(this);//listen for event

        /*
        Initialise the label to indicate server status
         */
        statusLabel = new JLabel(SERVER_STOPPED);
        statusLabel.setForeground(Color.RED);
        serverVersionLabel = new JLabel("Server Version: "+HTTPServer.SERVER);
        documentRootLabel = new JLabel(DOCUMENT_ROOT_UNSET);


        JTextArea tipArea = new JTextArea(getInstrunctions(),5,18);//the tip area where few instructions are shown
        tipArea.setEditable(false);//should not be edited
        tipArea.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));//give it a border for better view
        tipArea.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        pane = new JPanel(new BorderLayout());//initialise the main pane of the frame that will hold all other panels

        /*
        Initialise and set the panels
         */
        topPanel = new JPanel(new GridLayout(2,1));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,0));
        midPanel = new JScrollPane(messageDisplay);
        midPanel.setAutoscrolls(true);
        midPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));

        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,10));

        bottomPanel= new JPanel(new FlowLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));

        topPanel.add(serverVersionLabel);//add the server version and document root labels to the top panel
        topPanel.add(documentRootLabel);

        JPanel rightTopPanel = new JPanel(new GridLayout(3,1));//instantiate right top panel

        /*
        Add document root button,start/stop button,and port number text box to the right top panel
         */
        rightTopPanel.add(documentRootButton);
        rightTopPanel.add(startStopButton);
        rightTopPanel.add(portNumberTextField);
        rightTopPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        rightPanel.add(BorderLayout.NORTH,rightTopPanel);
        rightPanel.add(BorderLayout.CENTER,new JPanel().add(tipArea));

        bottomPanel.add(statusLabel);//add server status label to the bottom

        /*
        Add all other panels to the frame's content pane
         */
        pane.add(BorderLayout.NORTH,topPanel);
        pane.add(BorderLayout.EAST, rightPanel);
        pane.add(BorderLayout.CENTER, midPanel);
        pane.add(BorderLayout.SOUTH,bottomPanel);

        /*Set up the frame
         *
         */

        setSize(FRAME_WIDTH,FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//exit when close button is clicked
        setLocation(FRAME_LOCATION_X, FRAME_LOCATION_Y);
        setTitle(FRAME_TITLE);
        setContentPane(pane);//add the main pane to the frame
        setDefaultLookAndFeelDecorated(true);
        setVisible(true);//make it visible
    }

}
