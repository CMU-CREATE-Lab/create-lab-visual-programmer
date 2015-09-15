package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.cmu.ri.createlab.sequencebuilder.ContainerModel;
import edu.cmu.ri.createlab.sequencebuilder.SequenceExecutor;
import edu.cmu.ri.createlab.visualprogrammer.PathManager;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>SavedSequenceModel</code> is the {@link ProgramElementModel} for a saved sequence.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SavedSequenceModel extends BaseProgramElementModel<SavedSequenceModel>
   {
   public interface ExecutionEventListener
      {
      void handleExecutionStart();

      void handleExecutionEnd();

      void handleExecutionVisual();
      }

   private static final Logger LOG = Logger.getLogger(SavedSequenceModel.class);

   public static final String XML_ELEMENT_NAME = "saved-sequence";
   private static final String XML_ATTRIBUTE_FILE = "file";

   @Nullable
   public static SavedSequenceModel createFromXmlElement(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                                         @Nullable final Element element)
      {
      if (element != null)
         {
         LOG.debug("SavedSequenceModel.createFromXmlElement(): " + element);

         final String filename = element.getAttributeValue(XML_ATTRIBUTE_FILE);
         //--->
         //final File file = new File(PathManager.getInstance().getSequencesDirectory(), filename);
         //final File file = PathManager.getInstance().getSequencesZipSave().getAddedFile(filename);

         if (PathManager.getInstance().getSequencesZipSave().exist(filename))
            {
            return new SavedSequenceModel(visualProgrammerDevice,
                                          filename,
                                          getCommentFromParentXmlElement(element),
                                          getIsCommentVisibleFromParentXmlElement(element));
            }
         else
            {
            if (LOG.isEnabledFor(Level.WARN))
               {
               LOG.warn("SavedSequenceModel.createFromXmlElement(): Sequence file [" + filename + "] does not exist.  Returning null.");
               }
            }
         }
      return null;
      }

   private final String savedSequenceFileName;
   private final Set<ExecutionEventListener> executionEventListeners = new HashSet<ExecutionEventListener>();

   /** Creates a <code>SavedSequenceModel</code> with an empty hidden comment. */
   public SavedSequenceModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                             @NotNull final String savedSequenceFile)
      {
      this(visualProgrammerDevice, savedSequenceFile, null, false);
      }

   /** Creates a <code>SavedSequenceModel</code> with the given <code>comment</code>. */
   public SavedSequenceModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                             @NotNull final String savedSequenceFile,
                             @Nullable final String comment,
                             final boolean isCommentVisible)
      {
      super(visualProgrammerDevice, comment, isCommentVisible);
      this.savedSequenceFileName = savedSequenceFile;
      }

   /** Copy constructor */
   private SavedSequenceModel(final SavedSequenceModel originalSavedSequenceModel)
      {
      this(originalSavedSequenceModel.getVisualProgrammerDevice(),
           originalSavedSequenceModel.getSavedSequenceFileName(),
           originalSavedSequenceModel.getComment(),
           originalSavedSequenceModel.isCommentVisible());
      }

   public void addExecutionEventListener(@Nullable final ExecutionEventListener listener)
      {
      if (listener != null)
         {
         executionEventListeners.add(listener);
         }
      }

   public void removeExecutionEventListener(@Nullable final ExecutionEventListener listener)
      {
      if (listener != null)
         {
         executionEventListeners.remove(listener);
         }
      }

   public String getElementType()
      {
      return XML_ELEMENT_NAME;
      }

   public Map<String, Integer> getElementCounts()
      {
      //Creates the counts shown in the SavedSequence Standard Views
      final Map<String, Integer> countMap = new HashMap<String, Integer>();
      try
         {
         final ContainerModel containerModel = new ContainerModel();
         containerModel.load(getVisualProgrammerDevice(), XmlHelper.createDocument(PathManager.getInstance().getSequencesZipSave().getFile_InputStream(savedSequenceFileName)));

         // iterate over the models and execute them
         final List<ProgramElementModel> programElementModels = containerModel.getAsList();
         for (final ProgramElementModel model : programElementModels)
            {
            //LOG.debug("Sequence Model: Role Call:   " + model.getElementType());
            if (countMap.containsKey(model.getElementType()))
               {
               countMap.put(model.getElementType(), countMap.get(model.getElementType()) + 1);
               }
            else
               {
               countMap.put(model.getElementType(), 1);
               }
            }
         }
      catch (final Exception e)
         {
         LOG.error("IOException while trying to read [" + savedSequenceFileName + "] as XML.  Skipping this element.", e);
         }
      return countMap;
      }

   /** Returns the saved sequence's file name, without the .xml extension. */
   @Override
   @NotNull
   public String getName()
      {
      // get the filename, but strip off any .xml extension
      String fileName = savedSequenceFileName;
      if (fileName.toLowerCase().lastIndexOf(".xml") != -1)
         {
         fileName = fileName.substring(0, fileName.lastIndexOf('.'));
         }

      return fileName;
      }

   @Override
   public boolean isContainer()
      {
      return false;
      }

   @Override
   @NotNull
   public SavedSequenceModel createCopy()
      {
      return new SavedSequenceModel(this);
      }

   @NotNull
   @Override
   public Element toElement()
      {
      final Element element = new Element(XML_ELEMENT_NAME);
      element.setAttribute("file", savedSequenceFileName);
      element.addContent(getCommentAsElement());

      return element;
      }

   @Override
   public void execute()
      {
      LOG.debug("SavedSequenceModel.execute(): executing [" + this + "]");
      if (SequenceExecutor.getInstance().isRunning())
         {
         // notify listeners that we're about to begin
         for (final ExecutionEventListener listener : executionEventListeners)
            {
            listener.handleExecutionStart();
            }

         try
            {
            final ContainerModel containerModel = new ContainerModel();
            containerModel.load(getVisualProgrammerDevice(), XmlHelper.createDocument(PathManager.getInstance().getSequencesZipSave().getFile_InputStream(savedSequenceFileName)));

            for (final ExecutionEventListener listener : executionEventListeners)
               {
               listener.handleExecutionVisual();
               }

            // iterate over the models and execute them
            final List<ProgramElementModel> programElementModels = containerModel.getAsList();
            for (final ProgramElementModel model : programElementModels)
               {
               model.execute();
               }
            }
         catch (final Exception e)
            {
            LOG.error("IOException while trying to read [" + savedSequenceFileName + "] as XML.  Skipping this element.", e);
            }

         // notify listeners that we're done
         for (final ExecutionEventListener listener : executionEventListeners)
            {
            listener.handleExecutionEnd();
            }
         }
      }

   @Override
   public void refresh()
      {
      LOG.debug("SavedSequenceModel.refresh(): refreshing " + getName());
      // Nothing to do (TODO: I think?)
      }

   /*//---> Ask about this (Same as Expressions in expressionModel )
      public File getSavedSequenceFile()
         {
         return PathManager.getInstance().getSequencesZipSave().getAddedFile(savedSequenceFileName);
         }*/
   public String getSavedSequenceFileName()
      {
      return (savedSequenceFileName);
      }
   }
