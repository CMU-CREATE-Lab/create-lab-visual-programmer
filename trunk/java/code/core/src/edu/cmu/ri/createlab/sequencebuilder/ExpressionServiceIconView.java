package edu.cmu.ri.createlab.sequencebuilder;

import java.util.Set;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.terk.expression.XmlService;
import edu.cmu.ri.createlab.terk.services.ServiceManager;

/**
 * <p>
 * <code>ExpressionServiceIconView</code> helps render service icons in an expression program element.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ExpressionServiceIconView
   {
   JPanel createBlockIcons(final Set<XmlService> expressionServices, final ServiceManager serviceManager);
   }