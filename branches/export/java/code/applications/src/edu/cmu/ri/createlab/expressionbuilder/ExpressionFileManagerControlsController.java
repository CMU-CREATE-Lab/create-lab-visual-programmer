package edu.cmu.ri.createlab.expressionbuilder;

import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.terk.expression.manager.ExpressionFile;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
interface ExpressionFileManagerControlsController
   {
   void openExpression(final XmlExpression expression);

   void deleteExpression(final ExpressionFile selectedIndex);
   }
