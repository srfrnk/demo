package kafka;

import java.io.Serializable;

class TopicPosition implements Serializable {
  int idx;

  public TopicPosition(int idx) {
    this.idx = idx;
  }

  @Override
  public boolean equals(Object obj) {
    return this.idx == ((TopicPosition) obj).idx;
  }

  @Override
  public int hashCode() {
    return idx;
  }
}
