import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.io.File;

public class monitor {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println();
            System.out.println("Usage: java monitor urls_file");
            System.exit(0);
        }
        File urlsFile = null;
        Scanner fileReader = null;
        try {
            urlsFile = new File(args[0]);
            fileReader = new Scanner(urlsFile);
        } catch (Exception exp) {
            System.out.println("Cannot find file.");
            System.exit(1);
        }

        // get urls file from command line
        // String urlsFile = null;
        String url = null;
        while (fileReader.hasNextLine()) {
            url = fileReader.nextLine();

            int port = 0;
            String hostName = "";
            String path = "";

            String[] token = url.split("/+");
            hostName = token[1];

            if (token[0].equals("http:")) {

                port = 80;

            } else {
                port = 443;
            }
            if (token.length > 2) {
                for (int i = 2; i < token.length; i++)
                    if (i == token.length - 1) {
                        path += token[i];
                    } else {
                        path += token[i] + "/";
                    }
            }
            // create HTTP client instance for http://inet.cs.fiu.edu/

            HTTPClient client = null;
            try {
               client = new HTTPClient(hostName, port);
            } catch (UnknownHostException exp) {
                System.out.println("URL: " + url);
                System.out.println("Status: Network Error");
                System.out.println();
                continue;
            }

            System.out.println("URL: " + url);
            client.request("/" + path);
            client.response();
            client.disconnect();
            System.out.println();

        }

    }

}

class HTTPClient {

    private String host = null; // server host
    private Socket socket = null; // TCP socket
    private int port = 80;
    private BufferedReader reader = null; // reader
    private BufferedWriter writer = null; // writer

    public HTTPClient(String host, int port) throws IOException, UnknownHostException { //handle exception in main - if catch unknown host exception, just print network error and continue loop
        this.host = host;
        this.port = port;
        this.socket = new Socket(host, port);
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
                if (statusCode.equals("200")) {
                    System.out.println("Status Code: " + statusCode + " OK");
                } else if (statusCode.equals("404")) {
                    System.out.println("Status Code: " + statusCode + " Not Found");
                } else if (statusCode.equals("301") || statusCode.equals("302")) {
                    String nextLine = "";
                    String locationLine = "";
                    while((nextLine = reader.readLine()) != null) {
                        if(nextLine.startsWith("Location:")) {
                            locationLine = nextLine;
                        }
                    }
                    String path = "";
                    //String locationLine = reader.readLine();
                    String locationUrl = locationLine.split(" ")[1];
                    System.out.println("Status Code: " + statusCode + " Moved permanently");
                    String[] token = locationUrl.split("/+");

                    if (token.length > 2) {
                        for (int i = 2; i < token.length; i++)
                            if(i == token.length - 1) {
                                path += token[i];
                            } else {
                                path += token[i] + "/";
                            }
                    }

                    String hostName = token[1];
                    int port = token[0].equals("http:") ? 80 : 443;
                    System.out.println("Redirected URL: " + locationUrl);
                    HTTPClient client = new HTTPClient(hostName, port);
                    client.request("/" + path);
                    client.response();
                    client.disconnect();

                } else {
                    System.out.println("Status: " + statusLine.substring(statusParts[0].length() + 1)); //print out status code and message
                }
            }
        }
        String line = null;

        while ((line = reader.readLine()) != null) {

            // System.out.println(line);
            if (line.contains("<img src=")) {
                String[] tokens = line.split("\"");
                String extension = tokens[1];

                System.out.println("Referenced URL: " + (!extension.startsWith("http") ? host + extension : extension));
                HTTPClient reference = new HTTPClient(host, port);
                reference.request(extension);
                reference.response();
                reference.disconnect();
            }
        }
    }

    public void disconnect() throws IOException {
        socket.close();
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

}