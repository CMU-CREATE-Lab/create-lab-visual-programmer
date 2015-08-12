package edu.cmu.ri.createlab.sequencebuilder.export;

/**
 * Created by c3morales on 07-09-14.
 * XML_Filter
 * class that receives the path of an XML doc and removes the DOCTYPE
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import edu.cmu.ri.createlab.visualprogrammer.PathManager;

public class XML_Filter
   {
   private File convertedFileName = null;

   public File filterXML(final String path, String destination) throws IOException
      {

      try
         {
         //Convert XML (take out CDATA)
         final XMLInputFactory inFactory = XMLInputFactory.newFactory();
         final XMLOutputFactory outFactory = XMLOutputFactory.newFactory();
         final XMLEventReader input;

         if (destination.equals("Expressions"))
            {
            input = inFactory.createXMLEventReader(PathManager.getInstance().getExpressionsZipSave().getFile_InputStream(path));
            }
         else
            {
            input = inFactory.createXMLEventReader(PathManager.getInstance().getSequencesZipSave().getFile_InputStream(path));
            }

         final XMLEventReader filtered = inFactory.createFilteredReader(input, new DTDFilter());

         //converted.xml = temporary fil
         convertedFileName = new File(PathManager.getInstance().getArduinoDirectory() + File.separator + ".converted.xml");
         final XMLEventWriter output = outFactory.createXMLEventWriter(new FileOutputStream(convertedFileName));
         output.add(filtered);
         output.flush();
         output.close();
         }
      catch (final XMLStreamException e)
         {
         e.printStackTrace();
         } /*catch (FileNotFoundException e) {
           // e.printStackTrace();
        }*/

      //temporary document with out the DOCTYPE
      return convertedFileName;
      }

   class DTDFilter implements EventFilter
      {
      public boolean accept(final XMLEvent event)
         {
         return event.getEventType() != XMLStreamConstants.DTD;
         }
      }
   }
