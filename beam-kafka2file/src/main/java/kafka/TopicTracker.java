package kafka;

import org.apache.beam.sdk.transforms.splittabledofn.RestrictionTracker;
import org.apache.beam.sdk.transforms.splittabledofn.SplitResult;

class TopicTracker extends RestrictionTracker<TopicRestriction, TopicPosition> {
  TopicRestriction current;

  public TopicTracker(TopicRestriction initial) {
    this.current = initial;
  }

  @Override
  public boolean tryClaim(TopicPosition partition) {
    return current.tryClaim(partition);
  }

  @Override
  public TopicRestriction currentRestriction() {
    return current;
  }

  @Override
  public SplitResult<TopicRestriction> trySplit(double fractionOfRemainder) {
    return null;
  }

  @Override
  public void checkDone() throws IllegalStateException {}

  @Override
  public IsBounded isBounded() {
    return IsBounded.UNBOUNDED;
  }
}
