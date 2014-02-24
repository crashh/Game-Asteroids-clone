import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Shot {
	
	//The speed at which the shots move, in pixels per frame:
	private final double shotSpeed=1.5; 
	
	//Variables used in movement:
	private double x,y,xVelocity,yVelocity, angle; 
	private int lifeLeft;
	
	BufferedImage sprite = null;
	
	/* Definitions:
	 * {X}{Y}		x and y location shot from (-20 is needed to get a perfect fit)
	 * {angle}		is the direction
	 * {shipXVel}	is the ships current velocity when fired
	 * {lifeLeft} 	is how long the shot remains in frames */
	public Shot(double x, double y, double angle, double shipXVel,
				double shipYVel, int lifeLeft, BufferedImage sprite){
		this.x=x-20;
		this.y=y-20;
		//Add the velocity of the ship to the shot velocity
		//(so the shot's velocity is relative to the ship's velocity)
		xVelocity=shotSpeed*Math.cos(angle)+shipXVel;
		yVelocity=shotSpeed*Math.sin(angle)+shipYVel;
		
		this.angle = angle;
		this.sprite = sprite;
		this.lifeLeft=lifeLeft+150;
	}
	
	public void move(int scrnWidth, int scrnHeight){
		//Used to make shot disappear if it goes too long, decrement 1 for each frame:
		lifeLeft--; 
		//Movement of the shot:
		x+=xVelocity; 
		y+=yVelocity;
		//Wrapping:
		if(x<0) 
			x+=scrnWidth;
		else if(x>scrnWidth)
			x-=scrnWidth;
		if(y<0)
			y+=scrnHeight;
		else if(y>scrnHeight)
			y-=scrnHeight;
	}
	
	public void draw(Graphics g){
		
		//Handles rotation of the image:
		double rotationRequired = angle;
		double locationX = sprite.getWidth() / 2;
		double locationY = sprite.getHeight() / 2;
		AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		
		//Draws the image:
		g.drawImage(op.filter(sprite, null), (int)(x), (int)(y), null);
	}
	
	//Getters:
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public int getLifeLeft(){
		return lifeLeft;
	}
}