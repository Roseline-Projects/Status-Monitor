import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class monitor {

    

    public static void main(String[] args) throws IOException {


        int port = 0;
        String hostName = "";
        String path = "";
// get urls file from command line
        String urlsFile = null;
        if (args.length == 0) {
            System.out.println();
            System.out.println("Usage: java monitor urls_file");
            System.exit(0);
        } else {
            urlsFile = args[0];
           
        }

         String[] token = urlsFile.split("/+");
         hostName = token[1];

        if(token[0].equals("http:")){

            port = 80;

        }else{
            port = 443;
        }
        if(token.length > 2){
            for(int i = 2; i< token.length; i++ ){
            
             path += token[i] + "/"; 

            }

        }
// create HTTP client instance for http://inet.cs.fiu.edu/
        HTTPClient client = new HTTPClient(hostName, port);
        if(client.isConnected()){
            System.out.printf("URL: %s \n", urlsFile);
            System.out.println("Status: Network error");

        }
        client.request("/" + path);
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
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void request(String path) throws IOException {
        String message = "";
        message += "GET " + path + " HTTP/1.0\r\n";
        message += "Host: " + host + "\r\n";
        message += "\r\n";
        writer.write(message);
        writer.flush();
    }

    public void response() throws IOException {
        String statusLine = reader.readLine();
        if (statusLine != null) {
            String[] statusParts = statusLine.split(" ");
            if (statusParts.length >= 3) {
                String statusCode = statusParts[1];
                System.out.println("HTTP Status Code: " + statusCode);
            }
        }
        String line = null;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    public void disconnect() throws IOException {
        socket.close();
    }



    public boolean isConnected(){
        return socket.isConnected();
    }
}
