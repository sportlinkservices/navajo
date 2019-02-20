package com.dexels.navajo.tipi.components.swingimpl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.tipi.TipiComponentMethod;
import com.dexels.navajo.tipi.TipiHelper;
import com.dexels.navajo.tipi.components.swingimpl.embed.SwingEmbeddedContext;
import com.dexels.navajo.tipi.components.swingimpl.swing.TipiSwingFrame;
import com.dexels.navajo.tipi.components.swingimpl.swing.TipiSwingFrameImpl;
import com.dexels.navajo.tipi.components.swingimpl.swing.TipiSwingHelper;


public class TipiFrame extends TipiSwingDataComponentImpl{
	private static final long serialVersionUID = -1945154266267368285L;
	
	private final static Logger logger = LoggerFactory
			.getLogger(TipiFrame.class);
	private boolean fullscreen = false;
	private boolean visible = false;
	private int x = 0, y = 0, w = 0, h = 0;

	private RootPaneContainer myToplevel = null;
	private JPanel mySuperPanel = null;

	public TipiFrame() {
	}

	@Override
	public Object createContainer() {
		boolean internal = (getContext() instanceof SwingEmbeddedContext)
				;
		TipiHelper th = new TipiSwingHelper();
		th.initHelper(this);

		addHelper(th);
		if (internal) {
			RootPaneContainer or = ((SwingTipiContext) getContext())
					.getOtherRoot();
			myToplevel = or;
			mySuperPanel = new JPanel();
			mySuperPanel.setLayout(new BorderLayout());
			or.getContentPane().add(mySuperPanel, BorderLayout.CENTER);
			return or;
			// if (getContext() instanceof EmbeddedContext) {
			// EmbeddedContext ec = (EmbeddedContext) getContext();
			// myToplevel = (RootPaneContainer) ec.getRootComponent();
			// return ec.getRootComponent();
			// }
		} else {

			TipiSwingFrameImpl myFrame;
			UIManager.getLookAndFeelDefaults().put("ClassLoader", getClass().getClassLoader());
			UIManager.getDefaults();
			myFrame = new TipiSwingFrameImpl(this);
			myToplevel = myFrame;
			mySuperPanel = new JPanel();
			myFrame.setExtendedState(myFrame.getExtendedState()
					| Frame.MAXIMIZED_HORIZ);
			myFrame.getContentPane().add(mySuperPanel, BorderLayout.CENTER);
			mySuperPanel.setLayout(new BorderLayout());
			((SwingTipiContext) myContext).addTopLevel(myFrame);

			// mySuperPanel.addC
			return myFrame;
		}
	}

	@Override
	public void addToContainer(final Object c, final Object constraints) {
		if (this.getClass().getName().contains("Embed")) {

			return;
		}
		// final TipiSwingFrame myFrame = (TipiSwingFrame) getContainer();
		if (JMenuBar.class.isInstance(c)) {
			runSyncInEventThread(new Runnable() {
				@Override
				public void run() {
					if (myToplevel instanceof JFrame) {
						((JFrame) myToplevel).getRootPane().setJMenuBar(
								(JMenuBar) c);
						((JFrame) myToplevel).repaint();
						((JMenuBar) c).revalidate();
					}

					if (myToplevel instanceof JInternalFrame) {
						((JInternalFrame) myToplevel).getRootPane()
								.setJMenuBar((JMenuBar) c);
					}

				}
			});
		} else {
			// FIXME WTF?!!!
			runSyncInEventThread(new Runnable() {
				@Override
				public void run() {

					runSyncInEventThread(new Runnable() {
						@Override
						public void run() {
							mySuperPanel.add((Component) c, constraints);
							// mySuperPanel.doLayout();

						}
					});

				}
			});
		}
	}

	@Override
	public void disposeComponent() {
		runSyncInEventThread(new Runnable() {

			@Override
			public void run() {
				JFrame jj = (JFrame) getContainer();
				if (jj != null) {
					jj.dispose();
					Container parent = jj.getParent();
					if (parent != null) {
						parent.remove(jj);
					}
				}

				clearContainer();
				myToplevel = null;
				mySuperPanel = null;
			}
		});
		TipiFrame.super.disposeComponent();

	}

	@Override
	public void removeFromContainer(final Object c) {
		// final TipiSwingFrame myFrame = (TipiSwingFrame) getContainer();
		runSyncInEventThread(new Runnable() {
			@Override
			public void run() {
				// logger.debug("Beware! not working well");
				myToplevel.getContentPane().remove((Component) c);
			}
		});
	}

	protected void setBounds(Rectangle r) {
		if (myToplevel instanceof JFrame) {
			((JFrame) myToplevel).setBounds(r);
		}
	}

	protected Rectangle getBounds() {
		// final TipiSwingFrame myFrame = (TipiSwingFrame) getContainer();
		if (myToplevel instanceof JFrame) {
			return ((JFrame) myToplevel).getBounds();
		}
		return null;
	}

	protected void setIcon(ImageIcon ic) {
		if (ic == null) {
			return;
		}
		if (myToplevel instanceof JFrame) {
			((JFrame) myToplevel).setIconImage(ic.getImage());
		}
	}

	protected void setTitle(String s) {
		if (myToplevel instanceof JFrame) {
			((JFrame) myToplevel).setTitle(s);
		}
	}

	@Override
	public void setContainerLayout(Object layout) {
		mySuperPanel.setLayout((LayoutManager) layout);
	}

	private ImageIcon getIcon(URL u) {
		return new ImageIcon(u);
	}

	@Override
	public void setComponentValue(final String name, final Object object) {

		runSyncInEventThread(new Runnable() {

			@Override
			public void run() {
				if (name.equals("fullscreen")) {
					fullscreen = ((Boolean) object).booleanValue();
				}
				if (name.equals("visible")) {
					visible = ((Boolean) object).booleanValue();
				}
				if ("icon".equals(name)) {
					if (object instanceof URL) {
						setIcon(getIcon((URL) object));
					} else {
						logger.warn("Warning setting icon of tipiframe:");
					}
				}
				if ("title".equals(name)) {
					TipiFrame.this.setTitle((String) object);
				}

				if ("background".equals(name)) {
					setBackground((Color) object);
				}

			}
		});

		if (name.equals("x")) {
			runAsyncInEventThread(new Runnable() {

				@Override
				public void run() {
					Rectangle bounds = getBounds();
					x = ((Integer) object).intValue();
					bounds.x = x;
					setBounds(bounds);

				}
			});
		}
		if (name.equals("y")) {
			runAsyncInEventThread(new Runnable() {

				@Override
				public void run() {
					Rectangle bounds = getBounds();
					y = ((Integer) object).intValue();
					bounds.y = y;
					setBounds(bounds);

				}
			});
			y = ((Integer) object).intValue();
		}
		if (name.equals("h")) {
			runAsyncInEventThread(new Runnable() {

				@Override
				public void run() {
					Rectangle bounds = getBounds();
					h = ((Integer) object).intValue();
					bounds.height = h;
					setBounds(bounds);

				}
			});
		}
		if (name.equals("w")) {
			runAsyncInEventThread(new Runnable() {

				@Override
				public void run() {
					Rectangle bounds = getBounds();
					w = ((Integer) object).intValue();
					bounds.width = w;
					setBounds(bounds);

				}
			});

		}

		super.setComponentValue(name, object);
	}

	protected void setBackground(Color object) {
		if (myToplevel instanceof JFrame) {
			((JFrame) myToplevel).setBackground(object);
			return;
		}
		if (myToplevel instanceof Container) {
			((Container) myToplevel).setBackground(object);
		}
	}

	@Override
	public Object getComponentValue(String name) {
		if ("visible".equals(name)) {
			if (myToplevel instanceof JFrame) {
				return Boolean.valueOf(((JFrame) myToplevel).isVisible());
			}
		}

		if (name.equals("resizable")) {
			if (myToplevel instanceof JFrame) {
				return Boolean.valueOf(((JFrame) myToplevel).isResizable());
			}
		}

		if (name.equals("fullscreen")) {
			return Boolean.valueOf(
					Frame.MAXIMIZED_BOTH == ((JFrame) myToplevel)
							.getExtendedState());

			// Boolean.valueOf(JFrame.MAXIMIZED_BOTH == myFrame.getExtendedState());
		}

		if (name.equals("title")) {
			if (myToplevel instanceof JFrame) {
				return Boolean.valueOf(((JFrame) myToplevel).getTitle());
			}
			// return myFrame.getTitle();
		}
		Rectangle r = getBounds();
		if (r == null) {
			return super.getComponentValue(name);
		}
		if (name.equals("x")) {
			return Integer.valueOf(r.x);
		}
		if (name.equals("y")) {
			return Integer.valueOf(r.y);
		}
		if (name.equals("w")) {
			return Integer.valueOf(r.width);
		}
		if (name.equals("h")) {
			return Integer.valueOf(r.height);
		}

		// Watch out: Jump exit at getBounds
		return super.getComponentValue(name);
	}

	/**
	 * componentInstantiated
	 * 
	 * @todo Implement this com.dexels.navajo.tipi.TipiComponent method
	 */
	@Override
	public void componentInstantiated() {
		super.componentInstantiated();
		if (getContainer() instanceof TipiSwingFrame) {
			runSyncInEventThread(new Runnable() {
				@Override
                public void run() {
                    setBounds(new Rectangle(x, y, w, h));
                    if (fullscreen) {
                        String osName = System.getProperty("os.name");
                        if (osName != null && osName.startsWith("Linux")) {
                            // Gnome doesn't seem to respond propely to setExtendedState. 
                            // Therefore manually set size to make sure we are full screen.
                            // Use GraphicsEnvironment to support multi-monitor properly
                            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                            int width = gd.getDisplayMode().getWidth();
                            int height = gd.getDisplayMode().getHeight();
                            ((TipiSwingFrame) getSwingContainer()).setSize(width, height);
                        }

                        ((TipiSwingFrame) getSwingContainer()).setExtendedState(JFrame.MAXIMIZED_BOTH);
                    }
                    getSwingContainer().setVisible(visible);
                   
                }
			});

		}
	}

	public final void performTipiMethod(final String name,
			TipiComponentMethod compMeth) {
		runSyncInEventThread(new Runnable() {

			@Override
			public void run() {
				if (name.equals("iconify")) {
					if (getContainer() instanceof TipiSwingFrameImpl) {
						try {
							TipiSwingFrameImpl jj = (TipiSwingFrameImpl) getContainer();
							jj.setExtendedState(jj.getExtendedState()
									| Frame.ICONIFIED);
						} catch (Exception ex) {
							logger.error("Error detected",ex);
						}
					}
				}
				if (name.equals("maximize")) {
					if (getContainer() instanceof TipiSwingFrameImpl) {
						try {
							TipiSwingFrameImpl jj = (TipiSwingFrameImpl) getContainer();
							jj.setExtendedState(jj.getExtendedState()
									| Frame.MAXIMIZED_BOTH);
						} catch (Exception ex) {
							logger.error("Error detected",ex);
						}
					}
				}
				if (name.equals("restore")) {
					// if(getContainer() instanceof TipiSwingFrameImpl) {
					// try {
					// TipiSwingFrameImpl jj = (TipiSwingFrameImpl)
					// getContainer();
					// jj.setExtendedState(jj.getExtendedState()|JFrame.ICONIFIED
					// );
					// } catch (Exception ex) {
					// logger.error("Error detected",ex);
					// }
					// }
				}
			}
		});

	}

	public static void main(String[] args) {
		JFrame j = new JFrame("aap");

		// j.setSize(200,100);
		j.setExtendedState(j.getExtendedState() | Frame.MAXIMIZED_BOTH);
		j.setVisible(true);
	}
}
