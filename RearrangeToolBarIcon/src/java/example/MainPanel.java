// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JToolBar toolbar = new JToolBar("ToolBarButton");
    toolbar.setFloatable(false);
    DragHandler dh = new DragHandler();
    toolbar.addMouseListener(dh);
    toolbar.addMouseMotionListener(dh);
    toolbar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));

    Stream.of("Copy24.gif", "Cut24.gif", "Paste24.gif",
        "Delete24.gif", "Undo24.gif", "Redo24.gif",
        "Help24.gif", "Open24.gif", "Save24.gif")
        .map(name -> "/toolbarButtonGraphics/general/" + name)
        .map(this::createToolBarButton)
        .forEach(toolbar::add);

    add(toolbar, BorderLayout.NORTH);
    add(new JScrollPane(new JTree()));
    setPreferredSize(new Dimension(320, 240));
  }

  private Component createToolBarButton(String path) {
    JComponent b = new JLabel(new ImageIcon(getClass().getResource(path)));
    b.setOpaque(false);
    return b;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class DragHandler extends MouseAdapter {
  private static final Rectangle PREV_AREA = new Rectangle();
  private static final Rectangle NEXT_AREA = new Rectangle();
  private final JWindow window = new JWindow();
  private Component draggingComponent;
  private int index = -1;
  private final Component gap = Box.createHorizontalStrut(24);
  private final Point startPt = new Point();
  private final int gestureMotionThreshold = DragSource.getDragThreshold();

  @Override public void mousePressed(MouseEvent e) {
    Container parent = (Container) e.getComponent();
    if (parent.getComponentCount() > 0) {
      startPt.setLocation(e.getPoint());
      window.setBackground(new Color(0x0, true));
    }
  }

  private void startDragging(Container parent, Point pt) {
    Component c = parent.getComponentAt(pt);
    index = parent.getComponentZOrder(c);
    if (Objects.equals(c, parent) || index < 0) {
      return;
    }
    draggingComponent = c;
    swapComponentLocation(parent, c, gap, index);

    window.add(draggingComponent);
    window.pack();

    Dimension d = draggingComponent.getPreferredSize();
    Point p = new Point(pt.x - d.width / 2, pt.y - d.height / 2);
    SwingUtilities.convertPointToScreen(p, parent);
    window.setLocation(p);
    window.setVisible(true);
  }

  private static void swapComponentLocation(Container parent, Component remove, Component add, int idx) {
    parent.remove(remove);
    parent.add(add, idx);
    parent.revalidate();
    // parent.repaint();
  }

  @Override public void mouseDragged(MouseEvent e) {
    Point pt = e.getPoint();
    Container parent = (Container) e.getComponent();

    if (!window.isVisible() || Objects.isNull(draggingComponent)) {
      if (startPt.distance(pt) > gestureMotionThreshold) {
        startDragging(parent, pt);
      }
      return;
    }

    Dimension d = draggingComponent.getPreferredSize();
    Point p = new Point(pt.x - d.width / 2, pt.y - d.height / 2);
    SwingUtilities.convertPointToScreen(p, parent);
    window.setLocation(p);

    for (int i = 0; i < parent.getComponentCount(); i++) {
      Component c = parent.getComponent(i);
      Rectangle r = c.getBounds();
      int wd2 = r.width / 2;
      PREV_AREA.setBounds(r.x, r.y, wd2, r.height);
      NEXT_AREA.setBounds(r.x + wd2, r.y, wd2, r.height);
      if (PREV_AREA.contains(pt)) {
        swapComponentLocation(parent, gap, gap, i > 1 ? i : 0);
        return;
      } else if (NEXT_AREA.contains(pt)) {
        swapComponentLocation(parent, gap, gap, i);
        return;
      }
    }
    parent.remove(gap);
    parent.revalidate();
  }

  @Override public void mouseReleased(MouseEvent e) {
    if (!window.isVisible() || Objects.isNull(draggingComponent)) {
      return;
    }
    Point pt = e.getPoint();
    Container parent = (Container) e.getComponent();

    Component cmp = draggingComponent;
    draggingComponent = null;
    window.setVisible(false);

    for (int i = 0; i < parent.getComponentCount(); i++) {
      Component c = parent.getComponent(i);
      Rectangle r = c.getBounds();
      int wd2 = r.width / 2;
      PREV_AREA.setBounds(r.x, r.y, wd2, r.height);
      NEXT_AREA.setBounds(r.x + wd2, r.y, wd2, r.height);
      if (PREV_AREA.contains(pt)) {
        swapComponentLocation(parent, gap, cmp, i > 1 ? i : 0);
        return;
      } else if (NEXT_AREA.contains(pt)) {
        swapComponentLocation(parent, gap, cmp, i);
        return;
      }
    }
    if (parent.getBounds().contains(pt)) {
      swapComponentLocation(parent, gap, cmp, parent.getComponentCount());
    } else {
      swapComponentLocation(parent, gap, cmp, index);
    }
  }
}
