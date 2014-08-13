package edu.cmu.ri.createlab.sequencebuilder.export;

/**
 * Created by c3morales on 07-09-14.
 * XML_SequenceExtractor
 * class that extract information form the XML document
 */


import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.IOException;

public class XML_SequenceExtractor {
    private Document xmlDocument;
    private XPath xPath;
    private String expression;
    private NodeList nodeList;


    //constructor
    public XML_SequenceExtractor(final String path) throws SAXException, IOException {

        try {
            //filter the XML (remove the DOCTYPE)
            final XML_Filter filt = new XML_Filter();

            final  FileInputStream file = new FileInputStream(filt.filterXML(path));
            final  DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            final  DocumentBuilder builder = builderFactory.newDocumentBuilder();
            xmlDocument = builder.parse(file);
            xPath =  XPathFactory.newInstance().newXPath();

        }/* catch (final FileNotFoundException e) {
            System.out.println("HERE4");
        }
        catch (final IOException e) {
            e.printStackTrace();
        }*/
        catch (final ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

    //returns a list of the sequence/order of the XML document.
    public NodeList getSequence() throws XPathExpressionException{
        expression = "/sequence/program-element-container/*";
        try {
            nodeList= (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (final XPathExpressionException e) {
            e.printStackTrace();
        }
        return nodeList;
    }

    //Returns how many expressions are in the documents under the "program-element-container"
    //To have access to each expression is necessary to know the position of each expression
    public int getExpressionCount(){
        expression = "/sequence/program-element-container/expression";
        try {
            nodeList= (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (final XPathExpressionException e) {
            e.printStackTrace();
        }
        return nodeList.getLength();
    }

    //returns the name of the file form a specific expression
    public String getExpressionFile (final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/expression["+pos+"]/@file";
        return xPath.compile(expression).evaluate(xmlDocument);
    }

    //returns the value of delay of a specific expression
    public int getExpressionDelay(final int pos) throws NumberFormatException, XPathExpressionException{
        expression = "/sequence/program-element-container/expression["+pos+"]/@delay-in-millis";
        return Integer.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    //returns the value of delay of a specific expression in the if-branch
    public String getIfExpressionDelay(final int pos) throws NumberFormatException, XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional/if-branch/expression["+pos+"]/@delay-in-millis";
        return (xPath.compile(expression).evaluate(xmlDocument));
    }

    //returns true if the comment of the expression specify was as visible
    //(to be false does not mean that there were no comments, just that it was not set as visible)
    public boolean isExpressionComment(final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/expression["+pos+"]/comment/@is-visible";
        return Boolean.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }


    //returns the comment of a specific expression
    public String getExpressionComment(final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/expression["+pos+"]/comment";
        return (xPath.compile(expression).evaluate(xmlDocument).replace("\n", ""));
    }

    //Returns how many conditions are in the documents under the "program-element-container"
    //To have access to each condition is necessary to know the position of each condition
    public int getLoopConditionalCount(){
        expression = "/sequence/program-element-container/loopable-conditional";
        try {
            nodeList= (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (final XPathExpressionException e) {
            e.printStackTrace();
        }
        return nodeList.getLength();
    }

    //returns the name of the sensor that is been use in the condition
    public String getSensor(final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/sensor-conditional[1]/@sensor-name";
        return xPath.compile(expression).evaluate(xmlDocument);
    }

    //returns the threshold value use for the condition
    //the value is converted
    public int getSensorValue(final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/sensor-conditional/@threshold-percentage";
        return 1023*(Integer.valueOf(xPath.compile(expression).evaluate(xmlDocument)))/100;
        //return Integer.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    //returns the threshold value use for the condition
    //the value not converted
    public int getSensorValuePercent(final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/sensor-conditional/@threshold-percentage";
        //return 1023*(Integer.valueOf(xPath.compile(expression).evaluate(xmlDocument)))/100;
        return Integer.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    //returns the port of the sensor use in the condition
    public int getSensorPort(final int pos) throws NumberFormatException, XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/sensor-conditional/service/operation/device/@id";
        //the ports on the XML doc start at 0, this is not valid using the Hummingbird
        return Integer.valueOf(xPath.compile(expression).evaluate(xmlDocument))+1;

    }

    //Returns the comment of an expression under the if-branch
    public String getLoopComment (final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/comment";
        return (xPath.compile(expression).evaluate(xmlDocument).replace("\n", ""));
    }

    ////////////////////////////////////////////////////////////IF BRANCH STATEMENTS////////////////////////////////////////////////////////////////////


    // returns how many operations are under the if-branch (expressions + sequences)
    public int getIfBrenchOperatiosCount(final int pos) throws NumberFormatException, XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/if-branch/program-element-container/*";
        try {
            nodeList= (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (final XPathExpressionException e) {
            e.printStackTrace();
        }
        return nodeList.getLength();
    }

    //returns a list of the order of the operations
    public NodeList getIfBrenchOrder(final int pos) throws NumberFormatException, XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/if-branch/program-element-container/*";
        try {
            nodeList= (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (final XPathExpressionException e) {
            e.printStackTrace();
        }
        return nodeList;
    }

    //returns the name of an specify expression under the if-branch
    public String getIfBranchFile (final int pos,final  int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/if-branch/program-element-container/expression["+pos2+"]/@file";
        return xPath.compile(expression).evaluate(xmlDocument);
    }

    //true if the if branch is set to repeat
    public boolean ifBranchWhile(final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/@will-reevaluate-conditional-after-if-branch-completes";
        return Boolean.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    //returns the value of the delay of an expression under the if-branch
    public int getIfBranchDelay (final int pos,final  int pos2) throws NumberFormatException, XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/if-branch/program-element-container/expression["+pos2+"]/@delay-in-millis";
        return Integer.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    //Returns the comment of an expression under the if-branch
    public String getIfBranchComment (final int pos,final  int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/if-branch/program-element-container/expression["+pos2+"]/comment";
        return (xPath.compile(expression).evaluate(xmlDocument).replace("\n", ""));
    }

    //returns true if the comment under the if-branch is set as visible
    //*NOTE: if returns false, does not mean that there is no comments. Just that is set as not visible
    public boolean isIfBranchComment (final int pos,final  int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/if-branch/program-element-container/expression["+pos2+"]/comment/@is-visible";
        return Boolean.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    //returns the name of an specify sequence under the if-branch
    public String getIfBranchSeqFile(final int pos,final  int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/if-branch/program-element-container/saved-sequence["+pos2+"]/@file";
        return xPath.compile(expression).evaluate(xmlDocument);
    }

    //Returns the comment of a sequence under the if-branch
    public String getIfBranchSeqComment(final int pos,final  int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/if-branch/program-element-container/saved-sequence["+pos2+"]/comment";
        return (xPath.compile(expression).evaluate(xmlDocument).replace("\n", ""));
    }

    //returns true if the comment under the if-branch is set as visible
    //*NOTE: if returns false, does not mean that there is no comments. Just that is set as not visible
    public Boolean isIfBranchSeqComment(final int pos, final int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/if-branch/program-element-container/saved-sequence["+pos2+"]/comment/@is-visible";
        return Boolean.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }


    ///////////////////////////////////////////////////////ELSE-BRANCH STATEMENTS //////////////////////////////////////////////////////////////////////


    // returns how many operations are under the else-branch
    public int getElseBrenchOperatiosCount(final int pos) throws NumberFormatException, XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/else-branch/program-element-container/*";
        try {
            nodeList= (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (final XPathExpressionException e) {
            e.printStackTrace();
        }
        return nodeList.getLength();

    }

    //returns a list of the orter of the operations under the else branch
    public NodeList getElseBrenchOrder(final int pos) throws NumberFormatException, XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/else-branch/program-element-container/*";
        try {
            nodeList= (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (final XPathExpressionException e) {
            e.printStackTrace();
        }
        return nodeList;
    }

    //returns the name of an specify expression under the else-branch
    public String getElseBranchFile (final int pos, final int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/else-branch/program-element-container/expression["+pos2+"]/@file";
        return xPath.compile(expression).evaluate(xmlDocument);
    }

    //true if the else-branch is set to repeat
    public boolean elseBranchWhile(final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/@will-reevaluate-conditional-after-else-branch-completes";
        return Boolean.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    //returns the value of the delay of an expression under the else-branch
    public int getElseBranchDelay (final int pos,final  int pos2) throws NumberFormatException, XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/else-branch/program-element-container/expression["+pos2+"]/@delay-in-millis";
        return Integer.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    //Returns the comment of an expression under the else-branch
    public boolean isElseBranchComment (final int pos, final int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/else-branch/program-element-container/expression["+pos2+"]/comment/@is-visible";
        return Boolean.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    //returns true if the comment under the else-branch is set as visible
    //*NOTE: if returns false, does not mean that there is no comments. Just that is set as not visible
    public String getElseBranchComment (final int pos, final int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/else-branch/program-element-container/expression["+pos2+"]/comment";
        return (xPath.compile(expression).evaluate(xmlDocument).replace("\n", ""));
    }

    //returns the name of an specific sequence under the else-branch
    public String getElseBranchSeqFile(final int pos, final int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/else-branch/program-element-container/saved-sequence["+pos2+"]/@file";
        return xPath.compile(expression).evaluate(xmlDocument);
    }

    //Returns the comment of a sequence under the else-branch
    public String getElseBranchSeqComment(final int pos,final  int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/else-branch/program-element-container/saved-sequence["+pos2+"]/comment";
        return (xPath.compile(expression).evaluate(xmlDocument).replace("\n", ""));
    }

    //returns true if the comment under the else-branch is set as visible
    //*NOTE: if returns false, does not mean that there is no comments. Just that is set as not visible
    public Boolean isElseBranchSeqComment(final int pos,final  int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/loopable-conditional["+pos+"]/else-branch/program-element-container/saved-sequence["+pos2+"]/comment/@is-visible";
        return Boolean.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    ///////////////////////////////////////////////////////SAVED-SEQUECES/////////////////////////////////////////////////////////////////////

    //returns the name of the sequences file
    public String getSeqFile(final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/saved-sequence["+pos+"]/@file";
        return xPath.compile(expression).evaluate(xmlDocument);
    }

    //returns true if the sequence comment is set as visible
    public Boolean isSeqComment(final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/saved-sequence["+pos+"]/comment/@is-visible";
        return Boolean.valueOf(xPath.compile(expression).evaluate(xmlDocument));

    }

    //returns the comment of a sequence
    public String getSeqCommet(final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/saved-sequence["+pos+"]/comment";
        return xPath.compile(expression).evaluate(xmlDocument).replace("\n", "");

    }

    /////////////////////////////////////////////////////////// COUNTER /////////////////////////////////////////////////////////////

    //returns the amount of iterations of the counter
    public int getCounterIter(final int pos) throws NumberFormatException, XPathExpressionException{
        expression = "/sequence/program-element-container/counter-loop["+pos+"]/@iterations";
        return Integer.valueOf(xPath.compile(expression).evaluate(xmlDocument));

    }

    //returns the comments of the counter
    public String getCounterComment(final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/counter-loop["+pos+"]/comment";
        return xPath.compile(expression).evaluate(xmlDocument).replace("\n", "");

    }

    //returns true if the comment is set as visible
    public Boolean isCounterComment(final int pos) throws XPathExpressionException{
        expression = "/sequence/program-element-container/counter-loop["+pos+"]/comment/@is-visible";
        return Boolean.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    //returns the amount of operations inside the counter (expressions+sequences)
    public NodeList getCounterOperationsCount(final int pos){
        expression = "/sequence/program-element-container/counter-loop["+pos+"]/program-element-container/*";
        try {
            nodeList= (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (final XPathExpressionException e) {
            e.printStackTrace();
        }
        return nodeList;
    }

    //returns a list of of the operations inside the counter
    public NodeList getCounterOrder(final int pos){
        expression = "/sequence/program-element-container/counter-loop["+pos+"]/program-element-container/*";
        try {
            nodeList= (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        } catch (final XPathExpressionException e) {
            e.printStackTrace();
        }

        return nodeList;
    }

    //////////////////////////////////////////////////COUNTER - SEQUENCES //////////////////////////////////////////////////////////////

    //returns the name of a specific sequence inside the counter element
    //pos2 = specific sequence
    public String getCounterSeqFile(final int pos,final  int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/counter-loop["+pos+"]/program-element-container/saved-sequence["+pos2+"]/@file";
        return xPath.compile(expression).evaluate(xmlDocument);
    }

    //returns the comment of a sequence inside the counter element
    public String getCounterSeqComment(final int pos,final  int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/counter-loop["+pos+"]/program-element-container/saved-sequence["+pos2+"]/comment";
        return xPath.compile(expression).evaluate(xmlDocument).replace("\n", "");
    }

    //returns true if the comment of the sequence inside counter is set as visible
    public Boolean isCounterSeqComment(final int pos,final  int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/counter-loop["+pos+"]/program-element-container/saved-sequence["+pos2+"]/comment/@is-visible";
        return Boolean.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    ///////////////////////////////////////////////COUNTER - EXPRESSION /////////////////////////////////////////////////////////////////

    //returns the name of a specific expression inside the expression
    //pos2 = specific expression
    public String getCounterExpFile(final int pos,final  int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/counter-loop["+pos+"]/program-element-container/expression["+pos2+"]/@file";
        return xPath.compile(expression).evaluate(xmlDocument);
    }

    //returns the comment of an expression inside the counter
    public String getCounterExpComment(final int pos,final  int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/counter-loop["+pos+"]/program-element-container/expression["+pos2+"]/comment";
        return xPath.compile(expression).evaluate(xmlDocument).replace("\n", "");

    }
    //returns true if the comment  of the expression inside the counter is set as visible
    public Boolean isCounterExpComment(final int pos, final int pos2) throws XPathExpressionException{
        expression = "/sequence/program-element-container/counter-loop["+pos+"]/program-element-container/expression["+pos2+"]/comment/is-visible";
        return Boolean.valueOf(xPath.compile(expression).evaluate(xmlDocument));
    }

    //returns the delay of an expression inside the counter element
    public int getCounterExpDelay(final int pos, final int pos2) throws NumberFormatException, XPathExpressionException{
        expression = "/sequence/program-element-container/counter-loop["+pos+"]/program-element-container/expression["+pos2+"]/@ delay-in-millis";
        return Integer.valueOf(xPath.compile(expression).evaluate(xmlDocument));

    }







}

