import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Explosion {
	
	private double x,y;
	private double timeLeft;
	private BufferedImage sprite;

	public Explosion(double x, double y, BufferedImage sprite){
		this.x=x;
		this.y=y;
		timeLeft=2;				//Duration of explosion:
		this.sprite=sprite;
	}
	
	public void draw(Graphics g){
		if (timeLeft>0){	
			int w = sprite.getWidth();
			int h = sprite.getHeight();
			BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			
			at.translate(0.5*h, 0.5*w);
			at.scale((timeLeft*20/w)*2, (timeLeft*20/h)*2);		//Scales according to timeleft to create an 'animation'.
			at.translate(-0.5*w, -0.5*h);
			
			AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			after = op.filter(sprite, after);
		
			timeLeft-=0.1;
			//Draws the image:
			g.drawImage(after, (int)(x-0.5*w), (int)(y-0.5*h), null);
		}
	}
	
	public double getTimeLeft(){
		return timeLeft;
	}
}
