/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.dependency;

import java.io.File;
import java.util.regex.Pattern;

public class Dependency {
    public static final int UNKNOWN_TYPE = 0;
    public static final int INCLUDE_DEPENDENCY = 1;
    public static final int NAVAJO_DEPENDENCY = 2;
    public static final int METHOD_DEPENDENCY = 3;
    public static final int ENTITY_DEPENDENCY = 4;
    public static final int TASK_DEPENDENCY = 5;
    public static final int WORKFLOW_DEPENDENCY = 6;
    public static final int TIPI_DEPENDENCY = 7;
    public static final int ARTICLE_DEPENDENCY = 8;
    public static final int JAVA_DEPENDENCY = 9;
    
    private int type;
    private String scriptFile;
    private String dependeeFile;
    private int linenr;
    private boolean isBroken = false;
    
    public Dependency() {
        // JSON serialisation likes to have a constructor...
    }

    public Dependency(String scriptFile, String dependeeFile, int type, int linenr) {
        this(scriptFile, dependeeFile, type, linenr, false);
    }
    
    public Dependency(String scriptFile, String dependeeFile, int type, int linenr, boolean broken) {
        this.scriptFile = scriptFile;
        this.dependeeFile = dependeeFile;
        this.type = type;
        this.linenr = linenr;
        this.isBroken = broken;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(String scriptFile) {
        this.scriptFile = scriptFile;
    }

    public String getDependeeFile() {
        return dependeeFile;
    }

    public void setDependeeFile(String dependeeFile) {
        this.dependeeFile = dependeeFile;
    }

    public int getLinenr() {
        return linenr;
    }

    public boolean isBroken() {
        return isBroken;
    }

    public void setBroken(boolean isBroken) {
        this.isBroken = isBroken;
    }

    public String getScript() {
        String scriptFileRel = null;
        if (type == WORKFLOW_DEPENDENCY) {
            scriptFileRel = scriptFile.split("workflows")[1];
        } else if (type == TIPI_DEPENDENCY) {
            scriptFileRel = scriptFile.split("tipi")[1];
        } else if (type == JAVA_DEPENDENCY) {
            scriptFileRel = scriptFile.split("src")[1];
        } else if (type == ARTICLE_DEPENDENCY) {
            scriptFileRel = scriptFile.split("article")[1];
        } else if (type == TASK_DEPENDENCY) {
            // Tasks file as a bit special, since they don't have their own directory really
            // Hence we simulate this
            String pattern = Pattern.quote(File.separator);
            String[] filenameParts = scriptFile.split(pattern);
            String tenant = filenameParts[filenameParts.length- 3];
            scriptFileRel = File.separator +  tenant + File.separator + "tasks.xml";
        } else {
            scriptFileRel = scriptFile.split("scripts")[1];
        }
        String script = scriptFileRel.substring(1, scriptFileRel.indexOf('.'));
        
         // Replace win32 slashes to be consistent with Navajo script slashes        
        script = script.replace("\\", "/");
        return script;
    }

    public String getDependee() {
        String scriptFileRel =  dependeeFile.split("scripts")[1];
        String script = scriptFileRel.substring(1, scriptFileRel.indexOf('.'));
        
        // Replace win32 slashes to be consistent with Navajo script slashes        
        script = script.replace("\\", "/");
        return script;
    }

    @Override
    public String toString() {
        return getScript() + " - " + getDependee();
    }
    @Override
    public boolean equals(Object object) {    
        if (!(object instanceof Dependency)) {
            return false;
        }
        Dependency otherDep = (Dependency) object;

        if (!scriptFile.equals(otherDep.scriptFile)) {
            return false;
        }
        if (!dependeeFile.equals(otherDep.dependeeFile)) {
            return false;
        }
        if (type != otherDep.type) {
            return false;
        }
        if (linenr != otherDep.linenr) {
            return false;
        }
        if (isBroken != otherDep.isBroken) {
            return false;
        }
        
        
        return true;
    }

    public boolean isTentantSpecificDependee() {
        return tenantFromScriptPath(getDependee()) != null;
    }

    public String getTentantDependee() {
        return tenantFromScriptPath(getDependee());
    }
    
    private String tenantFromScriptPath(String scriptPath) {
        int scoreIndex = scriptPath.lastIndexOf('_');
        int slashIndex = scriptPath.lastIndexOf('/');
        if(scoreIndex>=0 && slashIndex < scoreIndex) {
            return scriptPath.substring(scoreIndex+1, scriptPath.length());
        } else {
            return null;
        }
    }

}

