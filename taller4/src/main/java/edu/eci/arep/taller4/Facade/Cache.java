package edu.eci.arep.taller4.Facade;

import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.*;

/**
 * Cache class implemented to simulated cache
 * @author Santiago Forero Yate 
 */
public class Cache {
    private ConcurrentHashMap<String, JsonObject> movieCache;
    private static Cache cache = null;


    /**
     * Cache's class constructor
     *
     */
    public Cache(){
        movieCache = new ConcurrentHashMap<String,JsonObject>();
    }

    /**
     * method that returns the instance of Cache class
     * @return the current instance of cache
     */
    public static Cache getInstance(){
        if(cache == null){
            cache = new Cache();
        }

        return cache;
    }


    /**
     * method that returns a result if this one exist inside cache
     * 
     * @param name name of the movie to search
     * @return All data of the movie
     */
    public JsonObject getMovie(String name){
        return movieCache.get(name);
    }

    /**
     * method that returns if a movie is inside cache
     * @param name the name of the movie to search inside cache
     * @return a boolean  
     */
    public boolean movieInCache(String name){
        return movieCache.containsKey(name);
    }

    /**
     * Add a consult of a movie in cache
     * @param name name of the movie
     * @param movieInfo Json about movie info
     */
    public void addMovieToCache(String name, JsonObject movieInfo){
        movieCache.putIfAbsent(name, movieInfo);
    }
}
