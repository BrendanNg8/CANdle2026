// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

import com.ctre.phoenix6.configs.CANdleConfiguration;
import com.ctre.phoenix6.controls.*;
import com.ctre.phoenix6.hardware.CANdle;
import com.ctre.phoenix6.signals.AnimationDirectionValue;
import com.ctre.phoenix6.signals.RGBWColor;
import com.ctre.phoenix6.signals.StatusLedWhenActiveValue;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the Main.java file in the project.
 */
public class Robot extends TimedRobot {
  private Command m_autonomousCommand;

  private final RobotContainer m_robotContainer;
  private final int STARTING_INDEX = 0;
  private final int ENDING_INDEX = 7;
  private final CANdle candle = new CANdle(4, "rio"); //Adjust ID
  private static final RGBWColor kBlue = RGBWColor.fromHex("#04D9FF").orElseThrow();
  private static final RGBWColor kRed = RGBWColor.fromHex("#FC1723").orElseThrow();
  private AnimationType state1 = AnimationType.Rainbow;
  private CommandXboxController controller = new CommandXboxController(0);

  private final SendableChooser<AnimationType> ledModeChooser1 = new SendableChooser<AnimationType>();
  
  public enum AnimationType {
        None,
        Rainbow,
        SolidBlue,
        SolidRed,
        Fire,
        Twinkle,
        TestColor
  }

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();
    var cfg = new CANdleConfiguration();
    cfg.LED.BrightnessScalar = 0.5;
    cfg.CANdleFeatures.StatusLedWhenActive = StatusLedWhenActiveValue.Disabled; // Turn off status LED
    candle.getConfigurator().apply(cfg);


    ledModeChooser1.setDefaultOption("Rainbow", AnimationType.Rainbow);
    ledModeChooser1.addOption("None", AnimationType.None);
    ledModeChooser1.addOption("SolidBlue", AnimationType.SolidBlue);
    ledModeChooser1.addOption("SolidRed", AnimationType.SolidRed);
    ledModeChooser1.addOption("Fire", AnimationType.Fire);
    ledModeChooser1.addOption("Twinkle", AnimationType.Twinkle);
    ledModeChooser1.addOption("TestColor", AnimationType.TestColor);
  }
  public void setLedMode(AnimationType newMode) {
    candle.setControl(new SolidColor(STARTING_INDEX, ENDING_INDEX).withColor(new RGBWColor(0, 0, 0, 0)));
    updateLogging("Enter all fields", null);
    SmartDashboard.putData("Current State", ledModeChooser1);
    if (state1 != newMode) {
        state1 = newMode; 
        switch(state1) {
          
          case SolidBlue:
              candle.setControl(new SolidColor(STARTING_INDEX, ENDING_INDEX).withColor(kBlue));
              updateLogging("Disable everything besides this animation", "SolidBlue");
              break;

          case SolidRed:
              candle.setControl(new SolidColor(STARTING_INDEX, ENDING_INDEX).withColor(kRed));
              updateLogging("Disable everything besides this animation", "SolidRed");
              break;
            
          case Fire:
              candle.setControl(new FireAnimation(STARTING_INDEX, ENDING_INDEX));
              updateLogging("Disable everything besides this animation", "Fire");
              break;

          case TestColor:
              candle.setControl(new SolidColor(STARTING_INDEX, ENDING_INDEX).withColor(new RGBWColor(100, 0, 0, 0)));
              updateLogging("Disable everything besides this animation", "TestColor");
              break;

          case None:
              candle.setControl(new SolidColor(STARTING_INDEX, ENDING_INDEX).withColor(new RGBWColor(0, 0, 0, 0)));
              updateLogging("Disable everything besides this animation", "None");
              break;

          case Rainbow:
              candle.setControl(new RainbowAnimation(STARTING_INDEX, ENDING_INDEX).withSlot(0)
              .withDirection(AnimationDirectionValue.Backward));
              updateLogging("Disable everything besides this animation", "Rainbow");
              break;
          default:
              SmartDashboard.putBoolean("NOT WORKING", false);
          }

    }
    return; 
}
public void updateLogging(String task, String animation) {
  String[] animationlist = {"Rainbow", "SolidBlue", "SolidRed", "Fire", "TestColor", "None"};
  if (task.equals("Disable everything besides this animation")) {
      for (String color : animationlist) {
          if (!(color.equals(animation))) {
            SmartDashboard.putBoolean(color, false);
          }
      }
      SmartDashboard.putBoolean(animation, true);
  }
  else{
    for (String color : animationlist) {
      SmartDashboard.putBoolean(color, false);
    }
  }
}



  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
    CommandScheduler.getInstance().run();
    
    AnimationType selectedMode = ledModeChooser1.getSelected();

    if (state1 != selectedMode) {
        setLedMode(selectedMode);
    }
    
    controller.a().onTrue(Commands.runOnce(() -> setLedMode(AnimationType.TestColor)));
    controller.x().onTrue(Commands.runOnce(() -> setLedMode(AnimationType.Rainbow)));
    controller.y().onTrue(Commands.runOnce(() -> setLedMode(AnimationType.None)));
  }

  /** This function is called once each time the robot enters Disabled mode. */
  @Override
  public void disabledInit() {
    //effectActive = false;
    //setLedMode(AnimationType.None);
    //setLedMode(AnimationType.None);
  }

  @Override
  public void disabledPeriodic() {
  }

  /** This autonomous runs the autonomous command selected by your {@link RobotContainer} class. */
  @Override
  public void autonomousInit() {
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();

    // schedule the autonomous command (example)
    if (m_autonomousCommand != null) {
      CommandScheduler.getInstance().schedule(m_autonomousCommand);
    }
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
    //if (!effectActive) {
      //setLedMode(AnimationType.Fire);
      //effectActive = true;
    //}
    //setLedMode(AnimationType.TestColor);
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    //setLedMode(AnimationType.TestColor);
  }

  @Override
  public void testInit() {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
    
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
