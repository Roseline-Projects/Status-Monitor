import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
public class monitor {
public static void main(String[] args) throws IOException {
// get urls file from command line
String urlsFile = null;
if (args.length == 0) {
System.out.println();
System.out.println("Usage: java monitor urls_file");
System.exit(0);
} else {
urlsFile = args[0];
}
// create HTTP client instance for http://inet.cs.fiu.edu/
HTTPClient client = new HTTPClient("inet.cs.fiu.edu", 80);
client.request("/");
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
reader = new BufferedReader(new
InputStreamReader(socket.getInputStream()));
writer = new BufferedWriter(new
OutputStreamWriter(socket.getOutputStream()));
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
String line = null;
while((line=reader.readLine()) != null) {
System.out.println(line);
}
}
public void disconnect() throws IOException {
socket.close();
}
}