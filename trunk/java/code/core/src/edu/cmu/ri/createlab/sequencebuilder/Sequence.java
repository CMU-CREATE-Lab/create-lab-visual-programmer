package edu.cmu.ri.createlab.sequencebuilder;

import edu.cmu.ri.createlab.sequencebuilder.programelement.model.ProgramElementModel;
import edu.cmu.ri.createlab.visualprogrammer.VisualProgrammerDevice;
import org.apache.log4j.Logger;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class Sequence
   {
   private static final Logger LOG = Logger.getLogger(Sequence.class);

   private static final String ELEMENT_NAME = "sequence";
   private static final String DOCTYPE_PUBLIC_ID = "-//CREATE Lab//TeRK//Sequence//EN";
   private static final String DOCTYPE_SYSTEM_ID = "http://www.createlab.ri.cmu.edu/dtd/terk/sequence.dtd";
   private static final DocType DOC_TYPE = new DocType(ELEMENT_NAME, DOCTYPE_PUBLIC_ID, DOCTYPE_SYSTEM_ID);

   private final JLayeredPane component = new JLayeredPane();

   private static final String DEFAULT_VERSION = "1.0";

   private final JPanel sequencePanel;
   private final ContainerModel containerModel;
   private final ContainerView containerView;

   public Sequence(@NotNull final ContainerModel containerModel, @NotNull final ContainerView containerView)
      {
      this.containerModel = containerModel;
      this.containerView = containerView;
      sequencePanel = new JPanel();

      //containerView.getComponent().setAutoscrolls(true);
      }

   public ContainerModel getContainerModel()
      {
      return containerModel;
      }

   public ContainerView getContainerView()
      {
      return containerView;
      }

   @NotNull
   public Document toXmlDocument()
      {
      final Element sequenceElement = new Element(ELEMENT_NAME);
      sequenceElement.setAttribute("version", DEFAULT_VERSION);
      sequenceElement.addContent(containerModel.toElement());

      return new Document(sequenceElement, (DocType)DOC_TYPE.clone());
      }

   /** Returns <code>true</code> if the sequence does not contain any program elements; <code>false</code> otherwise. */
   public boolean isEmpty()
      {
      return containerModel.isEmpty();
      }

   /** Removes all program elements from this sequence. */
   public void clear()
      {
      LOG.debug("Sequence.clear()");
      containerModel.removeAll();
      }

   /**
    * Clears the existing sequence and the loads the given sequence. This method assumes that the given {@link Document}
    * has already been validated.
    */
   public void load(@NotNull final VisualProgrammerDevice visualProgrammerDevice, @NotNull final Document sequenceXmlDocument)
      {
      containerModel.load(visualProgrammerDevice, sequenceXmlDocument);
      }

   /**
    * Appends the given {@link ProgramElementModel} to the sequence, exactly as if the user had dragged it.  This method
    * assumes that the model instance is unique.
    *
    * @see ProgramElementModel#createCopy()
    */
   public void appendProgramElement(final ProgramElementModel model)
      {
      if (model != null)
         {
         containerModel.add(model);
         }
      }
   }
