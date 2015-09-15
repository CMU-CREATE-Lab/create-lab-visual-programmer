package edu.cmu.ri.createlab.sequencebuilder.export;

/**
 * Created by c3morales on 07-09-14.
 * XML_ExpressionExtractor
 * Goes through the XML document and locate specify information such as ports and values
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XML_ExpressionExtractor
   {
   private File oldFile;
   private Document xmlDocument;
   private XPath xPath;
   private String expression;
   private NodeList nodeList;
   private int[] rgbValue = new int[3];

   //constructor
   public XML_ExpressionExtractor(final String path) throws IOException
      {

      try
         {

         oldFile = new File(path);
         //filter the XML (remove the DOCTYPE)
         final XML_Filter filt = new XML_Filter();
         final FileInputStream file = new FileInputStream((filt.filterXML(oldFile.getName(), "Expressions")));

         final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         final DocumentBuilder builder = builderFactory.newDocumentBuilder();
         xmlDocument = builder.parse(file);
         xPath = XPathFactory.newInstance().newXPath();
         }
      catch (final SAXException e)
         {
         e.printStackTrace();
         }
      /*catch (final FileNotFoundException e)
         {
         e.printStackTrace();
         }
      catch (final IOException e)
         {
         e.printStackTrace();
         }*/
      catch (final ParserConfigurationException e)
         {
         e.printStackTrace();
         }
      }

   //returns the name of the file being use
   public String getFileName()
      {
      return oldFile.getName();
      }

   //Get the device port.
   //Pos1 = which service listed in the XML doc
   //Pos2 = which device listed in the XML doc under the pos1 service
   public int getPort(final int pos1, final int pos2)
      {
      expression = "/expression/services/service[" + pos1 + "]/operation/device[" + pos2 + "]/@id";
      String devId = "";
      try
         {
         devId = xPath.compile(expression).evaluate(xmlDocument);
         }
      catch (final XPathExpressionException e)
         {
         e.printStackTrace();
         }

      return Integer.valueOf(devId) + 1;
      }

   // Returns the operation. (EJ: setSpeed, setColor, setIntensity)
   // Pos = which service listed in the XML doc
   public String getOperation(final int pos) throws XPathExpressionException
      {
      expression = "/expression/services/service[" + pos + "]/operation/@name";
      return xPath.compile(expression).evaluate(xmlDocument);
      }

   //Get the value of the indicated device
   //pos1 = which service listed in the XML doc
   //pos2 = which device listed in the XML doc under the pos1 service = THIS IS NOT THE SAME AS THE NUMBER OF THE PORT
   public int getValue(final int pos1, final int pos2)
      {
      expression = "/expression/services/service[" + pos1 + "]/operation/device[" + pos2 + "]/parameter";
      String parameter = "";
      try
         {
         parameter = xPath.compile(expression).evaluate(xmlDocument);
         }
      catch (final XPathExpressionException e)
         {
         e.printStackTrace();
         }
      return Integer.valueOf(parameter);
      }

   //arrange rgb values and returns array
   private void TriColorValue(final int pos1, final int pos2)
      {

      try
         {
         expression = "/expression/services/service[" + pos1 + "]/operation/device[" + pos2 + "]/parameter/@name";
         final NodeList name = (NodeList)xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

         expression = "/expression/services/service[" + pos1 + "]/operation/device[" + pos2 + "]/parameter";
         final NodeList parameter = (NodeList)xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

         for (int counter = 0; counter < name.getLength(); counter++)
            {
            final String rgb = name.item(counter).getFirstChild().getNodeValue();
            final String value = parameter.item(counter).getFirstChild().getNodeValue();
            orderRGBColors(rgb, Integer.valueOf(value));
            }
         }
      catch (final XPathExpressionException e)
         {
         e.printStackTrace();
         }
      }

   //returns an array with the values of the tri-color LEDs [R,G,B]
   public int[] getTriColorValue(final int pos1, final int pos2)
      {
      TriColorValue(pos1, pos2);
      final int[] copy = new int[rgbValue.length];

      for (int counter = 0; counter < rgbValue.length; counter++)
         {
         copy[counter] = rgbValue[counter];
         }
      return copy;
      }

   //returns the tri-color values in a String format
   public String getStringTriColorValue(final int pos1, final int pos2)
      {
      TriColorValue(pos1, pos2);
      return "[" + rgbValue[0] + "" + rgbValue[1] + "" + rgbValue[2] + "]";
      }

   //In arrayIndex 0=Red, 1=Green, 2=Blue
   private void orderRGBColors(final String rgb, final int value)
      {
      switch (rgb.charAt(0))
         {
         case 'r':
            rgbValue[0] = value;
            break;
         case 'g':
            rgbValue[1] = value;
            break;
         case 'b':
            rgbValue[2] = value;
            break;
         }
      }

   //Returns the amount of ports under a service
   //pos = which service listed in the XML doc
   public int getPortCount(final int pos)
      {
      expression = "/expression/services/service[" + pos + "]/operation/device";
      try
         {
         nodeList = (NodeList)xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
         }
      catch (final XPathExpressionException e)
         {
         e.printStackTrace();
         }
      return nodeList.getLength();
      }

   // Returns the amount of services
   public int getServicesCount()
      {
      expression = "/expression/services/service";
      try
         {
         nodeList = (NodeList)xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
         }
      catch (final XPathExpressionException e)
         {
         e.printStackTrace();
         }
      return nodeList.getLength();
      }

   //returns true if is a tri-color led instruction
   public boolean isTriColor(final int pos) throws XPathExpressionException
      {
      return (getOperation(pos).contentEquals("setColor"));
      }
   /* public  void closeFile(){
        filt.close();
    }*/
   }

