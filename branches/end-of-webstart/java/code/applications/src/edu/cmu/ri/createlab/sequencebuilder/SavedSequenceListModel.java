package edu.cmu.ri.createlab.sequencebuilder;

import java.io.File;
import java.util.Comparator;
import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.SavedSequenceListCellView;
import edu.cmu.ri.createlab.util.AbstractDirectoryPollingListModel;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jenn Cross (jenncross99@gmail.com)
 */
final class SavedSequenceListModel extends AbstractDirectoryPollingListModel<SavedSequenceListCellView>
   {
   private final ContainerView containerView;
   private final VisualProgrammerDevice visualProgrammerDevice;

   SavedSequenceListModel(@NotNull final ContainerView containerView, final VisualProgrammerDevice visualProgrammerDevice)
      {
      super(
            new Comparator<SavedSequenceListCellView>()
            {
            @Override
            public int compare(final SavedSequenceListCellView view1, final SavedSequenceListCellView view2)
               {
               return view1.getProgramElementModel().getSavedSequenceFile().compareTo(view2.getProgramElementModel().getSavedSequenceFile());
               }
            });
      this.containerView = containerView;
      this.visualProgrammerDevice = visualProgrammerDevice;
      }

   @Override
   protected SavedSequenceListCellView createListItemInstance(@NotNull final File file)
      {
      return new SavedSequenceListCellView(containerView, new SavedSequenceModel(visualProgrammerDevice, file));
      }
   }
