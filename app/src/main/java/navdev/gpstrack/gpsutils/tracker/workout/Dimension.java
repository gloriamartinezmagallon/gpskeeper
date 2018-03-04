package navdev.gpstrack.gpsutils.tracker.workout;

import navdev.gpstrack.R;
import navdev.gpstrack.gpsutils.utils.Constants;

public enum Dimension {

    TIME(Constants.DIMENSION.TIME, R.string.tiempo),
    DISTANCE(Constants.DIMENSION.DISTANCE, R.string.distancia),
    SPEED(Constants.DIMENSION.SPEED, R.string.velocidad),
    PACE(Constants.DIMENSION.PACE, R.string.paso);

    // TODO
    public static final boolean SPEED_CUE_ENABLED = true;

    int value = 0;
    int textId = 0;

    private Dimension(int val, int textId) {
        this.value = val;
        this.textId = textId;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    public int getTextId() {
        return textId;
    }

    public boolean equal(Dimension what) {
        if (what == null || what.value != this.value)
            return false;
        return true;
    }

    public static Dimension valueOf(int val) {
        switch(val) {
            case -1:
                return null;
            case Constants.DIMENSION.TIME:
                return TIME;
            case Constants.DIMENSION.DISTANCE:
                return DISTANCE;
            case Constants.DIMENSION.SPEED:
                return SPEED;
            case Constants.DIMENSION.PACE:
                return PACE;
            default:
                return null;
        }
    }
}

