package tablecircumnavigator;

import java.io.OutputStreamWriter;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.kernel.SoarException;
import org.jsoar.kernel.io.quick.*;
import org.jsoar.util.commands.*;
import org.jsoar.kernel.io.beans.*;

import roombaif.*;

/**
 * This simple example demonstrates how to:
 * + launch an agent;
 * + connect to a Roomba;
 * + connect the Roomba's sensors to the agent's input link using Quick Memory;
 * + and send commands from the agent's output link to the Roomba using SoarBeans.
 * 
 * @author Braden Phillips
 */
public class TableCircumnavigator {

    static private RoombaIF roomba;             // An interface to a Roomba
    static private ThreadedAgent navigator;     // A Soar agent running in its own thread
    static private QMemory qmemory;             // Quick memory interface to agent input-link
    static private boolean shuttingDown = false;

    /**
     * @param args the command line arguments
     * @throws SoarException
     * @throws InterruptedException
     * @throws java.io.IOException 
     * @throws RoombaIFException  
     */
    public static void main(String[] args)
            throws SoarException, InterruptedException, java.io.IOException,
            RoombaIFException {

        if (args.length != 1) {
            System.out.println("USAGE: TableCircumnavigator serial_port");
            System.out.println();
            System.out.println("e.g. for Windows, serial_port = COM1");
            System.out.println("     for Linux, serial_port = /dev/ttyS0");
            System.out.println("     for a simulated roomba, serial_port = dummy");
            return;
        }

        // Create a new Soar agent to run in its own thread
        openSoar();

        // Load some Soar productions
        SoarCommands.source(navigator.getInterpreter(), "soar/table-circumnavigator.soar");

        // Connect to the Roomba
        openRoomba(args[0]);

        // Launch the debugger - the agent can be run from there
        navigator.openDebuggerAndWait();
        // Start the agent running. Returns immediately.
        // navigator.runForever();

        System.out.println();
        System.out.println("Press enter to exit...");
        System.in.read();
        shuttingDown = true;
        navigator.dispose();
        roomba.driveCommand((short) 0, (short) 0);
        roomba.closeIF();
        System.out.println("Done.");
    }

    private static class DriveCommandHandler implements SoarBeanOutputHandler<DriveCommand> {

        @Override
        public void handleOutputCommand(SoarBeanOutputContext context, DriveCommand driveCommand) {
            if (shuttingDown) {
                return;
            }
            short velocity = (short) driveCommand.velocity;
            short radius = (short) driveCommand.radius;
            context.setStatus("complete");//place a ^status complete annotation on the command
            System.out.println();
            System.out.println("Received a drive command: ");
            System.out.println("    Velocity: " + velocity);
            System.out.println("    Radius: " + radius);

            try {
                roomba.driveCommand(velocity, radius);
            } catch (RoombaIFException ex) {
                System.err.println(ex);
            }
        }
    }

    private static class SensorPacketReceiver implements SensorPacketListener {

        private final int BumpRight = 0x1;
        private final int BumpLeft = 0x2;
        private final int WheelDropRight = 0x4;
        private final int WheelDropLeft = 0x8;

        @Override
        public void sensorPacketReceived() {
            if (shuttingDown) {
                return;
            }
            int[] sensorData = roomba.getSensorData();
            qmemory.setString("bump[0].location", "right");
            qmemory.setInteger("bump[0].value", ((sensorData[0] & BumpRight) > 0) ? 1 : 0);
            qmemory.setString("bump[1].location", "left");
            qmemory.setInteger("bump[1].value", ((sensorData[0] & BumpLeft) > 0) ? 1 : 0);
            qmemory.setString("wheel-drop[0].location", "right");
            qmemory.setInteger("wheel-drop[0].value", ((sensorData[0] & WheelDropRight) > 0) ? 1 : 0);
            qmemory.setString("wheel-drop[1].location", "left");
            qmemory.setInteger("wheel-drop[1].value", ((sensorData[0] & WheelDropLeft) > 0) ? 1 : 0);
            qmemory.setString("cliff[0].location", "left");
            qmemory.setInteger("cliff[0].value", sensorData[1]);
            qmemory.setString("cliff[1].location", "front-left");
            qmemory.setInteger("cliff[1].value", sensorData[2]);
            qmemory.setString("cliff[2].location", "front-right");
            qmemory.setInteger("cliff[2].value", sensorData[3]);
            qmemory.setString("cliff[3].location", "right");
            qmemory.setInteger("cliff[3].value", sensorData[4]);
            qmemory.setInteger("stasis.value", sensorData[5]);
//            System.out.println("Received a sensor packet");
        }
    }

    private static void openRoomba(String port) throws RoombaIFException {
        SensorPacket[] sensorPackets = {
            SensorPacket.BUMPS_AND_WHEEL_DROPS,
            SensorPacket.CLIFF_LEFT,
            SensorPacket.CLIFF_FRONT_LEFT,
            SensorPacket.CLIFF_FRONT_RIGHT,
            SensorPacket.CLIFF_RIGHT,
            SensorPacket.STASIS
        };

        if (port.equalsIgnoreCase("dummy")) {
            roomba = new RoombaDummyIF(sensorPackets);

        } else {
            roomba = new RoombaSerialIF(port, sensorPackets);
        }
        roomba.openIF(true);
        roomba.addSensorPacketListener(new SensorPacketReceiver());
    }

    private static void openSoar() {
        navigator = ThreadedAgent.create();
        navigator.initialize();
        navigator.setName("Table Circumnavigator");
        navigator.getPrinter().pushWriter(new OutputStreamWriter(System.out));
        // Use the Quick Input framework for input to the agent
        // https://github.com/soartech/jsoar/wiki/JSoarInput
        qmemory = DefaultQMemory.create();
        SoarQMemoryAdapter adapter = SoarQMemoryAdapter.attach(
                navigator.getInputOutput(), null, qmemory);
        // Handle output commands from the agent.
        final SoarBeanOutputManager manager = new SoarBeanOutputManager(navigator.getEvents());
        final SoarBeanOutputHandler<DriveCommand> driveCommandHandler = new DriveCommandHandler();
        manager.registerHandler("drive", driveCommandHandler, DriveCommand.class);
    }
}
