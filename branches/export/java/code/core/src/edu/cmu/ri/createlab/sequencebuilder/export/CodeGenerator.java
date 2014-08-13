package edu.cmu.ri.createlab.sequencebuilder.export;

/**
 * Created by c3morales on 07-09-14.
 */

import edu.cmu.ri.createlab.visualprogrammer.PathManager;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class CodeGenerator {

    private Expression_CodeGenerator expG;
    private XML_SequenceExtractor seqE;
    private Method_Manager method;
    private String expPath;
    private boolean loopFlow;
    private String currSeq;

    //It needs to be static
    private static ArrayList<Method_Manager> methodList= new ArrayList<Method_Manager>();


    //constructor
    public CodeGenerator(final String path) throws SAXException, IOException {
        seqE = new XML_SequenceExtractor (path);
        currSeq = (new File (path).getName());
        final File temp =  PathManager.getInstance().getExpressionsDirectory();
        expPath = (temp != null ? temp.getAbsolutePath() : null) + File.separator;

    }

    //returns a list of the order of the sequence
    public String[] getOrder() throws XPathExpressionException{
        final NodeList tep = seqE.getSequence();
        final String[] order = new String [tep.getLength()];
        for (int i = 0; i < order.length; i++) {
            order[i] = tep.item(i).toString();
        }
        return order;
    }
    public  String getCurrentSeq(){
        return currSeq;
    }

    //////////////////////////////////////////////////////////////////////EPRESSIONS//////////////////////////////////////////////////////////////


    // Generates the the expression and added to an ArrayList and returns the statement
    //that calls the method
    public String getExpression(final int pos) throws XPathExpressionException, IOException {
        String result = "";
        expG = new Expression_CodeGenerator(expPath+seqE.getExpressionFile(pos));
        //Verify if is already on the list
        if(!exist(seqE.getExpressionFile(pos))){
            method = new Method_Manager(seqE.getExpressionFile(pos), expG.getInstruction());
            methodList.add(method);
        }

        //check if there is a comment
        if(!"".equals(seqE.getExpressionComment(pos))){
            result+="//"+seqE.getExpressionComment(pos)+"\n\t";
        }
        result+=expG.getMethod();

        //check if the delay is zero
        if(seqE.getExpressionDelay(pos) !=0){
            result+= "\n\tdelay("+seqE.getExpressionDelay(pos)+");\n";
        }



        return result;


    }
    public String getExpFile(final int pos) throws XPathExpressionException{
        return seqE.getExpressionFile(pos);
    }


    ///////////////////////////////////////////////////////////////////////SEQUENCES/////////////////////////////////////////////////////////////

    //returns the name of the sequence
    public String getSeqFile(final int pos) throws XPathExpressionException{
        return seqE.getSeqFile(pos);
    }

    //returns the comment
    public String getSeqComment(final int pos) throws XPathExpressionException{
        return seqE.getSeqCommet(pos);
    }

    /////////////////////////////////////////////////////////////////////LOOP IF BRANCH///////////////////////////////////////////////////////////////

    //returns a list of the order of operations inside the if-branch
    public String[] getIfBranchOrder (final int pos) throws NumberFormatException, XPathExpressionException{
        final NodeList tep = seqE.getIfBrenchOrder(pos);
        final String[] order = new String [tep.getLength()];
        for (int i = 0; i < order.length; i++) {
            order[i] = tep.item(i).toString();
        }
        return order;
    }

    //returns the header of the if branch (if is an 'if' or a 'while', depending if the if-branch was set to repeat)
    public  String getIfBranchHead(final int pos) throws NumberFormatException, XPathExpressionException{
        String temp = "";

        //add comment
        if(!"".equalsIgnoreCase(seqE.getLoopComment(pos)))
            temp+="//"+seqE.getLoopComment(pos)+"\n";


        //if the if-branch does not repeats and the else-branch does repeat
        if ((!seqE.ifBranchWhile(pos)) && seqE.elseBranchWhile(pos)){
            loopFlow = false;
            temp+= "while (hummingbird.readSensorValue("+sensorValueConverter(pos, false)+") {\n";
        }
        else {
            loopFlow = true;
            //if branch does not repeat and else branch also does not repeat
            if(!seqE.ifBranchWhile(pos) && !seqE.elseBranchWhile(pos))
                temp +="if(hummingbird.readSensorValue("+sensorValueConverter(pos, true)+") {\n\t";
                //if branch repeats and else-branch also repeats
            else if (seqE.ifBranchWhile(pos) && seqE.elseBranchWhile(pos))
                temp+= "while(true){\n if(hummingbird.readSensorValue("+sensorValueConverter(pos, true)+") {\n\t";
            else // if-branch does repeat and else does not repeat
                temp+= "while(hummingbird.readSensorValue("+sensorValueConverter(pos, true)+") {\n\t";

        }
        return temp;
    }
    //When the user wants to repeat the else block but no the if, the order needs to be change.
    public boolean isRegularLoop(){
        return loopFlow;
    }

    public String sensorValueConverter(final int pos, final boolean sign) throws XPathExpressionException{
        String temp = "";
        String s = (sign) ? "<" : ">";
        final String sensor = seqE.getSensor(pos);

        if(sensor.contentEquals("Distance Sensor")){
            s = (sign) ? ">" : "<";
            //converted to a specific range
            final int value = (700*(seqE.getSensorValuePercent(pos))/100);
            //get opposite equivalent
            temp+= +seqE.getSensorPort(pos)+")"+s+""+(350+(350-value));
        }
        else if (sensor.contentEquals("Potentiometer")) {
            //(511+(511-i) -> get opposite equivalent
            s = (sign) ? ">" : "<";
            temp+= +seqE.getSensorPort(pos)+")"+s+""+(511+(511-seqE.getSensorValue(pos)));
        }
        else if (sensor.contentEquals("Light Sensor") ||sensor.contentEquals("Raw Value") ){
            temp+= +seqE.getSensorPort(pos)+")"+s+""+seqE.getSensorValue(pos);
        }
        else if (sensor.contentEquals("Sound Sensor")){
            final int value = (600*(seqE.getSensorValuePercent(pos))/100);
            temp+= +seqE.getSensorPort(pos)+")"+s+""+value;
        }
        else if (sensor.contentEquals("Temperature Sensor")){
            //Temperature wasn't behaving how it should, It was about 20% off ->(i-(0.20*i) adjustment
            temp+= seqE.getSensorPort(pos)+")"+s+""+(int)(seqE.getSensorValue(pos)-(0.20*seqE.getSensorValue(pos)));
        }
        else
            temp="ERROR";

        return temp;
    }

    //----------------------------------------------------------------------Expression---------------------------------------------------------------//

    //returns the name of a sequence inside the if-branch
    public String getIfBranchExpFile(final int pos,final  int pos2) throws XPathExpressionException{
        return seqE.getIfBranchFile(pos, pos2);
    }

    // Generates the expression inside the if-branch and added to an ArrayList and returns the statement
    //that calls the method
    public String getIfBranchExpression(final int pos,final int pos2) throws XPathExpressionException, IOException {

        expG = new Expression_CodeGenerator(expPath+seqE.getIfBranchFile(pos, pos2));

        //Generates the method instructions and added to a list
        if(!exist(seqE.getIfBranchFile(pos, pos2))){
            method = new Method_Manager(seqE.getIfBranchFile(pos, pos2), expG.getInstruction());
            methodList.add(method);
        }
        //check if there is a comment
        String result = "";
        if(!"".equalsIgnoreCase(seqE.getIfBranchComment(pos, pos2))){
            result+="//"+seqE.getIfBranchComment(pos, pos2)+"\n\t";
        }
        result+=expG.getMethod();

        //check if the delay is cero
        if(seqE.getIfBranchDelay(pos,pos2) !=0){
            result+= "\n\tdelay("+seqE.getIfBranchDelay(pos,pos2)+");\n\t";
        }
        return result;
    }

    //----------------------------------------------------------------------SEQUENCES---------------------------------------------------------------//

    //returns the name of a sequence inside the if-branch
    public String getIfBranchSeqFile(final int pos,final int pos2) throws XPathExpressionException {
        return seqE.getIfBranchSeqFile(pos, pos2);
    }

    ////////////////////////////////////////////////////////////////////LOOP ELSE-BRANCH////////////////////////////////////////////////////////////////

    //returns the order of the operations inside the else-branch
    public String[] getElseBranchOrder(final int pos) throws NumberFormatException, XPathExpressionException {
        final NodeList tep = seqE.getElseBrenchOrder(pos);
        final String[] order = new String [tep.getLength()];
        for (int i = 0; i < order.length; i++) {
            order[i] = tep.item(i).toString();
        }
        return order;
    }

    //returns the header of the else-branch (if is an 'else' or a 'while', depending if the else-branch was set to repeat)
    public String getElseBranchHead(final int pos) throws XPathExpressionException{
        String temp = "";
        //if the if-branch does not repeats and the else-branch does repeat
        if (!(!seqE.ifBranchWhile(pos) && seqE.elseBranchWhile(pos))){
            //if branch does not repeat and else branch also does not repeat
            if (!seqE.ifBranchWhile(pos) && !seqE.elseBranchWhile(pos))
                temp+= "else{\n";
                //if branch repeats and else-branch also repeats
            else if(seqE.elseBranchWhile(pos) && seqE.ifBranchWhile(pos))
                temp+= " else {\n";
        }

        return temp;
    }

    //returns the ending of the else-branch
    public String getElseBranchFoot(final int pos) throws XPathExpressionException{
        String temp = "";
        //set the ending of the statements
        if (!seqE.ifBranchWhile(pos) && seqE.elseBranchWhile(pos)) temp = "";
        else {
            if (seqE.elseBranchWhile(pos) && seqE.ifBranchWhile(pos))
                temp += "\t}\n}";
            else if (!(seqE.ifBranchWhile(pos) && !seqE.elseBranchWhile(pos)))
                temp += "}";
        }

        return temp+"\n";
    }

    //---------------------------------------------------------------------EXPRESSIONS---------------------------------------------------------------//

    // Generates the expression inside the else-branch and added to an ArrayList and returns the statement
    //that calls the method
    public String getElseBranchExpression(final int pos, final int pos2) throws NumberFormatException, XPathExpressionException, IOException {
        expG = new Expression_CodeGenerator(expPath+seqE.getElseBranchFile(pos, pos2));
        //verify if the expression exist on the list
        if(!exist(seqE.getElseBranchFile(pos, pos2))){
            method = new Method_Manager(seqE.getElseBranchFile(pos, pos2), expG.getInstruction());
            methodList.add(method);
        }
        //check if there is a comment
        String result = "";
        if(!"".equalsIgnoreCase(seqE.getElseBranchComment(pos, pos2))){
            result+="//"+seqE.getElseBranchComment(pos, pos2)+"\n\t";
        }
        result+=expG.getMethod();

        //check if the delay is zero
        if(seqE.getElseBranchDelay(pos,pos2) !=0){
            result+= "\n\tdelay("+seqE.getElseBranchDelay(pos,pos2)+");\n";
        }
        return result ;
    }
    //returns the name of a sequence inside the if-branch
    public String getElseBranchExpFile(final int pos,final  int pos2) throws XPathExpressionException{
        return seqE.getElseBranchFile(pos, pos2);
    }

    //---------------------------------------------------------------------SEQUENCES----------------------------------------------------------------//

    //returns the name of a sequence inside the else-branch
    public String getElseBranchSeqFile(final int pos,final int pos2) throws XPathExpressionException {
        return seqE.getElseBranchSeqFile(pos, pos2);
    }

    //////////////////////////////////////////////////////////////////////////COUNTER////////////////////////////////////////////////////////////////////

    //returns a list of the order of operations inside the counter
    public String[] getCounterOrder(final int pos) throws NumberFormatException, XPathExpressionException {
        final NodeList tep = seqE.getCounterOrder(pos);
        final String[] order = new String [tep.getLength()];
        for (int i = 0; i < tep.getLength(); i++) {
            order[i] = tep.item(i).toString();
        }
        return order;
    }

    //returns the start of the counter ("for" statement)
    public String getCounterHead(final int pos) throws NumberFormatException, XPathExpressionException{
        String temp = "";
        if(!"".equalsIgnoreCase(seqE.getCounterComment(pos)))
            temp+="//"+seqE.getCounterComment(pos)+"\n";
        return  temp+"for(int counter = 0; counter <"+seqE.getCounterIter(pos)+"; counter++){";
    }

    //-----------------------------------------------------------------------EXPRESSIONS-------------------------------------------------------------//

    //returns the name of an expression inside the counter
    public String getCountExpFile(final int pos,final int pos2) throws XPathExpressionException {
        return seqE.getCounterExpFile(pos, pos2);
    }

    // Generates the expression inside the counter, and added to an ArrayList. Then returns the statement
    //that calls the method
    public String getCounterExpression(final int pos,final int pos2) throws XPathExpressionException, IOException {

        expG = new Expression_CodeGenerator(expPath+seqE.getCounterExpFile(pos, pos2));
        //Verify that the expression is not on the list
        if(!exist(seqE.getCounterExpFile(pos, pos2))){
            method = new Method_Manager(seqE.getCounterExpFile(pos, pos2), expG.getInstruction());
            methodList.add(method);
        }
        String result = "";
        result+="\t"+expG.getMethod();

        //check if the delay is cero
        if(seqE.getCounterExpDelay(pos, pos2) !=0){
            result+= "\n\tdelay("+seqE.getCounterExpDelay(pos, pos2)+");\n";
        }
        return result;

    }

    //----------------------------------------------------------------------SQUENCES-----------------------------------------------------------------//

    //returns the name of a sequence inside the counter
    public String getCountSeqFile(final int pos,final int pos2) throws XPathExpressionException {
        return seqE.getCounterSeqFile(pos, pos2);
    }

    /////////////////////////////////////////////////////////////////EXPRESSIONS METHOD LIST//////////////////////////////////////////////////////////

    //Returns true if the method to be added exist in the list
    public boolean exist (final String name){
        if(methodList.size() ==0)
            return false;
        else{
            for (final Method_Manager elem: methodList) {
                if(name.equalsIgnoreCase(elem.getName()))
                    return true;
            }
            return false;
        }
    }

    //returns a list with the name of the methods, instructions and delays.
    public ArrayList<Method_Manager> getMethodList(){
        final ArrayList<Method_Manager> copy = new ArrayList<Method_Manager>();
        for(final Method_Manager temp : methodList)
            copy.add(temp);
        return copy;
    }

    public void clearList(){
        methodList= new ArrayList<Method_Manager>();
    }





}

