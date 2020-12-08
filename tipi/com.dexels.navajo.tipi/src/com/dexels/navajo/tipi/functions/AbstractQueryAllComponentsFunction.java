/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.tipi.functions;

import com.dexels.navajo.expression.api.FunctionInterface;
import com.dexels.navajo.tipi.TipiComponent;

public abstract class AbstractQueryAllComponentsFunction extends FunctionInterface {

    public static final String QUERY_TYPE_AND = "AND";
    public static final String QUERY_TYPE_OR = "OR";

    /**
     * Should return true or false depending on the state of the given component
     * 
     * @param tc
     *            the given TipiComponent
     * @return dependent on the subclass, cannot be null.
     */
    protected abstract Boolean querySingleComponent(TipiComponent tc);

    /**
     * Queries all components in the tree starting with the given rootComponent.
     * Returns the result of the "AND" or "OR" of the method
     * querySingleComponent. This is a recursive method.
     * 
     * @param root
     *            The rootComponent.
     * @param queryType
     *            the queryType. Use the constants. If null, default is
     *            queryType AND.
     * @return
     */
    protected Boolean queryAllComponents(TipiComponent root, String queryType) {
        if (queryType.equals(QUERY_TYPE_OR)) {
            return queryAllComponentsOr(root);
        } else {
            return queryAllComponentsAnd(root);
        }
    }

    private Boolean queryAllComponentsAnd(TipiComponent component) {
        Boolean result = querySingleComponent(component);
        if (!result) {
            return result; // return immediately
        }
        for (TipiComponent child : component.getChildren()) {
            result &= queryAllComponentsAnd(child);
            if (!result) {
                return result; // return immediately
            }
        }

        return result;
    }

    private Boolean queryAllComponentsOr(TipiComponent component) {
        Boolean result = querySingleComponent(component);
        for (TipiComponent child : component.getChildren()) {
            result |= queryAllComponentsOr(child);
        }

        return result;
    }

}
