package Java;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/********************************************************************
 * A Tcp Client Class capable of requesting and receiving files from
 * a user-specified server. Written for prof Kalafut's
 * Data Communications course at GVSU, fall 2017
 * @author Pieter Holleman
 ********************************************************************/
class TcpClient {

    /** Socket Channel **/
    SocketChannel sc;


    /**
     * Constructor attempts to connect to a server at the specified
     * ip address on the specified port.
     *
     * @param ip   address to connect
     * @param port port to connect
     */
    public TcpClient(String ip, int port) throws IOException {

        sc = SocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(ip, port);
        sc.socket().connect(address,10000);

    }


    public void disconnect(){

        try {
            ByteBuffer closeBuffer = ByteBuffer.wrap("*".getBytes());
            sc.write(closeBuffer);
            sc.close();
            System.out.println("Disconnected");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * send a String to the server which contains the name of
     * the file the user would like to download.
     *
     * @param fileName
     * @throws IOException
     */
    public void requestFile(String fileName) throws IOException {

        //send filename
        ByteBuffer buff = ByteBuffer.wrap(fileName.getBytes());
        sc.write(buff);
        System.out.println("File Requested");

        //receive file size in response
        buff = ByteBuffer.allocate(4096);
        sc.read(buff);

        //initiate file transfer if file is present
        int size = Integer.parseInt(
                new String(buff.array()).trim());
        if (size > 0) {
            System.out.println("File found, size " + size + " bytes");
            transferFile(fileName, size);
        } else System.out.println("File not present on server");
    }

    /**
     * A private helper method to encapsulate the process of sending a file
     * @param fileName file to transfer
     * @param size size of the file in bytes
     * @throws IOException
     */
    private void transferFile(String fileName, int size) throws IOException {

        
        ByteBuffer buff = ByteBuffer.wrap("*".getBytes());
        sc.write(buff);
        buff = ByteBuffer.allocate(size);
        //probably an excessive amount of time
        try {
            Thread.sleep(size/3);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
        sc.read(buff);

        byte[] array = buff.array();
        FileOutputStream fileOut = new FileOutputStream(fileName);
        fileOut.write(array);
        System.out.println("File written: " + fileName);

    }

    public void requestList() throws IOException{

        ByteBuffer buffer = ByteBuffer.wrap("?".getBytes());
        sc.write(buffer);
        buffer = ByteBuffer.allocate(256);
        sc.read(buffer);
        int listSize = Integer.parseInt(
                new String(buffer.array()).trim()
        );
        buffer = ByteBuffer.allocate(listSize);
        ByteBuffer Acknowledgment = ByteBuffer.wrap("*".getBytes());
        sc.write(Acknowledgment);
        sc.read(buffer);
        String list = new String(buffer.array()).trim();
        System.out.println(list);


    }




    /**
     * A private helper method attempts to receive the requested file from the server
     * and store it.
     *
     * @param fileName
     */
    private void recieveFile(String fileName) {


        //TODO: revise exception handling and add "handshake" with server

        //a ByteBuffer will be needed to hold the file size string from the server
        ByteBuffer buff = ByteBuffer.allocate(4000);
        String sizeStr;
        int size;

        try {

            //server will send file size first
            sc.read(buff);
            sizeStr = new String(buff.array()).trim();
            size = Integer.parseInt(sizeStr);

            //if the file exists on the server, begin receiving process
            if (size > 0) {


                System.out.println("Server: File size: " + size + " bytes");
                buff = ByteBuffer.wrap("0".getBytes());
                sc.write(buff);


                //allocate a buffer of the correct size, read the socket channel into the buffer,
                //and build a byte array with the buffer contents
                buff = ByteBuffer.allocate(size);
                sc.read(buff);
                byte[] array = buff.array();

                //Use a FileOutPutStream to write the the byte array into a file
                //with the specified name and extension
                FileOutputStream fileOut = new FileOutputStream(fileName);
                fileOut.write(array);
                System.out.println("File written: " + fileName);

            } else {
                System.out.println("Server: File not present");
                buff.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Main method implements a command line interface to
     * manage the client.
     *
     * @param args not used
     */
    public static void main(String args[]) {


    }


}