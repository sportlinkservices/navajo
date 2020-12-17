/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.geo.zipcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses a cvs file to query the population data of a gemeente
 * 
 * @author Frank Lyaruu
 *
 */
public class Gemeente2Population {

	private static Map<String,Integer> popMap = new HashMap<String,Integer>();
	
	private final static Logger logger = LoggerFactory
			.getLogger(Gemeente2Population.class);
	/**
	 * @param args
	 */
	
	public static void init() {
		String line;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(Gemeente2Population.class.getResourceAsStream("postcode.csv"),"UTF-8"));
				line = br.readLine();
				while(line!=null) {
					processLine(line);
					line = br.readLine();
				}
				br.close();
		} catch (IOException e) {
			logger.error("Error: ", e);
		}
	}
	
	private static void processLine(String line) {
		//"5392 Maasdonk";"795";"1671";"Maasdonk"
		StringTokenizer st = new StringTokenizer(line,";");
		st.nextToken();
		String pop = st.nextToken();
		String code = st.nextToken();
		st.nextToken();
//		name = name.substring(1,5);
		pop = pop.substring(1,pop.length()-1);
		code = code.substring(1,code.length()-1);
		Integer currentPop = popMap.get(code);
		if(currentPop==null) {
			currentPop = new Integer(Integer.parseInt(pop));
		} else {
			currentPop+=Integer.parseInt(pop);
		}
		popMap.put(code, currentPop);
		
	}
	
// 
	public static int getGemeente(String code) {
		return popMap.get(code);
	}
	
	public static void main(String[] args) {
		init();
		System.err.println("zipmap: "+popMap);
	}

}
