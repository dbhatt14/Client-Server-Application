package server.sys;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Group 1 #001 - #013
 * @version 1.0
 * @since 2018-02-23
 *
 */
class InputThread implements Runnable {
  private Scanner in;
  private ServerThread parent;

  private Socket socket;

  public InputThread(ServerThread parent) {
    this.parent = parent;
    this.socket = parent.socket;
  }

  @Override
  public void run() {
    try {
      in = new Scanner(this.parent.socket.getInputStream());
      if (in.hasNext()) {
        System.out.println("Sending to server handler to close this socket");
        Server.closeConnectedSocket(this.socket);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
