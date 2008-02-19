/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 * Created on 21.09.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.dasobert.das;

import java.net.*;
import java.util.*;
import java.util.logging.*;

import org.biojava.dasobert.dasregistry.*;
import org.biojava.dasobert.eventmodel.*;

/** a thread that connects to a DAS - Feature service and gets the features
 *
 * @author Andreas Prlic
 */



public class FeatureThread
    implements Runnable
{

  /** number of times the client tries to reconnect to the server if a "come back later" is returned.
   * the server should provide a reasonable estimation how long it will take him to create results.
   * if this number of requests is still not successfull, give up.
   */
  public static int MAX_COME_BACK_ITERATIONS = 5;

  public static int MAX_NR_FEATURES = 300;

  static Logger logger = Logger.getLogger("org.biojava.spice");

  Das1Source dasSource;
  String ac;
  List featureListeners;
  Thread thread;

  public FeatureThread(String accessionCode, Das1Source dasSource)
  {
    this.dasSource = dasSource;
    this.ac = accessionCode;
    featureListeners = new ArrayList();
  }

  public void addFeatureListener(FeatureListener li)
  {
    featureListeners.add(li);
  }

  public void clearFeatureListeners()
  {
    featureListeners.clear();
  }

  public synchronized void stop()
  {
    thread = null;
    notify();
  }

  public void run()
  {
    Thread me = Thread.currentThread();
    while (thread == me)
    {
      String url = dasSource.getUrl();
      String queryString = url + "features?segment=" + ac;
      URL cmd = null;
      try
      {
        cmd = new URL(queryString);
      }
      catch (MalformedURLException e)
      {
        logger.warning("got MalformedURL from das source " + dasSource);
        e.printStackTrace();

      }

      logger.info("requesting features from " + cmd);
      DAS_FeatureRetrieve ftmp = new DAS_FeatureRetrieve(cmd);

      int comeBackLater = ftmp.getComeBackLater();
      int securityCounter = 0;
      while ( (thread == me) && (comeBackLater > 0))
      {
        securityCounter++;
        if (securityCounter >= MAX_COME_BACK_ITERATIONS)
        {
          comeBackLater = -1;
          break;

        }
        notifyComeBackLater(comeBackLater);
        // server is still calculating - asks us to come back later
        try
        {
          wait(comeBackLater);
        }
        catch (InterruptedException e)
        {
          comeBackLater = -1;
          break;
        }

        ftmp.reload();
        comeBackLater = ftmp.getComeBackLater();
      }

      if (! (thread == me))
      {
        break;
      }

      List features = ftmp.get_features();

      // a fallback mechanism to prevent DAS sources from bringing down spice
      if (features.size() > MAX_NR_FEATURES)
      {
        logger.warning("DAS source returned more than " + MAX_NR_FEATURES +
                       "features. " +
                       " throwing away excess features at " + cmd);
        features = features.subList(0, MAX_NR_FEATURES);
      }

      // notify FeatureListeners
      Map[] feats = (Map[]) features.toArray(new Map[features.size()]);
      notifyFeatureListeners(feats);

      break;

    }
    thread = null;

  }

  public void start()
  {
    thread = new Thread(this);
    thread.start();
  }

  private void notifyFeatureListeners(Map[] feats)
  {
    logger.finest("FeatureThread found " + feats.length + " features");
    FeatureEvent fevent = new FeatureEvent(feats, dasSource);
    Iterator fiter = featureListeners.iterator();
    while (fiter.hasNext())
    {
      FeatureListener fi = (FeatureListener) fiter.next();
      fi.newFeatures(fevent);
    }
  }

  /** the Annotation server requested to be queried again in a while
   *
   * @param comeBackLater
   */
  private void notifyComeBackLater(int comeBackLater)
  {
    FeatureEvent event = new FeatureEvent(new HashMap[0], dasSource);
    event.setComeBackLater(comeBackLater);
    Iterator fiter = featureListeners.iterator();
    while (fiter.hasNext())
    {
      FeatureListener fi = (FeatureListener) fiter.next();
      fi.comeBackLater(event);
    }

  }

}
