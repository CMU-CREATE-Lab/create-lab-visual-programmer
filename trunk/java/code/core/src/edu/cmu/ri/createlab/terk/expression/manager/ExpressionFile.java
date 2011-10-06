package edu.cmu.ri.createlab.terk.expression.manager;

import java.io.File;
import edu.cmu.ri.createlab.terk.expression.XmlExpression;

/**
 * Simple wrapper for an {@link XmlExpression} and the {@link File} with which it's associated.
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ExpressionFile implements Comparable<ExpressionFile>
   {
   private final XmlExpression expression;
   private final File file;

   public ExpressionFile(final XmlExpression expression, final File file)
      {
      this.expression = expression;
      this.file = file;
      }

   public XmlExpression getExpression()
      {
      return expression;
      }

   public File getFile()
      {
      return file;
      }

   /** Returns the expression's file name, without the .xml extension. */
   public String getPrettyName()
      {
      // get the filename, but strip off any .xml extension
      String fileName = file.getName();
      if (fileName.toLowerCase().lastIndexOf(".xml") != -1)
         {
         fileName = fileName.substring(0, fileName.lastIndexOf('.'));
         }

      return fileName;
      }

   /**
    * Determines whether this instance is equal to the given instance.  Comparison is only performed on the {@link File}.
    *
    * @see #compareTo
    */
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

      final ExpressionFile that = (ExpressionFile)o;

      return !(file != null ? !file.equals(that.file) : that.file != null);
      }

   @Override
   public int hashCode()
      {
      return file != null ? file.hashCode() : 0;
      }

   /**
    * Comparison is performed on the {@link File} only, thus this class has a natural ordering that is consistent with
    * {@link #equals equals()} since {@link #equals equals()} also only uses the {@link File}.
    *
    * @see #equals(Object)
    */
   public int compareTo(final ExpressionFile that)
      {
      // yes, I really meant to use == and not .equals() here, since I want to check equivalence first
      if (this == that)
         {
         return 0;
         }

      return this.file.compareTo(that.getFile());
      }
   }
