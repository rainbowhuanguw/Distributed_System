package info;

/**
 * client1.Response object to be constructed given the post request body
 */
public class LiftRide {
  private final int skierId;
  private final int liftId;
  private final int time;

  public LiftRide(int skierId, int liftId, int time) {
    this.skierId = skierId;
    this.liftId = liftId;
    this.time = time;
  }

  public int getSkierId() {
    return skierId;
  }

  public int getLiftId() {
    return liftId;
  }

  public int getTime() {
    return time;
  }

  public String toJsonString() {
    return "{" +
        "\"skierId\":" + skierId +
        "\"liftId\":" + liftId +
        ", \"time\": " + time +
        '}';
  }

  @Override
  public String toString() {
    return skierId + "," + liftId + "," + time;
  }
}
