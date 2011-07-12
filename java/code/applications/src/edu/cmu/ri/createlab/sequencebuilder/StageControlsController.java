package edu.cmu.ri.createlab.sequencebuilder;

import edu.cmu.ri.createlab.xml.SaveXmlDocumentDialogRunnable;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
interface StageControlsController
   {
   void clearStage();

   void saveSequence(@Nullable final String filename, @Nullable final SaveXmlDocumentDialogRunnable.EventHandler eventHandler);

   void startOrStopSequenceExecution();
   }