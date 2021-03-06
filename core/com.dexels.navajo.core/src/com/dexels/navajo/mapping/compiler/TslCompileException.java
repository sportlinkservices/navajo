/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
/*
 * Created on Jun 2, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.dexels.navajo.mapping.compiler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.nanoimpl.XMLElement;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */


public class TslCompileException extends Exception {

	
	private static final Logger logger = LoggerFactory
			.getLogger(TslCompileException.class);
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 8007663369900721659L;
	
	public static final int TSL_UNKNOWN_MAP = -1;
    public static final int TSL_PARSE_EXCEPTION = -2;
    public static final int TSL_UNKNOWN_FIELD = -3;
    public static final int VALIDATION_NO_VALUE = -8;
    public static final int VALIDATION_NO_CODE = -9;
    public static final int VALIDATION_NO_SCRIPT_INCLUDE = -10;
    public static final int TSL_MISSING_FIELD_NAME = -12;
    public static final int SUB_MAP_ERROR = -13;
    public static final int TSL_MISSING_VALUE = -14;
    public static final int TSL_INAPPROPRIATE_NODE = -15;
    public static final int TSL_UNKNOWN_TAG = -16;
  
    
    private int startOffset;
    private int endOffset;
    private XMLElement mySource;
    private final int code;
	private Map solutions = null;
    private String offendingAttribute = null;
    
     public TslCompileException(int code, String message, XMLElement x) {
        super(message+" ("+x.getStartOffset()+"-"+x.getOffset()+")");
        this.startOffset = x.getStartOffset();
        this.endOffset = x.getOffset();
        this.code = code;
        mySource = x;
    }
   public TslCompileException(int code, String message, int startOffset, int endOffset) {
        super(message+" ("+startOffset+"-"+endOffset+")");
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.code = code;
    }

   public int getCode() {
       return code;
   }
   
    /**
     * @return
     */
    public int getEndOffset() {
        return endOffset;
    }

    /**
     * @return
     */
    public int getStartOffset() {
        return startOffset;
    }
    /**
     * @return
     */
    public XMLElement getSource() {
        return mySource;
    }
    
	@SuppressWarnings("rawtypes")
	public void setAttributeProblem(String attributeName, Map alternatives, XMLElement n) {
        offendingAttribute = attributeName;
        solutions = alternatives;
        startOffset = n.getAttributeOffset(attributeName);
        endOffset = n.getAttributeEndOffset(attributeName);
        logger.debug("Attribute problem: "+attributeName+" solutions: "+alternatives+" startOffset: "+startOffset+" endOff: "+endOffset);
    }
	@SuppressWarnings("rawtypes")
	public void setTagProblem(Map alternatives, XMLElement n) {
        offendingAttribute = null;
        solutions = alternatives;
        startOffset = n.getStartTagOffset()+1;
        endOffset = startOffset+n.getName().length();
        logger.debug("Tag problem. solutions: "+alternatives);
    }
    
    public boolean isAttributeProblem() {
        return offendingAttribute!=null;
    }
    
	@SuppressWarnings("rawtypes")
	public Map getSolutions() {
        return solutions;
    }
    
    public String getOffendingAttribute() {
        return offendingAttribute;
    }
 }
