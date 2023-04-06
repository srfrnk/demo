package kafka;

import java.io.Serializable;

class PartitionPosition implements Serializable {
  long offset;

  static PartitionPosition of(long offset) {
    return new PartitionPosition(offset);
  }


  public PartitionPosition(long offset) {
    this.offset = offset;
  }

  // PartitionPosition next() {
  //   return PartitionPosition.of(offset + 1);
  // }

  PartitionPosition advance(long offset) throws Exception {
    return PartitionPosition.of(Math.max(offset, this.offset));
  }

  @Override
  public boolean equals(Object obj) {
    return this.offset == ((PartitionPosition) obj).offset;
  }

  @Override
  public int hashCode() {
    return (int) offset;
  }
}
