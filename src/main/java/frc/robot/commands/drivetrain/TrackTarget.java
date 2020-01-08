/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands.drivetrain;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.FollowerType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Robot;
import frc.robot.subsystems.DriveTrain;
import frc.robot.subsystems.DriveTrain.DriveTrainSide;
import frc.robot.util.MercMath;

public class TrackTarget extends MoveHeading {
    private double allowableDistError = 19; //inches

    public TrackTarget() {
        super(0, 0);
        setName("TrackTarget MoveHeading Command");
    }

    // Called just before this Command runs the first time
    @Override
    protected void initialize() {
        super.initialize();
        Robot.driveTrain.configPIDSlots(DriveTrainSide.RIGHT, DriveTrain.DRIVE_PID_SLOT, DriveTrain.DRIVE_SMOOTH_MOTION_SLOT);
        Robot.driveTrain.configClosedLoopPeakOutput(DriveTrain.DRIVE_PID_SLOT, .3);
        Robot.driveTrain.configClosedLoopPeakOutput(DriveTrain.DRIVE_SMOOTH_MOTION_SLOT, .25);
    }

    // Called repeatedly when this Command is scheduled to run
    @Override
    protected void execute() {
        double adjustedDistance = MercMath.feetToEncoderTicks(Robot.driveTrain.getLimelight().getRawVertDistance() - allowableDistError);
        //adjustedDistance *= Robot.driveTrain.getDirection().dir;
        double adjustedHeading = MercMath.degreesToPigeonUnits(Robot.driveTrain.getLimelight().getTargetCenterXAngle());
        right.set(ControlMode.Position, adjustedDistance, DemandType.AuxPID, adjustedHeading);
        left.follow(right, FollowerType.AuxOutput1);
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    protected boolean isFinished() {
        if (initialCheckCount < checkThreshold) {
            initialCheckCount++;
            return false;
        }

        double distError = MercMath.inchesToEncoderTicks(Robot.driveTrain.getLimelight().getRawVertDistance() - allowableDistError),
                angleError = MercMath.degreesToPigeonUnits(Robot.driveTrain.getLimelight().getTargetCenterXAngle());

        angleError = MercMath.pigeonUnitsToDegrees(angleError);
        distError *= Robot.driveTrain.getDirection().dir;

        SmartDashboard.putString("ang error, dist error", angleError + ", " + distError);

        System.out.println(distError);

        boolean isFinished = false;

        boolean isOnTarget = (Math.abs(distError) < moveThresholdTicks &&
                Math.abs(angleError) < angleThresholdDeg &&
                Robot.driveTrain.getLimelight().isSafeToTrack());

        if (isOnTarget) {
            onTargetCount++;
        } else {
            if (onTargetCount > 0)
                onTargetCount = 0;
        }

        if (onTargetCount > onTargetMinCount) {
            isFinished = true;
            onTargetCount = 0;
        }

        return isFinished;
    }

    // Called once after isFinished returns true
    @Override
    protected void end() {
        Robot.driveTrain.configClosedLoopPeakOutput(DriveTrain.DRIVE_PID_SLOT, .75);
        Robot.driveTrain.configClosedLoopPeakOutput(DriveTrain.DRIVE_SMOOTH_MOTION_SLOT, 1.0);
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    @Override
    protected void interrupted() {
        this.end();
    }
}
