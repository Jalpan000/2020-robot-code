/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryUtil;

import frc.robot.subsystems.DriveTrain;

import com.ctre.phoenix.motion.TrajectoryPoint;
/**
 * Add your docs here.
 */
public class MercPathLoader {
    private static final String BASE_PATH_LOCATION = "/home/lvuser/deploy/trajectories/PathWeaver/output/";

    /**
     * @param pathName name + wpilib.json
     */
    public static List<TrajectoryPoint> loadPath(String pathName) {
        List<TrajectoryPoint> trajectoryPoints = new ArrayList<TrajectoryPoint>();
        List<Trajectory.State> trajectoryStates;
        Trajectory trajectory = null;

        try {
            Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(BASE_PATH_LOCATION + pathName);
            trajectory = TrajectoryUtil.fromPathweaverJson(trajectoryPath);
        } catch (IOException ex) {
            DriverStation.reportError("Unable to open trajectory: " + BASE_PATH_LOCATION + pathName, ex.getStackTrace());
            return null;
        }
        if (trajectory != null) {
            trajectoryStates = trajectory.getStates();
            Trajectory.State prevState = null;
            int prevTime = 0;
            double pos = 0.0;
            for(Trajectory.State state : trajectoryStates) {
                TrajectoryPoint point = new TrajectoryPoint();
                double heading, velocity;
                int time;

                //Time
                /*
                time = MercMath.secondsToMilliseconds(state.timeSeconds);
                point.timeDur = time - prevTime;
                prevTime = time;
                */
                point.timeDur = 20;

                //Velocity
                velocity = state.velocityMetersPerSecond;
                point.velocity = MercMath.inchesPerSecondToTicksPerTenth(velocity);
                //Distance
                if (prevState == null) {
                    point.position = 0.0;
                    point.zeroPos = true;
                } else {
                    double prevX, prevY, x, y;
                    prevX = prevState.poseMeters.getTranslation().getX();
                    prevY = prevState.poseMeters.getTranslation().getY();
                    x = state.poseMeters.getTranslation().getX();
                    y = state.poseMeters.getTranslation().getY();
                    pos += MercMath.distanceFormula(prevX, x, prevY, y);
                    state.poseMeters.getTranslation().getDistance(prevState.poseMeters.getTranslation());
                    point.position = MercMath.inchesToEncoderTicks(pos);
                    point.zeroPos = false;
                }
                prevState = state;
                //Heading
                heading = state.poseMeters.getRotation().getDegrees();
                point.auxiliaryPos = MercMath.degreesToPigeonUnits(heading); // heading stored as auxilliaryPos
                //PID Profile
                point.profileSlotSelect0 = DriveTrain.DRIVE_MOTION_PROFILE_SLOT;
                point.profileSlotSelect1 = DriveTrain.DRIVE_SMOOTH_TURN_SLOT;
                point.useAuxPID = true;
                //Says that point is not a last point
                point.isLastPoint = false;
                //Append point to point
                trajectoryPoints.add(point);
//                System.out.println("velocity: " + MercMath.inchesPerSecondToRevsPerMinute(state.velocityMetersPerSecond) +
//                                         " heading: " + state.poseMeters.getRotation().getDegrees() + 
//                                         " pos: " + pos + 
//                                         " state.pose2d.getTranslation.getX: " + state.poseMeters.getTranslation().getX() +
//                                         " state.pose2d.getTranslation.getY: " + state.poseMeters.getTranslation().getY()
//                );
            }
            trajectoryPoints.get(trajectoryPoints.size() - 1).isLastPoint = true;
//            for (TrajectoryPoint trajectoryPoint : trajectoryPoints) {
//                System.out.println("trajectoryPoint.velocity: " + trajectoryPoint.velocity +
//                                         " trajectoryPoint.headingDeg: " + trajectoryPoint.headingDeg + 
//                                         " trajectoryPoint.position: " + trajectoryPoint.position +
//                                         " trajectoryPoint.isLastPoint " + trajectoryPoint.isLastPoint
//                );
//            }
        }
        return trajectoryPoints;
    }
}
