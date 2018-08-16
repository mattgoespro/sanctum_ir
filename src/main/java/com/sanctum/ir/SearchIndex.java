/*
 * Copyright (C) 2018 Matt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.sanctum.ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class used to retrieve Tweets from the index.
 *
 * @author Matt
 */
public class SearchIndex {

    /**
     * Returns all Tweets containing a specific term.
     *
     * @param term
     * @return ArrayList
     * @throws java.io.IOException
     */
    public static ArrayList<String> search(String term) throws IOException {
        ArrayList<String> result = new ArrayList();
        String docs = documents(term);

        if (docs != null) {
            String[] documents = docs.split("; ");
            String[] docsCopy = documents.clone();
            System.out.println(documents.length + " documents found.");
            Arrays.sort(docsCopy, new DocumentComparator());

            if (!Arrays.equals(docsCopy, documents)) {
                System.out.println("Documents not sorted. Writing documents in order.");
                File ind = new File(Configuration.INDEX_SAVE_DIRECTORY + term.charAt(0) + "/" + term + ".txt");

                if (ind.exists()) {
                        try (PrintWriter writer = new PrintWriter(new FileWriter(ind))) {
                            for (String doc : docsCopy) {
                                writer.write(doc + "; ");
                                writer.flush();
                            }
                            writer.close();
                        }
                } else {
                    System.out.println("No index for '" + term + "' exists.");
                    return result;
                }
            }

            for (String document : documents) {
                String doc = ThreadedDataLoader.filePathStore.get(Integer.parseInt(document.substring(0, document.indexOf("("))));
                String[] lines = document.substring(document.indexOf("(") + 1, document.indexOf(")")).split(", ");
                try {
                    for (String t : getTweets(doc, lines)) {
                        result.add(t);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SearchIndex.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return result;
    }

    /**
     * Returns the Tweet text for a document on a specific line.
     *
     * @param doc
     * @param line
     * @return String
     */
    private static ArrayList<String> getTweets(String doc, String[] lines) throws IOException {
        try {
            BufferedReader scFile = new BufferedReader(new FileReader(new File(doc)));
            Arrays.sort(lines);
            int currLine = 0, stopIndex = 0;
            int nextStop = Integer.parseInt(lines[0]);
            ArrayList<String> retrieved = new ArrayList();
            String l = scFile.readLine();

            while (l != null) {
                if (currLine == nextStop) {
                    retrieved.add(l);
                    ++stopIndex;

                    if (stopIndex == lines.length) {
                        break;
                    }

                    nextStop = Integer.parseInt(lines[stopIndex]);
                }
                l = scFile.readLine();
                ++currLine;
            }

            return retrieved;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SearchIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Returns a list of documents for a term as a string.
     *
     * @param term
     * @return String
     */
    private static String documents(String term) {
        System.out.println("Checking " + Configuration.INDEX_SAVE_DIRECTORY + term.charAt(0) + "/" + term + ".txt");
        File ind = new File(Configuration.INDEX_SAVE_DIRECTORY + term.charAt(0) + "/" + term + ".txt");

        try {
            Scanner scFile = new Scanner(ind);
            String docs = "";

            while (scFile.hasNextLine()) {
                docs += scFile.nextLine();
            }

            return docs;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SearchIndex.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
