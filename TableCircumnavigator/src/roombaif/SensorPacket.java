package roombaif;

/**
 * Sensor packets that can be requested in the stream.
 */
public enum SensorPacket {

    BUMPS_AND_WHEEL_DROPS((byte) 7, 1, false, "Bumps and Wheel Drops"),
    WALL((byte) 8, 1, false, "Wall"),
    CLIFF_LEFT((byte) 9, 1, false, "Cliff Left"),
    CLIFF_FRONT_LEFT((byte) 10, 1, false, "Cliff Front Left"),
    CLIFF_FRONT_RIGHT((byte) 11, 1, false, "Cliff Front Right"),
    CLIFF_RIGHT((byte) 12, 1, false, "Cliff Right"),
    VIRTUAL_WALL((byte) 13, 1, false, "Virtual Wall"),
    WHEEL_OVERCURRENTS((byte) 14, 1, false, "Wheel Overcurrents"),
    DIRT_DETECT((byte) 15, 1, false, "Dirt Detect"),
    INFRARED_CHARACTER_OMNI((byte) 17, 1, false, "Infrared Character Omni"),
    INFRARED_CHARACTER_LEFT((byte) 52, 1, false, "Infrared Character Left"),
    INFRARED_CHARACTER_RIGHT((byte) 53, 1, false, "Infrared Character Right"),
    BUTTONS((byte) 18, 1, false, "Buttons"),
    DISTANCE((byte) 19, 2, true, "Distance"),
    ANGLE((byte) 20, 2, true, "Angle"),
    CHARGING_STATE((byte) 21, 1, false, "Charging State"),
    VOLTAGE((byte) 22, 2, false, "Voltage"),
    CURRENT((byte) 23, 2, true, "Current"),
    TEMPERATURE((byte) 24, 2, true, "Temperature"),
    BATTERY_CHARGE((byte) 25, 2, false, "Battery Charge"),
    BATTERY_CAPACITY((byte) 26, 2, false, "Battery Capacity"),
    WALL_SIGNAL((byte) 27, 2, false, "Wall Signal"),
    CLIFF_LEFT_SIGNAL((byte) 28, 2, false, "Cliff Left Signal"),
    CLIFF_FRONT_LEFT_SIGNAL((byte) 29, 2, false, "Cliff Front Left Signal"),
    CLIFF_FRONT_RIGHT_SIGNAL((byte) 30, 2, false, "Cliff Front Right Signal"),
    CLIFF_RIGHT_SIGNAL((byte) 31, 2, false, "Cliff Right Signal"),
    CHARGING_SOURCES_AVAILABLE((byte) 34, 1, false, "Charging Sources Available"),
    OI_MODE((byte) 35, 1, false, "OI Mode"),
    SONG_NUMBER((byte) 36, 1, false, "Song Number"),
    SONG_PLAYING((byte) 37, 1, false, "Song Playing"),
    NUMBER_OF_STREAM_PACKETS((byte) 38, 1, false, "Number of Stream Packets"),
    REQUESTED_VELOCITY((byte) 39, 2, true, "Requested Velocity"),
    REQUESTED_RADIUS((byte) 40, 2, true, "Requested Radius"),
    REQUESTED_RIGHT_VELOCITY((byte) 41, 2, true, "Requested Right Velocity"),
    REQUESTED_LEFT_VELOCITY((byte) 42, 2, true, "Requested Left Velocity"),
    RIGHT_ENCODER_COUNTS((byte) 43, 2, false, "Right Encoder Counts"),
    LEFT_ENCODER_COUNTS((byte) 44, 2, false, "Left Encoder Counts"),
    LIGHT_BUMPER((byte) 45, 1, false, "Light Bumper"),
    LIGHT_BUMP_LEFT_SIGNAL((byte) 46, 2, false, "Light Bump Left"),
    LIGHT_BUMP_FRONT_LEFT_SIGNAL((byte) 47, 2, false, "Light Bump Front Left"),
    LIGHT_BUMP_CENTER_LEFT_SIGNAL((byte) 48, 2, false, "Light Bump Center Left"),
    LIGHT_BUMP_CENTER_RIGHT_SIGNAL((byte) 49, 2, false, "Light Bump Center Right"),
    LIGHT_BUMP_FRONT_RIGHT_SIGNAL((byte) 50, 2, false, "Light Bump Front Right"),
    LIGHT_BUMP_RIGHT_SIGNAL((byte) 51, 2, false, "Light Bump Right"),
    LEFT_MOTOR_CURRENT((byte) 54, 2, true, "Left Motor Current"),
    RIGHT_MOTOR_CURRENT((byte) 55, 2, true, "Right Motor Current"),
    MAIN_BRUSH_MOTOR_CURRENT((byte) 56, 2, true, "Main Brush Motor Current"),
    SIDE_BRUSH_MOTOR_CURRENT((byte) 57, 2, true, "Side Brush Motor Current"),
    STASIS((byte) 58, 1, false, "Stasis");
    private final byte id;
    private final int length;
    private final boolean signed;
    private final String description;

    SensorPacket(byte id, int length, boolean signed, String description) {
        this.id = id;
        this.length = length;
        this.signed = signed;
        this.description = description;
    }

    public byte id() {
        return id;
    }

    public int length() {
        return length;
    }

    public boolean signed() {
        return signed;
    }

    public String description() {
        return description;
    }
}
