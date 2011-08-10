package edu.cmu.ri.createlab.sequencebuilder;

import edu.cmu.ri.createlab.sequencebuilder.programelement.model.SavedSequenceModel;
import edu.cmu.ri.createlab.sequencebuilder.programelement.view.listcell.SavedSequenceListCellView;
import edu.cmu.ri.createlab.util.AbstractDirectoryPollingListModel;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: jcross1
 * Date: 8/10/11
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class SavedSequenceFileListModel extends AbstractDirectoryPollingListModel<SavedSequenceListCellView>
   {
   private final ContainerView containerView;
   private final VisualProgrammerDevice visualProgrammerDevice;

   public SavedSequenceFileListModel(@NotNull final ContainerView containerView, final VisualProgrammerDevice visualProgrammerDevice)
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
