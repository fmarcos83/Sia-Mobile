/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.files;

public abstract class SiaNode {

    protected SiaNode parent;
    protected String name;

    public abstract boolean isDirectory();

    public String getName() {
        return name;
    }
}
