import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Loot {
	
	private double x, y, radius, size;
	private String item;
	private int value;
	private boolean b;
	
	//Used for gfx:
	private BufferedImage sprite = null;
	private BufferedImage after = null;
	
	
	public Loot(double x,double y,double radius, BufferedImage sprite){	
		this.x=x;
		this.y=y;
		this.radius=radius-2.2;
		this.sprite=sprite;
		this.item="gold";
		this.value=5;
		b=true;
	}
	
	
	public void draw(Graphics g){
		//Sets the proper image, setting size according to scale:
		if (size>=1.8)
			b=false;
		if (size<=1.1)
			b=true;
		
		if(b)
			size+=0.01;
		if(!b)
			size-=0.01;
		
		int w = sprite.getWidth();
		int h = sprite.getHeight();
		after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		AffineTransform at = new AffineTransform();
		
		at.translate(0.5*h, 0.5*w);;
		at.scale((size*radius/w)*2, (size*radius/h)*2);
		at.translate(-0.5*w, -0.5*h);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		after = op.filter(sprite, after);
	
		//Draws the image:
		g.drawImage(after, (int)(x-0.5*w), (int)(y-0.5*h), null);		
		
	
	}
	
	public boolean shipCollision(Ship ship){
		if(Math.pow((radius*3)+ship.getRadius(),2) > Math.pow(ship.getX()-x,2)+
				Math.pow(ship.getY()-y,2) && ship.isActive())
			return true;
		return false;
	}
	
		
	//Getters:	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public String getItem(){
		return item;
	}
	
	public int getValue(){
		return value;
	}
}