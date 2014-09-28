import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Smoke {

    //Variables used in movement:
	private double x;
    private double y;
    private final double angle;
    private double delay;

	private BufferedImage sprite = null;

	public Smoke(double x, double y, double angle, BufferedImage sprite){
		this.x=x-15;
		this.y=y-10;
		this.angle = angle;
		this.sprite = sprite;
        delay = 2;
	}
	
	public void draw(Graphics g){
		if (delay <= 0) {
            //Handles rotation of the image:
            double rotationRequired = angle;
            double locationX = sprite.getWidth() / 2;
            double locationY = sprite.getHeight() / 2;
            AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

            //Draws the image:
            g.drawImage(op.filter(sprite, null), (int) (x), (int) (y), null);
        }
        delay -= 0.1;
	}
	
	//Getters:
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
}