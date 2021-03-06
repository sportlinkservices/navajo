/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.geo.element;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoPointStore {

	private static GeoPointStore myInstance;
	private List<GeoPoint> myPoints = new ArrayList<GeoPoint>();

	private final static Logger logger = LoggerFactory.getLogger(GeoPointStore.class);

	private boolean normalizingEnabled = true;

	private GeoPointStore() {
	}
	
	public static GeoPointStore getInstance() {
		if(myInstance==null) {
			myInstance = new GeoPointStore();
		}
		return myInstance;
	}
	
	public void flush() {
		myPoints.clear();
	}
	
	public void setNormalizingEnabled(boolean normalizing) {
		normalizingEnabled = normalizing;
	}
	
	public GeoPoint getPoint(String coordinates) {
		if(!normalizingEnabled) {
			return new GeoPoint(coordinates);
		}
		for (GeoPoint p : myPoints) {
			if(p.distance(new GeoPoint(coordinates))<GeoPoint.MIN_DISTANCE) {
				return p;
			}
		}
		GeoPoint q = new GeoPoint(coordinates);
		q.setId("POINT:"+myPoints.size());
		myPoints.add(q);
		return q;
	}
	public GeoPoint getPoint(double lon, double lat) {
		if(!normalizingEnabled) {
			return new GeoPoint(lon,lat);
		}
		long l = System.currentTimeMillis();
		for (GeoPoint p : myPoints) {
			if(p.distance(new GeoPoint(lon,lat))<GeoPoint.MIN_DISTANCE) {
				return p;
			}
		}
		GeoPoint q = new GeoPoint(lon,lat);
		q.setId("POINT:"+myPoints.size());
		myPoints.add(q);
		long m = (System.currentTimeMillis()-l);
		if(m>100) {
			logger.info("getPoint: "+m+" # of points in store: "+myPoints.size());
		}
		return q;
	}
}
