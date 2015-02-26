import java.awt.*;

public class Shot {
	final double shotSpeed=12; //speed of shot pix per frame
	double x,y,xVelocity,yVelocity; //movement vars
	int lifeLeft; //life of shot before it is gone
	
	public Shot (double x, double y, double angle, double shipXVel, double shipYVel, int lifeLeft){
		this.x=x;
		this.y=y;
		xVelocity=shotSpeed*Math.cos(angle)+shipXVel; //add ships velocity to shot vel
		yVelocity=shotSpeed*Math.sin(angle) + shipYVel;
		this.lifeLeft = lifeLeft;
	}
	
	public void move (int scrnWidth, int scrnHeight) {
		lifeLeft --;
		x+= xVelocity;
		y+= yVelocity;
		
		// makes it appear on opposite side if it leaves window
		if (x<0)
			x+= scrnWidth;
		else if (x>scrnWidth)
			x-=scrnWidth;
		if (y<0)
			y+= scrnHeight;
		else if (y>scrnHeight)
			y -= scrnHeight;
	}
	
	public void draw (Graphics g){
		g.setColor(Color.GREEN);
		g.fillOval((int) (x-.5), (int) (y-.5), 3, 3);
	}
	
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
