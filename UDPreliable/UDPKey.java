package UDPreliable;



import java.net.InetAddress;
import java.util.UUID;
//UDPKey é uma classe que representa a chave de um socket UDP
//A chave é composta por um UUID, um endereço IP e uma porta
//A chave é utilizada para identificar um socket UDP
//A chave é utilizada para identificar um cliente
//A chave é utilizada para identificar um servidor
public class UDPKey {

    private UUID uuid;
    private InetAddress address;
    private int port;

    public UDPKey(UUID uuid, InetAddress address, int port){
        this.address = address;
        this.port = port;
        this.uuid = uuid;
    }

    public UDPKey(InetAddress address, int port){
        uuid = UUID.randomUUID();
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress(){
        return address;
    }

    public int getPort(){
        return port;
    }

    public UUID getUUID(){
        return uuid;
    }

    public void setUUID(UUID key){
        this.uuid = key;
    }

    public String hash(){
        return uuid.toString()/*+":"+address.getHostAddress()+":"+port*/;
    }

    public boolean equals(Object o){
        if(o instanceof UDPKey){
            return hash().equals(((UDPKey) o).hash());
        }
        return false;
    }
}
