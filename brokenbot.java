package u1500736;

import robocode.*;
import java.math.BigDecimal;
import java.awt.Color;

public class brokenbot extends Robot {
	
	public static int shotsFired;	//number of bullets fired by brokenbot
	public static int shotsHit;	//number of bullets that hit a target
	public double gunAngle;	//angle that the gun turns at the corresponding points in the loop
	public double distanceOne;	//first threshold distance, greater than this means minimum gun power
	public double distanceTwo;	//second threshold distance
	public double distanceThree;	//third threshold distance, less than this means maximum gun power
	public boolean track;	//whether tracking a robot to its predicted future position is used
	public boolean collisionShoot;	//whether brokenbot will attempt to shoot a robot it has collided with
	
	public void run() {
		Color green = new Color(0,91,39);	//set colours for the robot
		Color blue = new Color(0,91,139);
		setBodyColor(green);
		setRadarColor(blue);
		setGunColor(Color.green);
		setBulletColor(Color.red);
		
		if (getRoundNum() == 0) {	//initialise static variables
			shotsFired = 0;
			shotsHit = 0;
		}
		setAdjustGunForRobotTurn(true);	//don't turn the gun with the robot
		gunAngle = 217.9634548658593;	//results from the genetic algorithm
		distanceOne = 383.424521191921;
		distanceTwo = 136.3537002071083;
		distanceThree = 32.14402011160857;
		track = true;
		collisionShoot = true;
		while(true) {
			double turnangle = Math.random()*180 - 90; //turns up to 90 degrees left or right
			turnRight(turnangle);	
			turnGunRight(gunAngle);	//turn gun the set value
			double move = Math.random()*100 + 50;	//moves forward 50-150 pixels
			ahead(move);
			turnGunRight(gunAngle);	//turn gun the set value
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		if (getGunHeat() == 0) {	//if the gun can be fired
			double distance = e.getDistance();
			double relativeHeading = e.getHeading() - getGunHeading();
			double velocity = e.getVelocity();
			double power;
			if (distance > distanceOne)	//risks less energy on more distant shots
				power = 0.5;
			else if (distance > distanceTwo)
				power = 1;
			else if (distance > distanceThree)
				power = 2;
			else
				power = 3;
			
			double bullSpeed = 20 - 3*power;	//formula for bullet speed based on power of bullet
			double turnAngle = Math.toDegrees(Math.asin(velocity * Math.sin(Math.toRadians(relativeHeading))/bullSpeed));	//calculates angle needed to turn to hit the target in the future
			if (track)	//if tracking is enabled
				turnGunRight(turnAngle);	//turns to where it thinks the target will be
			fire(power);
			shotsFired++;
		}
	}
	
	public void onHitWall(HitWallEvent e) {
		double turnAngle = Math.random()*90 + 90;	//turn between 90 and 180 degrees
		if (e.getBearing() < 0)	//if turning right will get away from the wall faster
			turnRight(turnAngle);
		else 
			turnLeft(turnAngle);
		ahead(100);	//move away from the wall
	}
	
	public void onBulletHit(BulletHitEvent e) {
		shotsHit++;	//increment number of bullets that hit a target
	}

	public void onBulletHitBullet (BulletHitBulletEvent e) {
		shotsHit++;	//increment number of bullets that hit a target
	}
	
	public void onHitRobot (HitRobotEvent e) {
		double bearing = e.getBearing();
		double gunHeading = getGunHeading();	//gives heading of gun in relation to body
		double heading = getHeading();
		double gunBearing = gunHeading - heading;	//adjust to get bearing of gun in relation to due north
		if (gunBearing < 0)
			gunBearing = gunBearing + 360;
		double gunAngle = bearing - gunBearing; 
		if ((Math.abs(gunAngle) < 60)&&(collisionShoot))	//if collision shooting is enabled and I can turn my gun quickly towards the robot, do so
			turnGunRight(gunAngle);	//this should mean they will be scanned and onScannedRobot will handle shooting them
	}
	
	public void onBattleEnded (BattleEndedEvent e) {
		double prop = (double) shotsHit*100/shotsFired;	//accuracy as a percentage
		BigDecimal proportion = new BigDecimal(String.valueOf(prop)).setScale(2, BigDecimal.ROUND_HALF_UP);	//to cut down precision of accuracy
		System.out.println("Fired "+shotsFired+" bullets and hit with "+shotsHit+" of them");
		System.out.println(proportion.toString()+"%");	//report accuracy to console
	}
}
