package info;

/**
 * Represents one row in the skiers table
 */
public class SkierRow {
  private final int skierId;
  private final int season;
  private final int day;
  private final int numOfLiftRides;
  private final int liftId;

  public SkierRow(int skierId, int season, int day, int numOfLiftRides, int liftId) {
    this.skierId = skierId;
    this.season = season;
    this.day = day;
    this.numOfLiftRides = numOfLiftRides;
    this.liftId = liftId;
  }

  public int getSkierId() {
    return skierId;
  }

  public int getSeason() {
    return season;
  }

  public int getDay() {
    return day;
  }

  public int getNumOfLiftRides() {
    return numOfLiftRides;
  }

  public int getLiftId() {
    return liftId;
  }
}
