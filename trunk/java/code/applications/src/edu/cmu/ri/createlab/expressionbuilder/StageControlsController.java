package edu.cmu.ri.createlab.expressionbuilder;

import edu.cmu.ri.createlab.xml.SaveXmlDocumentDialogRunnable;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
interface StageControlsController
   {
   void clearControlPanels();

   void refreshControlPanels();

   void saveExpression(@Nullable final String filename, @Nullable final SaveXmlDocumentDialogRunnable.EventHandler eventHandler);
   }