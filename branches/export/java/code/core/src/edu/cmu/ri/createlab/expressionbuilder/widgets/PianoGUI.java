package edu.cmu.ri.createlab.expressionbuilder.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * Created by IntelliJ IDEA.
 * User: jcross
 * Date: 6/13/11
 * Time: 2:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class PianoGUI
   {

   private final Map<ButtonModel, Integer> keyToFreq = new HashMap<ButtonModel, Integer>();
   private final Map<Integer, ButtonModel> freqtoKey = new HashMap<Integer, ButtonModel>();

   private int whiteHeight = 60;
   private int whiteWidth = 14;
   private int blackHeight = 40;
   private int blackWidth = 10;

   private ActionListener keyAction;

   private final JLayeredPane panel = new JLayeredPane();
   private final ButtonGroup group = new ButtonGroup();
   private final JPanel holder = new JPanel();

   public PianoGUI(ActionListener action)
      {

      keyAction = action;

      final WhiteKey c1 = new WhiteKey("C", "131");
      final WhiteKey c2 = new WhiteKey("C", "262");
      final WhiteKey c3 = new WhiteKey("C", "523");
      final WhiteKey c4 = new WhiteKey("C", "1046");
      final WhiteKey c5 = new WhiteKey("C", "2093");

      final WhiteKey d1 = new WhiteKey("D", "147");
      final WhiteKey d2 = new WhiteKey("D", "294");
      final WhiteKey d3 = new WhiteKey("D", "587");
      final WhiteKey d4 = new WhiteKey("D", "1175");

      final WhiteKey e1 = new WhiteKey("E", "165");
      final WhiteKey e2 = new WhiteKey("E", "330");
      final WhiteKey e3 = new WhiteKey("E", "659");
      final WhiteKey e4 = new WhiteKey("E", "1318");

      final WhiteKey f1 = new WhiteKey("F", "175");
      final WhiteKey f2 = new WhiteKey("F", "349");
      final WhiteKey f3 = new WhiteKey("F", "698");
      final WhiteKey f4 = new WhiteKey("F", "1396");

      final WhiteKey g1 = new WhiteKey("G", "196");
      final WhiteKey g2 = new WhiteKey("G", "392");
      final WhiteKey g3 = new WhiteKey("G", "784");
      final WhiteKey g4 = new WhiteKey("G", "1568");

      final WhiteKey a1 = new WhiteKey("A", "220");
      final WhiteKey a2 = new WhiteKey("A", "440");
      final WhiteKey a3 = new WhiteKey("A", "880");
      final WhiteKey a4 = new WhiteKey("A", "1760");

      final WhiteKey b1 = new WhiteKey("B", "246");
      final WhiteKey b2 = new WhiteKey("B", "494");
      final WhiteKey b3 = new WhiteKey("B", "988");
      final WhiteKey b4 = new WhiteKey("B", "1976");

      final BlackKey c1s = new BlackKey("139");
      final BlackKey c2s = new BlackKey("277");
      final BlackKey c3s = new BlackKey("554");
      final BlackKey c4s = new BlackKey("1109");

      final BlackKey d1s = new BlackKey("156");
      final BlackKey d2s = new BlackKey("311");
      final BlackKey d3s = new BlackKey("622");
      final BlackKey d4s = new BlackKey("1244");

      final BlackKey f1s = new BlackKey("185");
      final BlackKey f2s = new BlackKey("370");
      final BlackKey f3s = new BlackKey("740");
      final BlackKey f4s = new BlackKey("1480");

      final BlackKey g1s = new BlackKey("208");
      final BlackKey g2s = new BlackKey("415");
      final BlackKey g3s = new BlackKey("830");
      final BlackKey g4s = new BlackKey("1661");

      final BlackKey a1s = new BlackKey("233");
      final BlackKey a2s = new BlackKey("466");
      final BlackKey a3s = new BlackKey("932");
      final BlackKey a4s = new BlackKey("1864");

      panel.setPreferredSize(new Dimension(whiteWidth * 7 * 4 + whiteWidth, whiteHeight));
      panel.setMinimumSize(panel.getPreferredSize());
      panel.setMaximumSize(panel.getPreferredSize());

      int oct = 0;

      c1.setLocation(whiteWidth * 7 * oct, 0);
      d1.setLocation(whiteWidth + whiteWidth * 7 * oct, 0);
      e1.setLocation(whiteWidth * 2 + whiteWidth * 7 * oct, 0);
      f1.setLocation(whiteWidth * 3 + whiteWidth * 7 * oct, 0);
      g1.setLocation(whiteWidth * 4 + whiteWidth * 7 * oct, 0);
      a1.setLocation(whiteWidth * 5 + whiteWidth * 7 * oct, 0);
      b1.setLocation(whiteWidth * 6 + whiteWidth * 7 * oct, 0);

      oct = 1;

      c2.setLocation(whiteWidth * 7 * oct, 0);
      d2.setLocation(whiteWidth + whiteWidth * 7 * oct, 0);
      e2.setLocation(whiteWidth * 2 + whiteWidth * 7 * oct, 0);
      f2.setLocation(whiteWidth * 3 + whiteWidth * 7 * oct, 0);
      g2.setLocation(whiteWidth * 4 + whiteWidth * 7 * oct, 0);
      a2.setLocation(whiteWidth * 5 + whiteWidth * 7 * oct, 0);
      b2.setLocation(whiteWidth * 6 + whiteWidth * 7 * oct, 0);

      oct = 2;

      c3.setLocation(whiteWidth * 7 * oct, 0);
      d3.setLocation(whiteWidth + whiteWidth * 7 * oct, 0);
      e3.setLocation(whiteWidth * 2 + whiteWidth * 7 * oct, 0);
      f3.setLocation(whiteWidth * 3 + whiteWidth * 7 * oct, 0);
      g3.setLocation(whiteWidth * 4 + whiteWidth * 7 * oct, 0);
      a3.setLocation(whiteWidth * 5 + whiteWidth * 7 * oct, 0);
      b3.setLocation(whiteWidth * 6 + whiteWidth * 7 * oct, 0);

      oct = 3;

      c4.setLocation(whiteWidth * 7 * oct, 0);
      d4.setLocation(whiteWidth + whiteWidth * 7 * oct, 0);
      e4.setLocation(whiteWidth * 2 + whiteWidth * 7 * oct, 0);
      f4.setLocation(whiteWidth * 3 + whiteWidth * 7 * oct, 0);
      g4.setLocation(whiteWidth * 4 + whiteWidth * 7 * oct, 0);
      a4.setLocation(whiteWidth * 5 + whiteWidth * 7 * oct, 0);
      b4.setLocation(whiteWidth * 6 + whiteWidth * 7 * oct, 0);

      c5.setLocation(whiteWidth * 7 * 4, 0);

      oct = 0;

      c1s.setLocation(whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      d1s.setLocation(whiteWidth + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      f1s.setLocation(whiteWidth * 3 + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      g1s.setLocation(whiteWidth * 4 + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      a1s.setLocation(whiteWidth * 5 + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      oct = 1;

      c2s.setLocation(whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      d2s.setLocation(whiteWidth + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      f2s.setLocation(whiteWidth * 3 + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      g2s.setLocation(whiteWidth * 4 + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      a2s.setLocation(whiteWidth * 5 + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      oct = 2;

      c3s.setLocation(whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      d3s.setLocation(whiteWidth + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      f3s.setLocation(whiteWidth * 3 + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      g3s.setLocation(whiteWidth * 4 + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      a3s.setLocation(whiteWidth * 5 + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      oct = 3;

      c4s.setLocation(whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      d4s.setLocation(whiteWidth + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      f4s.setLocation(whiteWidth * 3 + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      g4s.setLocation(whiteWidth * 4 + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      a4s.setLocation(whiteWidth * 5 + whiteWidth * 7 * oct + whiteWidth / 2 + 3, 0);

      group.add(c1);
      group.add(c2);
      group.add(c3);
      group.add(c4);
      group.add(c5);
      group.add(d1);
      group.add(d2);
      group.add(d3);
      group.add(d4);
      group.add(e1);
      group.add(e2);
      group.add(e3);
      group.add(e4);
      group.add(f1);
      group.add(f2);
      group.add(f3);
      group.add(f4);
      group.add(g1);
      group.add(g2);
      group.add(g3);
      group.add(g4);
      group.add(a1);
      group.add(a2);
      group.add(a3);
      group.add(a4);
      group.add(b1);
      group.add(b2);
      group.add(b3);
      group.add(b4);

      group.add(c1s);
      group.add(c2s);
      group.add(c3s);
      group.add(c4s);
      group.add(d1s);
      group.add(d2s);
      group.add(d3s);
      group.add(d4s);
      group.add(f1s);
      group.add(f2s);
      group.add(f3s);
      group.add(f4s);
      group.add(g1s);
      group.add(g2s);
      group.add(g3s);
      group.add(g4s);
      group.add(a1s);
      group.add(a2s);
      group.add(a3s);
      group.add(a4s);

      panel.add(c1, new Integer(0));
      panel.add(d1, new Integer(0));
      panel.add(e1, new Integer(0));
      panel.add(f1, new Integer(0));
      panel.add(g1, new Integer(0));
      panel.add(a1, new Integer(0));
      panel.add(b1, new Integer(0));

      panel.add(c2, new Integer(0));
      panel.add(d2, new Integer(0));
      panel.add(e2, new Integer(0));
      panel.add(f2, new Integer(0));
      panel.add(g2, new Integer(0));
      panel.add(a2, new Integer(0));
      panel.add(b2, new Integer(0));

      panel.add(c3, new Integer(0));
      panel.add(d3, new Integer(0));
      panel.add(e3, new Integer(0));
      panel.add(f3, new Integer(0));
      panel.add(g3, new Integer(0));
      panel.add(a3, new Integer(0));
      panel.add(b3, new Integer(0));

      panel.add(c4, new Integer(0));
      panel.add(d4, new Integer(0));
      panel.add(e4, new Integer(0));
      panel.add(f4, new Integer(0));
      panel.add(g4, new Integer(0));
      panel.add(a4, new Integer(0));
      panel.add(b4, new Integer(0));

      panel.add(c5, new Integer(0));

      panel.add(c1s, new Integer(1));
      panel.add(d1s, new Integer(1));
      panel.add(f1s, new Integer(1));
      panel.add(g1s, new Integer(1));
      panel.add(a1s, new Integer(1));

      panel.add(c2s, new Integer(1));
      panel.add(d2s, new Integer(1));
      panel.add(f2s, new Integer(1));
      panel.add(g2s, new Integer(1));
      panel.add(a2s, new Integer(1));

      panel.add(c3s, new Integer(1));
      panel.add(d3s, new Integer(1));
      panel.add(f3s, new Integer(1));
      panel.add(g3s, new Integer(1));
      panel.add(a3s, new Integer(1));

      panel.add(c4s, new Integer(1));
      panel.add(d4s, new Integer(1));
      panel.add(f4s, new Integer(1));
      panel.add(g4s, new Integer(1));
      panel.add(a4s, new Integer(1));

      panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

      holder.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 2, Color.BLACK));
      holder.setLayout(new BoxLayout(holder, BoxLayout.X_AXIS));
      holder.add(panel);
      }

   public ButtonGroup getButtonGroup()
      {
      return group;
      }

   public Component getComponent()
      {
      return holder;
      }

   public int getSelectedFrequency()
      {
      ButtonModel selectedKeyModel = group.getSelection();
      int freq = keyToFreq.get(selectedKeyModel);
      return freq;
      }

   public void setSelectedFrequency(int freq)
      {

      group.clearSelection();
      if (freqtoKey.containsKey(freq))
         {
         ButtonModel selectedKeyModel = freqtoKey.get(freq);
         group.setSelected(selectedKeyModel, true);
         }
      }

   private class BlackKey extends JToggleButton
      {
      private final int frequency;

      private BlackKey(final String freq)
         {
         super();
         this.setSize(blackWidth, blackHeight);
         this.setName("blackKey");
         frequency = getStringValueAsInteger(freq);
         this.addActionListener(keyAction);
         keyToFreq.put(this.getModel(), frequency);
         freqtoKey.put(frequency, this.getModel());
         this.setFocusable(false);
         }

      private int getFreq()
         {
         return frequency;
         }
      }

   private class WhiteKey extends JToggleButton
      {
      private final int frequency;

      private WhiteKey(final String name, final String freq)
         {
         super(name);
         this.setSize(whiteWidth, whiteHeight);
         this.setName("whiteKey");
         frequency = getStringValueAsInteger(freq);
         this.addActionListener(keyAction);
         keyToFreq.put(this.getModel(), frequency);
         freqtoKey.put(frequency, this.getModel());
         this.setFocusable(false);
         }

      private int getFreq()
         {
         return frequency;
         }
      }

   private Integer getStringValueAsInteger(final String valueStr)
      {
      return (valueStr == null || valueStr.length() <= 0) ? null : Integer.parseInt(valueStr);
      }
   }