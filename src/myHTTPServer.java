import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;

public class myHTTPServer extends Thread {

    static final String HTML_START =
            "<html>" +
                    "<title>HTTP Server in java</title>" +
                    "<body>";

    static final String HTML_END =
            "</body>" +
                    "</html>";

    Socket connectedClient = null;
    BufferedReader inFromClient = null;
    DataOutputStream outToClient = null;
    int port = -1;


    public myHTTPServer(Socket client, int port)
    {
        connectedClient = client;
        this.port = port;
    }

    public void run() {

        try {

            System.out.println( "TCPClient "+
                    connectedClient.getInetAddress() + ":" + connectedClient.getPort() + " is connected to the server");

            inFromClient = new BufferedReader(new InputStreamReader (connectedClient.getInputStream()));
            outToClient = new DataOutputStream(connectedClient.getOutputStream());

            String requestString = inFromClient.readLine();
            String headerLine = requestString;


            StringTokenizer tokenizer = new StringTokenizer(headerLine);
            String httpMethod = tokenizer.nextToken();
            String httpQueryString = tokenizer.nextToken();
            String httpRange = "";

            StringBuffer responseBuffer = new StringBuffer();
            responseBuffer.append("<i> This is the my HTTP Server Main Page.... </i><BR>");
            while (inFromClient.ready())
            {
                if (requestString.contains("Range:")){// Isolate range string
                    httpRange = requestString;
                }
                responseBuffer.append(requestString + "<BR>");
                System.out.println(requestString);
                requestString = inFromClient.readLine();
            }

            if (httpMethod.equals("GET")) {
                if (httpQueryString.equals("/")) {
                    // The default home page
                    sendResponse(200, responseBuffer.toString(), false, "");
                } else { //This is interpreted as a file name
                    String fileName = httpQueryString.replaceFirst("/", "");
                    fileName = "content/" +URLDecoder.decode(fileName);
                    if (new File(fileName).isFile()){
                        if (httpRange != ""){ //Partial File request
                            httpRange =  httpRange.split("=",2)[1];
                            sendResponse(206, fileName, true, httpRange);
                        } else { //Full file request
                            sendResponse(200, fileName, true, "");
                        }
                    } else {
                        sendResponse(404, "<b>The Requested resource not found ....", false, "");
                    }
                }
            }
            else sendResponse(404, "<b>The Requested resource not found ....", false, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    public void sendResponse (int statusCode, String responseString, boolean isFile, String range) throws Exception {

        String statusLine = null;
        String serverdetails = "Server: Java HTTPServer";
        String contentLengthLine = null;
        String fileName = null;
        String contentTypeLine = "Content-Type: text/html" + "\r\n";
        InputStream fin = null;
        String acceptRange = null;
        String contentRange = null;
        ZonedDateTime date = ZonedDateTime.now();

        if (statusCode == 200)
            statusLine = "HTTP/1.1 200 OK" + "\r\n";
        else if (statusCode == 206)
            statusLine = "HTTP/1.1 206 Partial Content" + "\r\n";
        else
            statusLine = "HTTP/1.1 404 Not Found" + "\r\n";

        if (isFile) {
            fileName = responseString;
            fin = new FileInputStream(fileName);
            contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
            if (fileName.endsWith(".html")) {
                contentTypeLine = "Content-Type: text/html\r\n";
            } else if(fileName.endsWith(".jpeg")) {
                contentTypeLine = "Content-Type: image/jpeg\r\n";
            } else if(fileName.endsWith(".css")) {
                contentTypeLine = "Content-Type: text/css\r\n";
            } else if(fileName.endsWith(".gif")) {
                contentTypeLine = "Content-Type: image/gif\r\n";
            } else if(fileName.endsWith(".mp4")) {
                contentTypeLine = "Content-Type: video/mp4\r\n";
            } else if(fileName.endsWith(".png")) {
                contentTypeLine = "Content-Type: image/png\r\n";
            } else if(fileName.endsWith(".txt")) {
                contentTypeLine = "Content-Type: text/txt\r\n";
            } else if(fileName.endsWith("..webm")) {
                contentTypeLine = "Content-Type: application/octet-stream\r\n";
            }
            byte[] byteArray = new byte[fin.available()];
            acceptRange = "Accept-Ranges: 0-" + byteArray.length +  "\r\n";
            contentRange = "Content-Ranges: 0-" + byteArray.length +  "\r\n";
        }
        else {
            responseString = myHTTPServer.HTML_START + responseString + myHTTPServer.HTML_END;
            contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
            acceptRange = "Accept-Ranges: 0-" + responseString.getBytes().length +  "\r\n";
            contentRange = "Content-Ranges: 0-" + responseString.getBytes().length +  "\r\n";
        }

        outToClient.writeBytes(statusLine);
        outToClient.writeBytes(serverdetails);
        outToClient.writeBytes("\r\n");
        outToClient.writeBytes(contentTypeLine);
        outToClient.writeBytes(contentLengthLine);
        outToClient.writeBytes(acceptRange);
        outToClient.writeBytes(contentRange);
        outToClient.writeBytes("Date: " + getServerTime() + "\r\n");
        outToClient.writeBytes("Connection: keep-alive\r\n");
        outToClient.writeBytes("\r\n");

        if (statusCode == 206)  cutFile(range, fileName, outToClient);
        else if (isFile)        sendFile(fin, outToClient);
        else                    outToClient.writeBytes(responseString);

        outToClient.close();
    }

    public void sendFile (InputStream fin, DataOutputStream out) throws Exception {
        byte[] buffer = new byte[1024] ;
        int bytesRead;

        while ((bytesRead = fin.read(buffer)) != -1 ) {
            out.write(buffer, 0, bytesRead);
        }
        fin.close();
    }

    public void cutFile(String range, String fileName, DataOutputStream out){
        try{
            RandomAccessFile file = new RandomAccessFile(fileName, "r");
            String[] ranges = range.split(",");
            for(int i=0; i<ranges.length; i++){ //Loop through each range set
                String begining = ranges[i].substring(0, ranges[i].indexOf("-"));
                String end      = ranges[i].substring(ranges[i].indexOf("-")+1, ranges[i].length());
                int start;
                int finish;
                if(begining.length() == 0){ // last-case
                    start = (int) file.length() - Integer.parseInt(end);
                    finish = (int) file.length();
                } else if(end.length() == 0){ // begin-case
                    start = Integer.parseInt(begining);
                    finish = (int) file.length();
                } else { // start-finish case
                    start = Integer.parseInt(begining);
                    finish = Integer.parseInt(end);
                }
                if(start>finish) throw new Exception("Invalid byte range request");
                
                byte[] data = new byte[finish-start+1];
                file.seek(start);
                int dataSize = file.read(data);
                out.write(data, 0, dataSize);
            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

    public static void main (String args[]) throws Exception {

        int port = 5000;
        if(args.length > 0) {
            port = Integer.valueOf(args[0]);
        }

        ServerSocket Server = new ServerSocket (port, 10, InetAddress.getByName("127.0.0.1"));
        System.out.println ("HttpServer started on port: " + port);

        while(true) {
            Socket connected = Server.accept();
            (new myHTTPServer(connected, port)).start();
        }
    }
}