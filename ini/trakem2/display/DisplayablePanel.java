/**

TrakEM2 plugin for ImageJ(C).
Copyright (C) 2005-2009 Albert Cardona and Rodney Douglas.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt )

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 

You may contact Albert Cardona at acardona at ini.phys.ethz.ch
Institute of Neuroinformatics, University of Zurich / ETH, Switzerland.
**/

package ini.trakem2.display;

import ini.trakem2.utils.Utils;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Event;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.ArrayList;


public final class DisplayablePanel extends JPanel implements MouseListener, ItemListener {

	static public final int HEIGHT = 52;

	static private ImageIcon LOCKED = new ImageIcon(DisplayablePanel.class.getResource("/img/locked.png"));
	static private ImageIcon UNLOCKED = new ImageIcon(DisplayablePanel.class.getResource("/img/unlocked.png"));
	static private ImageIcon VISIBLE = new ImageIcon(DisplayablePanel.class.getResource("/img/visible.png"));
	static private ImageIcon INVISIBLE = new ImageIcon(DisplayablePanel.class.getResource("/img/invisible.png"));
	static private ImageIcon LINKED = new ImageIcon(DisplayablePanel.class.getResource("/img/linked.png"));
	static private ImageIcon UNLINKED = new ImageIcon(DisplayablePanel.class.getResource("/img/unlinked.png"));

	private JCheckBox c, c_locked, c_linked;
	private JLabel title, title2;
	private JPanel titles;
	private SnapshotPanel sp;

	private Display display;
	private Displayable d;

	public DisplayablePanel(Display display, Displayable d) {
		this.display = display;
		this.d = d;

		this.c = new JCheckBox();
		this.c.setSelected(d.isVisible());
		this.c.addItemListener(this);
		this.c.setIcon(INVISIBLE);
		this.c.setSelectedIcon(VISIBLE);
		this.c.setBackground(Color.white);
		Dimension maxdim = new Dimension(26, 14);
		this.c.setPreferredSize(maxdim);
		this.c.setMaximumSize(maxdim);

		this.c_locked = new JCheckBox();
		this.c_locked.setIcon(UNLOCKED);
		this.c_locked.setSelectedIcon(LOCKED);
		this.c_locked.setSelected(d.isLocked2());
		this.c_locked.addItemListener(this);
		this.c_locked.setBackground(Color.white);
		Dimension maxdim10 = new Dimension(26, 10);
		this.c_locked.setPreferredSize(maxdim10);
		this.c_locked.setMaximumSize(maxdim10);

		this.c_linked = new JCheckBox();
		this.c_linked.setIcon(UNLINKED);
		this.c_linked.setSelectedIcon(LINKED);
		this.c_linked.setSelected(d.isLinked());
		this.c_linked.addItemListener(this);
		this.c_linked.setBackground(Color.white);
		this.c_linked.setPreferredSize(maxdim10);
		this.c_linked.setMaximumSize(maxdim10);

		this.sp = new SnapshotPanel(display, d);
		title = new JLabel();
		title.addMouseListener(this);
		title2 = new JLabel();
		title2.addMouseListener(this);
		titles = new JPanel();
		updateTitle();
		BoxLayout bt = new BoxLayout(titles, BoxLayout.Y_AXIS);
		titles.setLayout(bt);
		titles.setBackground(Color.white);
		titles.add(title);
		titles.add(title2);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JPanel checkboxes = new JPanel();
		checkboxes.setBackground(Color.white);
		checkboxes.setLayout(new GridLayout(3,1));
		checkboxes.setMaximumSize(new Dimension(26, 50));
		checkboxes.add(c);
		checkboxes.add(c_locked);
		checkboxes.add(c_linked);
		add(checkboxes);
		add(sp);
		add(titles);

		Dimension dim = new Dimension(250 - Display.scrollbar_width, HEIGHT);
		setMinimumSize(dim);
		setMaximumSize(dim);
		//setPreferredSize(dim);

		addMouseListener(this);
		setBackground(Color.white);
		setBorder(BorderFactory.createLineBorder(Color.black));
	}

	/** For instance-recycling purposes. */
	public void set(final Displayable d) {
		this.d = d;
		c.setSelected(d.isVisible());
		c_locked.setSelected(d.isLocked2());
		updateTitle();
		sp.set(d);
	}

	public void setActive(final boolean active) {
		if (active) {
			setBackground(Color.cyan);
		} else {
			setBackground(Color.white);
		}
	}

	public void paint(final Graphics g) {
		if (display.isSelected(d)) {
			if (null != display.getActive() && display.getActive() == d) { // can be null when initializing ... because swing is designed with built-in async
				setBackground(Color.cyan);
			} else {
				setBackground(Color.pink);
			}
		} else {
			setBackground(Color.white);
		}
		super.paint(g);
	}

	public void setBackground(Color c) {
		super.setBackground(c);
		if (null != titles) {
			titles.setBackground(c);
			title.setBackground(c);
			title2.setBackground(c);
		}
	}

	private String makeUpdatedTitle() {
		if (null == d) { Utils.log2("null d "); return ""; }
		else if (null == d.getTitle()) { Utils.log2("null title for " + d); return ""; }
		final Class c = d.getClass();
		if (c.equals(Patch.class)) {
			return d.getTitle();
		} else if (c.equals(DLabel.class)) {
			return d.getTitle().replace('\n', ' ');
		} else {
			// gather name of the enclosing object in the project tree
			return d.getProject().getMeaningfulTitle(d);
		}
	}

	static private int MAX_CHARS = 23;

	public void updateTitle() {
		String t = makeUpdatedTitle();
		if (t.length() <= MAX_CHARS) {
			title.setText(t);
			title2.setText("");
			return;
		}
		// else split at MAX_CHARS
		// First try to see if it can be cut nicely
		int i = -1;
		int back = t.length() < ((MAX_CHARS * 3) / 2) ? 12 : 5;
		for (int k=MAX_CHARS-1; k>MAX_CHARS-6; k--) {
			char c = t.charAt(k);
			switch (c) {
				case ' ':
				case '/':
				case '_':
				case '.':
					i = k; break;
				default:
					break;
			}
		}
		if (-1 == i) i = MAX_CHARS; // cut at MAX_CHARS anyway
		title.setText(t.substring(0, i));
		String t2 = t.substring(i);
		if (t2.length() > MAX_CHARS) {
			t2 = new StringBuilder(t2.substring(0, 7)).append("...").append(t2.substring(t2.length()-13)).toString();
		}
		title2.setText(t2);

		title.setToolTipText(t);
		title2.setToolTipText(t);
	}

	public void itemStateChanged(final ItemEvent ie) {
		Object source = ie.getSource();
		if (source.equals(c)) {
			if (ie.getStateChange() == ItemEvent.SELECTED) {
				d.setVisible(true);
			} else if (ie.getStateChange() == ItemEvent.DESELECTED) {
				// Prevent hiding when transforming
				if (Display.isTransforming(d)) {
					Utils.showStatus("Transforming! Can't change visibility.", false);
					c.setSelected(true);
					return;
				}
				d.setVisible(false);
			}
		} else if (source.equals(c_locked)) {
			if (ie.getStateChange() == ItemEvent.SELECTED) {
				// Prevent locking while transforming
				if (Display.isTransforming(d)) {
					Utils.logAll("Transforming! Can't lock.");
					c_locked.setSelected(false);
					return;
				}
				d.setLocked(true);
			} else if (ie.getStateChange() == ItemEvent.DESELECTED) {
				d.setLocked(false);
			}
			// Update lock checkboxes of linked Displayables, except of this one
			Collection<Displayable> lg = d.getLinkedGroup(null);
			if (null != lg) {
				lg.remove(d); // not this one!
				Display.updateCheckboxes(lg, LOCK_STATE, d.isLocked2());
			}
		} else if (source.equals(c_linked)) {
			// Prevent linking/unlinking while transforming
			if (Display.isTransforming(d)) {
				Utils.logAll("Transforming! Can't lock.");
					c_locked.setSelected(false);
					return;
			}
			if (ie.getStateChange() == ItemEvent.SELECTED) {
				Utils.log2("Called SELECTED");
				final Rectangle box = d.getBoundingBox();
				final Collection<Displayable> coll = new ArrayList<Displayable>(d.getLayer().find(box, true)); // only those visible and overlapping
				coll.addAll(d.getLayerSet().findZDisplayables(d.getLayer(), box, true));
				if (coll.size() > 1) {
					for (final Displayable other : coll) {   // TODO should not link images to images
						if (other == d) continue;
						d.link(other);
					}
				} else {
					// Nothing to link, restore icon
					c_linked.setSelected(false);
				}
			} else if (ie.getStateChange() == ItemEvent.DESELECTED) {
				Utils.log2("Called DESELECTED");
				d.unlink();
										// TODO should not unlink stack patches
										// TODO none of the checkbox changes are undoable yet.
			}

			// Recompute list of links in Selection
			Display.updateSelection(Display.getFront());

			// TODO: does setting the checkbox activate the above blocks?
		}
	}

	public void mousePressed(final MouseEvent me) {
		if (display.isTransforming()) return;
		display.select(d, me.isShiftDown());
		if (me.isPopupTrigger() || (ij.IJ.isMacOSX() && me.isControlDown()) || MouseEvent.BUTTON2 == me.getButton() || 0 != (me.getModifiers() & Event.META_MASK)) {
			display.getPopupMenu().show(this, me.getX(), me.getY());
		}
	}

	public void mouseReleased(MouseEvent me) {}
	public void mouseEntered(MouseEvent me) {}
	public void mouseExited (MouseEvent me) {}
	public void mouseClicked(MouseEvent me) {}

	public String toString() {
		return "Displayable panel for " + d.toString();
	}

	static public final int LOCK_STATE = 1;
	static public final int VISIBILITY_STATE = 2;
	static public final int LINK_STATE = 4;

	protected void updateCheckbox(final int cb, final boolean state) {
		switch(cb) {
			case LOCK_STATE:
				c_locked.setSelected(state);
				break;
			case VISIBILITY_STATE:
				c.setSelected(state);
				break;
			case LINK_STATE:
				c_linked.setSelected(state);
				break;
			default:
				Utils.log2("Ooops: don't know what to do with checkbox code " + cb);
				break;
		}
	}

	protected void updateCheckbox(final int cb) {
		switch(cb) {
			case LOCK_STATE:
				c_locked.setSelected(d.isLocked());
				break;
			case VISIBILITY_STATE:
				c.setSelected(d.isVisible());
				break;
			case LINK_STATE:
				c_linked.setSelected(d.isLinked());
				break;
			default:
				Utils.log2("Ooops: don't know what to do with checkbox code " + cb);
				break;
		}
	}
}
