package edu.cmu.ri.createlab.sequencebuilder.programelement.model;

import java.io.File;
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
   private final File savedSequenceFile;

   /** Creates a <code>SavedSequenceModel</code> with an empty comment. */
   public SavedSequenceModel(@NotNull final File savedSequenceFile)
      {
      this(savedSequenceFile, null);
      }

   /** Creates a <code>SavedSequenceModel</code> with the given <code>comment</code>. */
   public SavedSequenceModel(@NotNull final File savedSequenceFile, @Nullable final String comment)
      {
      super(comment);
      this.savedSequenceFile = savedSequenceFile;
      }

   /** Copy constructor */
   private SavedSequenceModel(final SavedSequenceModel originalSavedSequenceModel)
      {
      this(originalSavedSequenceModel.getSavedSequenceFile(),
           originalSavedSequenceModel.getComment());
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

   public File getSavedSequenceFile()
      {
      return savedSequenceFile;
      }

   @Override
   public boolean equals(final Object o)
      {
      if (this == o)
         {
         return true;
         }
      if (o == null || getClass() != o.getClass())
         {
         return false;
         }
      if (!super.equals(o))
         {
         return false;
         }

      final SavedSequenceModel that = (SavedSequenceModel)o;

      if (savedSequenceFile != null ? !savedSequenceFile.equals(that.savedSequenceFile) : that.savedSequenceFile != null)
         {
         return false;
         }

      return true;
      }

   @Override
   public int hashCode()
      {
      int result = super.hashCode();
      result = 31 * result + (savedSequenceFile != null ? savedSequenceFile.hashCode() : 0);
      return result;
      }
   }
