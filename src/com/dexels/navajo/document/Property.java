package com.dexels.navajo.document;

import java.util.*;

/**
 * <p>Title: Navajo Product Project</p>
 * <p>Description: This is the official source for the Navajo server</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Dexels BV</p>
 * @author Arjen Schoneveld
 * @version 1.0
 */

public interface Property {

    /**
     * Public constants for the property node.
     */
    public static final String PROPERTY_DEFINITION = "property";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_DESCRIPTION = "description";
    public static final String PROPERTY_LENGTH = "length";
    public static final String PROPERTY_CARDINALITY = "cardinality";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_DIRECTION = "direction";

    /**
     * Valid data types for Navajo properties.
     */
    public static final String STRING_PROPERTY = "string";
    public static final String INTEGER_PROPERTY = "integer";
    public static final String LONG_PROPERTY = "long";
    public static final String DATE_PROPERTY = "date";
    public static final String FLOAT_PROPERTY = "float";
    public static final String URL_PROPERTY = "url";
    public static final String SELECTION_PROPERTY = "selection";
    public static final String MEMO_PROPERTY = "memo";
    public static final String BOOLEAN_PROPERTY = "boolean";
    public static final String POINTS_PROPERTY = "points";
    public static final String DATE_PATTERN_PROPERTY = "date_pattern";
    public static final String PASSWORD_PROPERTY = "password";

    /**
     * Binary data properties (only supported by SOAP protocol)
     */
    public static final String GIF_PROPERTY = "image/gif";
    public static final String JPEG_PROPERTY = "image/jpeg";

    /**
     * Valid direction settings for Navajo properties.
     */
    public static final String DIR_IN = "in";
    public static final String DIR_OUT = "out";
    public static final String DIR_INOUT = "inout";

    /**
     * !!
     */
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    public Selection existsSelection(String name)  throws NavajoException;

    /**
     * Get a selection option with a specific name if it exists. If it does not exists, return null.
     * If the property is not of type "selection", a NavajoException is thrown.
     */
    public Selection getSelection(String name)  throws NavajoException;

    /**
     * Get a selection option with a specific name if it exists. If it does not exists, return null.
     * If the property is not of type "selection", a NavajoException is thrown.
     */
    public Selection getSelectionByValue(String value)  throws NavajoException;

    /**
     * Return all selection objects in the property. If the property object is not of a selection type,
     * a NavajoException is thrown.
     */
    public ArrayList getAllSelections()  throws NavajoException;

    public String getFullPropertyName()  throws NavajoException;

    /**
     * Unsets all options for a selection property.
     */
    public void clearSelections()  throws NavajoException;

    /**
     * Return all selection objects in the property. If the property object is not of a selection type,
     * a NavajoException is thrown.
     */
    public ArrayList getAllSelectedSelections()  throws NavajoException;

    /**
     * Add a selection option to a "selection" property. If the option name already exists, replace
     * it with the new one. If the property is not a "selection" type, a NavajoException is thrown.
     */
    public void addSelection(Selection s)  throws NavajoException;

    public void addSelectionWithoutReplace(Selection s)  throws NavajoException;

    /**
     * Get the property name.
     */
    public String getName();

    /**
     * Set the property name (required). A property name must be unique within a message.
     */
    public void setName(String name);

    /**
     * Get the type of a property (see PROPERTY_* constants).
     */
    public String getType();

    /**
     * Set the type of a property (required).
     */
    public void setType(String type);

    /**
     * Get the value of a (string, integer, float, boolean, date or memo) property.
     */
    public String getValue();

    public String toString();

    /**
     * Set the value of a (string, integer, float, boolean, date or memo) property.
     */
    public void setValue(String value);

    /**
     * Sets the selected option for a selection type property.
     */
    public void setSelected(String value)  throws NavajoException;

    public void setSelected(ArrayList keys)  throws NavajoException;

    public void setSelected(String[] keys)  throws NavajoException;

    /**
     * Get the length attribute of a (string, integer or float) property. The length defines
     * the maximum number of posititions a property value may have.
     */
    public int getLength();

    /**
     * Set the length attribute of a (string, integer or float) property.
     */
    public void setLength(int length);

    /**
     * Set the description attribute of a general property. This attribute has no functional
     * meaning, it is used merely for describing a property.
     */
    public String getDescription();

    /**
     * Get the description of a general property. This attribute is not required, hence
     * it could not be present.
     */
    public void setDescription(String description);

    /**
     * Get the direction of a general property. The direction defines whether the property
     * is input (in), output (out) or both (inout).
     */
    public String getDirection();

    /**
     * Set the direction of a general property.
     */
    public void setDirection(String dir);

    /**
     * Get the cardinality of a "selection" property. The cardinality defines the maximum number
     * of options that can be selected from a "selection" property. Currently to values are
     * supported: "+", for multiple selections and "1", for single selections.
     */
    public String getCardinality();

    /**
     * Set the cardinality of a "selection" property. If the property is not
     * of type "selection" the attribute is set anyway, however it is ignored.
     */
    public void setCardinality(String c);

    /**
     * Set the cartesian coordinates of a "points" property. Points is an array of Vectors.
     * Each vector contains the "point" (of any dimensionality).
     */
    public void setPoints(Vector[] points)  throws NavajoException;

    /**
     * Return the cartesian coordinates of a "points" property.
     * The return value is an array of Vectors (containing floats).
     */
    public Vector[] getPoints()  throws NavajoException;

      /**
     * Return the internal implementation specific representation of the Message.
     *
     * @return
     */
    public Object getRef();

    public boolean isDirIn();

    public boolean isDirOut();

}