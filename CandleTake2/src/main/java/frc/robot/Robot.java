// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix6.configs.CANdleConfiguration;
import com.ctre.phoenix6.controls.EmptyAnimation;
import com.ctre.phoenix6.controls.RainbowAnimation;
import com.ctre.phoenix6.controls.SolidColor;
import com.ctre.phoenix6.hardware.CANdle;
import com.ctre.phoenix6.signals.RGBWColor;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the Main.java file in the project.
 */
public class Robot extends TimedRobot {
  private Command m_autonomousCommand;

  private final RobotContainer m_robotContainer;
  private CANdle candle;
  private CANdleConfiguration config;
  AnimationTypes currentState;
  SendableChooser<AnimationTypes> colorSelectionList;
  final static int startingIndex = 0;
  static final int endingIndex = 7;
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();
    candle = new CANdle(4, "rio");
    colorSelectionList = new SendableChooser<AnimationTypes>();

    config = new CANdleConfiguration();
    config.LED.BrightnessScalar = 0.5;
    candle.getConfigurator().apply(config);

    colorSelectionList.setDefaultOption("None", AnimationTypes.None);
    colorSelectionList.addOption("Red", AnimationTypes.Red);
    colorSelectionList.addOption("Blue", AnimationTypes.Blue);
    colorSelectionList.addOption("Rainbow", AnimationTypes.Rainbow);
  }

  private enum AnimationTypes {
      Red,
      Blue,
      None,
      Rainbow
  }
  private void startLogging() {
      String[] animationList = {"Red", "Blue", "None", "Rainbow"};
      SmartDashboard.putData("Color List", colorSelectionList);
      for (String animation : animationList) {
          SmartDashboard.putBoolean(animation, false); //Put all the fields in
      }
  }
  private void updateSpecificLogging(String state) {
      String[] animationList = {"Red", "Blue", "None", "Rainbow"};
      SmartDashboard.putBoolean(state, true);
      for (String animation : animationList) {
        if (!(animation.equals(state))) { //If the animation isn't the animation you sent in
          SmartDashboard.putBoolean(animation, false); //Set it to false since it SHOULD no longer be active
        }
      }

  }
  private void setEffect(AnimationTypes requestedState) {
    EmptyAnimation clearAnimation = new EmptyAnimation(0);
    if (currentState != requestedState) {
      
      currentState = requestedState;
      switch(currentState) {
      case Red:
        candle.setControl(clearAnimation);
        updateSpecificLogging("Red");
        candle.setControl(new SolidColor(startingIndex, endingIndex)
        .withColor(new RGBWColor(360, 0, 0, 0)));
        break;

      case Blue:
        candle.setControl(clearAnimation);
        updateSpecificLogging("Blue");
        candle.setControl(new SolidColor(startingIndex, endingIndex)
        .withColor(new RGBWColor(0, 0, 360, 0)));
        break;
      

      case Rainbow:
        updateSpecificLogging("Rainbow");
        candle.setControl(new RainbowAnimation(startingIndex, endingIndex));
        break;
      
      default:
      case None:
        candle.setControl(clearAnimation);
        updateSpecificLogging("None");
        candle.setControl(new SolidColor(startingIndex, endingIndex)
        .withColor(new RGBWColor(0, 0, 0, 0))); 
        break;
      }
    }  
    return;
  }

  //_________________________________________
  @Override
  public void robotPeriodic() {
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
    CommandScheduler.getInstance().run();
    AnimationTypes selectedEffect = colorSelectionList.getSelected();
    if (selectedEffect != currentState) {
      setEffect(selectedEffect);
    }
  }
  @Override
  public void disabledInit() {}
  @Override
  public void disabledPeriodic() {}
  @Override
  public void autonomousInit() {
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();

    // schedule the autonomous command (example)
    if (m_autonomousCommand != null) {
      CommandScheduler.getInstance().schedule(m_autonomousCommand);
    }
  }
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
    setEffect(AnimationTypes.None);
    startLogging();
  }
  @Override
  public void teleopPeriodic() {}
  @Override
  public void testInit() {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
  }
  @Override
  public void testPeriodic() {}
  @Override
  public void simulationInit() {}
  @Override
  public void simulationPeriodic() {}
}
