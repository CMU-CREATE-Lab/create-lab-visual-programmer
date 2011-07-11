package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.io.File;
import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
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
   private static final Logger LOG = Logger.getLogger(SavedSequenceModel.class);

   public static final String XML_ELEMENT_NAME = "saved-sequence";
   private static final String XML_ATTRIBUTE_NAME_FILE = "file";

   private final File savedSequenceFile;

   @Nullable
   public static SavedSequenceModel createFromXmlElement(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                                                         @Nullable final Element element)
      {
      if (element != null)
         {
         LOG.debug("SavedSequenceModel.createFromXmlElement(): " + element);

         final String filename = element.getAttributeValue(XML_ATTRIBUTE_NAME_FILE);
         final File file = new File(TerkConstants.FilePaths.SEQUENCES_DIR, filename);
         if (file.exists())
            {
            return new SavedSequenceModel(visualProgrammerDevice,
                                          file,
                                          getCommentFromParentXmlElement(element),
                                          getIsCommentVisibleFromParentXmlElement(element));
            }
         else
            {
            if (LOG.isEnabledFor(Level.WARN))
               {
               LOG.warn("SavedSequenceModel.createFromXmlElement(): Sequence file [" + file + "] does not exist.  Returning null.");
               }
            }
         }
      return null;
      }

   /** Creates a <code>SavedSequenceModel</code> with an empty hidden comment. */
   public SavedSequenceModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                             @NotNull final File savedSequenceFile)
      {
      this(visualProgrammerDevice, savedSequenceFile, null, false);
      }

   /** Creates a <code>SavedSequenceModel</code> with the given <code>comment</code>. */
   public SavedSequenceModel(@NotNull final VisualProgrammerDevice visualProgrammerDevice,
                             @NotNull final File savedSequenceFile,
                             @Nullable final String comment,
                             final boolean isCommentVisible)
      {
      super(visualProgrammerDevice, comment, isCommentVisible);
      this.savedSequenceFile = savedSequenceFile;
      }

   /** Copy constructor */
   private SavedSequenceModel(final SavedSequenceModel originalSavedSequenceModel)
      {
      this(originalSavedSequenceModel.getVisualProgrammerDevice(),
           originalSavedSequenceModel.getSavedSequenceFile(),
           originalSavedSequenceModel.getComment(),
           originalSavedSequenceModel.isCommentVisible());
      }

   /** Returns the saved sequence's file name, without the .xml extension. */
   @Override
   @NotNull
   public String getName()
      {
      // get the filename, but strip off any .xml extension
      String fileName = savedSequenceFile.getName();
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
      element.setAttribute("file", savedSequenceFile.getName());
      element.addContent(getCommentAsElement());

      return element;
      }

   public File getSavedSequenceFile()
      {
      return savedSequenceFile;
      }
   }
