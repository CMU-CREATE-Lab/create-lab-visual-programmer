package edu.cmu.ri.createlab.sequencebuilder.sequence;

import edu.cmu.ri.createlab.sequencebuilder.ContainerModel;
import edu.cmu.ri.createlab.sequencebuilder.ContainerView;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.log4j.Logger;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

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

   private static final String DEFAULT_VERSION = "1.0";

   private final ContainerModel containerModel;
   private final ContainerView containerView;

   public Sequence(@NotNull final ContainerModel containerModel, @NotNull final ContainerView containerView)
      {
      this.containerModel = containerModel;
      this.containerView = containerView;
      }

   public ContainerModel getContainerModel()
      {
      return containerModel;
      }

   public ContainerView getContainerView()
      {
      return containerView;
      }

   public void save()
      {
      LOG.debug("Sequence.save()");

      final Element sequenceElement = new Element(ELEMENT_NAME);
      sequenceElement.setAttribute("version", DEFAULT_VERSION);
      sequenceElement.addContent(containerModel.toElement());

      final Document document = new Document(sequenceElement, (DocType)DOC_TYPE.clone());
      final String xml = XmlHelper.writeDocumentToStringFormatted(document);

      LOG.debug("XML = \n" + xml);

      // TODO: save to a file...
      }
   }
