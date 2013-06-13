package roombaif;

/**
 * An dummy interface to an imaginary iRobot Roomba 595.
 *
 * @author Braden Phillips
 */
public class RoombaDummyIF extends RoombaIF {

    private DummyInputFrame dummyInputFrame;

    /**
     * Constructor
     *
     * @param sensorPacketsRequested the sensor packets to stream from the
     * Roomba
     */
    public RoombaDummyIF(SensorPacket[] sensorPacketsRequested) {
        super("dummy", sensorPacketsRequested);
    }

    
    /**
     * Open the (dummy) serial interface to the Roomba. 
     *
     * @param safeMode ignored for the dummy interface
     * @throws RoombaIFException
     */
    @Override
    public void openIF(boolean safeMode) throws RoombaIFException {
        isOpened = true;
        dummyInputFrame = new DummyInputFrame(this);

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                dummyInputFrame.setVisible(true);
            }
        });

        System.out.println("Dummy port opened.");
    }

    /**
     * Close the (dummy) serial interface to the Roomba.
     * 
     * @throws RoombaIFException 
     */
    @Override
    public void closeIF() throws RoombaIFException {
        checkIsOpened("closeIF");
        dummyInputFrame.dispose();
        isOpened = false;
        System.out.println("Dummy port closed.");
    }

    /**
     * A dummy drive command that just prints its parameters.
     * 
     * @param velocity
     * @param radius
     * @throws RoombaIFException 
     */
    @Override
    public void driveCommand(short velocity, short radius)
            throws RoombaIFException {
        checkIsOpened("driveCommand");
        System.out.println("Dummy drive command sent.");
        System.out.println("    Velocity: " + velocity);
        System.out.println("    Radius: " + radius);
    }

    /**
     * A dummy motors command that just prints its parameters.    
     * 
     * @param sideBrush
     * @param sideBrushClockwise
     * @param mainBrush
     * @param mainBrushOutward
     * @param vacuum
     * @throws RoombaIFException 
     */
    @Override
    public void motorsCommand(boolean sideBrush, boolean sideBrushClockwise,
            boolean mainBrush, boolean mainBrushOutward, boolean vacuum)
            throws RoombaIFException {
        checkIsOpened("driveCommand");
        System.out.println("Dummy motors command sent.");
        System.out.println("    Side Brush: " + sideBrush);
        System.out.println("    Side Brush Clockwise: " + sideBrushClockwise);
        System.out.println("    Main Brush: " + mainBrush);
        System.out.println("    Main Brush Outward: " + mainBrushOutward);
        System.out.println("    Vacuum: " + vacuum);
    }

    /**
     * Buffer sensor values and indicate to the sensor packet listener that new
     * values have arrived.
     * 
     * @param values 
     */
    protected synchronized void setSensorData(int[] values) {
        if (values.length != sensorData.length) {
            return;
        }
        System.arraycopy(values, 0, sensorData, 0, values.length);
        sensorDataIsValid.set(true);
        if (sensorPacketListenerAdded) {
            sensorPacketListener.sensorPacketReceived();
        }
    }
}
