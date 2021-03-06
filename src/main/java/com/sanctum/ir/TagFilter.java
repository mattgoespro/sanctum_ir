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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * Class that is used to filter and process tags.
 *
 * @author Matt
 */
public class TagFilter {

    private final ArrayList<String> tagValueBlacklist;
    private final boolean inclMentions;
    private final boolean inclHashtags;

    /**
     * Constructor
     */
    public TagFilter() {
        this.inclMentions = Boolean.parseBoolean(com.sanctum.ir.Configuration.INDEXING_INCLUDE_MENTIONS);
        this.inclHashtags = Boolean.parseBoolean(com.sanctum.ir.Configuration.INDEXING_INCLUDE_HASHTAGS);
        this.tagValueBlacklist = new ArrayList();
    }

    /**
     * Loads the list of words from a file to exclude from indexing.
     *
     * @param fs
     * @throws FileNotFoundException
     */
    public void loadBlacklist(FileSystem fs) throws FileNotFoundException, IOException {
        if (fs != null) {
            FSDataInputStream filterStream = fs.open(new Path("sanctum/indexing_token_blacklist.cfg"));
            LineIterator lineIterator = IOUtils.lineIterator(filterStream, "UTF-8");
            String line;

            while (lineIterator.hasNext()) {
                line = lineIterator.nextLine();

                if (!line.startsWith("#") && !line.startsWith("START")) {
                    this.tagValueBlacklist.add(line);
                }
            }
        } else {
            try (Scanner scFile = new Scanner(new File("indexing_token_blacklist.cfg"))) {
                String line;

                while (scFile.hasNext()) {
                    line = scFile.nextLine();

                    if (!line.startsWith("#")) {
                        this.tagValueBlacklist.add(line);
                    }
                }
            }
        }
    }

    /**
     * Filters the text for indexing.
     *
     * @param words
     */
    public void filterText(ArrayList<String> words) {
        // store word and tags in hashmap
        Iterator it = words.iterator();

        while (it.hasNext()) {
            String w = (String) it.next();

            if (w != null) {
                if ((w.startsWith("#") && !this.inclHashtags) || (w.startsWith("@") && !this.inclMentions)) {
                    it.remove();
                } else if (this.tagValueBlacklist.contains(w)) {
                    it.remove();
                }
            }
        }
    }
    
    /**
     * Returns true if the filter blacklists a term when indexing.
     * @param term
     * @return boolean
     */
    public boolean blacklists(String term) {
        if(term.startsWith("#") && !this.inclHashtags) return true;
        if(term.startsWith("@") && !this.inclMentions) return true;
        return this.tagValueBlacklist.contains(term);
    }
}
