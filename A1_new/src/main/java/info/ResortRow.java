package info;

/**
 * Represents one row of the resorts table
 */
public class ResortRow {

  private final int liftId;
  private final int resortId;

  public ResortRow(int liftId, int resortId) {
    this.liftId = liftId;
    this.resortId = resortId;
  }

  public int getLiftId() {
    return liftId;
  }

  public int getResortId() {
    return resortId;
  }
}
