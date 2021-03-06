package edu.cmu.ri.createlab.terk.expression.manager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseListener;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import edu.cmu.ri.createlab.userinterface.util.ImageUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ExpressionFileManagerView
   {
   private static final int DEFAULT_VISIBLE_ROW_COUNT = 10;

   private final JPanel panel = new JPanel();
   private final JList jList;
   private final JScrollPane listScroller;
   private final Runnable setEnabledRunnable = new SetEnabledRunnable(true);
   private final Runnable setDisabledRunnable = new SetEnabledRunnable(false);
   private final AbstractListModel listModel;
   private final Font itemFont;

   /** Creates an <code>ExpressionFileManagerView</code> with a visible row count of 10. */
   public ExpressionFileManagerView(final AbstractListModel model, final Font itemFont)
      {
      this(model, itemFont, DEFAULT_VISIBLE_ROW_COUNT);
      }

   /** Creates an <code>ExpressionFileManagerView</code> with the given visible row count. */
   public ExpressionFileManagerView(final AbstractListModel model, final Font itemFont, final int visibleRowCount)
      {
      listModel = model;
      this.itemFont = itemFont;
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      jList = new JList(model);
      jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      jList.setLayoutOrientation(JList.VERTICAL);
      jList.setCellRenderer(new MyListCellRenderer());
      jList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      jList.setVisibleRowCount(visibleRowCount);
      jList.setFocusable(false);

      listScroller = new JScrollPane(jList);

      panel.add(listScroller);
      panel.setBorder(BorderFactory.createLineBorder(Color.gray, 1));

      listScroller.setPreferredSize(new Dimension(190, listScroller.getPreferredSize().height));
      listScroller.getVerticalScrollBar().setUnitIncrement(20);
      panel.setMaximumSize(new Dimension(200, panel.getMaximumSize().height));
      }

   public Component getComponent()
      {
      return panel;
      }

   public void setEnabled(final boolean isEnabled)
      {
      final Runnable runnable = isEnabled ? setEnabledRunnable : setDisabledRunnable;
      if (SwingUtilities.isEventDispatchThread())
         {
         runnable.run();
         }
      else
         {
         SwingUtilities.invokeLater(runnable);
         }
      }

   public boolean isSelectionEmpty()
      {
      return jList.isSelectionEmpty();
      }

   public void addListSelectionListener(final ListSelectionListener listener)
      {
      jList.addListSelectionListener(listener);
      }

   public void addMouseListener(final MouseListener listener)
      {
      jList.addMouseListener(listener);
      }

   public int getSelectedIndex()
      {
      return jList.getSelectedIndex();
      }

   public void setSelectedIndex(final int index)
      {
      if (index >= 0 && index < listModel.getSize())
         {
         jList.setSelectedIndex(index);
         }
      }

   public void clearSelection()
      {
      jList.clearSelection();
      }

   private class SetEnabledRunnable implements Runnable
      {
      private final boolean isEnabled;

      private SetEnabledRunnable(final boolean isEnabled)
         {
         this.isEnabled = isEnabled;
         }

      public void run()
         {
         jList.setEnabled(isEnabled);
         listScroller.setEnabled(isEnabled);
         }
      }

   private final class MyListCellRenderer implements ListCellRenderer
      {
      public Component getListCellRendererComponent(final JList list,
                                                    final Object value,
                                                    final int index,
                                                    final boolean isSelected,
                                                    final boolean cellHasFocus)
         {
         final ExpressionFile expressionFile = (ExpressionFile)value;

         // construct the label
         final JLabel label = new JLabel(expressionFile.getPrettyName());
         label.setOpaque(true);
         label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
         label.setHorizontalAlignment(JLabel.LEFT);
         label.setVerticalTextPosition(JLabel.CENTER);
         label.setFont(itemFont);
         label.setEnabled(list.isEnabled());
         label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
         label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
         label.setIcon(ImageUtils.createImageIcon("/edu/cmu/ri/createlab/expressionbuilder/images/expression-icon-small.png"));
         return label;
         }
      }
   }
