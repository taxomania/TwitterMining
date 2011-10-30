package uk.ac.manchester.cs.patelt9.twitter.practice;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.alchemyapi.api.AlchemyAPI;

public class AlchemyAPIPractice {

    public static void main(final String[] args) {
        AlchemyAPI api = null;
        try {
            api = AlchemyAPI.GetInstanceFromFile("alchemyapikey.txt");
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } // catch
    } // main(String)

}
