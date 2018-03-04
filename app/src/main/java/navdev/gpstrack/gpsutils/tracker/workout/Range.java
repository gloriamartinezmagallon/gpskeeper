package navdev.gpstrack.gpsutils.tracker.workout;

public class Range {

    public double minValue;
    public double maxValue;

    public Range(double minValue, double maxValue) {
        if (minValue <= maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        } else {
            this.minValue = maxValue;
            this.maxValue = minValue;
        }
    }

    public boolean inside(double d) {
        return compare(d) == 0;
    }

    public int compare(double value) {
        if (value < minValue)
            return -1;
        if (value > maxValue)
            return 1;
        return 0;
    }

    public boolean contentEquals(Range range) {
        return this.maxValue == range.maxValue && this.minValue == range.minValue;
    }

    public String toString() {
        return "[ " + minValue + " - " + maxValue + " ]";
    }
}
