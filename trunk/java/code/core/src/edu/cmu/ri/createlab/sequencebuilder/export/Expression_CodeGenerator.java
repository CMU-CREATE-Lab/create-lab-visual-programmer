package edu.cmu.ri.createlab.sequencebuilder.export;

/**
 * Created by c3morales on 07-09-14.
 * Expression_CodeGenerator
 * Generates the code of the expressions
 */

import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;

public class Expression_CodeGenerator
   {

   private XML_ExpressionExtractor xmlDoc;
   private boolean motor = false;

   //CONSTRUCTOR
   public Expression_CodeGenerator(final String path) throws IOException
      {
      xmlDoc = new XML_ExpressionExtractor(path);
      }

   //returns a string with the format of calling a method
   public String getMethod()
      {
      return xmlDoc.getFileName().replace(" ", "").replace(".xml", "").replace("-", "") + "();";
      }

   public String getFile()
      {
      return xmlDoc.getFileName();
      }

   //converts to Arduino commands
   public String expressionConverter(final String operation)
      {

      if (operation.contentEquals("setColor"))
         {
         return "setTriColorLED";
         }
      else if (operation.contentEquals("setPosition"))
         {
         return "setServo";
         }
      else if (operation.contentEquals("setVelocity"))
         {
         motor = true;
         return "setMotor";
         }
      else if (operation.contentEquals("setSpeed"))
         {
         return "setVibration";
         }
      else if (operation.contentEquals("setIntensity"))
         {
         return "setLED";
         }
      else
         {
         return "ERROR";
         }
      }

   //Generates the instruction code block
   public String getInstruction() throws XPathExpressionException
      {
      String instruction = "";
      for (int counter = 1; counter <= xmlDoc.getServicesCount(); counter++)
         {
         for (int counter2 = 1; counter2 <= xmlDoc.getPortCount(counter); counter2++)
            {
            // if is a tri-color led
            if (xmlDoc.isTriColor(counter))
               {
               instruction += "\t" + this.getInstructionTriColor(counter, counter2);
               }
            else
               {
               instruction += "\t" + this.getInstruction(counter, counter2);
               }
            instruction += "\n";
            }
         }
      return instruction + "}\n";
      }

   //Generates individual lines of instructions
   private String getInstruction(final int pos, final int pos2) throws XPathExpressionException
      {
      if ("ERROR".equals(expressionConverter(xmlDoc.getOperation(pos))))
         {
         return "//Sound is not supported";
         }
      else if (motor)
         {
         return "hummingbird." + expressionConverter(xmlDoc.getOperation(pos)) + "(" + xmlDoc.getPort(pos, pos2) + "," + (-1 * xmlDoc.getValue(pos, pos2)) + ");";
         }
      return "hummingbird." + expressionConverter(xmlDoc.getOperation(pos)) + "(" + xmlDoc.getPort(pos, pos2) + "," + xmlDoc.getValue(pos, pos2) + ");";
      }

   //True if is a tri-color led instuction
   public boolean isTriColor(final int pos) throws XPathExpressionException
      {
      return xmlDoc.isTriColor(pos);
      }

   //Generates individual lines of instructions for tri-color leds
   public String getInstructionTriColor(final int pos, final int pos2) throws XPathExpressionException
      {
      final int[] rgbVal = xmlDoc.getTriColorValue(pos, pos2);
      return "hummingbird." + expressionConverter(xmlDoc.getOperation(pos)) + "(" + xmlDoc.getPort(pos, pos2) + "," + rgbVal[0] + "," + rgbVal[1] + "," + rgbVal[2] + ");";
      }
   }

