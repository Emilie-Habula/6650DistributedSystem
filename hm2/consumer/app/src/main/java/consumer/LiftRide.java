package consumer;

public class LiftRide {
  private int time;

  private int liftID;


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

  public boolean isValid() {
    return time != 0 && liftID != 0;
  }
}

