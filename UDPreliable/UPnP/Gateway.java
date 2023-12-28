package UDPreliable.UPnP;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Gateway {

    private Inet4Address local;
    private String serviceType = null, controlURL = null;

    public Gateway(byte[] data, Inet4Address local)throws Exception {
        this.local = local;

        String location = null;
        StringTokenizer st = new StringTokenizer(new String(data), "\n");
        while(st.hasMoreTokens()){
            String s = st.nextToken().trim();
            if(s.isEmpty() || s.startsWith("HTTP/1.") || s.startsWith("NOTIFY *")){
                continue;
            }
            String name = s.substring(0, s.indexOf(':')), val = s.length() >= name.length() ? s.substring(name.length() + 1).trim() : null;
            if(name.equalsIgnoreCase("location")){
                location = val;
            }
        }
        if(location == null){
            throw new Exception("Unsupported Gateway");
        }

        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(location);
        NodeList services = d.getElementsByTagName("service");
        for(int i = 0; i < services.getLength(); i++){
            Node service = services.item(i);
            NodeList n = service.getChildNodes();
            String serviceType = null, controlURL = null;
            for(int j = 0; j < n.getLength(); j++){
                Node x = n.item(j);
                if(x.getNodeName().trim().equalsIgnoreCase("serviceType")){
                    serviceType = x.getFirstChild().getNodeValue();
                }else if(x.getNodeName().trim().equalsIgnoreCase("controlURL")){
                    controlURL = x.getFirstChild().getNodeValue();
                }
            }
            if(serviceType == null || controlURL == null){
                continue;
            }
            if(serviceType.trim().toLowerCase().contains(":wanipconnection:") || serviceType.trim().toLowerCase().contains(":wanpppconnection:")){
                this.serviceType = serviceType.trim();
                this.controlURL = controlURL.trim();
            }
        }

        if(controlURL == null){
            throw new Exception("Unsupported Gateway");
        }

        int slash = location.indexOf("/", 7); //finds first slash after http://
        if(slash == -1){
            throw new Exception("Unsupported Gateway");
        }

        location = location.substring(0, slash);
        if(!controlURL.startsWith("/")){
            controlURL = "/"+controlURL;
        }
        controlURL = location+controlURL;
    }

    private Map<String, String> command(String action, Map<String, String> params)throws Exception {
        Map<String, String> ret = new HashMap<>();
        String soap = "<?xml version=\"1.0\"?>\r\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<SOAP-ENV:Body><m:"+action+" xmlns:m=\""+serviceType+"\">";

        if(params != null){
            for(Map.Entry<String, String> entry : params.entrySet()){
                soap += "<"+entry.getKey()+">"+entry.getValue()+"</"+entry.getKey()+">";
            }
        }

        soap += "</m:"+action+"></SOAP-ENV:Body></SOAP-ENV:Envelope>";
        byte[] req = soap.getBytes();
        HttpURLConnection conn = (HttpURLConnection) new URL(controlURL).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/xml");
        conn.setRequestProperty("SOAPAction", "\""+serviceType+"#"+action+"\"");
        conn.setRequestProperty("Connection", "Close");
        conn.setRequestProperty("Content-Length", req.length+"");
        conn.getOutputStream().write(req);

        Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(conn.getInputStream());
        NodeIterator iter = ((DocumentTraversal) d).createNodeIterator(d.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
        Node n;
        while((n = iter.nextNode()) != null){
            try{
                if(n.getFirstChild().getNodeType() == Node.TEXT_NODE){
                    ret.put(n.getNodeName(), n.getTextContent());
                }
            }catch(Exception e){
            }
        }
        conn.disconnect();
        return ret;
    }

    public String getLocalIP(){
        return local.getHostAddress();
    }

    public InetAddress getExternalIP(){
        try{
            Map<String, String> r = command("GetExternalIPAddress", null);
            return InetAddress.getByName(r.get("NewExternalIPAddress"));
        }catch(Exception e){
            return null;
        }
    }

    public boolean openPort(int port, boolean udp){
        if(port < 0 || port > 65535){
            throw new IllegalArgumentException("Invalid port");
        }

        Map<String, String> params = new HashMap<>();
        params.put("NewRemoteHost", "");
        params.put("NewProtocol", udp ? "UDP" : "TCP");
        params.put("NewInternalClient", local.getHostAddress());
        params.put("NewExternalPort", port+"");
        params.put("NewInternalPort", port+"");
        params.put("NewEnabled", "1");
        params.put("NewPortMappingDescription", "WaifUPnP");
        params.put("NewLeaseDuration", "0");

        try{
            Map<String, String> r = command("AddPortMapping", params);
            return r.get("errorCode") == null;
        }catch(Exception e){
            return false;
        }
    }

    public boolean closePort(int port, boolean udp){
        if(port < 0 || port > 65535){
            throw new IllegalArgumentException("Invalid port");
        }

        Map<String, String> params = new HashMap<>();
        params.put("NewRemoteHost", "");
        params.put("NewProtocol", udp ? "UDP" : "TCP");
        params.put("NewExternalPort", port+"");

        try{
            command("DeletePortMapping", params);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    public boolean isMapped(int port, boolean udp){
        if(port < 0 || port > 65535){
            throw new IllegalArgumentException("Invalid port");
        }

        Map<String, String> params = new HashMap<>();
        params.put("NewRemoteHost", "");
        params.put("NewProtocol", udp ? "UDP" : "TCP");
        params.put("NewExternalPort", port+"");

        try{
            Map<String, String> r = command("GetSpecificPortMappingEntry", params);
            if(r.get("errorCode") != null){
                throw new Exception();
            }
            return r.get("NewInternalPort") != null;
        }catch(Exception e){
            return false;
        }
    }
}
