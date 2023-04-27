package kafka;

import org.apache.beam.sdk.transforms.splittabledofn.RestrictionTracker;
import org.apache.beam.sdk.transforms.splittabledofn.SplitResult;

class ReaderRestrictionTracker extends RestrictionTracker<ReaderRestriction, ReaderPosition> {
  ReaderRestriction current;

  public ReaderRestrictionTracker(ReaderRestriction initial) {
    this.current = initial;
  }

  @Override
  public boolean tryClaim(ReaderPosition readerPosition) {
    if (!current.consumers.contains(readerPosition.id())) {
      if (current.consumers.size() < current.maxConsumers) {
        current.consumers.add(readerPosition.id());
      } else {
        return false;
      }
    }
    return true;
  }



  // if (/* current.partitions.containsKey(readerPosition.partition())
  // && !current.partitions.get(readerPosition.partition()).booleanValue()
  // && */ current.totalNumRecords > readerPosition.totalConsumed()) {
  // current.totalConsumed = readerPosition.totalConsumed();
  // current.partitions.put(readerPosition.partition(), true);
  // return true;
  // } else {
  // return false;
  // }
  // }

  @Override
  public ReaderRestriction currentRestriction() {
    return current;
  }

  @Override
  public SplitResult<ReaderRestriction> trySplit(double fractionOfRemainder) {
    return SplitResult.of(null, current);
  }

  @Override
  public void checkDone() throws IllegalStateException {}

  @Override
  public IsBounded isBounded() {
    return IsBounded.UNBOUNDED;
  }
}
