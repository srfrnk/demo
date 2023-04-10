package kafka;

import java.io.Serializable;
import org.apache.beam.sdk.transforms.splittabledofn.HasDefaultTracker;

class PartitionRestriction
    implements Serializable, HasDefaultTracker<PartitionRestriction, PartitionTracker> {
  public PartitionRestriction() {}

  @Override
  public PartitionTracker newTracker() {
    return new PartitionTracker(this);
  }

  @Override
  public boolean equals(Object obj) {
    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
