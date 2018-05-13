package client.sys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import client.gui.ClientMainWindow;

/**
 * Client class used to connect to server, receive numbers on given number of
 * channels.
 * 
 * @author Group 1 #001 - #013
 * @version 1.0
 * @since FEB 2018
 */
public class Client implements Runnable {
  private static Socket socket;
  public static volatile boolean isConnected;
  private static PrintWriter out;
  private final String ipAddress = "127.0.0.1";
  private final int port = 8001;
  private int channels;
  private int frequency;
  public ClientMainWindow clientWindow;
  public static ArrayList<ArrayList<String>> numberList = new ArrayList<ArrayList<String>>();

  /**
   * Constructor accepts the number of channels through which data has to be
   * received from the server.
   * 
   * @param numChannels
   *          - number of channels to receive numbers from server
   */
  public Client(int numChannels, ClientMainWindow clientWindow) {
    channels = numChannels;
    isConnected = true;
    this.clientWindow = clientWindow;
    connectSocket();
  }

  private void connectSocket() {
    try {
      socket = new Socket(ipAddress, port);
    } catch (UnknownHostException e) {
      isConnected = false;
    } catch (IOException e) {
      isConnected = false;
      clientWindow.controlStartStopAction(isConnected);
    }
  }

  public int getFrequency() {
    return frequency;
  }

  public int getChannels() {
    return channels;
  }

  public void setConsoleInfo(String info) {
    ClientMainWindow.appendToConsole(info);
  }

  /**
   * Welcome message from server has frequency in it, split message to get the
   * frequency.
   */
  private void receiveFrequency() {
    InputStream inputStream;
    InputStreamReader reader;
    BufferedReader bufferReader;
    String message;

    try {
      inputStream = socket.getInputStream();
      reader = new InputStreamReader(inputStream);
      bufferReader = new BufferedReader(reader);
      message = bufferReader.readLine();

      frequency = Integer.parseInt(message.split("frequency=")[1].split(";")[0]);
      clientWindow.setFrequency(frequency);
      System.out.println("frequency is " + frequency);
    } catch (Exception exception) {
      isConnected = false;
    }
  }

  /**
   * Channels is required by server to send the appropriate numbers.
   */
  private void sendNumberOfChannels() {
    try {
      String channelsMessage = "channels=" + channels + ";";
      out = new PrintWriter(socket.getOutputStream());
      out.println(channelsMessage);
      out.flush();
    } catch (IOException e) {
      isConnected = false;
    }
  }

  /**
   * The number received in the form channelID_id=value; is split and added to
   * channeletails.
   */
  private void receiveNumbers() {
    while (isConnected) {
      InputStream inputStream;
      InputStreamReader reader;
      BufferedReader bufferReader;
      String message;
      int channelId, channelValue;
      try {
        inputStream = socket.getInputStream();
        reader = new InputStreamReader(inputStream);
        bufferReader = new BufferedReader(reader);
        message = bufferReader.readLine();

        channelId = Integer.parseInt(message.split("channelID_")[1].split("=")[0]);
        channelValue = Integer.parseInt(message.split("channelID_")[1].split("=")[1].split(";")[0]);
        setConsoleInfo("Channel (" + channelId + ") received value: " + channelValue + "&emsp;&emsp;&lt;"
            + LocalTime.now() + "&gt;");
        Channel channelDetails = new Channel(channelId, channelValue);
        UpdateClientWindow(channelDetails);
      } catch (IOException e) {
        isConnected = false;
      } catch (NullPointerException e) {
        setConsoleInfo("Server is no longer connected, check connection...");
        isConnected = false;
        clientWindow.controlStartStopAction(isConnected);
      } catch (Exception e) {
        isConnected = false;
      }
    }
  }

  /**
   * Terminates the connection with server.
   */
  public void closeConnection() {
    try {
      isConnected = false;
      out = new PrintWriter(socket.getOutputStream());
      out.println("closing");
      out.flush();
    } catch (NullPointerException e) {
      isConnected = false;
    } catch (SocketException e) {
      Client.isConnected = false;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        isConnected = false;
      }
    }
  }

  /**
   * Has calls to receive frequency,send channels and receive numbers.
   */
  @Override
  public void run() {
    while (isConnected) {
      setConsoleInfo("Client connected to " + socket.getLocalAddress().getHostName());
      receiveFrequency();
      sendNumberOfChannels();
      receiveNumbers();
    }
    setConsoleInfo("Client closing connection...");
  }

  /**
   * 
   * @param channelDetails
   *          - The channel id and value to update client window.
   * 
   */
  public void UpdateClientWindow(Channel channelDetails) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        clientWindow.displayGraph.updateGraph(channels, channelDetails);
        NumberStatistics.ComputeNumberStatistics(channelDetails);
        clientWindow.refreshWindow();
      }
    });
  }
}
