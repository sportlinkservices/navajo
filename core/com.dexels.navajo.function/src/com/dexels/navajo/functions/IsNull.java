/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.functions;

import com.dexels.navajo.document.types.NavajoType;
import com.dexels.navajo.expression.api.FunctionInterface;

/**
 * <p>
 * Title: Navajo Product Project
 * </p>
 * <p>
 * Description: This is the official source for the Navajo server
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Dexels BV
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class IsNull extends FunctionInterface {
    public IsNull() {
    }

    @Override
    public String remarks() {
        /**
         * @todo Implement this com.dexels.navajo.parser.FunctionInterface abstract
         *       method
         */
        return "Will return true if the supplied argument is null, false otherwise";
    }

    @Override
    public Object evaluate() throws com.dexels.navajo.expression.api.TMLExpressionException {
        Object arg = this.getOperands().get(0);
        if (arg == null) {
            return Boolean.TRUE;
        }
        if (arg instanceof NavajoType) {
            NavajoType n = (NavajoType) arg;
            return (n.isEmpty());
        }
        return Boolean.FALSE;
    }

    @Override
    public String usage() {
        return "IsNull( <argument>)";
    }

}
