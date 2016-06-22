package edu.cmu.ri.createlab.audio;

/**
 * Created by Brandon on 6/22/2016.
 * Records audio into a wav file
 */

import javax.sound.sampled.*;
import java.io.*;

public class AudioRecorder
   {
   private File wavFile;

   private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

   private TargetDataLine targetDataLine;

   public AudioRecorder(File outputFile)
      {
      wavFile = outputFile;
      }

   boolean startRecording()
      {
      try
         {
         AudioFormat format = new AudioFormat(16000, 8,
                                              2, true, true);
         DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

         if (!AudioSystem.isLineSupported(info))
            {
            return false;
            }
         targetDataLine = (TargetDataLine)AudioSystem.getLine(info);
         targetDataLine.open(format);
         targetDataLine.start();                                      // start capturing audio
         AudioInputStream stream = new AudioInputStream(targetDataLine);
         AudioSystem.write(stream, fileType, wavFile);         // start writing audio to file
         return true;
         }
      catch (LineUnavailableException e)
         {
         return false;
         }
      catch (IOException e)
         {
         return false;
         }
      }

   void stopRecording()
      {
      targetDataLine.stop();
      targetDataLine.close();
      }
   }
