package kafka;

import java.io.Serializable;
import com.google.auto.value.AutoValue;

@AutoValue
abstract class ReaderPosition implements Serializable {
  // abstract long totalConsumed();
  abstract int id();

  static ReaderPosition of(/* long totalConsumed, */ int id) {
    return new AutoValue_ReaderPosition(/* totalConsumed, */ id);
  }
}
