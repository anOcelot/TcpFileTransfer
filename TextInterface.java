package Java;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.channels.UnresolvedAddressException;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Created by pieterholleman on 9/22/17.
 */
public class TextInterface {

    private TcpClient client;

    private void awaitConnectionInput() {


        //TODO: loop until valid ip
        //TODO: loop until valid socket

        Scanner scanner = new Scanner(System.in);
        String ip = "";
        int port = -1;


        while (true) {


            try {


                if (ip.equals("")) {
                    System.out.print("Enter an ip address: ");
                    ip = scanner.next().trim();

                }

                if (port == -1) {
                    System.out.print("Enter a port number: ");
                    port = scanner.nextInt();
                    if (port == 0) {
                        throw new IllegalArgumentException();
                    }

                }


                client = new TcpClient(ip, port);
                System.out.println("Successfully connected");
                break;

            } catch (InputMismatchException e){
                System.out.println("Invalid port, not an integer");
                scanner.next();
                port = -1;
                //ip = "";

            } catch (UnresolvedAddressException e) {
                System.out.println("Unresolved address, try again with a valid ip");
                ip = "";

            } catch (IllegalArgumentException e) {
                System.out.println("Invalid port - out of range");
                port = -1;

            } catch (SocketTimeoutException e) {
                System.out.println("Connection timed out, try again");
                port = -1;
                ip = "";
            } catch (ConnectException e) {
                System.out.println("Connection Refused, try again");
                port = -1;
                ip = "";
            } catch (IOException e) {
                System.out.println("Invalid ip, try again");
                ip = "";
                //e.printStackTrace();
            }

        }

        awaitFileNameInput();
    }

    private void awaitFileNameInput() {

        Scanner scan = new Scanner(System.in);
        String fileName;


        while (true) {

            System.out.print("Enter the name of the file to download: ");
            fileName = scan.next();

            try {
                if (fileName.equals("list")) {
                    client.requestList();
                    continue;
                }
                if (fileName.equals("exit")) {
                    client.disconnect();
                    System.exit(2);
                    continue;
                } else {
                    client.requestFile(fileName);
                }

            } catch (IOException e) {

                e.printStackTrace();

            }

        }


        //TODO: loop until valid file name request
    }

    public static void main(String[] args) {

        TextInterface test = new TextInterface();
        test.awaitConnectionInput();

    }
}
