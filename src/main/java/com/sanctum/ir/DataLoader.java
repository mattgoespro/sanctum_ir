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
import java.util.ArrayList;

/**
 * Class for loading all Twitter data from the HDFS
 * @author Matt
 */
public class DataLoader {
    
    private ArrayList<TweetLoader> loaders;
    
    /**
     * Constructor
     */
    public DataLoader() {
        this.loaders = new ArrayList();
    }
    
    /**
     * Loads the tweet data from the HDFS.
     */
    public void loadData() {
        System.out.println("Loading data from " + Configuration.get(Configuration.DATA_DIRECTORY));
        long startTime = System.currentTimeMillis();
        File dataFiles = new File(Configuration.get(Configuration.DATA_DIRECTORY));
        
        if(!dataFiles.exists()) {
            System.out.println("Error: Unable to find directory " + Configuration.get(Configuration.DATA_DIRECTORY) + ".");
            return;
        }
        
        ArrayList<String> filePaths = new ArrayList();
        getFiles(dataFiles, filePaths, new TweetFileFilter());
        
        if(filePaths.isEmpty()) {
            System.out.println("Error: File paths could not be found.");
            return;
        }
        
        for (String filePath : filePaths) {
            loaders.add(new TweetLoader(filePath));
        }
        
        System.out.println("Loading successful (" + (System.currentTimeMillis() - startTime)/1000.0 + " sec)");
    }
    
    /**
     * Finds all Twitter file paths within a directory.
     * @param root
     * @param paths
     * @param filter 
     */
    private void getFiles(File root, ArrayList<String> paths, TweetFileFilter filter) {
        if(root.isDirectory()) {
            for (File child : root.listFiles()) {
                getFiles(child, paths, filter);
            }
        } else {
            paths.add(root.getAbsolutePath());
        }
    }
    
}
