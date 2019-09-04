/*
Computer Networks Project
Dustin Bagayna
Shivam Patel
Justin Dy
TCP File Transfer Project: FTP CLIENT
*/
package fileTransfer;
 
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
 
/*The FileTransferClient class handles sending and receiving data using
TCP. All methods use TCP upon construction. FileTransferClient initiates 
a connection with a corresponding FileTransferServer.*/
public class FileTransferClient 
{
	//Define protocol type being TCP, the byte buffer, socket, port, IP, and storage
    private Network.Protocol pro; 
    private ByteBuffer buff;
    private Socket tcpSocket;
    private int port;
    private InetAddress serverIP;
    private DataOutputStream writeBuffer;
    private DataInputStream readBuffer;
     
    //constructs the FTP client and initializes a socket for TCP and port from the Network class
    FileTransferClient(Network.Protocol p, int port) throws IOException 
    {
        pro = p;
        this.port = port;
        tcpSocket = new Socket();
        tcpSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), port)); //IP address and hostname pair is taken with its corresponding port as IP Socket address
    }
     
    //begin connection with a different PC running FileTransferServer
    public void beginConnection(InetAddress IP) throws IOException 
    {
        serverIP = IP;
        tcpSocket.setSoTimeout(Network.TIMEOUT);
        tcpSocket.connect(new InetSocketAddress(serverIP, port), Network.TIMEOUT);
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