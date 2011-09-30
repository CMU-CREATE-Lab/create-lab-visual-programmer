package edu.cmu.ri.createlab.util;

import java.io.File;
import org.jetbrains.annotations.Nullable;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface FileProvider
   {
   @Nullable
   File getFile();
   }