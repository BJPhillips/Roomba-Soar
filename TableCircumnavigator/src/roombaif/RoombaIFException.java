package roombaif;

public class RoombaIFException extends Exception {

    final public static String TYPE_NOT_OPEN = "Roomba interface not open";
    final public static String TYPE_ALREADY_OPEN = "Roomba interface is already open";
    final public static String TYPE_SERIAL = "Roomba serial interface exception";

    private String exceptionType;

    /**
     * Constructs an instance of
     * <code>RoombaIFException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public RoombaIFException(String exceptionType) {
        super(exceptionType);
    }
    
    public String getExceptionType() {
        return exceptionType;
    }
}
