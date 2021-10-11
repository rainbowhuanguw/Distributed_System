package client1;

/**
 * client1.Response object to be constructed given the post request body
 */
public class Response {
  private final int liftId;
  private final int time;

  public Response(int liftId, int time) {
    this.liftId = liftId;
    this.time = time;
  }

  public int getLiftId() {
    return liftId;
  }

  public int getTime() {
    return time;
  }

  @Override
  public String toString() {
    return "{" +
        "\"liftId\":" + liftId +
        ", \"time\": " + time +
        '}';
  }
}
