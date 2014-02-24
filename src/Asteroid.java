import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Asteroid {
	
	private double x, y, xVelocity, yVelocity, radius, rotate=0.4, randomValue;
	int hitsLeft, numSplit, golddrop;
	
	//Used for gfx:
	private BufferedImage asteroid1 = null;
	private BufferedImage after = null;
	
	
	public Asteroid(double x,double y,double radius,double minVelocity,
					double maxVelocity,int hitsLeft,int numSplit, int gold, BufferedImage sprite){
		asteroid1=sprite;
		
		this.x=x;
		this.y=y;
		this.radius=radius;
		this.hitsLeft=hitsLeft;				//number of shots left to destroy it
		this.numSplit=numSplit; 			//number of smaller asteroids it breaks up into when shot
		this.golddrop=gold;
		randomValue = Math.random() * ( 2 - 1 );
		if (randomValue<0.5)
			randomValue=0.0001;
		else
			randomValue=-0.0001;
		
		//Calculates a random direction and,
		//a random velocity between minVelocity and maxVelocity:
		double vel=minVelocity + Math.random()*(maxVelocity-minVelocity);
		double dir=2*Math.PI*Math.random(); 
		xVelocity=vel*Math.cos(dir);
		yVelocity=vel*Math.sin(dir);
	}
	
	public void move(int scrnWidth, int scrnHeight){
		x+=xVelocity; 
		y+=yVelocity;
		rotate+=randomValue;
		
		//Wrapping, edited to prevent asteroid wrapping too early:
		if(x<0-radius)
			x+=scrnWidth+2*radius;
		else if(x>scrnWidth+radius)
			x-=scrnWidth+2*radius;
		if(y<0-radius)
			y+=scrnHeight+2*radius;
		else if(y>scrnHeight+radius)
			y-=scrnHeight+2*radius;
	}
	
	public void draw(Graphics g){
		//Sets the proper image, setting size according to scale:
				
		int w = asteroid1.getWidth();
		int h = asteroid1.getHeight();
		after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		
		at.translate(0.5*h, 0.5*w);
		at.rotate(Math.PI/rotate);
		at.scale((radius/w)*2, (radius/h)*2);
		at.translate(-0.5*w, -0.5*h);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		after = op.filter(asteroid1, after);
	
		//Draws the image:
		g.drawImage(after, (int)(x-0.5*w), (int)(y-0.5*h), null);		
		
	
	}
	
	public boolean shipCollision(Ship ship){
		// Use the distance formula to check if the ship is touching this
		// asteroid: Distance^2 = (x1-x2)^2 + (y1-y2)^2 ("^" denotes
		// exponents). If the sum of the radii is greater than the
		// distance between the center of the ship and asteroid, they are
		// touching.
		// If (shipRadius + asteroidRadius)^2 > (x1-x2)^2 + (y1-y2)^2,
		// then they have collided.
		// It does not check for collisions if the ship is not active
		// (the player is waiting to start a new life or the game is paused).
		if(Math.pow(radius+ship.getRadius(),2) > Math.pow(ship.getX()-x,2)+
				Math.pow(ship.getY()-y,2) && ship.isActive())
			return true;
		return false;
	}
	
	public boolean shotCollision(Shot shot){
		// Same idea as shipCollision, but using shotRadius = 0
		if(Math.pow(radius,2) > Math.pow(shot.getX()-x,2)+
				Math.pow(shot.getY()-y,2))
			return true;
		return false;
	}
	
	public Asteroid createSplitAsteroid(double minVelocity,
										double maxVelocity){
		//when this asteroid gets hit by a shot, this method is called
		//numSplit times by AsteroidsGame to create numSplit smaller
		//asteroids. Dividing the radius by sqrt(numSplit) makes the
		//sum of the areas taken up by the smaller asteroids equal to
		//the area of this asteroid. Each smaller asteroid has one
		//less hit left before being completely destroyed.
		return new Asteroid(x,y,radius/Math.sqrt(numSplit),
							minVelocity,maxVelocity,hitsLeft-1,numSplit, golddrop+2, asteroid1);
	}
	
	//Getters:
	public int getHitsLeft(){
		return hitsLeft;					//when 0, it dissapeers.
	}
	
	public int getNumSplit(){
		return numSplit;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
}