package com.upic.upicserver;
// A simple Java class representing a LiftRide
public class LiftRide {
  private int time;

  private int liftID;

  private String seasonId;

  private String dayId;

  private int skierId;

  private int resortId;

  // Constructor to initialize the LiftRide object with skier id and a message
  public LiftRide(int time, int liftID) {
    this.time = time;
    this.liftID = liftID;
  }

  public int getTime() {
    return time;
  }

  public int getLiftId() {
    return liftID;
  }

  public String getSeasonId() {
    return seasonId;
  }

  public void setSeasonId(String seasonId) {
    this.seasonId = seasonId;
  }

  public String getDayId() {
    return dayId;
  }

  public void setDayId(String dayId) {
    this.dayId = dayId;
  }

  public int getSkierId() {
    return skierId;
  }

  public void setSkierId(int skierId) {
    this.skierId = skierId;
  }

  public int getResortId() {
    return resortId;
  }

  public void setResortId(int resortId) {
    this.resortId = resortId;
  }

  public boolean isValid() {
    return time != 0 && liftID != 0;
  }
}
