package com.seeks.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TailLogThread extends Thread
{
  private String id;
  private BufferedReader reader;
  
  public TailLogThread(InputStream in,  String id)
  {
    this.reader = new BufferedReader(new InputStreamReader(in));
    this.id = id;
    setName(this.id);
  }
  
  public void run()
  {
    try {
      String line;
      while ((line = this.reader.readLine()) != null)
      {
        System.out.println(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void shutdown() {
    this.reader = null;
    this.id = null;
  }
}


