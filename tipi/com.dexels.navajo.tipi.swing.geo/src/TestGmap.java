/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import javax.swing.JFrame;

import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.tipi.swing.geo.impl.tilefactory.GoogleTileFactoryInfo;

 
public class TestGmap {
	
	private final static Logger logger = LoggerFactory
			.getLogger(TestGmap.class);
	
	public static void main(String[] args)  {
//	 UIManager.setLookAndFeel(new SubstanceLookAndFeel());
		JXMapKit j = new JXMapKit();
		JFrame jf = new JFrame("Google map");
		j.addPropertyChangeListener(new PropertyChangeListener(){

			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				logger.info("Property: "+pce.getPropertyName());
			}});
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		String s = Locale.getDefault().getCountry();
		logger.info(":: "+s);
		
		j.setTileFactory(new DefaultTileFactory(new GoogleTileFactoryInfo(0, 15, 17, 256,  true,true,false)));
//		j.setTileFactory(new DefaultTileFactory(new OpenStreetMapTileFactoryInfo(17)));
		j.setZoom(14);
			j.setAddressLocation(new GeoPosition(52,5));
			j.setAddressLocation(new GeoPosition(52,6));
			j.setAddressLocation(new GeoPosition(52,7));
			jf.getContentPane().add(j);
		jf.setSize(400, 600);
		jf.setVisible(true);
	}
	
	
}
