/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates.commands;

//import edu.wpi.first.wpilibj.AnalogChannel;
//import edu.wpi.first.wpilibj.AnalogModule;
//import edu.wpi.first.wpilibj.CANJaguar;
//import edu.wpi.first.wpilibj.Encoder;
//import edu.wpi.first.wpilibj.Joystick;
//import edu.wpi.first.wpilibj.Victor;
//import edu.wpi.first.wpilibj.can.CANTimeoutException;
//import edu.wpi.first.wpilibj.Gyro;
import com.sun.squawk.util.MathUtils;
import edu.wpi.first.wpilibj.templates.RobotMap;

/**
 *
 * @author Giang
 */
public class driveCommand extends CommandBase {
    //joystick and it's values...

    
    //rotation motor controller.
    //Victor frTurn = new Victor(1);
    //Victor flTurn = new Victor(2);
    //Victor rlTurn = new Victor(3);
    //Victor rrTurn = new Victor(4);
    //speed controller.
    //CANJaguar frAcc;
    //CANJaguar flAcc;
    //CANJaguar rlAcc;
    //CANJaguar rrAcc;
    //represent the desired speed and direction of travel
    //[0]= frontLeft, [1]=frontright,[2]=Rearright,[3]=Rearleft
    //double[] magnitude = new double[4];
    //double[] angle = new double[4];
    // double lastfrDrive, lastflDrive, lastrlDrive,lastrrDrive;//last speed? need this??
    //double[] lastAngle = new double[4];//last current angle...
    // frAngle,flAngle,rlAngle,rrAngle;//actual angles, not encoder values
    //constants...
    double L = 9.5;
    double W = 14.0;
    double R = Math.sqrt(MathUtils.pow(L, 2.0) + MathUtils.pow(W, 2.0));

    public driveCommand() {
        // Use requires() here to declare subsystem dependencies
        requires(DriveSubsystem);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
        swerveWithRotation(RobotMap.stick1.getX(), RobotMap.stick1.getY(), RobotMap.stick1.getTwist());
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return false;
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }

    protected void swerveWithRotation(double STR, double FWD, double RCW) {
        //convert to field-centric...
        double temp = FWD * Math.cos(GyroSubsystem.SendableGyro.getAngle()) + STR * Math.sin(GyroSubsystem.SendableGyro.getAngle());
        STR = -FWD * Math.sin(GyroSubsystem.SendableGyro.getAngle()) + STR * Math.cos(GyroSubsystem.SendableGyro.getAngle());
        FWD = temp;

        //Vector components...
        double A = STR - RCW * (L / R);
        double B = STR + RCW * (L / R);
        double C = FWD - RCW * (W / R);
        double D = FWD + RCW * (W / R);

        //temp speed for each wheel
        DriveSubsystem.magnitude[0] = Math.sqrt(MathUtils.pow(B, 2.0) + MathUtils.pow(C, 2.0));
        DriveSubsystem.magnitude[1] = Math.sqrt(MathUtils.pow(B, 2.0) + MathUtils.pow(D, 2.0));
        DriveSubsystem.magnitude[2] = Math.sqrt(MathUtils.pow(A, 2.0) + MathUtils.pow(D, 2.0));
        DriveSubsystem.magnitude[3] = Math.sqrt(MathUtils.pow(A, 2.0) + MathUtils.pow(C, 2.0));
        // temp angle for each wheel
        DriveSubsystem.angle[0] = MathUtils.atan2(B, C) * 180 / Math.PI;
        DriveSubsystem.angle[1] = MathUtils.atan2(B, D) * 180 / Math.PI;
        DriveSubsystem.angle[2] = MathUtils.atan2(A, D) * 180 / Math.PI;
        DriveSubsystem.angle[3] = MathUtils.atan2(A, C) * 180 / Math.PI;

        //normalize the speed...
        double max = DriveSubsystem.magnitude[0];
        if (DriveSubsystem.magnitude[1] > max) {
            max = DriveSubsystem.magnitude[1];
        }
        if (DriveSubsystem.magnitude[2] > max) {
            max = DriveSubsystem.magnitude[2];
        }
        if (DriveSubsystem.magnitude[3] > max) {
            max = DriveSubsystem.magnitude[3];
        }
        if (max > 1) {
            DriveSubsystem.magnitude[0] /= max;
            DriveSubsystem.magnitude[1] /= max;
            DriveSubsystem.magnitude[2] /= max;
            DriveSubsystem.magnitude[3] /= max;
        }
        adjustSpeedAndAngle();

        //saving angles...
        for (int i = 0; i <= 3; i++) {
            DriveSubsystem.lastAngle[i] = DriveSubsystem.lastAngle[i] + DriveSubsystem.angle[i];
        }
        //set motors speed for each wheel...
        DriveSubsystem.setWheel();

    }
    //reverse speed rather than turn >180
    //Cannot turn more than 180 on map...

    protected void adjustSpeedAndAngle() {
        for (int i = 0; i <= 3; i++) {
            DriveSubsystem.magnitude[i] = DriveSubsystem.magnitude[i] * MathUtils.pow(-1, (int) (DriveSubsystem.angle[i] / 180.0));
            DriveSubsystem.angle[i] = (DriveSubsystem.angle[i] % 180.0);
            if (DriveSubsystem.angle[i] + DriveSubsystem.lastAngle[i] > 180) {
                DriveSubsystem.angle[i] = DriveSubsystem.angle[i] - 180.0;
                DriveSubsystem.magnitude[i] *= -1;
            }
        }

    }

    protected void resetAngle() {
        for (int i = 0; i <= 3; i++) {
            DriveSubsystem.lastAngle[i] = 0.0;
        }

    }
}