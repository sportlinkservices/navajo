/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.pdf.functions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;

import com.dexels.navajo.document.types.Binary;
import com.dexels.navajo.expression.api.FunctionInterface;
import com.dexels.navajo.expression.api.TMLExpressionException;

public class MergePDF extends FunctionInterface {

	@Override
	public String remarks() {
		return "MergePDF merges two PDF documents (represented as Binary)";
	}

	@Override
	public Object evaluate() throws TMLExpressionException {
		
		if ( getOperands().size() != 2 ) {
			throw new TMLExpressionException("Invalid number of operands.");
		}
		
		PDFMergerUtility merger = new PDFMergerUtility();
		
		try {
			File tempFile = File.createTempFile("pdfmerge", "pdf");
			String fileName = tempFile.getCanonicalPath();
			
			merger.setDestinationFileName(fileName);
			
			Binary b1 = null;
			if ( getOperand(0) != null ) {
				b1 = (Binary) getOperand(0);
			}
			
			Binary b2 = null;
			if ( getOperand(1) != null ) {
				b2 = (Binary) getOperand(1);
			}
			
			if ( b1 != null && b2 != null ) {
				merger.addSource(b1.getFile());
				merger.addSource(b2.getFile());
				merger.mergeDocuments();
				Binary result = new Binary(new File(fileName), false);
				tempFile.delete();
				return result;
			} else if ( b1 != null ) {
				return b1;
			} else if ( b2 != null ) {
				return b2;
			} else {
				return null;
			}
			
		} catch (IOException e) {
			throw new TMLExpressionException(this, e.getMessage(), e);
		} catch (COSVisitorException e) {
			throw new TMLExpressionException(this, e.getMessage(), e);
		}
		
	}

	public static void main(String [] args) throws Exception {
		
		Binary b1 = new Binary(new File("/Users/arjenschoneveld/widget_clubs.csv"));
		Binary b2 = new Binary(new File("/Users/arjenschoneveld/Wedstrijdformulier.pdf"));
		
		MergePDF m = new MergePDF();
		m.reset();
		m.insertBinaryOperand(b1);
		m.insertBinaryOperand(b2);
		
		Object o = m.evaluate();
		
		System.err.println("o = " + o);
		
		Binary r = (Binary) o;
		if ( o != null)
			r.write(new FileOutputStream(new File("/Users/arjenschoneveld/result.pdf")));
		
	}
}
