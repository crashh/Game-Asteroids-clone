import java.applet.*;
import java.awt.*; 
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;

public class Game extends Applet implements Runnable, KeyListener {

    private Thread thread;

	private Dimension dim;									//stores the size of the back buffer
	private Image img; 										//the back buffer object
	private Graphics g; 									//used to draw on the back buffer
	private BufferedImage background;
    private BufferedImage overlayImage;
    private BufferedImage asteroidImage;
    private BufferedImage goldImage;
    private BufferedImage shotImage;
    private BufferedImage exploImage;
    AudioClip asteroidBoom = null;
	private Ship ship; 										//reference to the ship class
	private Asteroid[] asteroids;							//array of asteroids
	private Shot[] shots; 									//array of shots
    private Smoke[] smokes;						        	//array of smoke
	private Explosion[] explosions;							//array of explosions
    AudioClip theme = null;
	
	private double astRadius,minAstVel,maxAstVel;
	private boolean paused, shooting; 						
	private int astNumHits;
    private int astNumSplit;
    private int numAsteroids;
    private int numShots;
    private int numExplosions;
    private int level;
    private int gold, highScore;
	private long startTime, endTime, framePeriod;
    private int numSmoke;
    private int smokeCntDown;

    /*
    * Constructor for the entire Game, sets all the values which can be used to tweak
    * different aspects of the game.
    * */
	public void init(){
		resize(1280,720);
		shots=new Shot[25];							//more than 25 shots/explosions will crash the game.
		explosions=new Explosion[35];
        smokes=new Smoke[19];

        smokeCntDown=0;

		level=0; 									//will be incremented to 1 when first level is set up
		gold=0;

		//Setting up asteroids:
		numAsteroids=0;
		astRadius=64;
		minAstVel=.1;								//min possible asteroid speed
		maxAstVel=.95;								//max possible asteroid speed
		astNumHits=3;								//how many times asteroid will split
		astNumSplit=3;								//asteroid splits into n amount

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
			loadGFX();                              //loads all the images.
		} catch (IOException e) {e.printStackTrace();}

		setUpNextLevel(); 							//ensures ship class is initiated before paint is.

		thread=new Thread(this);
		thread.start();
	}
	
	/*
	* Starts a new level with one more asteroid than the previous level:
	* */
    void setUpNextLevel(){
		if (level==0) {
            ship = new Ship(dim.getWidth() / 2, dim.getHeight() / 2, 30, 0.020, false);
            highScore=0;
        }else
			ship=new Ship(dim.getWidth()/2,dim.getHeight()/2, ship.getInfo("ammo")*10, ship.getInfo("speed"), ship.getShieldUpgrade());	//shield upgrade is bugged?
		
		level++;

        //Clear screen of shots/explosions on level switch.
		numShots=0;
        numSmoke=0;
        numExplosions=0;
        for (int i=0; i<shots.length; i++){
			shots[i]=null;
		}
        for (int i=0; i<smokes.length; i++){
            smokes[i]=null;
        }
		
		paused=false;
		shooting=false;
		
		//Create an array large enough to hold the biggest number of asteroids possible on this level (plus one because
		//the split asteroids are created first, then the original one is deleted). The level number is equal to the
		//number of asteroids at it's start:		
		//asteroids=new Asteroid[(level * (int)Math.pow(astNumSplit,astNumHits-1)+1)+15];
        asteroids=new Asteroid[(level * (int)Math.pow(astNumSplit,astNumHits-1)+1)];
		numAsteroids=(level*3/2);
		
		//Create asteroids in random spots on the screen:
		for(int i=0;i<numAsteroids;i++)
			asteroids[i]=new Asteroid(Math.random()*dim.width, 
									  Math.random()*dim.height,astRadius,minAstVel,maxAstVel,astNumHits,astNumSplit,1, asteroidImage);
	}

    /*
    * Paints everything to the canvas:
    * */
	public void paint(Graphics gfx){
		g.drawImage(background, 0, 0, dim.width, dim.height, null);

		for(int i=0;i<numShots;i++) 				//loop calls draw() for each shot
	    	shots[i].draw(g);
        //loop calls draw() for each smoke
        for (Smoke smoke : smokes)
            if (smoke != null)
                smoke.draw(g);
		for(int i=0;i<numAsteroids;i++)				//loop calls draw() for each asteroid
			if (asteroids[i] != null)
                asteroids[i].draw(g);
		for(int i=0;i<numExplosions;i++)			//loop calls draw() for each explosion
            if (explosions[i] != null)
                explosions[i].draw(g);
		
		ship.draw(g); 								//draw the ship
		drawGUI();
		
		gfx.drawImage(img,0,0,this); 				//copies back buffer to the screen
	} 

    /*
    * Same as paint, but calls paint without clearing the screen.
    * */
	public void update(Graphics gfx){ 
		 paint(gfx);
	}
	
	public void run(){
		while(true){
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

                //Creating new smoke entities: //todo: refactor this!
                if(ship.getAccelerating() && smokeCntDown<=0){
                    smokes[numSmoke]=ship.smoke();
                    numSmoke++;
                    smokeCntDown=11;
                } else if(smokeCntDown<=0){
                    smokes[numSmoke] = null;
                    smokeCntDown=11;
                    numSmoke++;
                }
                if (numSmoke >= 19)
                    numSmoke = 0;
                smokeCntDown--;
			}
			
			repaint();

			//Creates a steady framerate:
			try{
				endTime=System.currentTimeMillis();
				if(framePeriod > (endTime - startTime)) {
                    Thread.sleep(framePeriod - (endTime - startTime));
                }
			}catch(InterruptedException ignored){}
		}
	}
	
	private void deleteShot(int index){
		numShots--;
        System.arraycopy(shots, index + 1, shots, index, numShots - index);
		shots[numShots]=null;
	}
	
	private void deleteExplosion(int index){
		numExplosions--;
        System.arraycopy(explosions, index + 1, explosions, index, numExplosions - index);
		explosions[numExplosions]=null;
	}
	
	private void deleteAsteroid(int index){
        if (asteroids[index].getHitsLeft()==1)
            gold+=asteroids[index].goldDrop;
            highScore+=asteroids[index].goldDrop;
		numAsteroids--;
		//Create Explosion:
		explosions[numExplosions++]=(new Explosion(asteroids[index].getX(), asteroids[index].getY(), exploImage, asteroidBoom, 2)); //2=Duration

        System.arraycopy(asteroids, index + 1, asteroids, index, numAsteroids - index);
		asteroids[numAsteroids]=null;
	}

	private void addAsteroid(Asteroid ast){
		asteroids[numAsteroids]=ast;
		numAsteroids++;
	}

    private void updateAsteroids(){
		for(int i=0;i<numAsteroids;i++){ //Cycle through array of asteroids:
			
			//Move each asteroid:
			asteroids[i].move(dim.width,dim.height);
			
			//Check for collisions with the ship, restart the level if the ship gets hit:
			if(asteroids[i].shipCollision(ship)){
				if(ship.getInfo("shield")==0 && ship.getImmunity()<=0){
                    explosions[numExplosions++]=(new Explosion(ship.getX(), ship.getY(), exploImage, asteroidBoom, 4)); //3=Duration
					level=0;
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
					//deleteShot(j);

					//Split the asteroid up if needed:
					if(asteroids[i].getHitsLeft()>1){
						for(int k=0;k<asteroids[i].getNumSplit();k++)
							addAsteroid(asteroids[i].createSplitAsteroid(minAstVel,maxAstVel));
					}
					
					//Delete the original asteroid:
					deleteAsteroid(i);

					//Creak out of inner loop - it has already been hit, so dont need to check
					//for collision with other shots:
					i--;
                    break;
				}
			}
		}
	}
	
	private void loadGFX() throws IOException{
        //OBS: this (this.getClass().getResource) works in IDE and on a webserver, NOT LOCALLY!
		background = ImageIO.read(this.getClass().getResource("sprites/background3.jpg"));
		overlayImage = ImageIO.read(this.getClass().getResource("sprites/overlayImage2.png"));
		asteroidImage = ImageIO.read(this.getClass().getResource("sprites/asteroid_1_Image.png"));
		goldImage = ImageIO.read(this.getClass().getResource("sprites/goldImage.gif"));
		shotImage = ImageIO.read(this.getClass().getResource("sprites/shot.gif"));
		exploImage = ImageIO.read(this.getClass().getResource("sprites/explo.gif"));
        asteroidBoom = Applet.newAudioClip(this.getClass().getResource("wav/Boom.wav"));
	}
	
	private void drawGUI(){
		//Draws LEVEL:
		g.setColor(Color.red); 						
		g.setFont(new Font("default", Font.BOLD, 16));
		g.drawString("Level: " + level,25,25);
        g.drawString("High Score: " + highScore,25,690);
		
		//Draws AMMO:
        if ((ship.shotsLeft()/10) < 10)
		    for(int i=1; i<=((ship.shotsLeft()/10)+0.1); i++){
		    	g.drawImage(shotImage, (i*20)+5, 35, null);
		    }
        else
            for(int i=1; i<=10; i++){
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
				gold-=0;
			}
	}
	
	public void keyTyped(KeyEvent e){
	} 
}
