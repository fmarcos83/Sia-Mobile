/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.files;

import java.io.PrintStream;
import java.util.ArrayList;

public class SiaDir extends SiaNode {

    private SiaDir parent;
    private ArrayList<SiaFile> files;
    private ArrayList<SiaDir> directories;

    public SiaDir(String name, SiaDir parent) {
        this.name = name;
        this.parent = parent;
        files = new ArrayList<>();
        directories = new ArrayList<>();
    }

    /**
     * @param file            the file being added
     * @param path            should be relevant to the directory this method is being called on
     * @param currentLocation the index in path that we're currently at
     */
    public void addSiaFile(SiaFile file, String[] path, int currentLocation) {
        if (path.length == 1 || path.length == currentLocation + 1) { // the file belongs in this directory
            files.add(file);
        } else {
            String currentName = path[currentLocation];
            SiaDir nextDir = getImmediateDir(currentName);
            if (nextDir == null) { // directory that is the next step in the path doesn't already exist
                nextDir = new SiaDir(currentName, this);
                addImmediateDir(nextDir);
            }
            nextDir.addSiaFile(file, path, currentLocation + 1);
        }
    }

    public void addImmediateDir(SiaDir directory) {
        directories.add(directory);
    }

    public SiaDir getImmediateDir(String name) {
        for (SiaDir node : directories)
            if (node.getName().equals(name))
                return node;
        return null;
    }

    public ArrayList<SiaNode> getNodes() {
        ArrayList<SiaNode> result = new ArrayList<>();
        result.addAll(directories);
        result.addAll(files);
        return result;
    }

    public SiaDir getParent() {
        return parent;
    }

    public long getSize() {
        long result = 0;
        for (SiaFile file : files)
            result += file.getSize();
        for (SiaDir dir : directories)
            result += dir.getSize();
        return result;
    }

    public String getFullPath(String pathSoFar) {
        if (parent != null)
            return parent.getFullPath(name + "/" + pathSoFar);
        else
            return pathSoFar;
    }

    public void printAll(PrintStream p, int indent) {
        indent(p, indent);
        p.println(name);
        for (SiaFile file : files) {
            indent(p, indent + 1);
            p.println(file.getName());
        }
        for (SiaDir dir : directories)
            dir.printAll(p, indent + 1);
    }

    private void indent(PrintStream p, int indent) {
        for (int i = 0; i < indent; i++) {
            if (i < indent - 1)
                p.print("   ");
            else
                p.print(" |-");
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("SiaDir: ").append(name);
        result.append("\nFiles:\n");
        for (SiaFile file : files) {
            result.append(file.getName()).append("\n");
        }
        result.append("Directories:\n");
        for (SiaDir dir : directories) {
            result.append(dir.getName()).append("\n");
        }
        return result.toString();
    }
}
