import com.sun.media.jfxmedia.AudioClip;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

class Explosion {
	
	private final double x;
    private final double y;
	private double timeLeft;
	private final BufferedImage sprite;
    private AudioClip asteroidBoom;

	public Explosion(double x, double y, BufferedImage sprite, com.sun.media.jfxmedia.AudioClip sound, int timeLeft){
		this.x=x;
		this.y=y;
		this.timeLeft=timeLeft;				//Duration of explosion:
		this.sprite=sprite;
        asteroidBoom = sound;
        playSound_Boom();
	}
	
	public void draw(Graphics g){
		if (timeLeft>0){	
			int w = sprite.getWidth();
			int h = sprite.getHeight();
			BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			
			at.translate(0.5*h, 0.5*w);
			at.scale((timeLeft*20/w)*2, (timeLeft*20/h)*2);		//Scales according to timeLeft to create an 'animation'.
			at.translate(-0.5*w, -0.5*h);
			
			AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			after = op.filter(sprite, after);
		
			timeLeft-=0.1;
			//Draws the image:
			g.drawImage(after, (int)(x-0.5*w), (int)(y-0.5*h), null);
		}
	}

    public void playSound_Boom(){
        asteroidBoom.play();
    }
	
	public double getTimeLeft(){
		return timeLeft;
	}
}
