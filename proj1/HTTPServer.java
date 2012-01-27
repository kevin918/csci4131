
/**
 * HTTPServer object is a subclass of thread.
 * For every request, an HTTPServer object is created to handle that request.
 *
 * serves content from ~/webdir
 *
 * run: java HTTPServer 3333
 * access: http://[host]:3333/
 */
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.DateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Anand Tripathi [Univeristy of Minnesota] (original)
 * @author Jason Zerbe (assignment contributor)
 */
public class HTTPServer extends Thread {

    protected Socket myServerSocket = null;
    protected String myRootPath = null;

    HTTPServer(Socket theSocket) {
        System.out.println("DEBUG: spawn process for new client");
        this.myServerSocket = theSocket;
        myRootPath = System.getProperty("user.home") + "/webdir";
        System.out.println("DEBUG: server rootpath = " + myRootPath);
    }

    /**
     * getMimeType method returns the MIME type/subtype for a given file
     */
    private String getMimeType(String theRealPathStr) {

        if (theRealPathStr.endsWith(".html") || theRealPathStr.endsWith(".html")) {
            return "text/html";
        }
        if (theRealPathStr.endsWith(".txt") || theRealPathStr.endsWith(".c")
                || theRealPathStr.endsWith(".pl") || theRealPathStr.endsWith(".cc")
                || theRealPathStr.endsWith(".h")) {
            return "text/plain";
        }
        if (theRealPathStr.endsWith(".jpg") || theRealPathStr.endsWith(".jpeg")
                || theRealPathStr.endsWith(".jpe")) {
            return "image/jpeg";
        }
        if (theRealPathStr.endsWith(".gif")) {
            return "image/gif";
        }
        if (theRealPathStr.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (theRealPathStr.endsWith(".ps") || theRealPathStr.endsWith(".eps")) {
            return "application/postscript";
        }
        if (theRealPathStr.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (theRealPathStr.endsWith(".rtf")) {
            return "application/rtf";
        }
        if (theRealPathStr.endsWith(".doc")) {
            return "application/msword";
        }
        if (theRealPathStr.endsWith(".xls") || theRealPathStr.endsWith(".xla")
                || theRealPathStr.endsWith(".xlm") || theRealPathStr.endsWith(".xlc")) {
            return "application/vnd.ims-excel";
        }
        if (theRealPathStr.endsWith(".tgz")) {
            return "application/x-compressed";
        }

        return "text/plain";
    }

    @Override
    public void run() {
        /**
         * setup HTTP server input and output
         */
        InputStream aInputStream = null;
        try {
            aInputStream = myServerSocket.getInputStream();
        } catch (IOException ex) {
            Logger.getLogger(HTTPServer.class.getName()).log(Level.SEVERE, null, ex);
            if (aInputStream == null) {
                return;
            }
        }
        BufferedReader inLine = new BufferedReader(new InputStreamReader(aInputStream));
        OutputStream myResponseOutputStream = null;
        try {
            myResponseOutputStream = myServerSocket.getOutputStream();
        } catch (IOException ex) {
            Logger.getLogger(HTTPServer.class.getName()).log(Level.SEVERE, null, ex);
            if (myResponseOutputStream == null) {
                return;
            }
        }
        PrintStream myResponsePrintStream = new PrintStream(myResponseOutputStream);

        /**
         * the tokenizer we will use throughout (splitting on spaces)
         */
        StringTokenizer myStringTokenizer = null;


        /**
         * process first HTTP line - [method] [myRequestUri] [myRequestProtocol]
         */
        String requestLine = null;
        try {
            requestLine = inLine.readLine();
        } catch (IOException ex) {
            Logger.getLogger(HTTPServer.class.getName()).log(Level.WARNING, null, ex);
            if (requestLine == null) {
                return;
            }
        }
        System.out.println(requestLine);

        myStringTokenizer = new StringTokenizer(requestLine);

        String myRequestMethod = myStringTokenizer.nextToken();   // [GET|POST|HEAD]
        String myRequestUri = myStringTokenizer.nextToken();  // [URI]
        String myRequestProtocol = myStringTokenizer.nextToken();  // [1.1|1.0]

        URL myResourceURL = null;
        String myFilePath;
        if (myRequestUri.startsWith("http")) {
            try {
                myResourceURL = new URL(myRequestUri); //this is full URL
            } catch (MalformedURLException ex) {
                Logger.getLogger(HTTPServer.class.getName()).log(Level.WARNING, null, ex);
                if (myResourceURL == null) {
                    return;
                }
            }
            myFilePath = myResourceURL.getPath();
            System.out.println("DEBUG: absolute URL has filepath = " + myFilePath);
        } else {
            myFilePath = myRequestUri;
        }

        System.out.println("Request=" + myRequestMethod + "; Resource=" + myFilePath + "; Protocol=" + myRequestProtocol);

        String myFullResourcePath;
        if (myFilePath.equals("/") || myFilePath.equals("")) {
            myFullResourcePath = myRootPath + "/index.html";
        } else {
            myFullResourcePath = myRootPath + myFilePath;
        }

        System.out.println("DEBUG: the full resource path = " + myFullResourcePath);

        /**
         * print the remaining headers to the terminal
         */
        try {
            requestLine = inLine.readLine();
            while (requestLine.length() != 0) {
                System.out.println(requestLine);
                requestLine = inLine.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(HTTPServer.class.getName()).log(Level.WARNING, null, ex);
        }


        /**
         * create HTTP response
         */
        File aResourceFile = new File(myFullResourcePath);

        int lastSlash = myFullResourcePath.lastIndexOf("/");
        String fileName = myFullResourcePath.substring(lastSlash + 1);
        System.out.println("DEBUG: file name to output = " + fileName);

        /* Create a new instance of GMT  time formatter   */
        DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
        TimeZone tz = TimeZone.getTimeZone("GMT");
        df.setTimeZone(tz);

        if (aResourceFile.isFile()) {
            String mimeType = getMimeType(myFullResourcePath);

            System.out.println("");
            System.out.println("***-*-*-*-*- Sending HTTP Response Message -*-*-*-***");
            System.out.println("HTTP/1.1 200 OK");
            myResponsePrintStream.println("HTTP/1.1 200 OK");

            System.out.println("Date: " + df.format(new Date()));
            myResponsePrintStream.println("Date: " + df.format(new Date()));

            System.out.println("Last-Modified: " + df.format(new Date(aResourceFile.lastModified())));
            myResponsePrintStream.println("Last-Modeified: " + df.format(new Date(aResourceFile.lastModified())));

            System.out.println("Content-Length: " + aResourceFile.length());
            myResponsePrintStream.println("Content-Length: " + aResourceFile.length());

            System.out.println("Content-Type:" + mimeType + "; charset=ISO-8859-1");
            myResponsePrintStream.println("Content-Type:" + mimeType + "; charset=ISO-8859-1");

            System.out.println("Accept-Ranges: bytes");
            myResponsePrintStream.println("Accept-Ranges: bytes");

            System.out.println("Connection: close");
            myResponsePrintStream.println("Connection: close");

            myResponsePrintStream.println();

            /**
             * output the file data
             */
            FileInputStream aFileInputStream = null;
            try {
                aFileInputStream = new FileInputStream(myFullResourcePath);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(HTTPServer.class.getName()).log(Level.SEVERE, null, ex);
                if (aFileInputStream == null) {
                    return;
                }
            }
            byte buffer[] = new byte[4096];
            int read;
            try {
                while ((read = aFileInputStream.read(buffer)) >= 0) {
                    myResponseOutputStream.write(buffer, 0, read);
                }
            } catch (IOException ex) {
                Logger.getLogger(HTTPServer.class.getName()).log(Level.WARNING, null, ex);
            }
        } else {
            System.out.println("***-*-*-*-*- Sending HTTP Response Message -*-*-*-***");
            System.out.println("HTTP/1.1  404  Not found");
            myResponsePrintStream.println("HTTP/1.1 404 Not found");

            System.out.println("Date: " + df.format(new Date()));
            myResponsePrintStream.println("Date: " + df.format(new Date()));

            System.out.println("Content-Type: text/html; charset=ISO-8859-1");
            myResponsePrintStream.println("Content-Type: text/html; charset=ISO-8859-1");

            System.out.println("Accept-Ranges: bytes");
            myResponsePrintStream.println("Accept-Ranges: bytes");

            System.out.println("Connection: close");
            myResponsePrintStream.println("Connection: close");

            myResponsePrintStream.println();
        }
        System.out.println("DEBUG: client exit");
        System.out.println("---------------------------------------------------");
        try {
            myServerSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(HTTPServer.class.getName()).log(Level.WARNING, null, ex);
            return;
        }
    }

    /**
     * Main thread waits for a new TCP connection request to arrive and
     * for each connection, it creates a new HTTPServer objecct to
     * handle that request
     *
     * @param args String[]
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        String webroot;
        if (args.length != 1) {
            throw new RuntimeException("Syntax: HTTPServer port-number");
        }

        System.out.println("Starting server on port " + args[0]);
        webroot = System.getProperty("user.home") + "/webdir";
        System.out.println("Server rootpath = " + webroot);
        ServerSocket server = new ServerSocket(Integer.parseInt(args[0]));

        while (true) {
            System.out.println("Waiting for client request");
            Socket client = server.accept();
            System.out.println("*********************************");
            System.out.println("Received request from " + client.getInetAddress());

            /* Create a new instance of HTTPServer to handle the connection on "client" socket */
            HTTPServer c = new HTTPServer(client);
            c.start();
        }
    }
}
