// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix6.configs.CANdleConfiguration;
import com.ctre.phoenix6.controls.ColorFlowAnimation;
import com.ctre.phoenix6.controls.EmptyAnimation;
import com.ctre.phoenix6.controls.LarsonAnimation;
import com.ctre.phoenix6.controls.RainbowAnimation;
import com.ctre.phoenix6.controls.RgbFadeAnimation;
import com.ctre.phoenix6.controls.SolidColor;
import com.ctre.phoenix6.hardware.CANdle;
import com.ctre.phoenix6.signals.RGBWColor;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
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
  final static int startingIndex = 0; //Candle starting idx (first light)
  final static int endingIndex = 7; //Candle ending idx (last light)
  private final CommandXboxController controller = new CommandXboxController(0);
  private final RGBWColor kYellow = RGBWColor.fromHex("#F6E2AD").orElseThrow();
  private final RGBWColor kPink = RGBWColor.fromHex("#ed80e9").orElseThrow();

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();
    candle = new CANdle(4, "rio"); // CANdle object
    colorSelectionList = new SendableChooser<AnimationTypes>(); //A selection list in shuffleboard 

    config = new CANdleConfiguration(); //CANdle config -> Add more settings if using LED Strips
    config.LED.BrightnessScalar = 0.1;
    candle.getConfigurator().apply(config); //Apply config to the CANdle object

    //Add options to selection list by passing in the type of data the list takes in (AnimationTypes)
    colorSelectionList.setDefaultOption("None", AnimationTypes.None); //The option the list defaults to
    colorSelectionList.addOption("Red", AnimationTypes.Red);
    colorSelectionList.addOption("Blue", AnimationTypes.Blue);
    colorSelectionList.addOption("Rainbow", AnimationTypes.Rainbow);
    colorSelectionList.addOption("ColorFlow", AnimationTypes.ColorFlow);
    colorSelectionList.addOption("RGBFlow", AnimationTypes.RGBFade);
    colorSelectionList.addOption("Larson", AnimationTypes.Larson);
  }
  //Enum -> Red, Blue, None, Rainbow, ColorFlow, RGBFade
  private enum AnimationTypes {
      Red,
      Blue,
      None,
      Rainbow,
      ColorFlow,
      RGBFade,
      Larson
  }
  //________________________________________
  private void startLogging() {
    SmartDashboard.putBoolean("Started Logging?", true);
      String[] animationList = {"Red", "Blue", "None", "Rainbow", "ColorFlow", "RGBFade", "Larson"};
      SmartDashboard.putData("Color List", colorSelectionList);
      for (String animation : animationList) {
          SmartDashboard.putBoolean(animation, false); //Put all the fields in
      }
  }
  private void updateSpecificLogging(String state) {
      String[] animationList = {"Red", "Blue", "None", "Rainbow", "ColorFlow", "RGBFade", "Larson"};
      SmartDashboard.putBoolean(state, true);
      for (String animation : animationList) {
        if (!(animation.equals(state))) { //If the animation isn't the animation you sent in
          SmartDashboard.putBoolean(animation, false); //Set it to false since it SHOULD no longer be active
        }
      }

  }
  private void setEffect(AnimationTypes requestedState) {
    EmptyAnimation clearAnimation = new EmptyAnimation(startingIndex);
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
      
      case ColorFlow:
        updateSpecificLogging("ColorFlow");
        candle.setControl(new ColorFlowAnimation(startingIndex, endingIndex)
        .withSlot(startingIndex)
        .withColor(kYellow));
        break;
      
      case RGBFade:
        updateSpecificLogging("RGBFade");
        candle.setControl(new RgbFadeAnimation(startingIndex, endingIndex).withSlot(startingIndex));
        break;

      case Larson:
        updateSpecificLogging("Larson");
        candle.setControl(new LarsonAnimation(startingIndex, endingIndex).withSlot(startingIndex).withColor(kPink));
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
  @Override //Get list selection, compare to current, set effect if different
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
    controller.b().onTrue(Commands.sequence(
      Commands.runOnce(() -> setEffect(AnimationTypes.Red)),
      Commands.waitSeconds(1),
      Commands.runOnce(() -> setEffect(AnimationTypes.Blue)),
      Commands.waitSeconds(1),
      Commands.runOnce(() -> setEffect(AnimationTypes.Rainbow)),
      Commands.waitSeconds(1),
      Commands.runOnce(() -> setEffect(AnimationTypes.None))));
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
  @Override //Set effect to AnimationTypes.None, startLogging()
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
