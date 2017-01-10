package org.firstinspires.ftc.teamcode.Autonomous.OpModes.TestFiles;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;
/**
 * Created by sopa on 11/18/16.
 */
@Autonomous(name = "TestColor", group = "Autonomous")
public class TestColor extends LinearOpMode
{
    ColorSensor colorSensorWL;
    ColorSensor colorSensorWLA;
    ColorSensor colorSensorBeacon;
    public double colorSensorAverageValues(ColorSensor colorsensor) throws InterruptedException
    {
        double average = (colorsensor.red() + colorsensor.blue() + colorsensor.green())/3.0;
        return average;
    }
    @Override
    public void runOpMode() throws InterruptedException
    {
        //final View relativeLayout = ((Activity) hardwareMap.appContext).findViewById(com.qualcomm.ftcrobotcontroller.R.id.RelativeLayout);
        colorSensorWL = hardwareMap.colorSensor.get("cSWL");
        colorSensorWL.setI2cAddress(I2cAddr.create8bit(0x2a));
        colorSensorWL.enableLed(true);
        telemetry.addData("colorSensorWL", "initialized");
        colorSensorWLA = hardwareMap.colorSensor.get("cSWA");
        colorSensorWL.setI2cAddress(I2cAddr.create8bit(0x2e));
        telemetry.addData("colorSensorWLA", "initialized");
        colorSensorBeacon = hardwareMap.colorSensor.get("cSB");
        colorSensorBeacon.setI2cAddress(I2cAddr.create8bit(0x3c));
        telemetry.addData("colorSensorB", "initialized");
        telemetry.update();
        waitForStart();
        while(true)
        {
            telemetry.addData("cAverage", colorSensorAverageValues());
            telemetry.update();
            sleep(500);
            idle();
        }
    }
}
