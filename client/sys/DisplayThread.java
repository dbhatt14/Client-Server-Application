package client.sys;

import org.jfree.data.time.Millisecond;

import client.gui.DisplayGraph;

/**
 * This thread adds each value to the graph dynamically.
 * @author Group 1 #001 - #013
 * @version 1.0
 * @since FEB 2018
 */
public class DisplayThread implements Runnable {
  private static final int TIMING = 1000;
  private DisplayGraph displayGraph;

  public DisplayThread(DisplayGraph displayGraph) {
    this.displayGraph = displayGraph;
  }

  @Override
  public void run() {
    while (Client.isConnected) {
      int channelId = displayGraph.channelDetails.getChannelId();
      int value = displayGraph.channelDetails.getChannelValue();

      for (int i = 0; i < displayGraph.channel; i++) {
        if (i == channelId - 1) {
          displayGraph.graphSeries[i].add(new Millisecond(), value);
          displayGraph.dataset.getSeries(i).addOrUpdate(new Millisecond(), value);
        }
      }
      try {
        Thread.sleep(TIMING);
      } catch (InterruptedException e) {
        Client.isConnected = false;
      }
    }
  }
}
