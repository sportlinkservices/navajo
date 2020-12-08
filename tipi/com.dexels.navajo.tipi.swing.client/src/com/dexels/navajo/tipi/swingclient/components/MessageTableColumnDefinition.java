/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.tipi.swingclient.components;



public class MessageTableColumnDefinition {

    private String id;
    private String title;
    private boolean editable;

    public MessageTableColumnDefinition(String id, String title, boolean editable) {
        this.id = id;
        this.title = title;
        this.editable = editable;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isEditable() {
        return editable;
    }

}
