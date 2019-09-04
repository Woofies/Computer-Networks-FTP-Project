/*
Computer Networks Project
Dustin Bagayna
Shivam Patel
Justin Dy
TCP File Transfer Project: FTP SERVER
*/
package fileTransfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
 
/*The FileTransferServer class handles sending and receiving data using TCP.
All methods support TCP on construction, and FileTransferServer receives a connection 
from a corresponding FileTransferClient.*/
public class FileTransferServer 
{
    private Network.Protocol pro; 
    private ByteBuffer buff;
    private ServerSocket tcpServerSocket;
    private Socket tcpSocket;
    private int port;
    private InetAddress clientIP;
    private DataOutputStream writeBuffer;
    private DataInputStream readBuffer;
    
 
    //constructs the FTP server and initializes a socket for TCP and port
    FileTransferServer(Network.Protocol p, int port) throws IOException 
    {
        pro = p;
        this.port = port;
        tcpServerSocket = new ServerSocket(port); 
    }
     
    //accepts connection from a computer running FileTransferClient
    public void acceptConnection() throws IOException 
    {
        tcpServerSocket.setSoTimeout(Network.TIMEOUT);
        tcpSocket = tcpServerSocket.accept();
        clientIP = tcpSocket.getInetAddress();
        writeBuffer = new DataOutputStream(tcpSocket.getOutputStream());
        readBuffer = new DataInputStream(tcpSocket.getInputStream());   
    }
     
    public void sendInt(int data) throws IOException 
    {
        writeBuffer.writeInt(data);
    }
     
    public void sendBytes(byte[] byteArray, int offset, int length) throws IOException 
    {
    	//send and receive bytes functions communicate the number of bytes that are sent this value is not returned to the caller
        writeBuffer.writeInt(length);
        //data returned by receiveBytes
        writeBuffer.write(byteArray, offset, length);  
    }
     
    public void sendString(String message)throws IOException 
    {
        writeBuffer.writeUTF(message); 
    }
     
    public int receiveInt() throws IOException 
    {
        int data = readBuffer.readInt();
        return data;    
    }
     
    public byte[] receiveBytes() throws IOException
    {
        byte[] byteArray;
        //receive length of byte array from sendBytes()
        int length = receiveInt();
        byteArray = new byte[length];
        readBuffer.readFully(byteArray, 0, length);
        return byteArray;
    }
     
    public String receiveString() throws IOException
    {
        String string = readBuffer.readUTF();
        return string;       
    }
     
    public void closeConnection() throws IOException 
    {
        tcpSocket.close(); 
    }
}