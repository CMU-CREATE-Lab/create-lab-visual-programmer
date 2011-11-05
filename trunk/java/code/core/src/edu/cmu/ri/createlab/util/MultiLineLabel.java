package edu.cmu.ri.createlab.util;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import sun.rmi.runtime.Log;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: jcross
 * Date: 11/5/11
 * Time: 12:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class MultiLineLabel extends JLabel {

    private static final Logger LOG = Logger.getLogger(MultiLineLabel.class);

    public MultiLineLabel(@NotNull String inputText, final int rows, final int columns)
    {
        super(inputText);

        String line1 = inputText;

        final JTextArea textArea = new JTextArea(rows, columns);
        textArea.setEditable(false);
        textArea.setText(inputText);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setDragEnabled(true);
//
        final Dimension title_size = new Dimension(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
        textArea.setPreferredSize(title_size);
        textArea.setMaximumSize(title_size);
        textArea.setMinimumSize(title_size);

        setName("expressionBlockTitle");

        final Font font = getFont();

        int stringWidth = getFontMetrics(font).stringWidth(line1);



        while(stringWidth >= title_size.getWidth()){
            line1 = line1.substring(0, line1.length() - 1);
            stringWidth = getFontMetrics(font).stringWidth(line1);
        }



         String line2 = inputText.substring(line1.length());
        if(line2.length()>0)
        {
          final int wordBreak = line1.lastIndexOf(' ');
          if (line1.length() - wordBreak <6)
          {
            line2 = inputText.substring(wordBreak,line1.length()) + line2;
            line1 = line1.substring(0, wordBreak);
            //Move word
          }
          else{
           line2 = line1.substring(line1.length()-1) + line2;
           line1 = line1.substring(0, line1.length()-1) + "-";
           //hypenate
          }

        }

        String htmlText = "<HTML>"+line1;

        stringWidth = getFontMetrics(font).stringWidth(line2);
        while(stringWidth >= title_size.getWidth()){
            line2 = line2.substring(0, line2.length() - 1);
            stringWidth = getFontMetrics(font).stringWidth(line2);
        }


        if (inputText.length()-(line1.length()+line2.length())>0){
            //Ellipse...
            line2 = line2.substring(0, line2.length()-3) + "...";
        }


       htmlText = htmlText + "<BR>" + line2 + "</HTML>";

        this.setText(htmlText);
    }



}
