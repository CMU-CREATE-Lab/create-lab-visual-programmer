package edu.cmu.ri.createlab.sequencebuilder.export;

import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by c3morales on 07-11-14.
 */


public class ArduinoCodeWriter {
    private File file;
    private File outputFile;
    private FileWriter writer;
    private boolean cancel = false;
    public ArduinoCodeWriter(final ArduinoFileManager manager) throws IOException {

        outputFile = new File( manager.getArduinoFile()+File.separator+manager.getArduinoFileName());
        writer = new FileWriter(outputFile);
        file = manager.getMainFile();

    }
    public void generateSequence(){

        try {
            final CodeGenerator gen = new CodeGenerator(file.toString());
            init();
            driver(gen);

            if (!cancel){
                writer.write("\n}\n");

                //prints methods of methods
                final ArrayList<Method_Manager> method2 = gen.getMethodList();
                for (final Method_Manager myVal : method2) {
                    writer.write("void "+myVal.getName().replace(" ", "").replace(".xml", "").replace("-", "")+"(){\n");
                    writer.write(myVal.getInstructions());
                }
            }
            gen.clearList();
            writer.flush();
            writer.close();

            if(cancel){
                outputFile.delete();
            }

        } catch (final SAXException e) {
            e.printStackTrace();
        } catch (final XPathExpressionException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    public void generateExpression() {

        try {
            final Expression_CodeGenerator ardCode = new Expression_CodeGenerator(file.toString());
            init();
            writer.write(ardCode.getInstruction());
            writer.close();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final XPathExpressionException e) {
            e.printStackTrace();
        }

    }



    //Method that manage most of the call to other
    private void driver(final CodeGenerator gen) throws XPathExpressionException, IOException{
        final String[] order = gen.getOrder();

        //expC = is use to have access to the expressions on the XML Doc
        //loopC = is use to have access to the sequences on the XML Doc
        //countC= is use to have access to the counts on the XML Doc
        int expC = 1; int loopC = 1; int seqC=1; int countC = 1;
        //count = to go through all the operations
        for (final String anOrder : order) {
            if (anOrder.contains("expression") && !cancel) {
                try {
                    writer.write("\t" + gen.getExpression(expC));
                } catch (final IOException e) {
                    error(gen.getExpFile(expC),gen.getCurrentSeq());
                }finally {
                    expC++;
                }

            }
            //if is a sequence, it calls himself with the name of the new document.
            else if (anOrder.contains("sequence") && !cancel) {
                writer.write("//Start Seq: " + gen.getSeqFile(seqC) + ": " + gen.getSeqComment(seqC) + "\n");
                try {
                    final CodeGenerator temp = new CodeGenerator(file.getParent() + File.separator + gen.getSeqFile(seqC));
                    driver(temp);
                    writer.write("//End Seq: " + gen.getSeqFile(seqC) + "\n");
                }
                catch (final SAXException ignored) {}
                catch (final IOException e) { error(gen.getSeqFile(seqC),gen.getCurrentSeq()); }
                finally{
                    seqC++;
                }
            } else if (anOrder.contains("loopable")) {
                try {
                    printLoop(gen, loopC);
                }finally {
                    loopC++;
                }

            } else if (anOrder.contains("counter")) {
                try{
                    printCounter(gen, countC);
                }finally{
                    countC++;
                }

            }
        }

    }

    private void printLoop(final CodeGenerator gen, final int loopC) throws NumberFormatException, XPathExpressionException, IOException{

        //order needs to be change when the user wants to repeat the else branch and not the if branch
        writer.write(gen.getIfBranchHead(loopC));
        if(gen.isRegularLoop()){
            ///////////////if branch /////////////////////
            printLoopIfBranch(gen, loopC);
            writer.write("\n}\n");

            ////////////// else- branch//////////////
            writer.write(gen.getElseBranchHead(loopC));
            printLoopElseBranch(gen, loopC);
            writer.write(gen.getElseBranchFoot(loopC));

        }else {
            ///////////////if branch /////////////////////
         //   writer.write(gen.getIfBranchHead(loopC));
            printLoopElseBranch(gen, loopC);
            writer.write("\n}\n\t");

            ////////////// else- branch//////////////
            writer.write(gen.getElseBranchHead(loopC));
            printLoopIfBranch(gen, loopC);
            writer.write(gen.getElseBranchFoot(loopC));
        }
    }

    private void printLoopIfBranch (final CodeGenerator gen, final int loopC) throws NumberFormatException, XPathExpressionException{
        final String[] ifOrder = gen.getIfBranchOrder(loopC);

        //ifLoopExpC = to have access to the expressions inside the if-branch on the XML Doc
        //ifLoopSeqC = to have access to the sequences inside the if-branch on the XMLDoc
        int ifloopExpC =1; int ifloopSeqC=1;
        for (final String anIfOrder : ifOrder) {
            if (anIfOrder.contains("expression")) {
                try{
                    writer.write(gen.getIfBranchExpression(loopC, ifloopExpC));
                }catch (final IOException e){
                    error(gen.getIfBranchExpFile(loopC, ifloopExpC),gen.getCurrentSeq());
                }finally{
                    ifloopExpC++;
                }
            } else {

                try {
                    writer.write("//Start Seq: " + gen.getIfBranchSeqFile(loopC, ifloopSeqC) + "\n");
                    final CodeGenerator temp = new CodeGenerator(file.getParent() + File.separator + gen.getIfBranchSeqFile(loopC, ifloopSeqC));
                    driver(temp);
                    writer.write("//End Seq: " + gen.getIfBranchSeqFile(loopC, ifloopSeqC) + "\n");
                } catch (final SAXException ignored) {
                }catch (final IOException e){
                    error(gen.getIfBranchSeqFile(loopC, ifloopSeqC), gen.getCurrentSeq());
                }finally{
                    ifloopSeqC++;
                }
            }
        }
    }

    private  void printLoopElseBranch(final CodeGenerator gen, final int loopC) throws NumberFormatException, XPathExpressionException{
        final String[] ElseOrder = gen.getElseBranchOrder(loopC);

        //LoopExpC = to have access to the expressions inside the if-branch on the XML Doc
        //LoopSeqC = to have access to the sequences inside the if-branch on the XMLDoc
        int loopExpC =1; int loopSeqC=1;
        for (final String aElseOrder : ElseOrder) {
            if (aElseOrder.contains("expression") && !cancel) {
                try{
                    writer.write("\t" + gen.getElseBranchExpression(loopC, loopExpC) + "\n");
                }catch (final IOException e) {
                    error(gen.getElseBranchExpFile(loopC, loopExpC), gen.getCurrentSeq());
                }finally{
                    loopExpC++;
                }
            } else if (!cancel){

                try {
                    writer.write("//Start Seq: " + gen.getElseBranchSeqFile(loopC, loopSeqC) + "\n");
                    final CodeGenerator temp = new CodeGenerator(file.getParent() + File.separator + gen.getElseBranchSeqFile(loopC, loopSeqC));
                    driver(temp);
                    writer.write("//End Seq: " + gen.getElseBranchSeqFile(loopC, loopSeqC) + "\n");
                } catch (final SAXException ignored) {
                } catch (final IOException e){
                    error(gen.getElseBranchSeqFile(loopC, loopSeqC), gen.getCurrentSeq());
                }finally{
                    loopSeqC++;
                }
            }
        }
    }

    private  void printCounter (final CodeGenerator gen, final int countC) throws NumberFormatException, XPathExpressionException, IOException {
        final String[] counterOrder = gen.getCounterOrder(countC);

        writer.write(gen.getCounterHead(countC)+"\n");
        int countSeq = 1;
        int countExp = 1;
        for (final String aCounterOrder : counterOrder) {
            if (aCounterOrder.contains("expression") && !cancel) {
                try{
                    writer.write(gen.getCounterExpression(countC, countExp));
                }catch(final IOException e){
                    error(gen.getCountExpFile(countC, countExp), gen.getCurrentSeq()
                    );
                }finally{
                    countExp++;
                }
            } else if (!cancel){

                try {
                    writer.write("//Start Seq: " + gen.getCountSeqFile(countC, countSeq) + "\n");
                    final CodeGenerator temp = new CodeGenerator(file.getParent() + File.separator + gen.getCountSeqFile(countC, countSeq));
                    driver(temp);
                    writer.write("//End Seq: " + gen.getCountSeqFile(countC, countSeq) + "\n");
                } catch (final SAXException ignored) {
                } catch (final IOException e){
                    error(gen.getCountSeqFile(countC, countSeq), gen.getCurrentSeq());
                }finally{
                    countSeq++;
                }
            }
        }
        writer.write("}\n");
    }

    private void init() throws IOException{
        writer.write( "#include <Hummingbird.h>\n"+
                "Hummingbird hummingbird; \n"+

                " void setup()\n"+
                "{  \n" +
                "\thummingbird.init();\n " +
                "}\n\n void loop(){\n");
    }
    public void error(final String file, final String seq) {
        final String message = "ERROR: "+file + " in the sequence "+seq+" was previously deleted. Continue converting to Arduino code?";
        if(JOptionPane.showConfirmDialog(null, message, "Error", JOptionPane.CANCEL_OPTION) == JOptionPane.CANCEL_OPTION){
            cancel = true;

        }
    }
    public boolean isCancel(){
        return cancel;
    }


}
