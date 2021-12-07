package info;

/**
 * Skier and lift ride information object to be constructed given the post request body
 */
public class LiftRide {
  private final int skierId;
  private final int liftId;
  private final int time;
  private final int seasonId;
  private final int dayId;
  private final int resortId;

  public LiftRide(int skierId, int liftId, int time, int seasonsId, int dayId,
      int resortId) {
    this.skierId = skierId;
    this.liftId = liftId;
    this.time = time;
    this.seasonId = seasonsId;
    this.dayId = dayId;
    this.resortId = resortId;
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

  public int getSeasonId() { return seasonId; }

  public int getDayId() { return dayId; }

  public int getResortId() {
    return resortId;
  }

  public String toJsonString() {
    return "{" +
        "\"skierId\":" + skierId +
        ",\"liftId\":" + liftId +
        ",\"time\":" + time +
        ",\"seasonId\":" + seasonId +
        ",\"dayId\":" + dayId +
        ",\"resortId\":" + resortId +
        '}';
  }

  @Override
  public String toString() {
    return skierId + "," + liftId + "," + time + "," + seasonId + "," + dayId + "," + resortId;
  }
}
