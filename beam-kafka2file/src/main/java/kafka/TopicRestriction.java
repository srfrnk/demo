package kafka;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.apache.beam.sdk.transforms.splittabledofn.HasDefaultTracker;

class TopicRestriction
    implements Serializable, HasDefaultTracker<TopicRestriction, TopicTracker> {
  HashSet<TopicPosition> unclaimed;
  HashSet<TopicPosition> claimed;

  public TopicRestriction(Set<TopicPosition> partitions) {
    if (partitions == null) {
      partitions = new HashSet<>();
    }
    this.unclaimed = new HashSet<>(partitions);
    claimed = new HashSet<>();
  }

  @Override
  public TopicTracker newTracker() {
    return new TopicTracker(this);
  }

  @Override
  public boolean equals(Object obj) {
    return this.unclaimed.equals(((TopicRestriction) obj).unclaimed)
        && this.claimed.equals(((TopicRestriction) obj).claimed);
  }

  @Override
  public int hashCode() {
    return unclaimed.hashCode() * claimed.hashCode();
  }

  synchronized boolean tryClaim(TopicPosition partition) {
    if (unclaimed.contains(partition)) {
      unclaimed.remove(partition);
      claimed.add(partition);
      return true;
    } else {
      return false;
    }
  }
}
