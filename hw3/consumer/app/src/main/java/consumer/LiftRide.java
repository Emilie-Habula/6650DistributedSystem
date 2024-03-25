package consumer;

// A simple Java class representing a LiftRide
public class LiftRide {

  private String id;
  private int time;

  private int liftID;

  private String seasonId;

  private String dayId;

  private int skierId;

  private int resortId;

  public LiftRide(int time, int liftID, String seasonId, String dayId, int skierId, int resortId) {
    this.time = time;
    this.liftID = liftID;
    this.seasonId = seasonId;
    this.dayId = dayId;
    this.skierId = skierId;
    this.resortId = resortId;
  }

  public String getId() {
    return String.format("%d_%d_%s_%s_%d", skierId, resortId, dayId, seasonId, liftID);
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public int getLiftID() {
    return liftID;
  }

  public void setLiftID(int liftID) {
    this.liftID = liftID;
  }

  public int getTime() {
    return time;
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