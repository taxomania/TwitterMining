package uk.ac.manchester.cs.patelt9.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public final class StaticFunctions {
    public static String getDetails(final String filepath) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(filepath)));
            return br.readLine();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Login file not found");
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                } // catch
            } // if
        } // finally
        return null;
    } // getDetails(String)
} // StaticFunctions
