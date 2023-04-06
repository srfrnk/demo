package kafka;

import org.apache.beam.sdk.transforms.splittabledofn.RestrictionTracker;
import org.apache.beam.sdk.transforms.splittabledofn.SplitResult;

class PartitionTracker extends RestrictionTracker<PartitionRestriction, PartitionPosition> {
  PartitionRestriction current;

  public PartitionTracker(PartitionRestriction initial) {
    this.current = initial;
  }

  @Override
  public boolean tryClaim(PartitionPosition partition) {
    return current.tryClaim(partition);
  }

  @Override
  public PartitionRestriction currentRestriction() {
    return current;
  }

  @Override
  public SplitResult<PartitionRestriction> trySplit(double fractionOfRemainder) {
    return SplitResult.of(null, current);
  }

  @Override
  public void checkDone() throws IllegalStateException {}

  @Override
  public IsBounded isBounded() {
    return IsBounded.UNBOUNDED;
  }
}
