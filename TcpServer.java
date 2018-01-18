package Java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Scanner;

/**
 * A Tcp Server capable of sending a file over the internet using
 * the sockets interface.
 *
 * @author Pieter Holleman
 * @version Fall 2017
 */
class TcpServer {

    /**
     * Socket Channel
     **/
    private ServerSocketChannel c;

    /**
     * Constructor attempts connection on specified port
     *
     * @param port to listen on
     */
    public TcpServer(int port) throws IOException {

        c = ServerSocketChannel.open();
        c.bind(new InetSocketAddress(port));

    }

    /**
     * Server awaits connection from a client
     */
    private void awaitClient() {


        while (true) {

            try {
                SocketChannel sc = c.accept();
                System.out.println("Client connected");

                // SocketChannel is handed off to a new thread to allow simultaneous connections
                TcpServerThread t = new TcpServerThread(sc);
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * private helper method continuously waits for a file request
     * on a specified SocketChannel
     * @param sc SocketChannel to await requests
     * @throws IOException
     */
    private void awaitRequest(SocketChannel sc) throws IOException {

        System.out.println("awaiting file requests");

        while (sc.isConnected()) {


            ByteBuffer buffer = ByteBuffer.allocate(4096);

            //First data sent across connection will be the name
            //of the requested file
            sc.read(buffer);
            String filename = new String(buffer.array()).trim();

            //using ? and * as a control/flag because it can't be used in valid filenames
            if (filename.equals("?")) {
                System.out.println("Client: Requesting available file list");
                listFiles(sc);
            } else if (filename.equals("*")){
                System.out.println("Client disconnected");
                sc.close();
            //if no requests arrive, do nothing
            } else if (!filename.equals("")){
                System.out.println("Client: requesting " + filename);
                sendFile(filename, sc);
            }


        }

    }

    /**
     * A private helper method to encapsulate the process of sending a file
     * @param filename Name pf the file to send
     * @param sc Destination SocketChannel
     * @throws IOException
     */
    private void sendFile(String filename, SocketChannel sc) throws IOException {

        try {
            byte[] fileBytes = getByteArray(filename);
            String fileSize = Integer.toString(fileBytes.length);

            System.out.println("file present, size " + fileSize + " bytes");

            ByteBuffer buffer = ByteBuffer.wrap(fileSize.getBytes());
            sc.write(buffer);

            buffer = ByteBuffer.allocate(256);
            sc.read(buffer);

            String acknowledgement = new String(buffer.array()).trim();

            if (acknowledgement.equals("*")) {
                buffer = ByteBuffer.wrap(fileBytes);
                sc.write(buffer);
                System.out.println("file sent");

            } else {
                System.out.println("Acknowledgement failed");

            }
        }
        //filesize 0 is sent to indicate absence of the file
        catch (NoSuchFileException e) {
            ByteBuffer buffer = ByteBuffer.wrap("0".getBytes());
            sc.write(buffer);
            System.out.println("file not present on this server");
        }

    }

    /**
     * Convert a file to an array of bytes
     *
     * @param filename name of file
     * @return file represented as byte array
     * @throws FileNotFoundException if the file cannot be read
     */
    private byte[] getByteArray(String filename) throws NoSuchFileException {

        //file in the working directory
        File f = new File(filename);
        byte array[] = new byte[0];

        //try to read the file into a byte array
        try {
            array = Files.readAllBytes(f.toPath());
            return array;
        }
        //throw an exception if the file does not exist
        catch (NoSuchFileException e) {
            throw new NoSuchFileException("");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return array;
    }

    /**
     * private helper method encapsulates the task of building and sending a
     * list of the files available on the server
     * @param sc Destination SocketChannel
     * @throws IOException
     */
    public void listFiles(SocketChannel sc) throws IOException {
        File[] list = new File(".").listFiles();
        String fileList = "";

        //add each filename to the list along with a newline character
        for (File file : list) {

            //omit directories
            if (!file.isDirectory()) {
                fileList += file.getName();
                fileList += "\n";
            }

        }
        //send the size of the list first
        String listSize = Integer.toString(fileList.length());
        ByteBuffer buffer = ByteBuffer.wrap(listSize.getBytes());
        sc.write(buffer);

        //check for acknowledgement from the client
        buffer = ByteBuffer.allocate(256);
        sc.read(buffer);
        String acknowledgment = new String(buffer.array()).trim();

        //if the client acknowledges, send the list
        if (acknowledgment.equals("*")) {
            buffer = ByteBuffer.wrap(fileList.getBytes());
            System.out.println("list sent");
            sc.write(buffer);
        }


    }

    /**
     * Private thread subclass used to allow multithreading
     */
    private class TcpServerThread extends Thread {

        /** each thread maintains a SocketChannel **/
        SocketChannel sc;

        /**
         * Constructor
         * @param channel SocketChannel to be managed by the thread
         */
        TcpServerThread(SocketChannel channel) {
            sc = channel;
        }

        public void run() {
            try {
                awaitRequest(sc);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Main method launches server and allows the user to designate the port
     *
     * @param args
     */
    public static void main(String args[]) {


        Scanner scan = new Scanner(System.in);

        //repeatedly attempt to obtain a valid port from the user
        while (true) {
            System.out.print("Enter a port number to listen on: ");

            try {
                int port = Integer.parseInt(scan.next().trim());
                TcpServer server = new TcpServer(port);
                if (port == 0) throw new IllegalArgumentException();
                System.out.println("Listening for connection on port " + Integer.toString(port));
                server.awaitClient();

                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid port, not a integer");
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid port, out of range");
            } catch (IOException e) {
                System.out.println("reserved port, try a different port");
                //e.printStackTrace();
            }

        }

    }
}