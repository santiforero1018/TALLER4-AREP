package edu.eci.arep.taller4;

import edu.eci.arep.taller4.Facade.WebServer;

/**
 * Main class to start the application
 * @author Santiago Forero Yate
 */
public class Main {

    /**
     * Default Constructor
     */
    public Main(){
         
    }

    /**
     * main void to start the application
     * @param args arguements to start the application,throws IOException if something fails
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        try {

            // adding arsw service to the server
            WebServer.get("/arsw", (m) -> {
                return "/arsw.html";
            });

            // adding arep service to the server
            WebServer.get("/arep", (m) -> {
                return "/arep.html";
            });


            WebServer.getInstace().startSever();
        } catch (Exception e) {
            System.out.println("Error al iniciar el server: "+ e.getMessage());
        }
        
    }
}