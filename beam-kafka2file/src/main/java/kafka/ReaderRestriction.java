package kafka;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.beam.sdk.transforms.splittabledofn.HasDefaultTracker;

class ReaderRestriction
    implements Serializable, HasDefaultTracker<ReaderRestriction, ReaderRestrictionTracker> {
  long totalNumRecords;
  long totalConsumed=0;
  Set<Integer> consumers=new HashSet<>();
  int maxConsumers;
  // Map<Integer, Boolean> partitions;

  ReaderRestriction(long totalNumRecords, int maxConsumers
      /* Map<Integer, Boolean> partitions */) {
    this.totalNumRecords = totalNumRecords;
    // this.partitions = partitions;
    this.maxConsumers = maxConsumers;
  }

  @Override
  public ReaderRestrictionTracker newTracker() {
    return new ReaderRestrictionTracker(this);
  }

  @Override
  public boolean equals(Object obj) {
    return this.totalNumRecords == ((ReaderRestriction) obj).totalNumRecords
        && this.totalConsumed == ((ReaderRestriction) obj).totalConsumed
        // && this.partitions.equals(((ReaderRestriction) obj).partitions);
        && this.consumers.equals(((ReaderRestriction) obj).consumers);
  }

  @Override
  public int hashCode() {
    return ((int) this.totalNumRecords) * ((int) this.totalConsumed) * this.consumers.hashCode();
    // * this.partitions.hashCode();
  }
}
