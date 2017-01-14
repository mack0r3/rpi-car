import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;

import java.nio.charset.StandardCharsets;

import javax.microedition.io.StreamConnection;
import java.io.InputStream;

public class ProcessConnectionThread extends Thread{

	private static int MOTOR_LEFT_ONE = 1;
	private static int MOTOR_LEFT_FOUR = 4;
	private static int MOTOR_RIGHT_FIVE = 5;
	private static int MOTOR_RIGHT_SIX = 6;

	private StreamConnection connection;

	public ProcessConnectionThread(StreamConnection connection) {
		this.connection = connection;

		Gpio.wiringPiSetup();
		SoftPwm.softPwmCreate(MOTOR_LEFT_ONE, 0, 100);
		SoftPwm.softPwmCreate(MOTOR_LEFT_FOUR, 0, 100);

		SoftPwm.softPwmCreate(MOTOR_RIGHT_FIVE, 0, 100);
		SoftPwm.softPwmCreate(MOTOR_RIGHT_SIX, 0, 100);
	}

	@Override
	public void run() {
		InputStream inputStream = null;
		try {
			inputStream = connection.openInputStream();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Waiting for inputStream");
		while(true) {
			byte[] bytes = new byte[10];
			try {
				inputStream.read(bytes); }
			catch (Exception ex) {
				ex.printStackTrace(); }
			String string = new String(bytes, StandardCharsets.UTF_8);
			String strengthString = "0";
			String degreeString = "0";
			try {
				strengthString = string.substring(string.lastIndexOf("#") + 1);
				degreeString = string.substring(0, string.lastIndexOf("#"));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			int strength = 0;
			int degree = 0;
			try {
				strength = Integer.parseInt(strengthString.toString().trim());
				degree = Integer.parseInt(degreeString.toString().trim());
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			//SoftPwm.softPwmWrite(MOTOR_ONE, strength);
			//SoftPwm.softPwmWrite(MOTOR_TWO, strength);

			moveLeftEngine(strength, degree, MOTOR_LEFT_ONE, MOTOR_LEFT_FOUR);
			moveRightEngine(strength, degree, MOTOR_RIGHT_FIVE, MOTOR_RIGHT_SIX);
		}
	}

	private void moveLeftEngine(int strength, int degree, int motorOne, int motorTwo) {
		if (degree < 0) {
			degree += 360;
		}

		double degreePower = 0;
		double angle = -(20.00/9.00);
		if(degree >= 0 && degree <= 90) {
			degreePower = 100;
		}

		if(degree >= 90 && degree <= 180) {
			degreePower  = (angle * degree) + 300.00;
		}

		if(degree >= 180 && degree <= 270) {
			degreePower = -100;
		}

		if(degree >= 270 && degree <= 360) {
			degreePower = (-1 * (angle)) * degree - 700.00;
		}

		double power = (strength / 100.00) * degreePower;
		setPower((int)power, motorOne, motorTwo);
	}

	private void moveRightEngine(int strength, int degree, int motorOne, int motorTwo) {
		moveLeftEngine(strength, degree - 90, motorOne, motorTwo);
	}

	private void setPower(int power, int motorOne, int motorTwo) {
		if(power >= 0) {
			SoftPwm.softPwmWrite(motorTwo, 0);
			SoftPwm.softPwmWrite(motorOne, power);
		} else {
			SoftPwm.softPwmWrite(motorOne, 0);
			SoftPwm.softPwmWrite(motorTwo, (-1) * power);
		}
	}
}
