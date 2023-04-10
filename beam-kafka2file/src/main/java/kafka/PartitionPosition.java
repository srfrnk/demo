package kafka;

import java.io.Serializable;

class PartitionPosition implements Serializable {
  public PartitionPosition() {}

  @Override
  public boolean equals(Object obj) {
    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
