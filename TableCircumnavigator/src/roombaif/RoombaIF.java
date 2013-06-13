package roombaif;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An interface to an iRobot Roomba 595.
 *
 * This abstract base class should be extended to realise the actual connection
 * to the Roomba. This has been done so that there can be multiple subclasses
 * with different connections: ostensibly one with a serial connection and one 
 * with a dummy connection for testing without a Roomba.
 * 
 * When the interface is opened, it places the Roomba in Full control mode. It
 * also requests the Roomba stream a list of sensor data packets every 15 ms.
 *
 * For further information, including the meaning of the sensor data packets,
 * consult the iRobot Roomba 500 Open Interface (OI) Specification.
 *
 * @author Braden Phillips
 */
public abstract class RoombaIF {

    /* Instance variables */
    protected String portName;
    protected boolean isOpened = false;
    protected SensorPacket[] sensorPacketsRequested;
    protected int[] sensorData;
    protected AtomicBoolean sensorDataIsValid;
    protected SensorPacketListener sensorPacketListener;
    protected boolean sensorPacketListenerAdded = false;

    /**
     * Constructor
     * 
     * @param portName 
     * @param sensorPacketsRequested the sensor packets to stream from the
     * Roomba
     */
    public RoombaIF(String portName, SensorPacket[] sensorPacketsRequested) {
        this.portName = portName;
        this.sensorPacketsRequested = java.util.Arrays.copyOf(sensorPacketsRequested,
                sensorPacketsRequested.length);
        sensorData = new int[sensorPacketsRequested.length];
        sensorDataIsValid = new AtomicBoolean(false);
    }

    /**
     * Get the name of the port passed to constructor.
     * 
     * @return portName
     */
    public String getPortName() {
        return portName;
    }
    
    /**
     * Check whether the port has been opened.
     * 
     * @return true if the port is open
     */
    public boolean checkIsOpened() {
        return isOpened;
    }

    /**
     * Get the list of sensor packets requested
     * 
     * @return SensorPacket[]
     */
    public SensorPacket[] getSensorPacketsRequested() {
        SensorPacket[] result = java.util.Arrays.copyOf(sensorPacketsRequested,
                sensorPacketsRequested.length);
        return result;
    }

    /**
     * Check whether the buffered sensor data is valid.
     * 
     * @return true if the sensor data in the buffer is valid
     */
    public boolean checkSensorDataIsValid() {
        return sensorDataIsValid.get();
    }

    /**
     * Get the latest sensor data streamed from the Roomba.
     *
     * @return the sensor data in the same order as the sensor packets
     * requested, converted to signed or unsigned integer data as appropriate
     */
    public synchronized int[] getSensorData() {
        int[] result;
        result = java.util.Arrays.copyOf(sensorData, sensorData.length);
        return result;
    }

    /**
     * Open the interface to the Roomba. Place the Roomba into Full control mode
     * and start the sensor data streaming.
     *
     * Must set isOpened to true.
     *
     * @param safeMode true for safe control mode, false for full control mode
     * @throws RoombaIFException 
     */
    public abstract void openIF(boolean safeMode) throws RoombaIFException;

    /**
     * Close the interface to the Roomba. Restore the Roomba to passive mode and
     * stop it streaming data.
     *
     * Must set isOpened to false.
     *
     * @throws RoombaIFException 
     */
    public abstract void closeIF() throws RoombaIFException;

    /**
     * Control the Roomba's drive wheels.<p>
     *
     * To drive straight use radi serialPort.purgePort(serialPort.PURGE_RXCLEAR
     * | serialPort.PURGE_TXCLEAR); us -32768 or 32767. To turn in place
     * clockwise use radius -1. To turn in place anticlockwise use radius 1.
     *
     * @param velocity in mm/s
     * @param radius in mm, positive is left, negative is right
     * @throws RoombaIFException
     */
    public abstract void driveCommand(short velocity, short radius)
            throws RoombaIFException;

    /**
     * Control the forward and backward motion of the main brush, side brush and
     * vacuum. When enabled these motors will run at full speed. (Variable speed
     * is possible with a PWM_MOTORS command, not yet implemented here.)
     *
     * @param sideBrush
     * @param sideBrushClockwise
     * @param mainBrush
     * @param mainBrushOutward
     * @param vacuum
     * @throws RoombaIFException
     */
    public abstract void motorsCommand(boolean sideBrush, boolean sideBrushClockwise,
            boolean mainBrush, boolean mainBrushOutward, boolean vacuum)
            throws RoombaIFException;

    /**
     * Add a listener with a sensorPacketReveived method to be called whenever a
     * good sensor packet is received. If the function is called more than once
     * the latest SensorPacketListener will replace any previously added
     * listeners.
     *
     * @param listener
     */
    public void addSensorPacketListener(SensorPacketListener listener) {
        sensorPacketListener = listener;
        sensorPacketListenerAdded = true;
    }

    /* Protected helper functions */
    /**
     * 
     * @param methodName
     * @throws RoombaIFException
     */
    protected void checkIsOpened(String methodName) throws RoombaIFException {
        if (!isOpened) {
            throw new RoombaIFException(RoombaIFException.TYPE_NOT_OPEN +
                    "in: " + methodName);
        }
    }

}