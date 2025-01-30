import java.net.Socket;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

public class Monitor {
    public static void main(String[] args) throws IOException {
        // get urls file from command line
        String urlsFile = null;
        int port = 443;
        if (args.length == 0) {
            System.out.println();
            System.out.println("Usage: java monitor urls_file");
            System.exit(0);
        } else {
            urlsFile = args[0];
        }

        String[] tokens = urlsFile.split("/+");
        if(tokens[0].equals("http:"))
            port = 80;

        // create HTTP client instance for http://inet.cs.fiu.edu/
        HTTPClient client = new HTTPClient(tokens[1], port);
        if(!client.isTCPConnected()) { //checks TCP connection
            System.out.printf("URL: " + urlsFile);
            System.out.println("Status: Network Error");
            System.exit(1);
        }

        System.out.println("TCP Connected...");
        String req = "";
        if(tokens.length > 2) //checks if there's something else in the request
            for(int i = 2; i < tokens.length; i++) 
                req += tokens[i] + "/";

        
        System.out.println(req); 

        client.request("/" + req);
        client.response();
        client.disconnect();
    }

}

class HTTPClient {
    private String host = null; // server host
    private Socket socket = null; // TCP socket
    private BufferedReader reader = null; // reader
    private BufferedWriter writer = null; // writer

    public HTTPClient(String host, int port) throws IOException {
        this.host = host;
        try{
            socket = new Socket(host, port);
        } catch(UnknownHostException exp) { //catches error when the host site doesn't exist at all
            System.out.println("URL: " + host);
            System.out.println("Network Error");
            System.exit(1);
        }
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public boolean isTCPConnected () {
        return socket.isConnected();
    }

    public void request(String path) throws IOException { //writes out HTTP request format
        System.out.println("Sending request....");
        String message = "";
        message += "GET " + path + " HTTP/1.0\r\n";
        message += "Host: " + host + "\r\n";
        message += "\r\n";
        writer.write(message);
        writer.flush();
    }

    public void response() throws IOException {
        System.out.println("Recieving Response...");
        System.out.println();
        System.out.println();

        String line = null;
        String status = reader.readLine();
        if(status != null) {
            String[] statusTokens = status.split(" ");
            String statusCode = statusTokens[1];

            System.out.println("Status: " + status.substring(statusTokens[0].length() + 1)); //print out status code and message 
            
            if(statusCode.equals("301") || statusCode.equals("302")) { //if status code is 301 or 302, redirect
                //redirection
            }

            System.out.println(status); //print out http response
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        } else if(status == null || status.isEmpty()) { //no response
            System.out.println("URL: " + host);
            System.out.println("Status: Could not receive HTTP Response Message.");
        }

        System.out.println();
        System.out.println();
    }

    public void disconnect() throws IOException {
        System.out.println("Disconnecting socket...");
        socket.close();
    }
}