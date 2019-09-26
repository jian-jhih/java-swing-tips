// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    List<? extends JTabbedPane> list = Arrays.asList(
        makeTestTabbedPane(new ClippedTitleTabbedPane()),
        makeTestTabbedPane(new TextOverflowFadeTabbedPane()));

    JPanel p = new JPanel(new GridLayout(list.size(), 1));
    list.forEach(p::add);

    JCheckBox check = new JCheckBox("LEFT");
    check.addActionListener(e -> {
      int tabPlacement = ((JCheckBox) e.getSource()).isSelected() ? JTabbedPane.LEFT : JTabbedPane.TOP;
      list.forEach(t -> t.setTabPlacement(tabPlacement));
    });

    add(check, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTestTabbedPane(JTabbedPane jtp) {
    jtp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    jtp.addTab("1111111111111111111", new ColorIcon(Color.RED), new JScrollPane(new JTree()));
    jtp.addTab("2", new ColorIcon(Color.GREEN), new JLabel("666666666"));
    jtp.addTab("33333333333333", new ColorIcon(Color.BLUE), new JScrollPane(new JTree()));
    jtp.addTab("444444444444444", new ColorIcon(Color.ORANGE), new JLabel("7777777777"));
    jtp.addTab("55555555555555555555555555555555", new ColorIcon(Color.CYAN), new JLabel("8"));
    return jtp;
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
    frame.setMinimumSize(new Dimension(256, 200));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ClippedTitleTabbedPane extends JTabbedPane {
  protected ClippedTitleTabbedPane() {
    super();
  }

  protected ClippedTitleTabbedPane(int tabPlacement) {
    super(tabPlacement);
  }

  private Insets getSynthInsets(Region region) {
    SynthStyle style = SynthLookAndFeel.getStyle(this, region);
    SynthContext context = new SynthContext(this, region, style, SynthConstants.ENABLED);
    return style.getInsets(context, null);
  }

  private Insets getTabInsets() {
    return Optional.ofNullable(UIManager.getInsets("TabbedPane.tabInsets"))
        .orElseGet(() -> getSynthInsets(Region.TABBED_PANE_TAB));
  }

  private Insets getTabAreaInsets() {
    return Optional.ofNullable(UIManager.getInsets("TabbedPane.tabAreaInsets"))
        .orElseGet(() -> getSynthInsets(Region.TABBED_PANE_TAB_AREA));
  }

  @Override public void doLayout() {
    int tabCount = getTabCount();
    if (tabCount == 0 || !isVisible()) {
      super.doLayout();
      return;
    }
    Insets tabInsets = getTabInsets();
    Insets tabAreaInsets = getTabAreaInsets();
    Insets insets = getInsets();
    int tabPlacement = getTabPlacement();
    int areaWidth = getWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right;
    int tabWidth; // = tabInsets.left + tabInsets.right + 3;
    int gap;

    if (tabPlacement == LEFT || tabPlacement == RIGHT) {
      tabWidth = areaWidth / 4;
      gap = 0;
    } else { // TOP || BOTTOM
      tabWidth = areaWidth / tabCount;
      gap = areaWidth - tabWidth * tabCount;
    }

    // "3" is magic number @see BasicTabbedPaneUI#calculateTabWidth
    tabWidth -= tabInsets.left + tabInsets.right + 3;
    updateAllTabWidth(tabWidth, gap);

    super.doLayout();
  }

  @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    super.insertTab(title, icon, component, Objects.toString(tip, title), index);
    setTabComponentAt(index, new JLabel(title, icon, SwingConstants.LEADING));
  }

  private void updateAllTabWidth(int tabWidth, int gap) {
    Dimension dim = new Dimension();
    int rest = gap;
    for (int i = 0; i < getTabCount(); i++) {
      Component c = getTabComponentAt(i);
      if (c instanceof JComponent) {
        JComponent tab = (JComponent) c;
        int a = (i == getTabCount() - 1) ? rest : 1;
        int w = rest > 0 ? tabWidth + a : tabWidth;
        dim.setSize(w, tab.getPreferredSize().height);
        tab.setPreferredSize(dim);
        rest -= a;
      }
    }
  }
}

class TextOverflowFadeTabbedPane extends ClippedTitleTabbedPane {
  protected TextOverflowFadeTabbedPane() {
    super();
  }

  protected TextOverflowFadeTabbedPane(int tabPlacement) {
    super(tabPlacement);
  }

  @Override public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    super.insertTab(title, icon, component, Objects.toString(tip, title), index);
    JPanel p = new JPanel(new BorderLayout(2, 0));
    p.setOpaque(false);
    p.add(new JLabel(icon), BorderLayout.WEST);
    p.add(new TextOverflowFadeLabel(title));
    setTabComponentAt(index, p);
  }
}

class TextOverflowFadeLabel extends JLabel {
  private static final int LENGTH = 20;
  private static final float DIFF = .05f;

  protected TextOverflowFadeLabel(String text) {
    super(text);
  }

  @Override public void paintComponent(Graphics g) {
    Insets i = getInsets();
    int w = getWidth() - i.left - i.right;
    int h = getHeight() - i.top - i.bottom;
    Rectangle rect = new Rectangle(i.left, i.top, w - LENGTH, h);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setFont(g.getFont());
    g2.setPaint(getForeground());

    FontRenderContext frc = g2.getFontRenderContext();
    TextLayout tl = new TextLayout(getText(), getFont(), frc);
    int baseline = getBaseline(w, h);

    g2.setClip(rect);
    tl.draw(g2, getInsets().left, baseline);

    rect.width = 1;
    float alpha = 1f;
    for (int x = w - LENGTH; x < w; x++) {
      rect.x = x;
      alpha = Math.max(0f, alpha - DIFF);
      g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
      g2.setClip(rect);
      tl.draw(g2, getInsets().left, baseline);
    }
    g2.dispose();
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(1, 2, getIconWidth() - 2, getIconHeight() - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}
