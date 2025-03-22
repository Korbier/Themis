package org.sc.themis.shared.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

public class Timer {

  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Timer.class);

  private final List<Long> measures = new ArrayList<>();
  private long measure = 0;

  private String name;
  private long currentDisplay = System.currentTimeMillis();
  private long lastDisplay = 0;

  public void start(String name) {
    this.name = name;
    this.measure = System.currentTimeMillis();
  }

  public void stopAndShow() {
    stop();
    show();
  }

  public void stop() {
    this.measures.add(System.currentTimeMillis() - this.measure);
  }

  public void show() {

    this.lastDisplay += System.currentTimeMillis() - this.currentDisplay;
    this.currentDisplay = System.currentTimeMillis();

    if (this.lastDisplay > 1000) {
      logger.info("{} > Average rendering time in ms : {}", this.name, this.measures.stream().mapToLong(l -> l).average().orElse(0));
      this.measures.clear();
      this.lastDisplay = 0;
    }

  }

}
