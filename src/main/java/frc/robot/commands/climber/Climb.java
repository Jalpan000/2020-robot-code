/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands.climber;

import edu.wpi.first.wpilibj.command.CommandGroup;
import frc.robot.subsystems.Climber;

public class Climb extends CommandGroup {
  
  private final double HI = 22, LO = 0;

  public Climb() {
   addSequential(new LowerAllScrews(HI));
   addSequential(new DriveOnClimbBase());
   addSequential(new RaiseBackScrews(HI));
   addSequential(new DriveOnClimbBase());
   addSequential(new RaiseAllScrews(LO));
  }
}