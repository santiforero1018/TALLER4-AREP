package edu.eci.arep.taller3.Facade;

import java.net.*;

import com.google.gson.*;

import java.io.*;

/**
 * Rest API facade to do petitions on a external Rest API
 * @author Santiago Forero Yate
 */
public class APIRestFacade {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String MOVIE_URL = "http://www.omdbapi.com/?apikey=7c113d4c&t=";
    private Cache cache = null;

    /**
     * Constructor for APIRestFacade class
     */
    public APIRestFacade(){
        this.cache = Cache.getInstance();
    }

    /**
     * Search for a specific movie by name on a external API
     * @param name name of the movie to search
     * @throws IOException throws IOException if something fails
     * @return a Json with all data about the movie 
    */ 
    public JsonObject searchMovie(String name) throws IOException {
        if(cache.movieInCache(name)){
            return cache.getMovie(name);
        }
        URL obj = new URL(MOVIE_URL+name);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        // JsonObject finalResponse = null;

        // The following invocation perform the connection implicitly before getting the
        // code
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();


            // finalResponse = JsonParser.parseString(response.toString()).getAsJsonObject();

            cache.addMovieToCache(name, JsonParser.parseString(response.toString()).getAsJsonObject());
            // print result
            System.out.println(response.toString());
        } else {
            System.out.println("no se pudo realizar la petición");
        }
        return Cache.getInstance().getMovie(name);
    }
}
