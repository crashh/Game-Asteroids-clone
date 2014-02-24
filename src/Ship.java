import java.awt.*; 
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class Ship { 
	//Radius of circle used to approximate the ship:
	private final int radius=6; 

	//Variables used in movement:
	private double x, y, angle, xVelocity, yVelocity, acceleration, velocityDecay, rotationalSpeed, ammo, max_ammo, immunity; 							
	private boolean turningLeft, turningRight, accelerating, active, shieldActive; 
	private int shotDelay, shotDelayLeft; 
	
	//Used for gfx:
	private BufferedImage sprite = null;			//Hold the currently used image.
	private BufferedImage image_ship = null;		//Imported to sprite when not moving.
	private BufferedImage image_shipmoving = null;	//...
	private BufferedImage shot = null;				//The shot image. (Actually imported two places, ill fix l8r)
	private BufferedImage shield = null;			//The shield image.
	
	//Unlike other classes, most stats are hardcoded because we only need one ship:
	public Ship(double x, double y, double max_ammo, double acceleration, boolean shieldStatus){ 
		loadImage();							//loads in the images.
		this.x=x; 
		this.y=y; 
		this.angle=0; 							//not built around 360 system - i h8 linear algebra aparrently :(.
		this.acceleration=acceleration; 
		this.velocityDecay=0.995;
		this.rotationalSpeed=0.03; 
		this.shotDelay=15; 						//# of frames between shots
		this.ammo=0;
		this.max_ammo=max_ammo;
		this.shieldActive=shieldStatus;
		shotDelayLeft=0; 						//ready to shoot 
		xVelocity=0; 							//not moving when 0
		yVelocity=0; 
		turningLeft=false; 						
		turningRight=false; 
		accelerating=false;
		shieldActive=false;
		active=false; 							//start game as paused 
	}
	
	public void draw(Graphics g){ 
		//Sets the proper image, depending on movement:
		if (accelerating)
			sprite=image_shipmoving;
		else
			sprite=image_ship;
		
		//Handles rotation of the image:
		double rotationRequired = angle;
		double locationX = sprite.getWidth() / 2;
		double locationY = sprite.getHeight() / 2;
		AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		
		//Draw shield:
		if (shieldActive){
			g.drawImage(shield, (int)x-(shield.getWidth()/2), (int)y-(shield.getHeight()/2), null);
		}
		
		//Draws the image:
		g.drawImage(op.filter(sprite, null), (int)x-sprite.getWidth()/2, (int)y-sprite.getHeight()/2, null);
	} 
	
	public void move(int scrnWidth, int scrnHeight){ 
		if(shotDelayLeft>0) 									//move() is called every frame that the game is run, so this ticks down the shot delay.
			shotDelayLeft--; 		
		if(ammo>=0.1)
			ammo-=0.1;
		if(immunity>0.0)
			immunity-=0.1;
		if(turningLeft) 										//this is backwards from typical polar coordinates because positive y is downward,
			angle-=rotationalSpeed; 							//because of that, adding to the angle is rotating clockwise (to the right).
		if(turningRight) 										
			angle+=rotationalSpeed; 
		if(angle>(2*Math.PI)) 									//Keep angle within bounds of 0 to 2*PI to prevent variable from overlapping.
			angle-=(2*Math.PI); 
		else if(angle<0) 
			angle+=(2*Math.PI); 
	
		//Adds acceleration to velocity in direction pointed:
		if(accelerating){ 										
			xVelocity+=acceleration*Math.cos(angle); 
			yVelocity+=acceleration*Math.sin(angle); 
		} 
		
		//Moving the coordinates based on velocity:
		x+=xVelocity; 											
		y+=yVelocity; 
		
		//Decay in velocity, slows down ship slowly - creating a gliding effect:
		xVelocity*=velocityDecay; 								
		yVelocity*=velocityDecay; 								 
		
		//Wraps the borders so ship cant leave the screen:
		if(x<0) 												
			x+=scrnWidth; 										 
		else if(x>scrnWidth) 
			x-=scrnWidth; 
		if(y<0) 
			y+=scrnHeight; 
		else if(y>scrnHeight) 
			y-=scrnHeight; 
		}
	
	public Shot shoot() {
		shotDelayLeft=shotDelay; 								//set delay till next shot can be fired
		ammo+=10;
		return new Shot(x+10,y+14,angle,xVelocity,yVelocity,40,shot); 
	}
	
	public void loadImage(){
		try {
			// The ClassLoader.getResource() ensures we get the sprite
			// from the appropriate place, this helps with deploying the game
			// with things like webstart. 
			URL url = this.getClass().getClassLoader().getResource("sprites/ship.gif");
			image_ship = ImageIO.read(url);
			url = this.getClass().getClassLoader().getResource("sprites/ship2.gif");
			image_shipmoving = ImageIO.read(url);
			url = this.getClass().getClassLoader().getResource("sprites/shot.gif");
			shot = ImageIO.read(url);
			url = this.getClass().getClassLoader().getResource("sprites/shield.png");
			shield = ImageIO.read(url);

		} catch (IOException e) {}
	}

	//Setters and getters mostly:
	public void setAccelerating(boolean accelerating){ 
		this.accelerating=accelerating; 						//start or stop accelerating the ship 
	} 

	public void setTurningLeft(boolean turningLeft){ 
		this.turningLeft=turningLeft; 							//start or stop turning the ship 
	} 

	public void setTurningRight(boolean turningRight){ 
		this.turningRight=turningRight; 
	} 

	public boolean isActive(){ 
		return active; 
	} 

	public boolean canShoot(){ 
		if(shotDelayLeft>0 || ammo>=max_ammo-10) 					//checks to see if the ship is ready to 
			return false; 											//shoot again yet or if it needs to wait longer 
		else 
			return true; 
	}	

	public void setActive(boolean active){ 
		this.active=active; 									//used when the game is paused or unpaused 
	} 
	
	public double getRadius(){ 
		return radius; 											//returns radius of circle that approximates the ship 
	} 
	
	public double getX(){ 
		return x; 												
	} 

	public double getY(){ 
		return y; 
	} 
	
	public void setSpeedUpgrade(double upgrade){
		acceleration+=upgrade;
	}
	
	public void setAmmoUpgrade(double upgrade){
		max_ammo+=upgrade*10;
	}
	
	public void setShieldUpgrade(boolean status){
		if (status==false)
			immunity=10;
		shieldActive=status;
	}
	
	public boolean getShieldUpgrade(){
		return shieldActive;
	}
	
	public double getImmunity(){
		return immunity;
	}
	
	public double getInfo(String info){
		if (info=="speed")
			return acceleration;
		if (info=="ammo")
			return max_ammo/10;
		if (info=="shield"){
			if (shieldActive)
				return 1;
			else
				return 0;
		}
		else
			return 0;
	}
	
	public double shotsLeft(){
		return max_ammo-ammo;
	}

} 