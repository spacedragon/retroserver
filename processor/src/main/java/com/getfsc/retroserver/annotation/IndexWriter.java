package com.getfsc.retroserver.annotation;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: draco
 * Date: 16/4/16
 * Time: 上午10:48
 */
public class IndexWriter {


    private Filer filer;

    public IndexWriter(Filer filer) {
        this.filer = filer;
    }

    private FileObject readOldIndexFile(Set<String> entries, String resourceName) throws IOException {
        FileObject resource = openingFiles.get(resourceName);
        if (resource != null) {
            return resource;
        }
        Reader reader = null;
        try {
            resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
            openingFiles.put(resourceName, resource);
            reader = resource.openReader(true);
            readOldIndexFile(entries, reader);
            return resource;
        } catch (FileNotFoundException e) {
            /**
             * Ugly hack for Intellij IDEA incremental compilation.
             * The problem is that it throws FileNotFoundException on the files, if they were not created during the
             * current session of compilation.
             */
            final String realPath = e.getMessage();
            if (new File(realPath).exists()) {
                try (Reader fileReader = new FileReader(realPath)) {
                    readOldIndexFile(entries, fileReader);
                }
            }
        } catch (IOException e) {
            // Thrown by Eclipse JDT when not found
        } catch (UnsupportedOperationException e) {
            // Java6 does not support reading old index files
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return null;
    }

    private static void readOldIndexFile(Set<String> entries, Reader reader) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                entries.add(line);
                line = bufferedReader.readLine();
            }
        }
    }

    private void writeIndexFile(Set<String> entries, String resourceName, FileObject overrideFile) throws IOException {
        FileObject file = overrideFile;
        if (file == null) {
            file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
        }
        try (Writer writer = file.openWriter()) {
            for (String entry : entries) {
                writer.write(entry);
                writer.write("\n");
            }
        }
    }

    HashMap<String,FileObject> openingFiles=new HashMap<>();

    public void writeSimpleNameIndexFile(Set<String> elementList, String resourceName)
            throws IOException {
        if (openingFiles.containsKey(resourceName)) {

            return ;
        }
        FileObject file = readOldIndexFile(elementList, resourceName);
        if (file != null) {
            /**
             * Ugly hack for Eclipse JDT incremental compilation.
             * Eclipse JDT can't createResource() after successful getResource().
             * But we can file.openWriter().
             */
            try {
                writeIndexFile(elementList, resourceName, file);
                return;
            } catch (IllegalStateException e) {
                // Thrown by HotSpot Java Compiler
            }
        }
        writeIndexFile(elementList, resourceName, null);
    }

    public void writeFile(String content, String resourceName) throws IOException {
        FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
        try (Writer writer = file.openWriter()) {
            writer.write(content);
        }
    }
}
