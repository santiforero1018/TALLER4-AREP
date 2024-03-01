package edu.eci.arep.taller4.Facade;

import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
// import edu.eci.arep.taller4.service.*;
import java.util.*;

import com.google.gson.*;

import edu.eci.arep.taller4.Service.WebService;
import edu.eci.arep.taller4.annotations.Component;
import edu.eci.arep.taller4.annotations.RequestMapping;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Web server class to use the web application
 * 
 * @author Santiago Forero Yate
 */
public class WebServer {

    private static final int PORT = 35000;
    private static final APIRestFacade apf = new APIRestFacade();
    private static Map<String, WebService> services = new HashMap<String, WebService>();
    private static Map<String, Method> frwMethods = new HashMap<String, Method>();

    // Variable para patron singleton
    private static WebServer _instance = getInstace();

    /**
     * Defautl Constructor
     */
    public WebServer() {

    }

    /**
     * method that returns the instance of this class
     * 
     * @return the instance of this class
     */
    public static WebServer getInstace() {
        return _instance;
    }

    /**
     * Method that start the web server
     * 
     * @throws IOException            throws IOException if something fails
     * @throws ClassNotFoundException
     */
    public static void startSever() throws IOException, URISyntaxException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
        ServerSocket serverSocket = null;
        getClasses();
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine = "";
            boolean readingFirst = true;
            String petition = "";
            String method = "";
            Boolean searchMovie = false;

            while ((inputLine = in.readLine()) != null) {

                if (readingFirst) {
                    if (inputLine.contains("GET")) {
                        method = "GET";
                        petition = inputLine.split(" ")[1];
                        break;
                    } else if (inputLine.contains("POST")) {
                        method = "POST";
                        petition = inputLine.split(" ")[1];
                        break;
                    }
                }
                if (!in.ready()) {
                    break;
                }
            }
            System.out.println("Metodo de petición: " + method);
            // System.out.println("Asi llego la petición: " + petition);
            searchMovie = (petition.contains("/film?name=")) ? true : false;

            try {
                URI requestUri = new URI(petition);
                String path = requestUri.getPath();
                String query = requestUri.getQuery();

                query = (query != null) ? query.split("=")[1] : "";

                if (path.startsWith("/service")) {
                    String webUri = path.replace("/service", "");
                    // System.out.println("webUri obtenida despues del replace de /serivce: " +
                    // webUri);

                    outputLine = (services.containsKey(webUri))
                            ? petitionPage(services.get(webUri).handle(query), clientSocket.getOutputStream())
                                    .replace("{query}", query)
                            : ((webUri.contains("css") || webUri.contains("jpg") || webUri.contains("js"))
                                    ? petitionPage(webUri, clientSocket.getOutputStream())
                                    : errorPage("/NotFound.html", clientSocket.getOutputStream()));
                } else if (path.startsWith("/framework")) {
                    String webUri = path.replace("/framework", "");
                    if (frwMethods.containsKey(webUri)) {
                        Method invokedMethod = frwMethods.get(webUri);
                        outputLine = (invokedMethod.getParameterCount() == 1)
                                ? petitionPage("/framework.html",
                                        clientSocket.getOutputStream()).replace("{resp}",
                                                invokedMethod.invoke(null, (Object) query).toString())
                                : ((invokedMethod.getParameterCount() == 0) ? petitionPage("/framework.html",
                                        clientSocket.getOutputStream()).replace("{resp}",
                                                invokedMethod.invoke(null).toString())
                                        : ((webUri.contains("css") || webUri.contains("jpg") || webUri.contains("js"))
                                                ? petitionPage(webUri, clientSocket.getOutputStream())
                                                : errorPage("/NotFound.html", clientSocket.getOutputStream())));
                    } else {
                        outputLine = errorPage("/NotFound.html", clientSocket.getOutputStream());
                    }

                }

                else {
                    outputLine = (searchMovie)
                            ? movieInfo(query, clientSocket.getOutputStream())
                            : petitionPage(petition, clientSocket.getOutputStream());
                }

            } catch (Exception e) {
                System.out.println("An error happened: " + e.getMessage());
                e.printStackTrace();
            }

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    /**
     * return a html structure with some movie information
     * 
     * @param name the name of the movie
     * @param op   OutputStream to return an image if is necessary
     * @return a html structure with some movie information, with some headers
     */
    private static String movieInfo(String name, OutputStream op) {
        try {

            JsonObject resp = apf.searchMovie(name);
            JsonElement title = resp.get("Title"), poster = resp.get("Poster"), director = resp.get("Director"),
                    plot = resp.get("Plot");

            String outputLine = getOKHeader() + "text/html\r\n"
                    + "\r\n"
                    + getStaticFile("/movieInfo.html", op).replace("{Title}", title.toString())
                            .replace("\"{Poster}\"", poster.toString()).replace("{Directors}", director.toString())
                            .replace("{Plot}", plot.toString());

            return outputLine;
        } catch (Exception e) {
            System.out.println("una excepción: " + e.getMessage());
        }

        return null;
    }

    /**
     * method that returns the requested page
     * 
     * @param filePetition the path where is allocate the html page and other files
     *                     that use this page
     * @param op           OutputStream to return an image if is necessary
     * @return the requested page for the browser
     */
    private static String petitionPage(String filePetition, OutputStream op) {

        return getOKHeader() + getMimeType(filePetition) + "\r\n"
                + "\r\n"
                + getStaticFile(filePetition, op);
    }

    /**
     * Method that returns a error page when something is not found
     * 
     * @param filePetition
     * @param op
     * @return
     */
    private static String errorPage(String filePetition, OutputStream op) {
        return getNotFoundHeader() + getMimeType(filePetition) + "\r\n"
                + "\r\n"
                + getStaticFile(filePetition, op);
    }

    /**
     * Method that identify the MIME type of the files to return to the client
     * 
     * @param filePetition path of the petition
     * @return a String with the MIME type of the file
     */
    private static String getMimeType(String filePetition) {
        return (filePetition.endsWith(".html") || filePetition.endsWith("/")) ? "text/html"
                : ((filePetition.endsWith(".css")) ? "text/css"
                        : (filePetition.endsWith(".js")) ? "application/javascript"
                                : (filePetition.endsWith(".jpg")) ? "image/jpg" : "text/plain");
    }

    /**
     * this mehtod returns the static file related with the request
     * 
     * @param filePetition path of the file
     * @param op           OutputStream to return an image if is necessary
     * @return A string with all information insite the file
     */
    private static String getStaticFile(String filePetition, OutputStream op) {
        Path file = (filePetition.equals("/")) ? Paths.get("target/classes/public/static/client.html")
                : (Paths.get("target/classes/public/static" + filePetition));

        // System.out.println(filePetition);
        Charset charset = Charset.forName("ISO_8859_1");
        StringBuilder outputLine = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line = null;

            while ((line = reader.readLine()) != null) {
                if (filePetition.contains(".jpg")) {
                    byte[] imageBytes = getAnImage(filePetition);
                    String response = getOKHeader() + getMimeType(filePetition) + "\r\n" +
                            "Content-Length: " + imageBytes.length + "\r\n" +
                            "\r\n";
                    op.write(response.getBytes());
                    op.write(imageBytes);
                }
                outputLine.append(line).append("\n");

            }
        } catch (Exception e) {
            System.err.format(e.getMessage(), e);
            // e.printStackTrace();
        }

        return outputLine.toString();
    }

    /**
     * Method that return the bytes of an image
     * 
     * @param filePetition the route of the file to return to the browser
     * @return an array of bytes
     */
    private static byte[] getAnImage(String filePetition) {

        Path image = Paths.get("target/classes/public/static" + filePetition);

        try {
            return Files.readAllBytes(image);
        } catch (Exception e) {
            System.out.println("can't send the image: " + e.getMessage());
            ;
        }
        return null;
    }

    /**
     * Method when a get request occurs
     * 
     * @param r  name of the service invoked
     * @param ws webService to save on hasMap service
     */
    public static void get(String r, WebService ws) {
        services.put(r, ws);
    }

    /**
     * Method when a get request occurs
     * 
     */
    public static void post() {
        // not implemented yet
    }

    /**
     * method that returns the Ok header when a petition was succesful
     * 
     * @return A string which content is the Ok header
     */
    private static String getOKHeader() {
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: ";
    }

    /**
     * Method that returns the Not Found header
     * 
     * @return a String with not found header
     */
    private static String getNotFoundHeader() {
        return "HTTP/1.1 404 NOT FOUND\r\n"
                + "Content-Type: ";
    }

    /**
     * Load classes with annotations
     * @throws IOException 
     * @throws ClassNotFoundException
     */
    private static void getClasses() throws IOException, ClassNotFoundException {
        Set<String> fileSet = new HashSet<>();
        try (DirectoryStream<Path> stream = Files
                .newDirectoryStream(Paths.get("target/classes/edu/eci/arep/taller4/framework"))) {
            for (Path path : stream) {
                if (!Files.isDirectory(path)) {
                    fileSet.add(path.toString());
                }
            }
        }

        for (String file : fileSet) {

            String classFullName = file.replace(".class", "").replace("target\\classes\\", "").replace("\\", ".");

            Class<?> c = Class.forName(classFullName);

            if (c.isAnnotationPresent(Component.class)) {
                for (Method m : c.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(RequestMapping.class)) {
                        System.out.println(m.getAnnotation(RequestMapping.class).value());
                        frwMethods.put(m.getAnnotation(RequestMapping.class).value(), m);
                    }
                }
            }
        }
    }

}
