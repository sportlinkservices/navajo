package com.dexels.navajo.mapping.compiler.meta;

import java.io.File;

import com.dexels.navajo.script.api.Dependency;
import com.dexels.navajo.server.DispatcherFactory;

public final class NavajoDependency extends Dependency {
    private static final long serialVersionUID = 8394568139676144098L;
    private String scriptPath = null;

    public NavajoDependency(long timestamp, String id) {
        super(timestamp, id);
    }

    public NavajoDependency(long timestamp, String id, String path) {
        super(timestamp, id);
        this.scriptPath = path;
    }

    @Override
    public final long getCurrentTimeStamp() {
        // Not important.
        return -1;
    }

    @Override
    public final boolean recompileOnDirty() {
        return false;
    }

    public final static long getScriptTimeStamp(String id) {
        // Try to find included script.
        String scriptPath = DispatcherFactory.getInstance().getNavajoConfig().getScriptPath();
        File f = new File(scriptPath, id + ".xml");
        if (f.exists()) {
            return f.lastModified();
        } else {
            return -1;
        }
    }

    public boolean isTentantSpecificInclude() {
        return tenantFromScriptPath(scriptPath) != null;
    }

    public String getTentant() {
        return tenantFromScriptPath(scriptPath);
    }

    private String tenantFromScriptPath(String scriptPath) {
        int scoreIndex = scriptPath.lastIndexOf("_");
        int slashIndex = scriptPath.lastIndexOf("/");
        if (scoreIndex >= 0 && slashIndex < scoreIndex) {
            return scriptPath.substring(scoreIndex + 1, scriptPath.length());
        } else {
            return null;
        }
    }

    public String getScriptPath() {
       return scriptPath;
    }

}
