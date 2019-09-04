/*
Computer Networks Project
Dustin Bagayna
Shivam Patel
Justin Dy
TCP File Transfer Project
*/
package fileTransfer;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
 
public class FileTransferMain
{
 
    private static Network.Protocol pro;
    private static ByteBuffer buff;
    private static Scanner in;
     
    public static void main(String[] args) throws IOException 
    {
        //Displays IP Address and Host Name
        InetAddress addr = InetAddress.getLocalHost();
        System.out.println("Host:" + addr.getHostName());
        System.out.println("IP: " + addr.getHostAddress());
       

        in = new Scanner(System.in);
        pro = Network.Protocol.TCP;

        //prompt for client or server
        int x = 0;
        while (x == 0)
        {
            System.out.println("Choose whether this PC will act as the CLIENT or the SERVER");
            System.out.print("Type 1 for SERVER or 2 for CLIENT: ");
            x = in.nextInt();
            
            if (x == 1)
            {
                System.out.println("You are the SERVER");
                server();
            }
            
            else if (x == 2)
            {
                System.out.println("You are the CLIENT" );
                client();
            }
            
            else
            {
                System.out.println("Invalid Input" );
                x = 0;
            }
        }
        in.close();
    }
 
    //Routine for the server
    public static void server() throws IOException 
    {
        FileTransferServer ser = new FileTransferServer(pro, Network.TCP_PORT);

        //Prompt the user to start connection between the server and client. Server should always try to start first.
        System.out.print("Press any key when you are ready to start the connection: ");
        while(!in.hasNext());
        System.out.println("Attempting to connect...");
        ser.acceptConnection();
        System.out.println("Connection Established");
         
        //Use while loop to constantly ask for a file name until the user specifies to exit
        int fileRequest = 1;
        while((fileRequest = ser.receiveInt()) != 0) 
        {
            System.out.println("Client is currently selecting a file...");
            String fileName = ser.receiveString();
            File input = new File(fileName);
            if(input.exists()) {
                //send ACK
                ser.sendInt(1);
                String fileFound = "OK - File Found";
                System.out.println(fileFound);
                ser.sendString(fileFound);
 
                //waiting for an ACK from the client
                ser.receiveInt();
 
                //calculate and send the checksum
                String checksum = getFileChecksum(input);
                ser.sendString(checksum);
 
                //setup for actual file transfer
                FileInputStream in = new FileInputStream(input);
                byte[] byteArray = new byte[Network.PACKET_SIZE + 4];
                int count, packetNum = 0;
 
                //send whatever the expected number of packets is which is why we Math.ceil
                int packet = (int) Math.ceil((double)in.available()/1024);
                ser.sendInt(packet);
 
                //sending the actual file
                while ((count = in.read(byteArray, 4, Network.PACKET_SIZE)) != -1) 
                {
                    buff = ByteBuffer.allocate(Network.PACKET_SIZE + 4);
                    buff.putInt(packetNum);
                    buff.put(byteArray, 4, count);
                    buff.rewind();
                    buff.get(byteArray, 0, count + 4);
                    ser.sendBytes(byteArray, 0, count + 4);
                    packetNum++;
                }
                
                //Confirm that the file was sent and confirm that the checksums were right
                System.out.println("File sent");
                String checksumCon = ser.receiveString();
                System.out.println(checksumCon);
                in.close();
            }
            
            //Give error message if the file has not been found
            else
            {
                ser.sendInt(0);
                String FileNotFound = "File Not Found";
                ser.sendString(FileNotFound);
                System.out.println(FileNotFound);
            }
            ser.receiveInt();
            ser.sendInt(1);
        }
        System.out.println("Connection has Closed");
        ser.closeConnection();
    }
 
    //Routine for the client
    public static void client() throws IOException 
    {
        FileTransferClient client = new FileTransferClient(pro, Network.TCP_PORT);
        
        // Prompt for the IP address that user wishes to connect to
        System.out.print("Enter the server IP Address to connect to: ");
        in = new Scanner(System.in);
        String IPAddress = in.nextLine();
        InetAddress addr = InetAddress.getByName(IPAddress);
        System.out.println("Connecting...");
        client.beginConnection(addr);  
        System.out.println("Connection Established");
        int continueSending = 1;
         
        while(continueSending == 1) 
        {
            //Prompts user to enter file name or quit.
            System.out.print("Enter file name with .extension or \"exit\" to end FTP: ");
            String fileName = in.nextLine();
            if(fileName.equals("exit")) continueSending = 0;
            client.sendInt(continueSending);
            if(continueSending == 0) break;
            client.sendString(fileName);
 
            //ACK
            int ack = client.receiveInt();
            String message = client.receiveString();
            System.out.println(message);
            if (ack == 1)
            {
                //send a confirmation to the server
                client.sendInt(ack);
                //receive the checksum from server 
                String checksum = client.receiveString();
 
                //create ArrayList of the packets
                int packetNum = client.receiveInt(), index;
                ArrayList<byte[]> fileBuffer = new ArrayList<byte[]>(packetNum);
                byte[] packet, data;
 
                //store the bytes from each packet into the buffer
                //index packets to store in proper order
                for (int i=0; i<packetNum; i++)
                {
                    packet = client.receiveBytes();
                    buff = ByteBuffer.wrap(packet);
                    index = buff.getInt();
                    data = new byte[buff.remaining()];
                    buff.get(data, 0, buff.remaining());
                    fileBuffer.add(index, data);            
                }
                File file = new File(fileName);
                FileOutputStream out = new FileOutputStream(file);
                //builds file from the buffer as previously stated
                for(byte[] b : fileBuffer) out.write(b);
 
                //confirms reception of the file and that the checksum is correct
                System.out.println("File Received");
                String newCheckSum = getFileChecksum(file);
                if(checksum.equals(newCheckSum))
                {
                    message = "Checksums match. File received. ACK";
                    System.out.println(message);
                    client.sendString(message);
                }
                else
                {
                    message = "Checksums don't match. File corrupted. NACK";
                    System.out.println(message);
                    client.sendString(message);
                }
                out.close();
            }
            else
            {
                System.out.println("Request a different file.");
            }
            client.sendInt(1);
            client.receiveInt();
        }
        System.out.println("End of file transfer.");
        client.closeConnection();
    }
     
     
    //to calculate checksum we use MessageDigest and the included MD5 hash algorithm
    private static String getFileChecksum(File file) throws IOException
    {
        MessageDigest digest;
        try 
        {
            digest = MessageDigest.getInstance("MD5");
        } 
        
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        
        FileInputStream in = new FileInputStream(file);
        byte[] byteArray = new byte[Network.PACKET_SIZE];
        int bytesCount = 0;
 
        //Read the data of the file and update message digest
        while ((bytesCount = in.read(byteArray)) != -1)
        {
            digest.update(byteArray, 0, bytesCount);
        }
        in.close();
         
 
        //Get MD5 checksum
        byte[] bytes = digest.digest();
 
        //bytes[] in decimal format change to hex format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
