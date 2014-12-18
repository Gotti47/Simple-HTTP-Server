import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

/**
 * Created by newton on 12/17/14.
 */
public class HTTPGUI extends JFrame implements ActionListener,MouseListener {


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
    private static StringBuilder messageBuilder;
    private boolean documentIsSet;

    private JPanel pane,topPanel, rightPanel,bottomPanel;
    private JScrollPane  midPanel;
    private static  JTextArea messageDisplay;
    private JButton startStopButton,documentRootButton;
    private JFileChooser fileChooser;
    private  JLabel statusLabel,serverVersionLabel,documentRootLabel;
    private JTextField portNumberTextField;


    static {
        messageBuilder = new StringBuilder(" ");
    }


    public HTTPGUI(){


        messageDisplay = new JTextArea();
        messageDisplay.setEditable(false);
        messageDisplay.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        Font font = new Font("Verdana",Font.BOLD,12);
        messageDisplay.setFont(font);
        messageDisplay.setForeground(Color.RED);
        messageDisplay.setLineWrap(true);
        messageDisplay.setWrapStyleWord(true);
       // messageDisplay.setAutoscrolls(true);
        portNumberTextField = new JTextField(ENTER_PORT_NUMBER);
        portNumberTextField.setCursor(Cursor.getDefaultCursor());
        portNumberTextField.setFont(font);
        portNumberTextField.setForeground(Color.RED);
        portNumberTextField.addMouseListener(this);
        portNumberTextField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        portNumberTextField.setHorizontalAlignment(JTextField.CENTER);
        //portNumberTextField.setBounds(5,10,10,10);

        startStopButton = new JButton(BUTTON_START);
        startStopButton.addActionListener(this);
       // startStopButton.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
        startStopButton.setBounds(5,10,10,10);

        startStopButton.setEnabled(false);
        documentRootButton = new JButton("Document Root");
        documentRootButton.addActionListener(this);
       // documentRootButton.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
        documentRootButton.setBounds(5,10,10,10);
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.addActionListener(this);
        statusLabel = new JLabel(SERVER_STOPPED);
        statusLabel.setForeground(Color.RED);
        serverVersionLabel = new JLabel("Server Version: "+HTTPServer.SERVER);
        documentRootLabel = new JLabel(DOCUMENT_ROOT_UNSET);

        JTextArea tipArea = new JTextArea(getInstrunctions(),5,18);
        tipArea.setEditable(false);
        tipArea.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        // tipArea.setFont(new Font(Font.SERIF,Font.CENTER_BASELINE,12));
        //tipArea.setMargin(new Insets(5,5,5,5));
        tipArea.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        pane = new JPanel(new BorderLayout());
        topPanel = new JPanel(new GridLayout(2,1));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,0));
        midPanel = new JScrollPane(messageDisplay);
        midPanel.setAutoscrolls(true);
        midPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));

        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10,0,10,10));

        bottomPanel= new JPanel(new FlowLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));

        topPanel.add(serverVersionLabel);
        topPanel.add(documentRootLabel);
       // midPanel.add(messageDisplay);
        JPanel rightTopPanel = new JPanel(new GridLayout(3,1));

        rightTopPanel.add(documentRootButton);
        rightTopPanel.add(startStopButton);
        rightTopPanel.add(portNumberTextField);
        rightTopPanel.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        rightPanel.add(BorderLayout.NORTH,rightTopPanel);
        rightPanel.add(BorderLayout.CENTER,new JPanel().add(tipArea));
        bottomPanel.add(statusLabel);
        pane.add(BorderLayout.NORTH,topPanel);
        pane.add(BorderLayout.EAST, rightPanel);
        pane.add(BorderLayout.CENTER, midPanel);
        pane.add(BorderLayout.SOUTH,bottomPanel);

        setSize(FRAME_WIDTH,FRAME_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(FRAME_LOCATION_X, FRAME_LOCATION_Y);
        setTitle(FRAME_TITLE);
        setContentPane(pane);
        setDefaultLookAndFeelDecorated(true);
        setVisible(true);



    }

    public  static void updateDisplay(String message){
        messageBuilder.append(message+"\n ");
        messageDisplay.setText(" " + messageDisplay.getText() + "\n" + " " + message + "\n");

    }

    public JLabel getDocumentRootLabel() {
        return documentRootLabel;
    }

    public JLabel getServerVersionLabel() {
        return serverVersionLabel;
    }

    public JPanel getTopPanel() {
        return topPanel;
    }


    @Override
    public void actionPerformed(ActionEvent event) {

        if(event.getSource()==documentRootButton){
            boolean indexFileExists=false;
            int returnVal = fileChooser.showOpenDialog(this);
            if(returnVal==JFileChooser.APPROVE_OPTION){

                File file = fileChooser.getSelectedFile();
                String[] files =file.list();
                for(String subfile : files){
                    if(subfile.contains("index.html")){
                        documentRootLabel.setText("Document Root : "+file.getAbsolutePath());
                        indexFileExists=true;
                        break;
                    }
                }

                if(indexFileExists){

                    HTTPServer.DOCUMENT_ROOT=file.getAbsolutePath();
                    documentIsSet=true;
                    if(HTTPServer.PORT_NUMBER>1024 &&HTTPServer.PORT_NUMBER<65536) {
                        startStopButton.setEnabled(true);
                    }
                }else{
                    documentRootLabel.setText("Document Root : "+"No index.html in the selected directory");

                }


            }

        }else if(event.getSource()==startStopButton){

            if(startStopButton.getText().equals(BUTTON_START)){

                new BackgroundThread().start();
                documentRootButton.setEnabled(false);
                portNumberTextField.setEnabled(false);
                statusLabel.setText(SERVER_RUNNING);
                //statusLabel.setFont(new Font("verdana",Font.BOLD,15));
                statusLabel.setForeground(Color.BLUE);
                startStopButton.setText(BUTTON_STOP);

            }else if (startStopButton.getText().equals(BUTTON_STOP)){
                startStopButton.setText(BUTTON_START);
                //stop the server
                BackgroundThread.stopServer();
                statusLabel.setText(SERVER_STOPPED);
                statusLabel.setForeground(Color.RED);
                documentRootButton.setEnabled(true);
                portNumberTextField.setEnabled(true);


            }

        }



    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if(event.getSource()==portNumberTextField){
            if(portNumberTextField.getText().equals(ENTER_PORT_NUMBER)){
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

            try{
                HTTPServer.PORT_NUMBER= Integer.parseInt(portNumberTextField.getText());
                if (HTTPServer.PORT_NUMBER<1025 || HTTPServer.PORT_NUMBER>65535){
                    throw new NumberFormatException();
                }

                if(documentIsSet){
                    startStopButton.setEnabled(true);
                }
            }catch (NumberFormatException e){

                portNumberTextField.setText(ENTER_PORT_NUMBER);
            }

        }
    }

    public String getInstrunctions() {
        return "\n\n A port number should be an\n integer greater than 1024\n " +
                "and less than 65535" +
                "\n\n\n Your document root is\n where your website resides." +
                "\n Make sure that this directory\n contains an index.html file";
    }

    public static  void main(String[] args){

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                HTTPGUI gui = new HTTPGUI();
            }
        });

    }

    private  static class BackgroundThread extends  Thread{

        public void run(){
            HTTPServer.startServer();
        }

        public static void stopServer(){
           HTTPServer.stopServer();
        }

    }

}
