import java.util.*;
import java.io.*;
import java.math.*;

class Player {

	//These constants are entity types
	private static final int TEAM_0_BUSTER = 0;
	private static final int TEAM_1_BUSTER = 1;
	private static final int GHOST = -1;
	
	private int bustersPerPlayer; // the number of busters you control
    private int ghostCount; // the number of ghosts on the map
    private int myTeamID; // if this is 0, your base is on the top left of the map, if it is one, on the bottom right
    
	
	private List<Ghost> ghosts;
	private List<Buster> allies;
	private List<Buster> foes;
	
	/*public void setTeam (int myTeamID) {
		this.myTeamID = myTeamID;
	} public void setGhostCount (int ghostCount) {
		this.ghostCount = ghostCount;
	} public void setBustersPerPlayer (int bustersPerPlayer) {
		this.bustersPerPlayer = bustersPerPlayer;
	}*/
	
	public Player (int myTeamID, int ghostCount, int bustersPerPlayer) {
		this.myTeamID = myTeamID;
		this.ghostCount = ghostCount;
		this.bustersPerPlayer = bustersPerPlayer;
		
		ghosts = new ArrayList<Ghost>();
		allies = new ArrayList<Buster>();
		foes = new ArrayList<Buster>();
	}
	
	/**
	 * Step 1: Check if 
	 */
	public void dumbAI() {
		// Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");
		// MOVE x y | BUST id | RELEASE
		
        for (int i = 0; i < this.bustersPerPlayer; i++) {
        	
        	Buster curr = allies.get(i); //presumably
        	
        	//move to centre of arena if no ghosts can be seen and you are not carrying a ghost
        	if (ghosts.isEmpty() && curr.state == 0) {
        		System.out.println("MOVE 8000 4500");
        	} else if (curr.state == 1) { //if carrying a ghost
        		//if ((this.myTeamID == 0 && distanceTo(0,0,curr.x,curr.y) < 1600) 
        		//		|| (this.myTeamID == 1 && distanceTo(16001,9001,curr.x,curr.y) < 1600)) {
        		if (distanceTo(16001*this.myTeamID,9001*this.myTeamID,curr.x,curr.y) < 1600) { //a little hackier
        			System.out.println("RELEASE");
        		} else { //Move towards base
        			System.out.println(String.format("MOVE %d %d", 16001*this.myTeamID, 9001*this.myTeamID));
        		}
        	} else {
        		Ghost target = ghosts.get(0); //first in list
        		double distance = distanceTo(target.x, target.y, curr.x, curr.y);
        		if (900 <= distance && distance <= 1760) {
        			System.out.println(String.format("BUST %d", target.entityID));//capture
        		} else { //advance towards present location of target ghost
        			System.out.println(String.format("MOVE %d %d", target.x, target.y));
        		}
        	}
        }
	}
	
	public double distanceTo (int toX, int toY, int fromX, int fromY) {
		return Math.sqrt(Math.pow((double)toX-fromX, 2) + Math.pow((double)toY-fromY, 2));
	}
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		
		int bu = in.nextInt();
        int gh = in.nextInt();
        int my = in.nextInt();
        
        Player p = new Player(bu, gh, my);
        
        // game loop
        while (true) {
            int entities = in.nextInt(); // the number of busters and ghosts visible to you
            turnLoop:
            for (int i = 0; i < entities; i++) {
            	int entityID = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
                int state = in.nextInt();
                int value = in.nextInt();
                
                //updating our arrays
                if (entityType == GHOST) {
                	
                	for (Ghost g: p.ghosts) {
                		//If I could have more files to put my classes in, I'd make getters for these
                		if (g.entityID == entityID) {
                			g.x = x;
                			g.y = y;
                			g.value = value;
                			continue turnLoop;
                		}
                	}
                	
                	p.ghosts.add(new Ghost(entityID, x, y, value));
                	
                } else if (entityType == p.myTeamID) {
                	
                	for (Buster a: p.allies) {
                		//If I could have more files to put my classes in, I'd make getters for these
                		if (a.entityID == entityID) {
                			a.x = x;
                			a.y = y;
                			a.state = state;
                			a.value = value;
                			continue turnLoop;
                		}
                	}
                	
                	p.allies.add(new Buster(entityID, x, y, state, value, entityType));
                	//I'm worried that I will need to change to order of the allies in the list
                	//if the input decides to be a bitch and change up the order of the busters with each turn
                } else {
                	
                	for (Buster f: p.foes) {
                		//If I could have more files to put my classes in, I'd make getters for these
                		if (f.entityID == entityID) {
                			f.x = x;
                			f.y = y;
                			f.state = state;
                			f.value = value;
                			continue turnLoop;
                		}
                	}
                	
                	p.foes.add(new Buster(entityID, x, y, state, value, entityType));
                }
            }
            p.dumbAI();
        }
	}

}

//entityID and team do not change

class Entity {
	int entityID; // buster id or ghost id
	int x;
	int y;
	
	public Entity (int entityID, int x, int y) {
		this.entityID = entityID; 
		this.x = x;
		this.y = y;
	}
}

class Buster extends Entity {
	int team;//entityType;
	int state; // For busters: 0=idle, 1=carrying a ghost.
	int value; // For busters: Ghost id being carried.
	
	public Buster (int entityID, int x, int y, int state, int value, int entityType) {
		super(entityID, x, y);
		this.state = state;
		this.value = value;
		this.team = entityType; //should only be either TEAM_0_BUSTER or TEAM_1_BUSTER, should never be GHOST
	}
}

class Ghost extends Entity {
	int value; //For ghosts: number of busters attempting to trap this ghost.
	
	public Ghost (int entityID, int x, int y, int value) {
		super(entityID, x, y);
		this.value = value;
	}
}

//Original
/*import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 **/
/*class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int bustersPerPlayer = in.nextInt(); // the amount of busters you control
        int ghostCount = in.nextInt(); // the amount of ghosts on the map
        int myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right

        // game loop
        while (true) {
            int entities = in.nextInt(); // the number of busters and ghosts visible to you
            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // buster id or ghost id
                int x = in.nextInt();
                int y = in.nextInt(); // position of this buster / ghost
                int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
                int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost.
                int value = in.nextInt(); // For busters: Ghost id being carried. For ghosts: number of busters attempting to trap this ghost.
            }
            for (int i = 0; i < bustersPerPlayer; i++) {

                // Write an action using System.out.println()
                // To debug: System.err.println("Debug messages...");
                System.out.println("MOVE 8000 4500"); // MOVE x y | BUST id | RELEASE
            }
        }
    }
}*/