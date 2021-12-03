package info;

/**
 * Skier and lift ride information object to be constructed given the post request body
 */
public class LiftRide {
  private final int skierId;
  private final int liftId;
  private final int time;
  private final int season;
  private final int day;

  public LiftRide(int skierId, int liftId, int time, int season, int day) {
    this.skierId = skierId;
    this.liftId = liftId;
    this.time = time;
    this.season = season;
    this.day = day;
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

  public int getSeason() { return season; }

  public int getDay() { return day; }

  public String toJsonString() {
    return "{" +
        "\"skierId\":" + skierId +
        ",\"liftId\":" + liftId +
        ",\"time\":" + time +
        ",\"season\":" + season +
        ",\"day\":" + day +
        '}';
  }

  @Override
  public String toString() {
    return skierId + "," + liftId + "," + time + "," + season + "," + day;
  }
}
