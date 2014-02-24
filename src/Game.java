import java.applet.*; 
import java.awt.*; 
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;

public class Game extends Applet implements Runnable, KeyListener {
	
	/**
	 * First attempt at a game, specificly creating proper movements.
	 * Created by Lars Thomasen
	 */
	
	private static final long serialVersionUID = 1337L;
	Thread thread; 

	Dimension dim;									//stores the size of the back buffer 
	Image img; 										//the back buffer object 
	Graphics g; 									//used to draw on the back buffer 
	BufferedImage background, overlayImage, asteroid_1_Image, goldImage, shotImage, exploImage;
	Ship ship; 										//reference to the ship class
	Asteroid[] asteroids;							//array of asteroids
	Shot[] shots; 									//array of shots
	Explosion[] explosions;							//array of explosions
	Loot[] loot;									//array of loot
	
	private double astRadius,minAstVel,maxAstVel;
	private boolean paused, shooting; 						
	private int astNumHits,astNumSplit, numAsteroids, numShots, numLoot, numExplosions, level, gold;
	private long startTime, endTime, framePeriod; 
	
	public void init(){
		resize(1280,720);							
		shots=new Shot[15];							//more than 15 shots will crash the game.
		explosions=new Explosion[10];
		loot=new Loot[10];
		level=0; 									//will be incremented to 1 when first level is set up
		gold=500;		
		
		//Setting up asteroids:
		numAsteroids=0;
		astRadius=64; 								
		minAstVel=.1;								//min possible asteroid speed
		maxAstVel=2;								//max possible asteroid speed
		astNumHits=3;								//how many times asteroid will split
		astNumSplit=2;								//splites into n amount
		
		//Running parameters:
		endTime=0;
		startTime=0;
		framePeriod=10;
		
		addKeyListener(this); 	
		
		//Setting up double buffering:				
		dim=getSize();
		img=createImage(dim.width, dim.height);
		g=img.getGraphics();
		
		try {
			loadGFX();
		} catch (IOException e) {e.printStackTrace();}									//loads all the images.
		setUpNextLevel(); 							//ensures ship class is initiated before paint is.
		
		thread=new Thread(this);
		thread.start();
	}
	
	//Starts a new level with one more asteroid:
	public void setUpNextLevel(){ 
		if (level==0)
			ship=new Ship(dim.getWidth()/2,dim.getHeight()/2, 30, 0.025, false);
		else
			ship=new Ship(dim.getWidth()/2,dim.getHeight()/2, ship.getInfo("ammo")*10, ship.getInfo("speed"), ship.getShieldUpgrade());	//shield upgrade is bugged?
		
		level++;
		//level=10;
		
		numShots=0; 								//no shots on the screen at beginning of level
		numExplosions=0;
		numLoot=0;
		for (int i=0; i<shots.length; i++){
			shots[i]=null;
		}
		
		paused=false;
		shooting=false;
		
		//Create an array large enough to hold the biggest number of asteroids possible on this level (plus one because
		//the split asteroids are created first, then the original one is deleted). The level number is equal to the
		//number of asteroids at it's start:		
		asteroids=new Asteroid[level * (int)Math.pow(astNumSplit,astNumHits-1)+1];
		numAsteroids=level*3/2;
		
		//Create asteroids in random spots on the screen:
		for(int i=0;i<numAsteroids;i++)
			asteroids[i]=new Asteroid(Math.random()*dim.width, 
									  Math.random()*dim.height,astRadius,minAstVel,maxAstVel,astNumHits,astNumSplit,2,asteroid_1_Image);
	}
		 
	public void paint(Graphics gfx){
		g.drawImage(background, 0, 0, dim.width, dim.height, null);
		
		for(int i=0;i<numLoot;i++)					//loop calls draw() for each lootitem
			loot[i].draw(g);
		for(int i=0;i<numShots;i++) 				//loop calls draw() for each shot
	    	shots[i].draw(g);
		for(int i=0;i<numAsteroids;i++)				//loop calls draw() for each asteroid
			asteroids[i].draw(g);
		for(int i=0;i<numExplosions;i++)			//loop calls draw() for each explosion
			explosions[i].draw(g);
		
		
		ship.draw(g); 								//draw the ship
		drawGUI();
		
		gfx.drawImage(img,0,0,this); 				//copies back buffer to the screen
	} 
		 
	public void update(Graphics gfx){ 
		 paint(gfx); 								//call paint without clearing the screen:
	}
	
	public void run(){
		for(;;){
			startTime=System.currentTimeMillis();
			
			//Start next level when all asteroids are destroyed:
			if(numAsteroids<=0){
				setUpNextLevel();
			}
			
			if(!paused){
				//Move the ship:
				ship.move(dim.width,dim.height);				
				
				//Move shots and remove dead shots:
				for(int i=0;i<numShots;i++){
					shots[i].move(dim.width,dim.height);
	
					if(shots[i].getLifeLeft()<=0){
						deleteShot(i);
						i--;
					}
				}
				
				//Move asteroids and check for collisions:
				updateAsteroids();
				//pickUpLoot();
				
				//remove dead explosions:
				for(int i=0;i<numExplosions;i++){
	
					if(explosions[i].getTimeLeft()<=0){
						deleteExplosion(i);
						i--;
					}
				}
				
				//Creating new shot entities:
				if(shooting && ship.canShoot()){
					shots[numShots]=ship.shoot();
					numShots++;
				}
			}
			
			repaint();
			
			//Creates a steady framrate:
			try{
				endTime=System.currentTimeMillis();
				if(framePeriod-(endTime-startTime)>0)
					Thread.sleep(framePeriod-(endTime-startTime));
			}catch(InterruptedException e){}
		}
	}
	
	private void deleteShot(int index){
		numShots--;
		for(int i=index;i<numShots;i++)
			shots[i]=shots[i+1];
		shots[numShots]=null;
	}
	
	private void deleteExplosion(int index){
		numExplosions--;
		for(int i=index;i<numExplosions;i++)
			explosions[i]=explosions[i+1];
		explosions[numExplosions]=null;
	}
	
	private void deleteAsteroid(int index){	
		numAsteroids--;
		//Create Explosion:
		explosions[numExplosions]=(new Explosion(asteroids[index].getX(), asteroids[index].getY(), exploImage));	
		numExplosions++;
		//Create Loot:
		//loot[numLoot]=(new Loot(asteroids[index].getX(), asteroids[index].getY(), 10, goldImage));	
		//numLoot++;
		
		for(int i=index;i<numAsteroids;i++)
			asteroids[i]=asteroids[i+1];
		gold+=asteroids[numAsteroids].golddrop;
		asteroids[numAsteroids]=null;
	}

	private void addAsteroid(Asteroid ast){
		asteroids[numAsteroids]=ast;
		numAsteroids++;
	}
	
	private void pickUpLoot(){
		for(int i=0;i<numLoot;i++){ //Cycle through array of loot:
			//Check for collisions with the ship, pickup if collsion:
			if(loot[i].shipCollision(ship)){
				if (loot[i].getItem()=="gold")
					gold+=loot[i].getValue();
				numLoot--;
				for(int j=i;j<numLoot;j++)
					loot[j]=loot[j+1];
				loot[numLoot]=null;
			}
		}
	}
	
	private void updateAsteroids(){
		for(int i=0;i<numAsteroids;i++){ //Cycle through array of asteroids:
			
			//Move each asteroid:
			asteroids[i].move(dim.width,dim.height);
			
			//Check for collisions with the ship, restart the level if the ship gets hit:
			if(asteroids[i].shipCollision(ship)){
				if(ship.getInfo("shield")==0 && ship.getImmunity()<=0){
					level--;
					gold=0;
					numAsteroids=0;
					return;
				}
				else if(ship.getInfo("shield")==1){
					ship.setShieldUpgrade(false);
				}
			}
			
			//Check for collisions with any of the shots:
			for(int j=0;j<numShots;j++){
				if(asteroids[i].shotCollision(shots[j])){
					
					//If the shot hit an asteroid, delete the shot:
					deleteShot(j);
					
					//Split the asteroid up if needed:
					if(asteroids[i].getHitsLeft()>1){
						for(int k=0;k<asteroids[i].getNumSplit();k++)
							addAsteroid(asteroids[i].createSplitAsteroid(minAstVel,maxAstVel));
					}
					
					//Delete the original asteroid:
					deleteAsteroid(i);
					j=numShots;
					//Creak out of inner loop - it has already been hit, so don�t need to check
					//for collision with other shots:
					i--;
				}
			}
		}
	}
	
	private void loadGFX() throws IOException{
		URL url  = this.getClass().getClassLoader().getResource("sprites/background3.jpg");
		background = ImageIO.read(url);
		url = this.getClass().getClassLoader().getResource("sprites/overlayImage2.png");
		overlayImage    = ImageIO.read(url);
		url = this.getClass().getClassLoader().getResource("sprites/asteroid_1_Image.png");
		asteroid_1_Image  = ImageIO.read(url);
		url = this.getClass().getClassLoader().getResource("sprites/goldImage.gif");
		goldImage  = ImageIO.read(url);
		url = this.getClass().getClassLoader().getResource("sprites/shot.gif");
		shotImage  = ImageIO.read(url);
		url = this.getClass().getClassLoader().getResource("sprites/explo.gif");
		exploImage  = ImageIO.read(url);
	}
	
	private void drawGUI(){
		//Draws LEVEL:
		g.setColor(Color.red); 						
		g.setFont(new Font("default", Font.BOLD, 16));
		g.drawString("Level: " + level,25,25);
		
		//Draws AMMO:
		for(int i=1; i<=((ship.shotsLeft()/10)+0.1); i++){
			g.drawImage(shotImage, (i*20)+5, 35, null);
		}
				
		//Draws GOLD:
		g.drawImage(goldImage, dim.width-130, 1, null);
		g.drawString("Gold: " + gold,dim.width-100,20);
		
		//Draws UPGRADES:
		g.setFont(new Font("default", Font.PLAIN, 10));
		DecimalFormat df = new DecimalFormat("#.###");
		g.drawString("Speed: " + df.format(ship.getInfo("speed")) ,dim.width-100,35);	
		g.drawString("Ammo: " + ship.getInfo("ammo"),dim.width-100,45);
		
		//Draws PAUSE-SCREEN:
		if (!ship.isActive()){
	   		g.drawImage(overlayImage, dim.width/2-(overlayImage.getWidth()/2), 25, null);
		}
	}
	
	//Keylisteners:
	public void keyPressed(KeyEvent e){ 
		if(e.getKeyCode()==KeyEvent.VK_ENTER){ 
			if(!ship.isActive() && !paused) 
				ship.setActive(true); 
			else{ 
				paused=!paused; 					//enter is the pause button 
				if(paused) 							
					ship.setActive(false); 
				else 
					ship.setActive(true); 
			} 
		}else if(paused || !ship.isActive()) 		
			return;						
												
		if(e.getKeyCode()==KeyEvent.VK_UP) 
			ship.setAccelerating(true); 
		if(e.getKeyCode()==KeyEvent.VK_LEFT) 
			ship.setTurningLeft(true); 
		if(e.getKeyCode()==KeyEvent.VK_RIGHT) 
			ship.setTurningRight(true); 
		if(e.getKeyCode()==KeyEvent.VK_SPACE)
			shooting=true;		
	} 

	public void keyReleased(KeyEvent e){ 
		if(e.getKeyCode()==KeyEvent.VK_UP) 
			ship.setAccelerating(false); 
		if(e.getKeyCode()==KeyEvent.VK_LEFT) 
			ship.setTurningLeft(false); 
		if(e.getKeyCode()==KeyEvent.VK_RIGHT) 
			ship.setTurningRight(false); 
		if(e.getKeyCode()==KeyEvent.VK_SPACE)
			shooting=false;
		if(e.getKeyCode()==KeyEvent.VK_1) //Speed
			if (paused && gold>=10){
				gold-=10;
				ship.setSpeedUpgrade(0.0025);
			}
		if(e.getKeyCode()==KeyEvent.VK_2) //Shield
			if (paused && gold>=100){
				gold-=100;
				ship.setShieldUpgrade(true);
			}
		if(e.getKeyCode()==KeyEvent.VK_3) //Ammo
			if (paused && gold>=50){
				gold-=50;
				ship.setAmmoUpgrade(1);
			}
		if(e.getKeyCode()==KeyEvent.VK_4) //Multishot
			if (paused && gold>=200){
				gold-=200;
			}
	}
	
	public void keyTyped(KeyEvent e){
	} 
}
