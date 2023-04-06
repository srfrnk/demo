package kafka;

import java.io.Serializable;
import org.apache.beam.sdk.transforms.splittabledofn.HasDefaultTracker;

class PartitionRestriction
    implements Serializable, HasDefaultTracker<PartitionRestriction, PartitionTracker> {
  PartitionPosition lastCommited;

  static PartitionRestriction of(PartitionPosition position) {
    return new PartitionRestriction(position);
  }

  public PartitionRestriction(PartitionPosition lastCommited) {
    this.lastCommited = lastCommited;
  }

  // PartitionRestriction next() {
  //   return PartitionRestriction.of(lastCommited.next());
  // }

  @Override
  public PartitionTracker newTracker() {
    return new PartitionTracker(this);
  }

  @Override
  public boolean equals(Object obj) {
    return this.lastCommited.equals(((PartitionRestriction) obj).lastCommited);
  }

  @Override
  public int hashCode() {
    return lastCommited.hashCode();
  }

  synchronized boolean tryClaim(PartitionPosition position) {
    return position.offset > lastCommited.offset;
  }
}
