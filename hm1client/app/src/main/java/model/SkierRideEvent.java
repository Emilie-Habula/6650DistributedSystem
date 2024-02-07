package model;

import java.util.Random;

/**
 * A Java class representing a SkierRideEvent
 */
public class SkierRideEvent {
  // Private member variables to store skier id, resort id, lift id, season id, day id, and time
  private int skierId;
  private int resortId;
  private int liftId;
  private String seasonId;
  private String dayId;
  private int time;

  // Constructor to initialize the SkierRideEvent object with provided values
  public SkierRideEvent(int skierId, int resortId, int liftId, String seasonId, String dayId,
      int time) {
    this.skierId = skierId;
    this.resortId = resortId;
    this.liftId = liftId;
    this.seasonId = seasonId;
    this.dayId = dayId;
    this.time = time;
  }

  // Getter method for retrieving the skier id
  public int getSkierId() {
    return skierId;
  }

  // Getter method for retrieving the resort id
  public int getResortId() {
    return resortId;
  }

  // Getter method for retrieving the lift id
  public int getLiftId() {
    return liftId;
  }

  // Getter method for retrieving the season id
  public String getSeasonId() {
    return seasonId;
  }

  // Getter method for retrieving the day id
  public String getDayId() {
    return dayId;
  }

  // Getter method for retrieving the time
  public int getTime() {
    return time;
  }
}
