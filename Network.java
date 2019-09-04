package fileTransfer;
 
//This class holds global constants used by other fileTransfer classes
public class Network 
{
    public static final int TCP_PORT = 42027;
    public static final int TIMEOUT = 600000; //30 seconds
    public static final int PACKET_SIZE = 1024; //for byte[] packets
    public static enum Protocol {TCP};
}
