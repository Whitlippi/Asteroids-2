import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class AsteroidsGame extends Applet implements Runnable,KeyListener{

Thread thread;
long startTime, endTime, framePeriod; 
Dimension dim; // stores the size of the back buffer
Image img; // the back buffer object
Graphics g; // used to draw on the back buffer
Ship ship;
boolean paused;
Shot[] shots;
int numShots;
boolean shooting;
Asteroid[] asteroids; 
int numAsteroids; 
double astRadius,minAstVel,maxAstVel; 
int astNumHits,astNumSplit;
int level; //the current level number
int totalShots;
int shotsOnTarget;
double hitPrct;

	public void init(){
		resize(500,500);
		shots=new Shot[41]; //41 max shots life period plus 1
		numAsteroids=0;
		level=0; //will be incremented to 1 when first level is set up
		astRadius=60; //values used to create the asteroids
		minAstVel=.5;
		maxAstVel=5;
		astNumHits=3;
		astNumSplit=2;
		endTime=0;
		startTime=0;
		framePeriod=25;
		totalShots=0;
		shotsOnTarget=0;
		addKeyListener(this); //tell it to listen for KeyEvents
		dim=getSize();
		img=createImage(dim.width, dim.height);
		g=img.getGraphics();
		thread=new Thread(this);
		thread.start();
	}
	
	public void setUpNextLevel(){ //starts a new level with one more asteroid
		level++;
		ship=new Ship(250,250,0,.35,.98,.1,12);
		numShots=0; //no shots on the screen at beginning of level
		paused=false;
		shooting=false;
		asteroids=new Asteroid[level * (int)Math.pow(astNumSplit,astNumHits-1)+1]; 
		numAsteroids=level;
		//create asteroids in random spots on the screen
		for(int i=0;i<numAsteroids;i++)
			asteroids[i]=new Asteroid(Math.random()*dim.width,Math.random()*dim.height,astRadius,minAstVel, maxAstVel,astNumHits,astNumSplit);
	} 

	public void paint(Graphics gfx){
		g.setColor(Color.black);
		g.fillRect(0,0,500,500);
		
		for(int i=0; i<numShots; i++)  //draw shots
			shots[i].draw(g);
		
		for(int i=0;i<numAsteroids;i++)
			asteroids[i].draw(g);
		
		ship.draw(g); //draw the ship
		g.setColor(Color.cyan); //Display the level number in top left corner
		g.drawString("Level " + level,20,20); 
		g.drawString("Hit Percentage " + (int) hitPrct + "%",380,20);
		
		gfx.drawImage(img,0,0,this); 
	}
	
	public void update(Graphics gfx){
		paint(gfx); // call paint without clearing the screen
	} 

	public void run(){
		for(;;){
			startTime=System.currentTimeMillis();
			
			//start next level when all asteroids are destroyed
			if(numAsteroids<=0)
				setUpNextLevel(); 
			
			if(!paused){
				ship.move(dim.width,dim.height); //move the ship
				for (int i=0; i<numShots; i++){
					shots[i].move(dim.width, dim.height); 
					if (shots[i].getLifeLeft()<=0){
						//removes expired shots
						deleteShot(i);
						i--;
					}
				}
				
				if (totalShots == 0){
					hitPrct=0;
				}else{
				hitPrct = shotsOnTarget/totalShots*100;
				}
				
				updateAsteroids();
				
				if (shooting && ship.canShoot()){
					shots[numShots]=ship.shoot();
					numShots++;
					totalShots++;
				}
			}
			
			repaint();
			try{
				endTime=System.currentTimeMillis();
				if(framePeriod-(endTime-startTime)>0)
					Thread.sleep(framePeriod-(endTime-startTime));
			}catch(InterruptedException e){
			}
		}
	}
	
	
	private void deleteShot(int index){
		//delete shot and alter array
		numShots--;
		for (int i = index; i<numShots;i++)
			shots[i]=shots[i+1];
		shots[numShots]=null;
	}
	
	private void deleteAsteroid(int index){
		//delete asteroid and shift ones after it up in the array
		numAsteroids--;
		for(int i=index;i<numAsteroids;i++)
			asteroids[i]=asteroids[i+1];
			asteroids[numAsteroids]=null;
	}
	
	private void addAsteroid(Asteroid ast){
		//adds the asteroid passed in to the end of the array
		asteroids[numAsteroids]=ast;
		numAsteroids++;
	}
	
	private void updateAsteroids(){
		for(int i=0;i<numAsteroids;i++){
			// move each asteroid
			asteroids[i].move(dim.width,dim.height);
			//check for collisions with the ship, restart the
			//level if the ship gets hit
			if(asteroids[i].shipCollision(ship)){
				level--; //restart this level
				numAsteroids=0;
				return;
			}
			//check for collisions with any of the shots
			for(int j=0;j<numShots;j++){
				if(asteroids[i].shotCollision(shots[j])){
					//if the shot hit an asteroid, delete the shot
					deleteShot(j);
					shotsOnTarget++;
					//split the asteroid up if needed
					if(asteroids[i].getHitsLeft()>1){
						for(int k=0;k<asteroids[i].getNumSplit();k++)
							addAsteroid(
									asteroids[i].createSplitAsteroid(
											minAstVel,maxAstVel));
					}
					//delete the original asteroid
					deleteAsteroid(i);
					j=numShots; 
				}
			}
		}
	}
	
	public void keyPressed(KeyEvent e){
		if(e.getKeyCode()==KeyEvent.VK_ENTER){
			
			if(!ship.isActive() && !paused)
				ship.setActive(true);
			else{
				paused=!paused; //enter is the pause button
		 if(paused) // grays out the ship if paused
			 ship.setActive(false);
		 else
			 ship.setActive(true);
			}
		}else if(paused || !ship.isActive()) //if the game is paused or ship is inactive, do not respond to the controls except for enter to unpause
			return;
		else if(e.getKeyCode()==KeyEvent.VK_UP)
			ship.setAccelerating(true);
		else if(e.getKeyCode()==KeyEvent.VK_LEFT)
			ship.setTurningLeft(true);
		else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
			ship.setTurningRight(true);
		else if(e.getKeyCode()==KeyEvent.VK_CONTROL)
			shooting=true;
	}
	
	public void keyReleased(KeyEvent e){
		if(e.getKeyCode()==KeyEvent.VK_UP)
			ship.setAccelerating(false);
		else if(e.getKeyCode()==KeyEvent.VK_LEFT)
			ship.setTurningLeft(false);
		else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
			ship.setTurningRight(false);
		else if(e.getKeyCode()==KeyEvent.VK_CONTROL)
			shooting=false;
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	} 
} 





