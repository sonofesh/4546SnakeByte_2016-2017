package org.firstinspires.ftc.teamcode.Autonomous.OpModes;

import android.graphics.Color;
import android.util.Range;

import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.hardware.adafruit.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

/**
 * Created by sopa on 11/28/16.
 * Information: turnRight is negative whereas turnLeft is positive
 */
public abstract class AutoOpMode extends LinearOpMode {
    DcMotor FR;
    DcMotor BR;
    DcMotor FL;
    DcMotor BL;
    DcMotor ShooterB;
    DcMotor ShooterF;
    DcMotor ManLift;
    DcMotor ManIn;
    Servo Beacon;
    //average encoder value
    int beforeALV = 0;
    double beforeAngle = 2;
    final double whiteACV = 27;
    final double CORRECTION = .02;
    int FRV = 0;
    int FLV = 0;
    int avg = 0;
    long beforeTime = 0;
    long currentTime = 0;
    public BNO055IMU imu;
    BNO055IMU.Parameters parameters;
    ColorSensor colorSensorWL;
    ColorSensor colorSensorBlueBeacon;
    ColorSensor colorSensorRedBeacon;
    public void initialize() throws InterruptedException {
        FR = hardwareMap.dcMotor.get("FR");
        BR = hardwareMap.dcMotor.get("BR");
        FL = hardwareMap.dcMotor.get("FL");
        BL = hardwareMap.dcMotor.get("BL");
        ShooterB = hardwareMap.dcMotor.get("B");
        ShooterF = hardwareMap.dcMotor.get("F");
        ManLift = hardwareMap.dcMotor.get("ManLift");
        ManIn = hardwareMap.dcMotor.get("ManIn");
        Beacon = hardwareMap.servo.get("Beacon");
        Beacon.setPosition(0);
        FR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        FL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ManLift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        FR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        FL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        ManLift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        telemetry.addData("gyro", "initalizing");
        telemetry.update();
        parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "AdafruitIMUCalibration.json"; // see the calibration sample opmode0
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();
        imu = hardwareMap.get(BNO055IMU.class, "IMU");
        imu.initialize(parameters);
        telemetry.addData("gyro", "initalized");
        colorSensorWL = hardwareMap.colorSensor.get("cSWL");
        colorSensorWL.setI2cAddress(I2cAddr.create8bit(0x2a));
        telemetry.addData("colorSensorL", "initalized");
        colorSensorBlueBeacon = hardwareMap.colorSensor.get("cSB");
        colorSensorBlueBeacon.setI2cAddress(I2cAddr.create8bit(0x3c));
        telemetry.addData("colorSensorB", "initalized");
        telemetry.update();
        //telemetry.addData("test1", "initalized");

    }
    //movement methods
    public void zero() throws InterruptedException {
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
    }

    public void moveForward(double power) throws InterruptedException {
        FR.setPower(power);
        BR.setPower(power);
        FL.setPower(-power);
        BL.setPower(-power);
    }

    public void moveBackward(double power) throws InterruptedException {
        moveForward(-power);
    }

    public void turnRight(double power) throws InterruptedException {
        FR.setPower(power);
        BR.setPower(power);
        FL.setPower(power);
        BL.setPower(power);
    }

    public void turnLeft(double power) throws InterruptedException {
        turnRight(-power);
    }

    //Shooter
    public void bringDownShooter(double power, int distance) throws InterruptedException {
        int beforePos = Math.abs(ManLift.getCurrentPosition());
        telemetry.addData("ManLift", ManLift.getCurrentPosition());
        telemetry.update();
        while (Math.abs(ManLift.getCurrentPosition() - beforePos) < distance) {
            ManLift.setPower(power);
            idle();
        }
        ManLift.setPower(0);
        telemetry.addData("ManLift", ManLift.getCurrentPosition());
        telemetry.update();
    }

    /**
     * The entire shooting process can be hastened with the addition of two servos that
     * would reduce the overall bringDownShooter() distance
     * @param power
     * @param distance
     * @throws InterruptedException
     */
    public void shoot(double power, int distance) throws InterruptedException {
        int beforeManLift = ManLift.getCurrentPosition();
        ShooterF.setPower(power);
        ShooterB.setPower(-power);
        bringDownShooter((.4 * -1), (distance + (Math.abs(beforeManLift - 1100))));
        sleep(1000);
        beforeTime = System.currentTimeMillis();
        while(Math.abs(System.currentTimeMillis() - beforeTime) < 1000)
            idle();
        ManIn.setPower(-.15);
        beforeTime = System.currentTimeMillis();
        while(Math.abs(System.currentTimeMillis() - beforeTime) < 1000)
            idle();
        ManIn.setPower(0);
        ShooterF.setPower(0);
        ShooterB.setPower(0);
        ManIn.setPower(0);
    }

    //Sensor methods

    //color sensor
    public double colorSensorAverageValues(ColorSensor sensor) throws InterruptedException {
        double average = (sensor.red() + sensor.blue() + sensor.green())/3.0;
        return average;
    }
    public double colorSensorRed(ColorSensor sensor) throws InterruptedException {
        return colorSensorBlueBeacon.red();
    }
    public double colorSensorBlue(ColorSensor sensor) throws InterruptedException {
        return colorSensorBlueBeacon.blue();
    }

    //gyro methods
    public float getGyroYaw() throws InterruptedException {
        Orientation angles = imu.getAngularOrientation();
        return (angles.firstAngle * -1);
    }

    //encoders
    public int getAvg() throws InterruptedException {
        FRV = Math.abs(FR.getCurrentPosition());
        FLV = Math.abs(FL.getCurrentPosition());
        avg = Math.abs((FRV + FLV)/2);
        return avg;
    }

    //Forwards, Backwards and Turning

    //forward
    public void moveForwardWithEncoders(double power, int distance) throws InterruptedException {
        telemetry.addData("encodersR", getAvg());
        telemetry.update();
        beforeALV = getAvg();
        //original moveForward

        while(Math.abs(getAvg() - beforeALV) < distance) {
            moveForward(power);
            idle();
        }
        telemetry.addData("encodersR", getAvg());
        telemetry.update();
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
    }

    public void moveBackWardWithEncoders(double power, int distance) throws InterruptedException {
        moveForwardWithEncoders(-power, distance);
    }

    //turn right
    public void turnRightWithGyro(double power, double angle) throws InterruptedException {
        beforeAngle = getGyroYaw();
        telemetry.addData("beforeYawAngle", beforeAngle);
        telemetry.update();
        while(Math.abs(getGyroYaw() - beforeAngle) < angle) {
            turnRight(power);
            idle();
        }
        beforeAngle = getGyroYaw();
        telemetry.addData("afterYawAngle", beforeAngle);
        telemetry.update();
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
    }

    //turn left
    public void turnLeftWithGyro(double power, double angle) throws InterruptedException {
        turnRightWithGyro(-power, angle);
    }

    public void turnRightWithPID(double angle) throws InterruptedException
    {
        //calibration constants
        double p = .004; double i = .000015; double d = 2.0;
        double error = angle;
        double pastError = 0.0;
        double output;
        double proportional = 0.0;
        double reset = 0.0;
        double derivative = 0.0;
        double deltaTime;
        beforeAngle = getGyroYaw();
        telemetry.addData("beforeYawAngle", beforeAngle);
        telemetry.update();
        long lastTime = System.currentTimeMillis();
        while(Math.abs(getGyroYaw() - beforeAngle) < angle) {
            error = angle - Math.abs(getGyroYaw() - beforeAngle);
            //proportional
            proportional = error * p;
            deltaTime = System.currentTimeMillis() - lastTime;
            //integral
            reset += (error * deltaTime);
            //derivative
            derivative = deltaTime/(error-pastError); //(error - pastError)/deltaTime;
            //output
            output = proportional + (reset * i);
            //Range.clip(output, -1, 1);
            if(output < .15)
                output = 0;
            //+ (reset * i) + derivative
            turnRight(output);
            telemetry.addData("output", output);
            telemetry.addData("proportion", proportional);
            telemetry.addData("reset", reset * i);
            telemetry.addData("derivative", derivative * d);
            telemetry.update();
            pastError = error;
            lastTime = System.currentTimeMillis();
            idle();
        }
        double afterAngle = getGyroYaw();
        telemetry.addData("afterYawAngle", beforeAngle);
        if(Math.abs(afterAngle - beforeAngle) > angle - 1 && Math.abs(afterAngle - beforeAngle) < angle + 1)
            telemetry.addData("turn", "success");
        else
            telemetry.addData("turn", "failure");
        telemetry.update();
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
    }

    public void turnRightWithPID(double angle, double p, double i, double d) throws InterruptedException{
        //calibration constants
        double error = angle;
        double pastError = 0.0;
        double output;
        double proportional = 0.0;
        double reset = 0.0;
        double derivative = 0.0;
        double deltaTime;
        beforeAngle = getGyroYaw();
        telemetry.addData("beforeYawAngle", beforeAngle);
        telemetry.update();
        long lastTime = System.currentTimeMillis();
        while(Math.abs(getGyroYaw() - beforeAngle) < angle) {
            error = angle - Math.abs(getGyroYaw() - beforeAngle);
            //proportional
            proportional = error * p;
            deltaTime = System.currentTimeMillis() - lastTime;
            //integral
            reset += (error * deltaTime);
            //derivative
            derivative = deltaTime/(error-pastError); //(error - pastError)/deltaTime;
            //output
            output = proportional + (reset * i);
            //Range.clip(output, -1, 1);
            if(output < .15)
                output = 0;
            //+ (reset * i) + derivative
            turnRight(output);
            telemetry.addData("output", output);
            telemetry.addData("proportion", proportional);
            telemetry.addData("reset", reset * i);
            telemetry.addData("derivative", derivative * d);
            telemetry.update();
            pastError = error;
            lastTime = System.currentTimeMillis();
            idle();
        }
        double afterAngle = getGyroYaw();
        telemetry.addData("afterYawAngle", beforeAngle);
        if(Math.abs(afterAngle - beforeAngle) > angle - 1 && Math.abs(afterAngle - beforeAngle) < angle + 1)
            telemetry.addData("turn", "success");
        else
            telemetry.addData("turn", "failure");
        telemetry.update();
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
    }

    public void turnLeftWithPID(double angle) throws InterruptedException
    {
        //calibration constants
        double p = .004; double i = .000015; //double d = 2.0;
        double error = angle;
        double pastError = 0.0;
        double output;
        double proportional = 0.0;
        double reset = 0.0;
        double derivative = 0.0;
        double deltaTime;
        beforeAngle = getGyroYaw();
        telemetry.addData("beforeYawAngle", beforeAngle);
        telemetry.update();
        long lastTime = System.currentTimeMillis();
        while(Math.abs(getGyroYaw() - beforeAngle) < angle) {
            error = angle - Math.abs(getGyroYaw() - beforeAngle);
            //proportional
            proportional = error * p;
            deltaTime = System.currentTimeMillis() - lastTime;
            //integral
            reset += (error * deltaTime);
            //derivative
            derivative = deltaTime/(error-pastError); //(error - pastError)/deltaTime;
            //output
            output = proportional + (reset * i);
            //Range.clip(output, -1, 1);
            if(output < .15)
                output = 0;
            //+ (reset * i) + derivative
            turnLeft(output);
            telemetry.addData("output", output);
            telemetry.addData("proportion", proportional);
            telemetry.addData("reset", reset * i);
            telemetry.addData("derivative", derivative);
            telemetry.update();
            pastError = error;
            lastTime = System.currentTimeMillis();
            idle();
        }
        double afterAngle = getGyroYaw();
        telemetry.addData("afterYawAngle", beforeAngle);
        if(Math.abs(afterAngle - beforeAngle) > angle - 1 && Math.abs(afterAngle - beforeAngle) < angle + 1)
            telemetry.addData("turn", "success");
        else
            telemetry.addData("turn", "failure");
        telemetry.update();
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
    }
    public void turnLeftWithPID(double angle, double p, double i, double d) throws InterruptedException {
        //calibration constants
        double error = angle;
        double pastError = 0.0;
        double output;
        double proportional = 0.0;
        double reset = 0.0;
        double derivative = 0.0;
        double deltaTime;
        beforeAngle = getGyroYaw();
        telemetry.addData("beforeYawAngle", beforeAngle);
        telemetry.update();
        long lastTime = System.currentTimeMillis();
        while (Math.abs(getGyroYaw() - beforeAngle) < angle) {
            error = angle - Math.abs(getGyroYaw() - beforeAngle);
            //proportional
            proportional = error * p;
            deltaTime = System.currentTimeMillis() - lastTime;
            //integral
            reset += (error * deltaTime);
            //derivative
            derivative = deltaTime / (error - pastError); //(error - pastError)/deltaTime;
            //output
            output = proportional + (reset * i);
            //Range.clip(output, -1, 1);
            if (output < .15)
                output = 0;
            //+ (reset * i) + derivative
            turnLeft(output);
            telemetry.addData("output", output);
            telemetry.addData("proportion", proportional);
            telemetry.addData("reset", reset * i);
            telemetry.addData("derivative", derivative * d);
            telemetry.update();
            pastError = error;
            lastTime = System.currentTimeMillis();
            idle();
        }
        double afterAngle = getGyroYaw();
        telemetry.addData("afterYawAngle", beforeAngle);
        if (Math.abs(afterAngle - beforeAngle) > angle - 1 && Math.abs(afterAngle - beforeAngle) < angle + 1)
            telemetry.addData("turn", "success");
        else
            telemetry.addData("turn", "failure");
        telemetry.update();
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
    }

    //gyro stabilization
    public void moveForward(double power, int distance) throws InterruptedException {
        beforeALV = getAvg();
        beforeAngle = getGyroYaw();
        double correction = CORRECTION;
        long lastTime = System.nanoTime();
        double signedDifference;
        while (Math.abs(getAvg() - beforeALV) < distance) {
            FR.setPower(power);
            BR.setPower(power);
            FL.setPower(-power);
            BL.setPower(-power);
            idle();
        }
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
        telemetry.addData("EncoderMovement", Math.abs(getAvg() - beforeALV));
        telemetry.update();
        if (Math.abs(beforeAngle - getGyroYaw()) < 2) {
            telemetry.addData("success", "correction works");
            telemetry.update();
        }
        else {
            telemetry.addData("success", "correction failed");
            telemetry.update();
        }

    }

    //gyro stabilization
    public void moveForwardWithCorrection(double power, int distance) throws InterruptedException {
        beforeALV = getAvg();
        beforeAngle = getGyroYaw();
        double correction = CORRECTION;
        long lastTime = System.nanoTime();
        double signedDifference;
        while (Math.abs(getAvg() - beforeALV) < distance) {
            FR.setPower(power);
            BR.setPower(power);
            FL.setPower(-power);
            BL.setPower(-power);
            double difference = Math.abs(getGyroYaw() - beforeAngle);
            while (difference > 2 && Math.abs(getAvg() - beforeALV) < distance) {
                if(getGyroYaw() < beforeAngle) {
                    FR.setPower(power * (1 + difference * correction));
                    BR.setPower(power * (1 + difference * correction));
                    FL.setPower(-power);
                    BL.setPower(-power);
                }
                else if(getGyroYaw() > beforeAngle) {
                    FR.setPower(power);
                    BR.setPower(power);
                    FL.setPower(-power * (1 + difference * correction));
                    BL.setPower(-power  * (1 + difference * correction));
                }
                telemetry.addData("LeftPower", FR.getPower());
                telemetry.addData("RightPower", BR.getPower());
                telemetry.update();
                difference = Math.abs(getGyroYaw() - beforeAngle);
                idle();
            }
            idle();
        }
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
        telemetry.addData("EncoderMovement", Math.abs(getAvg() - beforeALV));
        telemetry.update();
        if (Math.abs(beforeAngle - getGyroYaw()) < 2) {
            telemetry.addData("success", "correction works");
            telemetry.update();
        }
        else {
            telemetry.addData("success", "correction failed");
            telemetry.update();
        }

    }

    public void moveBackWardWithCorrection(double power, int distance) throws InterruptedException {
        moveForwardWithCorrection(-power, distance);
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
    }

    //gyro stabilization with PID
    public void moveForwardPID(int distance) throws InterruptedException {
        //calibration constants
        double p = .00015; double i = .00000015; //double d = 2.0;
        double error = distance;
        double pastError = 0.0;
        double output;
        double proportional = 0.0;
        double reset = 0.0;
        double derivative = 0.0;
        double deltaTime;
        int angleError;
        beforeALV = getAvg();
        beforeAngle = getGyroYaw();
        double correction = CORRECTION;
        long lastTime = System.currentTimeMillis();
        while (Math.abs(getAvg() - beforeALV) < distance) {
            error = distance - Math.abs(getAvg() - beforeALV);
            //proportional
            proportional = error * p;
            //integral
            deltaTime = System.currentTimeMillis() - lastTime;
            //integral
            reset += (error * deltaTime);
            //derivative
            //derivative = d * (error - pastError)/deltaTime;
            //output
            output = proportional + (reset * i);
            if(output < .05)
                output = 0;
            moveForward(output);
//            double difference = Math.abs(getGyroYaw() - beforeAngle);
//            while (difference > 2 && Math.abs(getAvg() - beforeALV) < distance) {
//                difference = Math.abs(getGyroYaw() - beforeAngle);
//                if(getGyroYaw() < beforeAngle) {
//                    FR.setPower(output * (1 + difference * correction));
//                    BR.setPower(output * (1 + difference * correction));
//                    FL.setPower(-output * ((1 + difference * correction) - 1));
//                    BL.setPower(-output * ((1 + difference * correction) - 1));
//                }
//                else if(getGyroYaw() > beforeAngle) {
//                    FR.setPower(output * ((1 + difference * correction) - 1));
//                    BR.setPower(output * ((1 + difference * correction) - 1));
//                    FL.setPower(-output * (1 + difference * correction));
//                    BL.setPower(-output  * (1 + difference * correction));
//                }
//                telemetry.addData("LeftPower", FR.getPower());
//                telemetry.addData("RightPower", BR.getPower());
//                telemetry.update();
//                difference = Math.abs(getGyroYaw() - beforeAngle);
//                idle();
//            }
            telemetry.addData("output", output);
            telemetry.addData("proportion", proportional);
            telemetry.addData("reset", reset * i);
            //telemetry.addData("derivative", derivative * d);
            telemetry.update();
            pastError = error;
            lastTime = System.currentTimeMillis();
            idle();
        }
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
        telemetry.addData("EncoderMovement", Math.abs(getAvg() - beforeALV));
        if (Math.abs(beforeAngle - getGyroYaw()) < 2)
            telemetry.addData("success", "correction works");
        else
            telemetry.addData("failure", "correction failed");
        if(error < -20 && error > 20)
            telemetry.addData("success", "PID works");
        else
            telemetry.addData("failure", "PID failed");
        telemetry.update();
    }

    //this contains adjustable constants
    public void moveForwardPID(double p, double i, double d, int distance) throws InterruptedException{
        //calibration constants
        double error = distance;
        double pastError = 0.0;
        double output;
        double proportional = 0.0;
        double reset = 0.0;
        double derivative = 0.0;
        double deltaTime;
        int angleError;
        beforeALV = getAvg();
        beforeAngle = getGyroYaw();
        double correction = CORRECTION;
        long lastTime = System.currentTimeMillis();
        while (Math.abs(getAvg() - beforeALV) < distance) {
            error = distance - Math.abs(getAvg() - beforeALV);
            //proportional
            proportional = error * p;
            //integral
            deltaTime = System.currentTimeMillis() - lastTime;
            //integral
            reset += (error * deltaTime);
            //derivative
            //derivative = d * (error - pastError)/deltaTime;
            //output
            output = proportional + (reset * i);
            if(output < .05)
                output = 0;
            moveForward(output);
            telemetry.addData("output", output);
            telemetry.addData("proportion", proportional);
            telemetry.addData("reset", reset * i);
            //telemetry.addData("derivative", derivative * d);
            telemetry.update();
            pastError = error;
            lastTime = System.currentTimeMillis();
            idle();
        }
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
        telemetry.addData("EncoderMovement", Math.abs(getAvg() - beforeALV));
        if (Math.abs(beforeAngle - getGyroYaw()) < 2)
            telemetry.addData("success", "correction works");
        else
            telemetry.addData("failure", "correction failed");
        if(error < -20 && error > 20)
            telemetry.addData("success", "PID works");
        else
            telemetry.addData("failure", "PID failed");
        telemetry.update();
    }


    public void moveBackwardPID(int distance) throws InterruptedException
    {
        //calibration constants
        double p = .00015; double i = .00000015; //double d = 2.0;
        double error = distance;
        double pastError = 0.0;
        double output;
        double proportional = 0.0;
        double reset = 0.0;
        double derivative = 0.0;
        double deltaTime;
        int angleError;
        beforeALV = getAvg();
        beforeAngle = getGyroYaw();
        double correction = CORRECTION;
        long lastTime = System.currentTimeMillis();
        while (Math.abs(getAvg() - beforeALV) < distance) {
            error = distance - Math.abs(getAvg() - beforeALV);
            //proportional
            proportional = error * p;
            //integral
            deltaTime = System.currentTimeMillis() - lastTime;
            //integral
            reset += (error * deltaTime);
            //derivative
            //derivative = d * (error - pastError)/deltaTime;
            //output
            output = proportional + (reset * i);
            if(output < .05)
                output = 0;
            moveBackward(output);
            //double difference = Math.abs(getGyroYaw() - beforeAngle);
//            while (difference > 2 && Math.abs(getAvg() - beforeALV) < distance) {
//                if(getGyroYaw() < beforeAngle) {
//                    FR.setPower(output * (1 + difference * correction));
//                    BR.setPower(output * (1 + difference * correction));
//                    FL.setPower(-output);
//                    BL.setPower(-output);
//                }
//                else if(getGyroYaw() > beforeAngle) {
//                    FR.setPower(output);
//                    BR.setPower(output);
//                    FL.setPower(-output * (1 + difference * correction));
//                    BL.setPower(-output  * (1 + difference * correction));
//                }
//                telemetry.addData("LeftPower", FR.getPower());
//                telemetry.addData("RightPower", BR.getPower());
//                telemetry.update();
//                difference = Math.abs(getGyroYaw() - beforeAngle);
//                idle();
//            }
            telemetry.addData("output", output);
            telemetry.addData("proportion", proportional);
            telemetry.addData("reset", reset * i);
            //telemetry.addData("derivative", derivative * d);
            telemetry.update();
            pastError = error;
            lastTime = System.currentTimeMillis();
            idle();
        }
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
        telemetry.addData("EncoderMovement", Math.abs(getAvg() - beforeALV));
        if (Math.abs(beforeAngle - getGyroYaw()) < 2)
            telemetry.addData("success", "correction works");
        else
            telemetry.addData("failure", "correction failed");
        if(error < -20 && error > 20)
            telemetry.addData("success", "PID works");
        else
            telemetry.addData("failure", "PID failed");
        telemetry.update();
    }

    //this contains adjustable constants
    public void moveBackwardPID(double p, double i, double d, int distance) throws InterruptedException{
        //calibration constants
        double error = distance;
        double pastError = 0.0;
        double output;
        double proportional = 0.0;
        double reset = 0.0;
        double derivative = 0.0;
        double deltaTime;
        int angleError;
        beforeALV = getAvg();
        beforeAngle = getGyroYaw();
        double correction = CORRECTION;
        long lastTime = System.currentTimeMillis();
        while (Math.abs(getAvg() - beforeALV) < distance) {
            error = distance - Math.abs(getAvg() - beforeALV);
            //proportional
            proportional = error * p;
            //integral
            deltaTime = System.currentTimeMillis() - lastTime;
            //integral
            reset += (error * deltaTime);
            //derivative
            //derivative = d * (error - pastError)/deltaTime;
            //output
            output = proportional + (reset * i);
            if(output < .05)
                output = 0;
            moveBackward(output);
            telemetry.addData("output", output);
            telemetry.addData("proportion", proportional);
            telemetry.addData("reset", reset * i);
            //telemetry.addData("derivative", derivative * d);
            telemetry.update();
            pastError = error;
            lastTime = System.currentTimeMillis();
            idle();
        }
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
        telemetry.addData("EncoderMovement", Math.abs(getAvg() - beforeALV));
        if (Math.abs(beforeAngle - getGyroYaw()) < 2)
            telemetry.addData("success", "correction works");
        else
            telemetry.addData("failure", "correction failed");
        if(error < -20 && error > 20)
            telemetry.addData("success", "PID works");
        else
            telemetry.addData("failure", "PID failed");
        telemetry.update();
    }

    //beacon pushing methods
    public void moveForwardsToWhiteLine(int distance) throws InterruptedException {
        //calibration constants
        double p = .00015; double i = .00000015; //double d = .00000000002;
        double error = distance;
        double pastError = 0.0;
        double output;
        double proportional = 0.0;
        double reset = 0.0;
        double derivative = 0.0;
        double deltaTime;
        int angleError;
        beforeALV = getAvg();
        beforeAngle = getGyroYaw();
        double correction = CORRECTION;
        long lastTime = System.currentTimeMillis();
        while (Math.abs(getAvg() - beforeALV) < distance && Math.abs(colorSensorAverageValues(colorSensorWL) - whiteACV) > 10) {
            error = distance - Math.abs(getAvg() - beforeALV);
            //proportional
            proportional = error * p;
            //integral
            deltaTime = System.currentTimeMillis() - lastTime;
            lastTime = System.currentTimeMillis();
            //integral
            reset += (error * deltaTime);
            //derivative
            //derivative = ((error - pastError)/deltaTime) * d;
            //output
            output = proportional + (reset * i) + derivative;
            if(output < .05)
                output = 0;
            moveForward(output);
            telemetry.addData("output", output);
            telemetry.addData("proportion", proportional * p);
            telemetry.addData("reset", reset * i);
            //telemetry.addData("derivative", derivative * d);
            telemetry.update();
            pastError = error;
            idle();
        }
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
        telemetry.addData("EncoderMovement", Math.abs(getAvg() - beforeALV));
        if (Math.abs(beforeAngle - getGyroYaw()) < 2)
            telemetry.addData("success", "correction works");
        else
            telemetry.addData("failure", "correction failed");
        if(error < -20 && error > 20)
            telemetry.addData("success", "PID works");
        else
            telemetry.addData("failure", "PID failed");
        telemetry.addData("colorAverage", colorSensorAverageValues(colorSensorWL));
        telemetry.update();
    }

    //beacon pushing methods
    public void moveBackwardsToWhiteLine(int distance) throws InterruptedException {
        //calibration constants
        double p = .00015; double i = .00000015; //double d = .00000000002;
        double error = distance;
        double pastError = 0.0;
        double output;
        double proportional = 0.0;
        double reset = 0.0;
        double derivative = 0.0;
        double deltaTime;
        int angleError;
        beforeALV = getAvg();
        beforeAngle = getGyroYaw();
        double correction = CORRECTION;
        long lastTime = System.currentTimeMillis();
        while (Math.abs(getAvg() - beforeALV) < distance && Math.abs(colorSensorAverageValues(colorSensorWL) - whiteACV) > 10  && (System.currentTimeMillis() - lastTime) < 1500) {
            error = distance - Math.abs(getAvg() - beforeALV);
            //proportional
            proportional = error * p;
            //integral
            deltaTime = System.currentTimeMillis() - lastTime;
            lastTime = System.currentTimeMillis();
            //integral
            reset += (error * deltaTime);
            //derivative
            //derivative = ((error - pastError)/deltaTime) * d;
            //output
            output = proportional + (reset * i) + derivative;
            if(output < .05)
                output = 0;
            moveBackward(output);
            telemetry.addData("output", output);
            telemetry.addData("proportion", proportional * p);
            telemetry.addData("reset", reset * i);
            //telemetry.addData("derivative", derivative * d);
            telemetry.update();
            pastError = error;
            idle();
        }
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
        telemetry.addData("EncoderMovement", Math.abs(getAvg() - beforeALV));
        if (Math.abs(beforeAngle - getGyroYaw()) < 2)
            telemetry.addData("success", "correction works");
        else
            telemetry.addData("failure", "correction failed");
        if(error < -20 && error > 20)
            telemetry.addData("success", "PID works");
        else
            telemetry.addData("failure", "PID failed");
        telemetry.addData("colorAverage", colorSensorAverageValues(colorSensorWL));
        telemetry.update();
        if(Math.abs(colorSensorAverageValues(colorSensorWL) - whiteACV) < 10) {
            telemetry.addData("whiteLine", "found");
            telemetry.update();
        }
        else {
            telemetry.addData("whiteLine", "not found");
            telemetry.update();
            moveBackwardsToWhiteLine(200);
        }

    }
    //0 represents blue and 1 represents red
    public int beaconValue() throws InterruptedException {
        if(colorSensorBlue(colorSensorBlueBeacon) > colorSensorRed(colorSensorBlueBeacon)){
            telemetry.addData("Beacon", "Blue");
            telemetry.update();
            return 0;
        }
        telemetry.addData("Beacon", "Red");
        telemetry.update();
        return 1;
    }

    public void pushRedBeacon(double power, int distance) throws InterruptedException {
        //power: .15
        //distance: 25
        //move forward and push the correct beacon
        if (colorSensorRed(colorSensorBlueBeacon) < colorSensorBlue(colorSensorBlueBeacon)) {
            beforeALV = getAvg();
            moveBackWardWithCorrection(power, distance);
            Beacon.setPosition(.43);
            beforeALV = getAvg();
            moveForwardWithCorrection(power, distance);
            telemetry.addData("hit1", "rip");
            sleep(3000); //change sleep values when this part works
            Beacon.setPosition(1);
            beforeALV = getAvg();
            moveForwardWithCorrection(power, distance);
            idle();
        }
        else {
            Beacon.setPosition(.43);
            sleep(2000);
            moveBackWardWithCorrection(.15, 40);
            moveForwardWithCorrection(.15, 40);
            idle();
            telemetry.addData("hit2", "rip");
            Beacon.setPosition(1);
            telemetry.addData("encodersA", getAvg());
            beforeALV = getAvg();
        }
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
        sleep(2000);
    }

    public int pushBlueBeacon(int distance) throws InterruptedException {
        //power: .15
        //distance: 25
        int output;
        //move forward and push the correct beacon
        if (beaconValue() == 1) {
            moveBlueSideServo();
            output = 0;
        }
        else {
            moveForwardPID(.003, .0000002, 0.0, distance);
            moveBlueSideServo();
            output = 1;
        }
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
        sleep(2000);
        return output;
    }

    //test methods

    //tests to see whether turning left or turning right is negative or postive
    public void testTurningNegative(double power, int angle) throws InterruptedException
    {
        double beforeAngle = getGyroYaw();
        turnRightWithGyro(power, angle);
        sleep(1000);
        double finalAngle = getGyroYaw();
        telemetry.addData("turnRightResults", (finalAngle - beforeAngle));
        telemetry.update();
        sleep(5000);
        beforeAngle = getGyroYaw();
        turnLeftWithGyro(power, angle);
        sleep(1000);
        finalAngle = getGyroYaw();
        telemetry.addData("turnLeftResults", (finalAngle - beforeAngle));
        telemetry.update();
        sleep(5000);
    }

    public void correct(double perpendicular, double p, double i, double d) throws InterruptedException {
        //double p = .004; double i = .000015; //double d = 2.0;
        double angle = Math.abs(perpendicular - getGyroYaw());
        if(perpendicular > getGyroYaw()) {
            turnRightWithPID(angle, p, i, d);
        }
        else if(perpendicular < getGyroYaw()) {
            turnLeftWithPID(angle, p, i, d);
        }
    }

    //miscellaneous
    public void moveBackwardsWithATiltRight(double power, double distance) throws InterruptedException {
        beforeALV = getAvg();
        while(Math.abs(getAvg() - beforeALV) <  distance){
            FR.setPower(-power * .75);
            BR.setPower(-power * .75);
            FL.setPower(power * 2.6);
            BL.setPower(power * 2.6);
            idle();
        }
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
    }
    public void moveBackwardsWithATiltLeft(double power, double distance) throws InterruptedException{
        beforeALV = getAvg();
        while(Math.abs(getAvg() - beforeALV) <  distance){
            FR.setPower(-power * 2.60);
            BR.setPower(-power * 2.60);
            FL.setPower(power * .65);
            BL.setPower(power * .65);
            idle();
        }
        FR.setPower(0);
        BR.setPower(0);
        FL.setPower(0);
        BL.setPower(0);
    }

    public void nonAutoClear() throws InterruptedException {
        telemetry.log().add("testlog");
        telemetry.update();
        telemetry.log().add("nestLine");
        telemetry.update();
    }
    public void moveBlueSideServo() throws InterruptedException { }

    public void moveRedSideServo() throws InterruptedException { }
}
