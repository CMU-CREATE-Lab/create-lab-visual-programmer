package edu.cmu.ri.createlab.visualprogrammer;

import java.awt.Desktop;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class HtmlPane extends JEditorPane
   {
   private static final Logger LOG = Logger.getLogger(HtmlPane.class);

   HtmlPane(final String htmlContent)
      {
      super("text/html", htmlContent);
      this.setEditable(false);
      this.addHyperlinkListener(
            new HyperlinkListener()
            {
            public void hyperlinkUpdate(final HyperlinkEvent event)
               {
               try
                  {
                  if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType()))
                     {
                     Desktop.getDesktop().browse(event.getURL().toURI());
                     }
                  }
               catch (Exception e)
                  {
                  LOG.error("HtmlPane.hyperlinkUpdate(): Exception while trying to launch the link in the browser.", e);
                  }
               }
            });
      }
   }
