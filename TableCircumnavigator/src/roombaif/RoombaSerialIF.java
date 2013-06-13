package roombaif;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.LinkedList;

/**
 * An interface to an iRobot Roomba 595 via a serial port.
 *
 * We assume the Roomba is set for 115200 baud, its default rate. Resetting the
 * Roomba (by turning it on and holding down the Spot and Dock buttons) should
 * reset it to 115200 baud.
 *
 * For further information, including the meaning of the sensor data packets,
 * consult the iRobot Roomba 500 Open Interface (OI) Specification.
 *
 * @author Braden Phillips
 */
public class RoombaSerialIF extends RoombaIF {

    /* Communication parameters */
    private static final int BAUDRATE = SerialPort.BAUDRATE_115200;
    private static final int DATABITS = SerialPort.DATABITS_8;
    private static final int STOPBITS = SerialPort.STOPBITS_1;
    private static final int PARITY = SerialPort.PARITY_NONE;

    /* Command packets */
    private static final byte START_COMMAND = (byte) 128;
    private static final byte SAFE_COMMAND = (byte) 131;
    private static final byte FULL_COMMAND = (byte) 132;
    private static final byte STREAM_COMMAND = (byte) 148;
    private static final byte PAUSE_STREAM_COMMAND = (byte) 150;
    private static final byte DRIVE_COMMAND = (byte) 137;
    private static final byte MOTORS_COMMAND = (byte) 137;

    /* Sensor stream header */
    private static final byte SENSOR_STREAM_HEADER = (byte) 19;

    /* Instance variables */
    private SerialPort serialPort;
    private List<Byte> rawSensorData;
    private int sensorDataLength;
    private AtomicBoolean watchdog;
    private Thread watchdogTimer;

    /**
     * Constructor
     *
     * @param portName serial port e.g. "COM4" or "/dev/ttyS0"
     * @param sensorPacketsRequested the sensor packets to stream from the
     * Roomba
     */
    public RoombaSerialIF(String portName, SensorPacket[] sensorPacketsRequested) {
        super(portName, sensorPacketsRequested);
        sensorDataLength = 3 + sensorPacketsRequested.length; //header, n-bytes, checksum, IDs
        for (SensorPacket s : sensorPacketsRequested) {
            sensorDataLength += s.length();
        }
        rawSensorData = new LinkedList<Byte>();
        watchdog = new AtomicBoolean(false);
    }

    /**
     * Open the serial interface to the Roomba. We place the Roomba into 
     * Safe or Full control mode and start the sensor data streaming.
     *
     */
    @Override
    public void openIF(boolean safeMode) throws RoombaIFException {
        serialPort = new SerialPort(portName);
        if (isOpened) {
            throw new RoombaIFException(RoombaIFException.TYPE_ALREADY_OPEN);
        }
        try {
            serialPort.openPort();
            serialPort.setParams(BAUDRATE, DATABITS, STOPBITS, PARITY);
            int mask = SerialPort.MASK_RXCHAR;
            serialPort.setEventsMask(mask);
            serialPort.addEventListener(new SerialPortReader());
            serialPort.writeByte(START_COMMAND); //passive mode
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                //Don't do anything. Nothing sends this thread an interrupt.
            }
            if (safeMode) {
                serialPort.writeByte(SAFE_COMMAND); //safe mode 
            } else {
                serialPort.writeByte(FULL_COMMAND); //full mode 
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                //Don't do anything. Nothing sends this thred an interrupt.
            }
            byte[] c = new byte[sensorPacketsRequested.length + 2];
            c[0] = STREAM_COMMAND;
            c[1] = (byte) sensorPacketsRequested.length;
            for (int i = 0; i < sensorPacketsRequested.length; i++) {
                c[i + 2] = sensorPacketsRequested[i].id();
            }
            serialPort.writeBytes(c);
        } catch (SerialPortException ex) {
            try {
                if (serialPort.isOpened()) {
                    serialPort.closePort();
                }
            } catch (SerialPortException ex2) {
                //Don't do anything. We are already going to throw an exception.
            }
            throw new RoombaIFException(RoombaIFException.TYPE_SERIAL + ": "
                    + ex.getMessage());
        }
        watchdogTimer = new Thread(new WatchdogTimer());
        watchdogTimer.start();
        isOpened = true;
    }

    /**
     * Close the interface to the Roomba and clean up resources.
     * 
     * @throws RoombaIFException 
     */
    @Override
    public void closeIF() throws RoombaIFException {
        checkIsOpened("closeIF");
        watchdogTimer.interrupt();//clean up the watchdog timer
        isOpened = false;
        byte[] c = {PAUSE_STREAM_COMMAND, (byte) 0, START_COMMAND};
        try {
            serialPort.writeBytes(c);// pause stream and return to passive mode
            serialPort.removeEventListener();
            serialPort.closePort();
        } catch (SerialPortException ex) {
            throw new RoombaIFException(RoombaIFException.TYPE_SERIAL + ": "
                    + ex.getMessage());
        }
    }

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
    @Override
    public void driveCommand(short velocity, short radius)
            throws RoombaIFException {
        checkIsOpened("driveCommand");
        if (velocity > 500) {
            velocity = 500;
        } else if (velocity < -500) {
            velocity = -500;
        }
        if ((radius > 2000) & (radius != 32767)) {
            radius = 2000;
        }
        if ((radius < -2000) & (radius != -32768)) {
            radius = -2000;
        }
        byte[] c = {DRIVE_COMMAND, highByte(velocity), lowByte(velocity),
            highByte(radius), lowByte(radius)};
        writeBytes(c);
    }

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
    @Override
    public void motorsCommand(boolean sideBrush, boolean sideBrushClockwise,
            boolean mainBrush, boolean mainBrushOutward, boolean vacuum)
            throws RoombaIFException {
        checkIsOpened("driveCommand");
        byte d = 0;
        d += ((sideBrush) ? 0 : 1);
        d += ((vacuum) ? 0 : 2);
        d += ((mainBrush) ? 0 : 4);
        d += ((sideBrushClockwise) ? 0 : 8);
        d += ((mainBrushOutward) ? 0 : 16);
        byte[] c = {MOTORS_COMMAND, d};
        writeBytes(c);
    }

    private byte highByte(short s) {
        return (byte) (s >>> 8);
    }

    private byte lowByte(short s) {
        return (byte) (s & 0xff);
    }

    private void writeBytes(byte[] b)
            throws RoombaIFException {
        try {
            serialPort.writeBytes(b);
        } catch (SerialPortException ex) {
            throw new RoombaIFException(RoombaIFException.TYPE_SERIAL + ": "
                    + ex.getMessage());
        }

    }

    /**
     * Receive and interpret sensor data streams from the Roomba and store the
     * results in sensorData.
     */
    private class SerialPortReader implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {
            if (!event.isRXCHAR()) {//Ensure data is avaiable
                return;
            }
            if (event.getEventValue() <= 0) {//Ensure there is data to read 
                return;
            }
            //Get the data from the serial port
            byte buffer[];
            try {
                buffer = serialPort.readBytes(event.getEventValue());
            } catch (SerialPortException ex) {
                System.err.println(ex);
                buffer = new byte[0];
            }
            //Buffer the data in rawSensorData
            for (byte b : buffer) {
                rawSensorData.add(b);
            }
            //Look for valid streams
            while (rawSensorData.size() >= sensorDataLength) {
                if (rawSensorData.get(0) != (byte) SENSOR_STREAM_HEADER) {//Quickly serch for a possible header, dumping anything ahead of it
                    rawSensorData.remove(0);
                } else if (checkRawSensorData()) {//A valid stream at the start of the buffer
//                    System.out.println("Found a good one!");
                    interpretRawSensorData();
                    sensorDataIsValid.set(true);
                    watchdog.set(true);
                    if (sensorPacketListenerAdded) {
                        sensorPacketListener.sensorPacketReceived();
                    }
                } else {//It was not a header after all
                    rawSensorData.remove(0);
                }
            }
        }
    }

    /**
     * Check if there is a valid sensor data stream at the start of the
     * rawSensorData.
     *
     * Beware: this is not synchronized and hence memory consistency could be an
     * issue. At present it is only called from within the SerialPortReader
     * thread, the only thread to write to rawSensorData and hence it should not
     * be a problem.
     *
     * @return true if the raw sensor data is a valid stream reads for
     * interpretation
     */
    private boolean checkRawSensorData() {
        if (rawSensorData.size() < sensorDataLength) {
//            System.out.println("Check Failed: too short");
            return false;
        }
        if (rawSensorData.get(0) != (byte) SENSOR_STREAM_HEADER) {
//            System.out.println("Check Failed: not header");
            return false;
        }
        if (rawSensorData.get(1) != sensorDataLength - 3) {
//            System.out.println("Check Failed: n-bytes incorrect");
            return false;
        }
        for (int j = 0; j < sensorDataLength; j++) {
//            System.out.println("byte: " + j + " value: " + rawSensorData.get(j));
        }
        int i = 2; // index of packet ID 1
        for (SensorPacket s : sensorPacketsRequested) {
            if (rawSensorData.get(i) != s.id()) {
//                System.out.println("Check Failed: packet id for " + s.id + "incorrect");
                return false;
            }
            i += s.length() + 1;
        }
        int checksum = 0;
        /* Desipte what it says in the Roomba documentation, the checksum calculated
         * by the Roomba actually includes the header byte (0x13).
         */
        for (int j = 0; j < sensorDataLength; j++) {
            checksum += (rawSensorData.get(j) & 0xff);
        }
        if ((checksum & 0xff) != 0) {
//            System.out.println("Check Failed: incorrect checksum computed " + checksum);
            return false;
        }
        return true;
    }

    /**
     * Interpret a valid raw sensor data stream and store the results as
     * correctly signed integers in sensorData. Removes the stream from the
     * rawSensorData buffer.
     *
     * Note: this is synchronized as it updates the shared sensorData. Any other
     * thread that reads the sensorData will do so via the getSensorData method
     * that will be synchronized with this one.
     */
    private synchronized void interpretRawSensorData() {
        int sensorDataIndex = 0;
        rawSensorData.remove(0); //The header
        rawSensorData.remove(0); //The n-bytes field
        for (SensorPacket s : sensorPacketsRequested) {
            rawSensorData.remove(0); //The packet ID field
            sensorData[sensorDataIndex] = 0;
            for (int i = 0; i < s.length(); i++) {
                sensorData[sensorDataIndex] *= 256;
                sensorData[sensorDataIndex] += rawSensorData.remove(0);
            }
            if (s.signed() && (sensorData[sensorDataIndex] >= 1 << (8 * s.length() - 1))) {
                sensorData[sensorDataIndex] -= 1 << (8 * s.length());
            }
            sensorDataIndex++;
        }
        rawSensorData.remove(0); //The checksum
    }

    private class WatchdogTimer implements Runnable {

        @Override
        public void run() {
            while (true) {
                watchdog.set(false);
                try {
                    Thread.sleep(65);
                } catch (InterruptedException ex) {
                    //System.out.println("Watchdog: whimper");
                    return;
                }
                if (watchdog.get() == false) {
                    //System.out.println("Watchdog: growl");
                    sensorDataIsValid.set(false);
                }
            }
        }
    }
}
