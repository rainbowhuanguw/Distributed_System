package client2;

public class InfoPackage {
  private final long start;
  private final long end;
  private final String type;
  private final int responseCode;

  public InfoPackage(long start, long end, String type, int responseCode) {
    this.start = start;
    this.end = end;
    this.type = type;
    this.responseCode = responseCode;
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {
    return end;
  }

  public String getType() {
    return type;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public long getLatency() {
    return this.end - this.start;
  }
}
