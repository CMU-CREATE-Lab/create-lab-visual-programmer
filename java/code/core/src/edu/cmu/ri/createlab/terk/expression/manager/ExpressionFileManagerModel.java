package edu.cmu.ri.createlab.terk.expression.manager;

import java.awt.Component;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.AbstractListModel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import edu.cmu.ri.createlab.terk.TerkConstants;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import edu.cmu.ri.createlab.xml.LocalEntityResolver;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.commons.collections.list.TreeList;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ExpressionFileManagerModel extends AbstractListModel
   {
   private static final Logger LOG = Logger.getLogger(ExpressionFileManagerModel.class);

   private static final int POLLER_INITIAL_DELAY_SECS = 0;
   private static final int POLLER_DELAY_SECS = 2;

   private final TreeList expressionsFiles = new TreeList();

   // variable for synchronization
   private final byte[] expressionCollectionChangeLock = new byte[0];

   public ExpressionFileManagerModel()
      {
      XmlHelper.setLocalEntityResolver(LocalEntityResolver.getInstance());

      // make sure the Expressions directory exists
      if (TerkConstants.FilePaths.EXPRESSIONS_DIR.exists())
         {
         if (!TerkConstants.FilePaths.EXPRESSIONS_DIR.isDirectory())
            {
            throw new IllegalStateException("The expressions directory is not a directory: " + TerkConstants.FilePaths.EXPRESSIONS_DIR.getAbsolutePath());
            }
         }
      else
         {
         if (!TerkConstants.FilePaths.EXPRESSIONS_DIR.mkdirs())
            {
            throw new IllegalStateException("Failed to create expressions directory: " + TerkConstants.FilePaths.EXPRESSIONS_DIR.getAbsolutePath());
            }
         }

      final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("ExpressionFileManagerModel"));
      executorService.scheduleWithFixedDelay(
            new Runnable()
            {
            public void run()
               {
               pollDirectory(TerkConstants.FilePaths.EXPRESSIONS_DIR);
               }
            },
            POLLER_INITIAL_DELAY_SECS,
            POLLER_DELAY_SECS,
            TimeUnit.SECONDS);
      }

   @SuppressWarnings({"ConstantConditions"})
   private void pollDirectory(final File expressionsDir)
      {
      LOG.trace("ExpressionFileManagerModel.pollDirectory()");
      if (expressionsDir != null && expressionsDir.exists() && expressionsDir.isDirectory())
         {
         final TreeList newExpressionsFilesSet = new TreeList();
         final File[] files = expressionsDir.listFiles();
         if (LOG.isTraceEnabled())
            {
            LOG.trace("   Found [" + files.length + "] files...");
            }
         for (final File file : files)
            {
            if (LOG.isTraceEnabled())
               {
               LOG.trace("      Checking [" + file.getName() + "]");
               }
            if (file != null && file.exists() && file.isFile() && !file.isHidden())
               {
               //noinspection UnusedCatchParameter
               try
                  {
                  final XmlExpression expression = XmlExpression.create(file);
                  if (expression != null)
                     {
                     newExpressionsFilesSet.add(new ExpressionFile(expression, file));
                     }
                  }
               catch (Exception e)
                  {
                  if (LOG.isTraceEnabled())
                     {
                     LOG.trace("Exception while trying to read file [" + file + "] as an expression.  Ignoring.", e);
                     }
                  }
               }
            }

         synchronized (expressionCollectionChangeLock)
            {
            // see whether the contents have changed
            if (newExpressionsFilesSet.size() != expressionsFiles.size() ||
                !newExpressionsFilesSet.equals(expressionsFiles))
               {
               expressionsFiles.clear();
               expressionsFiles.addAll(newExpressionsFilesSet);

               // yeah, I know this is inefficient, but it sure is a whole lot easier than
               // figuring out exactly which items changed
               fireContentsChanged(this, 0, expressionsFiles.size());
               }
            else
               {
               if (LOG.isTraceEnabled())
                  {
                  LOG.trace("XmlExpression files have not changed (" + newExpressionsFilesSet.size() + "," + expressionsFiles.size() + ").");
                  }
               }
            }
         }
      else
         {
         throw new IllegalStateException("Missing expressions directory [" + expressionsDir + "]");
         }
      }

   public int getSize()
      {
      synchronized (expressionCollectionChangeLock)
         {
         return expressionsFiles.size();
         }
      }

   public Object getElementAt(final int index)
      {
      synchronized (expressionCollectionChangeLock)
         {
         return expressionsFiles.get(index);
         }
      }

   public ExpressionFile getExpressionFileAt(final int index)
      {
      synchronized (expressionCollectionChangeLock)
         {
         return (ExpressionFile)expressionsFiles.get(index);
         }
      }

   public XmlExpression getExpressionAt(final int selectedIndex)
      {
      final ExpressionFile expressionFile = (ExpressionFile)getElementAt(selectedIndex);
      if (expressionFile != null)
         {
         return expressionFile.getExpression();
         }
      return null;
      }

   public void saveExpression(final XmlExpression expression, final Component parentComponent, final String stageTitle, final JTextField field)
      {
      final ExpressionSavingDialogRunnable runnable = new ExpressionSavingDialogRunnable(expression, parentComponent, stageTitle, field);

      if (SwingUtilities.isEventDispatchThread())
         {
         runnable.run();
         }
      else
         {
         SwingUtilities.invokeLater(runnable);
         }
      }

   public void deleteExpression(final ExpressionFile expressionFile)
      {
      if (expressionFile != null)
         {
         final File file = expressionFile.getFile();
         synchronized (expressionCollectionChangeLock)
            {
            if (file.exists() && expressionsFiles.contains(expressionFile))
               {
               if (file.delete())
                  {
                  expressionsFiles.remove(expressionFile);
                  }
               else
                  {
                  LOG.debug("ExpressionFileManagerModel.deleteExpression(): Failed to delete expression [" + file + "]");
                  }
               }
            }
         }
      }
   }
