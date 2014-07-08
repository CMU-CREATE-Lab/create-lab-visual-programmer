package edu.cmu.ri.createlab.visualprogrammer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import edu.cmu.ri.createlab.util.StandardVersionNumber;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * <code>UpdateChecker</code> asynchronously checks the server for current version number, providing a means for users
 * to determine whether an update is available.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class UpdateChecker
   {
   private static final int TIMEOUT_MILLIS = 2000;

   private static final Logger LOG = Logger.getLogger(UpdateChecker.class);

   public interface UpdateCheckResultListener
      {
      void handleUpdateCheckResult(final boolean wasCheckSuccessful,
                                   final boolean isUpdateAvailable,
                                   @Nullable final StandardVersionNumber versionNumberOfUpdate);
      }

   @NotNull
   private final StandardVersionNumber currentVersionNumber;

   @NotNull
   private final String versionNumberUri;

   private final ExecutorService executorService = Executors.newCachedThreadPool(new DaemonThreadFactory(this.getClass().getSimpleName()));
   private final Set<UpdateCheckResultListener> updateCheckResultListeners = new HashSet<UpdateCheckResultListener>();

   public UpdateChecker(@NotNull final StandardVersionNumber currentVersionNumber,
                        @NotNull final VisualProgrammerDevice visualProgrammerDevice,
                        @NotNull final UserPreferences userPreferences)
      {
      this.currentVersionNumber = currentVersionNumber;

      String uri;
      try
         {
         final URIBuilder uriBuilder = new URIBuilder(VisualProgrammerConstants.Urls.CURRENT_VERSION_NUMBER_URL);

         final String deviceVersion = visualProgrammerDevice.getDeviceVersion();
         final String deviceName = visualProgrammerDevice.getDeviceName() + (deviceVersion == null ? "" : "(" + deviceVersion + ")");

         // Add some query string params, so we can do some simple (anonymous) stats
         uriBuilder.addParameter("u", userPreferences.getUserId());
         uriBuilder.addParameter("d", deviceName);
         uriBuilder.addParameter("v", currentVersionNumber.toString());
         uriBuilder.addParameter("j", System.getProperty("java.version"));
         uriBuilder.addParameter("o", System.getProperty("os.name"));
         uriBuilder.addParameter("ov", System.getProperty("os.version"));

         uri = uriBuilder.build().toString();
         }
      catch (URISyntaxException e)
         {
         LOG.error("UpdateChecker.UpdateChecker(): URISyntaxException while trying to build the version number URI, defaulting to the no-query string version", e);
         uri = VisualProgrammerConstants.Urls.CURRENT_VERSION_NUMBER_URL;
         }

      versionNumberUri = uri;
      }

   public void addUpdateCheckResultListener(@Nullable final UpdateCheckResultListener updateCheckResultListener)
      {
      if (updateCheckResultListener != null)
         {
         updateCheckResultListeners.add(updateCheckResultListener);
         }
      }

   public void removeUpdateCheckHandler(@Nullable final UpdateCheckResultListener updateCheckResultListener)
      {
      if (updateCheckResultListener != null)
         {
         updateCheckResultListeners.remove(updateCheckResultListener);
         }
      }

   public void checkForUpdate()
      {
      if (!updateCheckResultListeners.isEmpty())
         {
         try
            {
            LOG.debug("UpdateChecker.checkForUpdate(): running update check");
            executorService.execute(
                  new Runnable()
                  {
                  @Override
                  public void run()
                     {
                     final StandardVersionNumber latestVersionNumber = getLatestVersionNumber();
                     final boolean wasCheckSuccessful = latestVersionNumber != null;
                     final boolean isUpdateAvailable = wasCheckSuccessful && !currentVersionNumber.equals(latestVersionNumber);

                     if (LOG.isDebugEnabled())
                        {
                        LOG.debug("UpdateChecker.checkForUpdate(): wasCheckSuccessful = [" + wasCheckSuccessful + "], isUpdateAvailable = [" + isUpdateAvailable + "], latestVersionNumber = [" + latestVersionNumber + "]");
                        }

                     // notify all listeners
                     for (final UpdateCheckResultListener updateCheckResultListener : updateCheckResultListeners)
                        {
                        updateCheckResultListener.handleUpdateCheckResult(wasCheckSuccessful,
                                                                          isUpdateAvailable,
                                                                          latestVersionNumber);
                        }
                     }
                  });
            }
         catch (Exception e)
            {
            LOG.error("Exception while trying to execute the update check", e);
            }
         }
      }

   @Nullable
   private StandardVersionNumber getLatestVersionNumber()
      {
      // Execute a GET with timeout settings and return response content as String.
      String versionNumber = null;
      try
         {
         // Taken from http://hc.apache.org/httpcomponents-client-4.3.x/tutorial/html/fluent.html

         versionNumber = Request.Get(versionNumberUri)
               .setCacheControl("no-cache")
               .userAgent("CREATE Lab Visual Programmer Update Checker (Apache-HttpClient/4.3.1)")
               .connectTimeout(TIMEOUT_MILLIS)
               .socketTimeout(TIMEOUT_MILLIS)
               .execute().handleResponse(
                     new ResponseHandler<String>()
                     {

                     public String handleResponse(final HttpResponse response) throws IOException
                        {
                        final StatusLine statusLine = response.getStatusLine();
                        if (statusLine.getStatusCode() >= 300)
                           {
                           throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
                           }

                        final HttpEntity entity = response.getEntity();
                        if (entity == null)
                           {
                           throw new ClientProtocolException("Response contains no content");
                           }

                        // Get the content and convert to a string (http://stackoverflow.com/a/309448)
                        final InputStream contentStream = entity.getContent();
                        final StringWriter writer = new StringWriter();
                        IOUtils.copy(contentStream, writer);
                        return writer.toString();
                        }
                     });
         }
      catch (HttpResponseException e)
         {
         LOG.error("UpdateChecker.getLatestVersionNumber(): HttpResponseException while trying to get the latest version number: " + e);
         }
      catch (IOException e)
         {
         LOG.error("UpdateChecker.getLatestVersionNumber(): IOException while trying to get the latest version number: " + e);
         }
      catch (Exception e)
         {
         LOG.error("UpdateChecker.getLatestVersionNumber(): Exception while trying to get the latest version number: " + e);
         }

      return StandardVersionNumber.parse(versionNumber);
      }
   }
