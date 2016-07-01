package edu.cmu.ri.createlab.sequencebuilder.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Created by c3morales on 07-11-14.
 * Re-Created by baprice on 06-09-16
 */

public class ArduinoCodeWriter
   {
   //Starting XML file
   private File inputFile;
   //File to export arduino code
   private File outputFile;
   //The writier that writes to the arduino file
   private BufferedWriter bufferedWriter;
   //String builders for the main setup/loop and for methods respectively
   private StringBuilder arduinoCodeString;
   private StringBuilder methodCodeString;
   private String normalHeader;
   //This is to keep track of recursive calls that are generating methods instead of adding to the main loop
   private boolean isWritingMethod = false;

   private boolean cancel = false;
   private static final Logger LOG = Logger.getLogger(ArduinoCodeWriter.class);

   public ArduinoCodeWriter(final ArduinoFileManager manager) throws IOException
      {

      outputFile = new File(manager.getArduinoFile() + File.separator + manager.getArduinoFileName());
      methodCodeString = new StringBuilder();
      arduinoCodeString = new StringBuilder();
      bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
      inputFile = new File(manager.getMainFile());
      }

   public void generateSequence()
      {

      try
         {
         final CodeGenerator gen = new CodeGenerator(inputFile.toString());
         init();
         driver(gen);

         if (!cancel)
            {
            arduinoCodeString.append("\n}\n");

            //prints methods of methods
            final ArrayList<Method_Manager> method2 = gen.getMethodList();
            for (final Method_Manager myVal : method2)
               {
               methodCodeString.append("void ").append(myVal.getName().replace(" ", "").replace(".xml", "").replace("-", "")).append("(){\n");
               methodCodeString.append(myVal.getInstructions());
               }
            }
         gen.clearList();
         bufferedWriter.append(normalHeader);
         bufferedWriter.append(arduinoCodeString);
         bufferedWriter.append(methodCodeString);
         bufferedWriter.flush();
         bufferedWriter.close();

         if (cancel)
            {
            outputFile.delete();
            }
         }
      catch (final SAXException e)
         {
         e.printStackTrace();
         }
      catch (final XPathExpressionException e)
         {
         e.printStackTrace();
         }
      catch (final IOException e)
         {
         e.printStackTrace();
         }
      }

   public void generateExpression()
      {

      try
         {
         final Expression_CodeGenerator ardCode = new Expression_CodeGenerator(inputFile.toString());
         init();
         arduinoCodeString.append(ardCode.getInstruction());
         bufferedWriter.append(normalHeader);
         bufferedWriter.append(arduinoCodeString);
         bufferedWriter.append(methodCodeString);
         bufferedWriter.flush();
         bufferedWriter.close();
         }
      catch (final IOException e)
         {
         e.printStackTrace();
         }
      catch (final XPathExpressionException e)
         {
         e.printStackTrace();
         }
      }

   //Method that manage most of the call to other
   private void driver(final CodeGenerator gen) throws XPathExpressionException, IOException
      {
      final String[] order = gen.getOrder();

      //expC = is use to have access to the expressions on the XML Doc
      //loopC = is use to have access to the sequences on the XML Doc
      //countC= is use to have access to the counts on the XML Doc
      int expC = 1;
      int loopC = 1;
      int seqC = 1;
      int countC = 1;
      int forkC = 1;
      int linkC = 1;
      StringBuilder builder = isWritingMethod ? methodCodeString : arduinoCodeString;
      //count = to go through all the operations
      for (final String anOrder : order)
         {
         if (anOrder.contains("expression") && !cancel)
            {
            //LOG.debug("ArduinoCodeWriter.driver(): Got Expression" + anOrder);
            try
               {
               builder.append("\t").append(gen.getExpression(expC));
               }
            catch (final IOException e)
               {

               error(gen.getExpFile(expC), gen.getCurrentSeq());
               }
            finally
               {
               expC++;
               }
            }
         //if is a sequence, it calls himself with the name of the new document.
         else if (anOrder.contains("sequence") && !cancel)
            {
            //LOG.debug("ArduinoCodeWriter.driver(): Got Sequence: " + anOrder);
            builder.append("//Start Seq: ").append(gen.getSeqFile(seqC)).append(": ").append(gen.getSeqComment(seqC)).append("\n");
            try
               {

               // final CodeGenerator temp = new CodeGenerator(inputFile.getParent() + File.separator + gen.getSeqFile(seqC));
               final CodeGenerator temp = new CodeGenerator(gen.getSeqFile(seqC));
               driver(temp);
               builder.append("//End Seq: ").append(gen.getSeqFile(seqC)).append("\n");
               }
            catch (final SAXException ignored)
               {
               }
            catch (final IOException e)
               {
               error(gen.getSeqFile(seqC), gen.getCurrentSeq());
               }
            finally
               {
               seqC++;
               }
            }
         else if (anOrder.contains("loopable"))
            {
            //LOG.debug("ArduinoCodeWriter.driver(): Got Loopable: " + anOrder);

            try
               {
               printLoop(gen, loopC);
               }
            finally
               {
               loopC++;
               }
            }
         else if (anOrder.contains("counter"))
            {
            //LOG.debug("ArduinoCodeWriter.driver(): Got Counter: " + anOrder);
            try
               {
               printCounter(gen, countC);
               }
            finally
               {
               countC++;
               }
            }
         else if (anOrder.contains("fork"))
            {
            //LOG.debug("ArduinoCodeWriter.driver(): Got fork: " + anOrder);
            try
               {
               printFork(gen, forkC);
               }
            finally
               {
               forkC++;
               }
            }
         else if (anOrder.contains("link"))
            {
            LOG.debug("ArduinoCodeWriter.driver(): Got Link: " + anOrder);
            try
               {
               printLink(gen, linkC);
               }
            finally
               {
               linkC++;
               }
            }
         }
      }

   private void printLoop(final CodeGenerator gen, final int loopC) throws NumberFormatException, XPathExpressionException, IOException
      {
      StringBuilder builder = isWritingMethod ? methodCodeString : arduinoCodeString;
      //order needs to be change when the user wants to repeat the else branch and not the if branch
      builder.append(gen.getIfBranchHead(loopC));
      if (gen.isRegularLoop())
         {
         ///////////////if branch /////////////////////
         printLoopIfBranch(gen, loopC);
         builder.append("\n}\n");

         ////////////// else- branch//////////////
         builder.append(gen.getElseBranchHead(loopC));
         printLoopElseBranch(gen, loopC);
         builder.append(gen.getElseBranchFoot(loopC));
         }
      else
         {
         ///////////////if branch /////////////////////
         //   arduinoCodeString.append(gen.getIfBranchHead(loopC));
         printLoopElseBranch(gen, loopC);
         builder.append("\n}\n\t");

         ////////////// else- branch//////////////
         builder.append(gen.getElseBranchHead(loopC));
         printLoopIfBranch(gen, loopC);
         builder.append(gen.getElseBranchFoot(loopC));
         }
      }

   private void printLoopIfBranch(final CodeGenerator gen, final int loopC) throws NumberFormatException, XPathExpressionException
      {
      StringBuilder builder = isWritingMethod ? methodCodeString : arduinoCodeString;
      final String[] ifOrder = gen.getIfBranchOrder(loopC);

      //ifLoopExpC = to have access to the expressions inside the if-branch on the XML Doc
      //ifLoopSeqC = to have access to the sequences inside the if-branch on the XMLDoc
      int ifloopExpC = 1;
      int ifloopSeqC = 1;
      for (final String anIfOrder : ifOrder)
         {
         if (anIfOrder.contains("expression"))
            {
            try
               {
               builder.append(gen.getIfBranchExpression(loopC, ifloopExpC));
               }
            catch (final IOException e)
               {
               error(gen.getIfBranchExpFile(loopC, ifloopExpC), gen.getCurrentSeq());
               }
            finally
               {
               ifloopExpC++;
               }
            }
         else
            {
            try
               {
               builder.append("//Start Seq: ").append(gen.getIfBranchSeqFile(loopC, ifloopSeqC)).append("\n");
               //final CodeGenerator temp = new CodeGenerator(inputFile.getParent() + File.separator + gen.getIfBranchSeqFile(loopC, ifloopSeqC));
               final CodeGenerator temp = new CodeGenerator(gen.getIfBranchSeqFile(loopC, ifloopSeqC));
               driver(temp);
               builder.append("//End Seq: ").append(gen.getIfBranchSeqFile(loopC, ifloopSeqC)).append("\n");
               }
            catch (final SAXException ignored)
               {
               }
            catch (final IOException e)
               {
               error(gen.getIfBranchSeqFile(loopC, ifloopSeqC), gen.getCurrentSeq());
               }
            finally
               {
               ifloopSeqC++;
               }
            }
         }
      }

   private void printLoopElseBranch(final CodeGenerator gen, final int loopC) throws NumberFormatException, XPathExpressionException
      {
      StringBuilder builder = isWritingMethod ? methodCodeString : arduinoCodeString;
      final String[] ElseOrder = gen.getElseBranchOrder(loopC);

      //LoopExpC = to have access to the expressions inside the if-branch on the XML Doc
      //LoopSeqC = to have access to the sequences inside the if-branch on the XMLDoc
      int loopExpC = 1;
      int loopSeqC = 1;
      for (final String aElseOrder : ElseOrder)
         {
         if (aElseOrder.contains("expression") && !cancel)
            {
            try
               {
               builder.append("\t").append(gen.getElseBranchExpression(loopC, loopExpC)).append("\n");
               }
            catch (final IOException e)
               {
               error(gen.getElseBranchExpFile(loopC, loopExpC), gen.getCurrentSeq());
               }
            finally
               {
               loopExpC++;
               }
            }
         else if (!cancel)
            {

            try
               {
               builder.append("//Start Seq: ").append(gen.getElseBranchSeqFile(loopC, loopSeqC)).append("\n");
               // final CodeGenerator temp = new CodeGenerator(inputFile.getParent() + File.separator + gen.getElseBranchSeqFile(loopC, loopSeqC));
               final CodeGenerator temp = new CodeGenerator(gen.getElseBranchSeqFile(loopC, loopSeqC));
               driver(temp);
               builder.append("//End Seq: ").append(gen.getElseBranchSeqFile(loopC, loopSeqC)).append("\n");
               }
            catch (final SAXException ignored)
               {
               }
            catch (final IOException e)
               {
               error(gen.getElseBranchSeqFile(loopC, loopSeqC), gen.getCurrentSeq());
               }
            finally
               {
               loopSeqC++;
               }
            }
         }
      }

   private void printCounter(final CodeGenerator gen, final int countC) throws NumberFormatException, XPathExpressionException, IOException
      {
      StringBuilder builder = isWritingMethod ? methodCodeString : arduinoCodeString;

      final String[] counterOrder = gen.getCounterOrder(countC);

      builder.append(gen.getCounterHead(countC)).append("\n");
      int countSeq = 1;
      int countExp = 1;
      for (final String aCounterOrder : counterOrder)
         {
         if (aCounterOrder.contains("expression") && !cancel)
            {
            try
               {
               builder.append(gen.getCounterExpression(countC, countExp));
               }
            catch (final IOException e)
               {
               error(gen.getCountExpFile(countC, countExp), gen.getCurrentSeq());
               }
            finally
               {
               countExp++;
               }
            }
         else if (!cancel)
            {

            try
               {
               builder.append("//Start Seq: ").append(gen.getCountSeqFile(countC, countSeq)).append("\n");
               // final CodeGenerator temp = new CodeGenerator(inputFile.getParent() + File.separator + gen.getCountSeqFile(countC, countSeq));
               final CodeGenerator temp = new CodeGenerator(gen.getCountSeqFile(countC, countSeq));
               driver(temp);
               builder.append("//End Seq: ").append(gen.getCountSeqFile(countC, countSeq)).append("\n");
               }
            catch (final SAXException ignored)
               {
               }
            catch (final IOException e)
               {
               error(gen.getCountSeqFile(countC, countSeq), gen.getCurrentSeq());
               }
            finally
               {
               countSeq++;
               }
            }
         }
      builder.append("}\n");
      }
   private void printLink(final CodeGenerator gen, int linkC) throws NumberFormatException, XPathExpressionException, IOException
      {
      StringBuilder builder = isWritingMethod ? methodCodeString : arduinoCodeString;
      builder.append(gen.getLinkCode(linkC));
      }
   private void printFork(final CodeGenerator gen, int forkC) throws NumberFormatException, XPathExpressionException, IOException
      {
      int countExp, countSeq;
      StringBuilder builder = isWritingMethod ? methodCodeString : arduinoCodeString;
      builder.append("//Start Fork\n");
      isWritingMethod = true;
      builder = methodCodeString;
      //THREAD 1 PARSING -----------------------------------------------------------------------------------------------
      final String[] t1Order = gen.getThread1Order(forkC);
      builder.append(gen.getThread1Head(forkC)).append("\n");
      countSeq = 1;
      countExp = 1;
      for (final String aT1Order : t1Order)
         {
         if (aT1Order.contains("expression") && !cancel)
            {
            try
               {
               builder.append(gen.getThread1Expression(forkC, countExp));
               }
            catch (final IOException e)
               {
               error(gen.getThread1ExpFile(forkC, countExp), gen.getCurrentSeq());
               }
            finally
               {
               countExp++;
               }
            }
         else if (!cancel)
            {

            try
               {
               builder.append("//Start Seq: ").append(gen.getThread1SeqFile(forkC, countSeq)).append("\n");
               // final CodeGenerator temp = new CodeGenerator(inputFile.getParent() + File.separator + gen.getCountSeqFile(countC, countSeq));
               final CodeGenerator temp = new CodeGenerator(gen.getThread1SeqFile(forkC, countSeq));
               driver(temp);
               builder.append("//End Seq: ").append(gen.getThread1SeqFile(forkC, countSeq)).append("\n");
               }
            catch (final SAXException ignored)
               {
               }
            catch (final IOException e)
               {
               error(gen.getCountSeqFile(forkC, countSeq), gen.getCurrentSeq());
               }
            finally
               {
               countSeq++;
               }
            }
         }
      builder.append("}\n");
      //THREAD 1 END PARSING -------------------------------------------------------------------------------------------
      //THREAD 2 PARSING -----------------------------------------------------------------------------------------------
      final String[] t2Order = gen.getThread2Order(forkC);
      builder.append(gen.getThread2Head(forkC)).append("\n");
      countSeq = 1;
      countExp = 1;
      for (final String aT2Order : t2Order)
         {
         if (aT2Order.contains("expression") && !cancel)
            {
            try
               {
               builder.append(gen.getThread2Expression(forkC, countExp));
               }
            catch (final IOException e)
               {
               error(gen.getThread2ExpFile(forkC, countExp), gen.getCurrentSeq());
               }
            finally
               {
               countExp++;
               }
            }
         else if (!cancel)
            {

            try
               {
               builder.append("//Start Seq: ").append(gen.getThread2SeqFile(forkC, countSeq)).append("\n");
               // final CodeGenerator temp = new CodeGenerator(inputFile.getParent() + File.separator + gen.getCountSeqFile(countC, countSeq));
               final CodeGenerator temp = new CodeGenerator(gen.getThread2SeqFile(forkC, countSeq));
               driver(temp);
               builder.append("//End Seq: ").append(gen.getThread2SeqFile(forkC, countSeq)).append("\n");
               }
            catch (final SAXException ignored)
               {
               }
            catch (final IOException e)
               {
               error(gen.getCountSeqFile(forkC, countSeq), gen.getCurrentSeq());
               }
            finally
               {
               countSeq++;
               }
            }
         }
      builder.append("}\n");
      //THREAD 2 END PARSING -------------------------------------------------------------------------------------------
      isWritingMethod = false;
      builder = arduinoCodeString;
      builder.append("fork").append(forkC).append("Thread1;\n");
      builder.append("fork").append(forkC).append("Thread2;\n");
      builder.append("//End Fork\n");
      }

   private void init() throws IOException
      {
      normalHeader = "#include <Hummingbird.h>\n" +
                     "Hummingbird hummingbird; \n" +
                     "void setup()\n" +
                     "{  \n" +
                     "\thummingbird.init();\n" +
                     "}\n\nvoid loop(){\n";
      }

   public void error(final String file, final String seq)
      {
      final String message = "ERROR: " + file + " in the sequence " + seq + " was previously deleted. Continue converting to Arduino code?";
      if (JOptionPane.showConfirmDialog(null, message, "Error", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
         {
         cancel = true;
         }
      }

   public boolean isCancel()
      {
      return cancel;
      }
   }
