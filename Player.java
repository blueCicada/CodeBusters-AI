import java.util.*;
import java.io.*;
import java.math.*;

/**
 * 
 * @author blueCicada
 * 
 * Normally, the fields should always be made private,
 * and only accessible through getters and setters
 * 
 * But since I'm forced to put everything in a single file,
 * I don't want to bloat this code anymore than I need to.
 * Any getters and setters would be just dumb one-liners
 * 
 * I am also the only person working on this code, so I find
 * it easier to work this way.
 *
 */
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
	
	public Player (int bustersPerPlayer, int ghostCount, int myTeamID) {
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
		
		System.err.println("initiate AI");
		System.err.println(this.bustersPerPlayer);
		
		
        for (int i = 0; i < this.bustersPerPlayer; i++) {
        	
        	System.err.println(String.format("\nBuster %d reporting for duty", i));
        	Buster curr = allies.get(i); //presumably
        	
        	ArrayList<Buster> stunnableFoes = new ArrayList<Buster>(); 
        	//foes within stunning range which are not already stunned
        	for (Buster f : foes) {
    			if (distanceTo(curr.x, curr.y, f.x, f.y) <= 1760 && f.state != 2 && f.radarCount == 0) {
    				stunnableFoes.add(f);
    			}
    		}
        	
        	if (curr.stunCooldown == 0 && curr.state != 1 && !stunnableFoes.isEmpty()) {
        		//if we are ready to stun, and we are not carrying a ghost, and there are enemies within stunning range
        		curr.stunCooldown += 20;
        		System.err.println(String.format("Stunning Buster %d", stunnableFoes.get(0).entityID));
        		System.out.println(String.format("STUN %d", stunnableFoes.get(0).entityID));
        		//assuming that the stun worked, the following line should be okay:
        		stunnableFoes.get(0).state = 2; //we set the state manually so other allies don't get confused
        	} else if (curr.state == 3) {//if currently busting a ghost and the above does not apply - 
        		//then continue busting it
        		System.err.println(String.format("Struggling with ghost %d", curr.value));
        		System.out.println(String.format("BUST %d", curr.value));
        	} else if (ghosts.isEmpty() && curr.state == 0) {
        		//if no ghosts can be seen and you are not carrying a ghost
        		System.err.println("I can't see anything!");
        		
        		//de facto centre coordinates
        		int centreX = 1556+9*800;
        		int centreY = 1556+4*800;
        		

        		if (i % 2 == 1) spiralAlpha(curr);
    			else spiralBeta(curr);
        		
        		
        	} else if (curr.state == 1) { //if carrying a ghost, return it to base
        		System.err.println("Carrying ghost lol");
        		//if ((this.myTeamID == 0 && distanceTo(0,0,curr.x,curr.y) < 1600) 
        		//		|| (this.myTeamID == 1 && distanceTo(16001,9001,curr.x,curr.y) < 1600)) {
        		if (distanceTo(16001*this.myTeamID,9001*this.myTeamID,curr.x,curr.y) <= 1600) { //a little hackier
        			System.out.println("RELEASE");
        		} else { //Move towards base
        			System.out.println(String.format("MOVE %d %d", 16001*this.myTeamID, 9001*this.myTeamID));
        		}
        		
        	} else if (!ghosts.isEmpty()){ //chase or bust ghost
        		Ghost target = ghosts.get(0); //first in list
        		double distance = distanceTo(target.x, target.y, curr.x, curr.y);
        		System.err.println(String.format("I am located at (%d, %d)", curr.x, curr.y));
        		System.err.println(String.format("Targeting ghost %d, located at (%d, %d)", 
        				target.entityID, target.x, target.y));
        		System.err.println(String.format("According to my calculations, distance is %f", distance));
        		if (900 <= distance && distance <= 1760) {
        			System.err.println(String.format("Attempting to capture ghost %d", target.entityID));
        			System.out.println(String.format("BUST %d", target.entityID));//capture
        		} else if (distance >= 900) { //advance towards present location of target ghost if not too close
        			System.out.println(String.format("MOVE %d %d", target.x, target.y));
        		} else {
        			System.err.println("hurr durr");
        			System.out.println("MOVE 8000 4500");
        		}
        	} else {
        	    System.err.println("Defaulting, fuck it");
        		//System.out.println("MOVE 8000 4500");
        	    System.out.println(String.format("MOVE %d %d", ((~myTeamID)&00000001)*16001, ((~myTeamID)&00000001)*8001));
        	}
        }
        System.err.println("come at me");
	}
	
	public void spiralAlpha (Buster curr) {
		System.err.println("Patrolling");
		ArrayList<Coordinates> alphaSpiral = new ArrayList<Coordinates>();
		alphaSpiral.add(new Coordinates(1556+9*800+2200, 1556+4*800)); //centre
		alphaSpiral.add(new Coordinates(1556+17*800, 1556+4*800));
		alphaSpiral.add(new Coordinates(1556+17*800, 1556));
		alphaSpiral.add(new Coordinates(1556, 1556));
		alphaSpiral.add(new Coordinates(1556, 1556+3*800));
		alphaSpiral.add(new Coordinates(1556+16*800, 1556+3*800));
		alphaSpiral.add(new Coordinates(1556+16*800, 1556+800));
		alphaSpiral.add(new Coordinates(1556+800, 1556+800));
		alphaSpiral.add(new Coordinates(1556+800, 1556+2*800));
		alphaSpiral.add(new Coordinates(1556+15*800, 1556+2*800));
		
		boolean initiatingPatrol = true;
		
		for (Coordinates pt: alphaSpiral) {
			if (pt.x == curr.destX && pt.y == curr.destY) {
				initiatingPatrol = false;
				break;
			}
		}
		
		if (initiatingPatrol == true) {
			curr.destX = alphaSpiral.get(0).x;
			curr.destY = alphaSpiral.get(0).y;
		} else {
			for (Coordinates pt: alphaSpiral) {
				int index = alphaSpiral.indexOf(pt);
				if (pt.x == curr.x && pt.y == curr.y) { //then we've reached our location
					if (index == 9) { //9 is the last index for this list
						curr.destX = 1556+9*800;
						curr.destY = 1556+4*800;
					} else {
						Coordinates next = alphaSpiral.get(alphaSpiral.indexOf(pt) + 1);
						curr.destX = next.x;
						curr.destY = next.y;
					}
				} 
			}
		}
		System.err.println(String.format("Bound for %d,%d", curr.destX, curr.destY));
		System.out.println(String.format("MOVE %d %d", curr.destX, curr.destY));
	}
	
	public void spiralBeta (Buster curr) {
		System.err.println("Patrolling");
		ArrayList<Coordinates> betaSpiral = new ArrayList<Coordinates>();
		betaSpiral.add(new Coordinates(1556+9*800-2200, 1556+4*800)); //centre
		betaSpiral.add(new Coordinates(1556, 1556+4*800));
		betaSpiral.add(new Coordinates(1556, 1556+8*800));
		betaSpiral.add(new Coordinates(1556+17*800, 1556+8*800));
		betaSpiral.add(new Coordinates(1556+17*800, 1556+5*800));
		betaSpiral.add(new Coordinates(1556+800, 1556+5*800));
		betaSpiral.add(new Coordinates(1556+800, 1556+7*800));
		betaSpiral.add(new Coordinates(1556+16*800, 1556+7*800));
		betaSpiral.add(new Coordinates(1556+16*800, 1556+6*800));
		betaSpiral.add(new Coordinates(1556+2*800, 1556+6*800));
		
		boolean initiatingPatrol = true;
		
		for (Coordinates pt: betaSpiral) {
			if (pt.x == curr.destX && pt.y == curr.destY) {
				initiatingPatrol = false;
				break;
			}
		}
		
		if (initiatingPatrol == true) {
			curr.destX = betaSpiral.get(0).x;
			curr.destY = betaSpiral.get(0).y;
		} else {
			for (Coordinates pt: betaSpiral) {
				int index = betaSpiral.indexOf(pt);
				if (pt.x == curr.x && pt.y == curr.y) { //then we've reached our location
					if (index == 9) { //9 is the last index for this list
						curr.destX = 1556+9*800;
						curr.destY = 1556+4*800;
					} else {
						Coordinates next = betaSpiral.get(betaSpiral.indexOf(pt) + 1);
						curr.destX = next.x;
						curr.destY = next.y;
					}
				} 
			}
		}
		System.err.println(String.format("Bound for %d,%d", curr.destX, curr.destY));
		System.out.println(String.format("MOVE %d %d", curr.destX, curr.destY));
	}
	
	/*public void spiralBeta (Buster curr) {
		ArrayList<Coordinates> toVisit = new ArrayList<Coordinates>();
		toVisit.add(new Coordinates(1556, 1556+4*800));
		toVisit.add(new Coordinates(1556, 1556+8*800));
		toVisit.add(new Coordinates(1556+17*800, 1556+8*800));
		toVisit.add(new Coordinates(1556+17*800, 1556+5*800));
		toVisit.add(new Coordinates(1556+800, 1556+5*800));
		toVisit.add(new Coordinates(1556+800, 1556+7*800));
		toVisit.add(new Coordinates(1556+16*800, 1556+7*800));
		toVisit.add(new Coordinates(1556+16*800, 1556+6*800));
		toVisit.add(new Coordinates(1556+2*800, 1556+6*800));
		
		if (curr.destX == -1 || (curr.destX == 1556+9*800 && curr.destY == 1556+4*800)) {
			curr.destX = toVisit.get(0).x;
			curr.destY = toVisit.get(0).y;
		} else {
			for (Coordinates pt: toVisit) {
				int index = toVisit.indexOf(pt);
				if (pt.x == curr.x && pt.y == curr.y) { //then we've reached our location
					if (index == 8) { //8 is the last index for this list
						curr.destX = 1556+9*800;
						curr.destY = 1556+4*800;
					} else {
						Coordinates next = toVisit.get(toVisit.indexOf(pt) + 1);
						curr.destX = next.x;
						curr.destY = next.y;
					}
				} 
			}
		}
		System.out.println(String.format("MOVE %d %d", curr.destX, curr.destY));
	}*/
	
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
            p.ghosts.clear();
            for (Buster f: p.foes ){
            	f.radarCount++;
            }
            turnLoop:
            for (int i = 0; i < entities; i++) {
            	//System.err.println("fuck");
            	int entityID = in.nextInt();
                int x = in.nextInt();
                int y = in.nextInt();
                int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
                int state = in.nextInt();
                int value = in.nextInt();
                
                //updating our arrays
                if (entityType == GHOST) {
                	
                	/*for (Ghost g: p.ghosts) {
                		//If I could have more files to put my classes in, I'd make getters for these
                		if (g.entityID == entityID) {
                			g.x = x;
                			g.y = y;
                			g.value = value;
                			continue turnLoop;
                		}
                	}*/
                	
                	p.ghosts.add(new Ghost(entityID, x, y, value));
                	
                } else if (entityType == p.myTeamID) {
                	
                	for (Buster a: p.allies) {
                		//If I could have more files to put my classes in, I'd make getters for these
                		if (a.entityID == entityID) {
                			a.x = x;
                			a.y = y;
                			a.state = state;
                			a.value = value;
                			if (a.stunCooldown != 0) a.stunCooldown--;
                			if (a.state == 1) { //this buster is carrying a ghost
                				//this loop was intended to remove the carried ghost from the ghost list
                				//but I've since changed my code to reconstruct the ghost list from scratch
                				//every turn, so this may no longer be necessary
                				/*
                				for (Iterator<Ghost> iterator = p.ghosts.iterator(); iterator.hasNext();) {
                				    Ghost g = iterator.next();
                				    if (g.entityID == value) {
                				        // Remove the current element from the iterator and the list.
                				        iterator.remove();
                				        continue turnLoop;
                				    }
                				}*/
                			}
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
                			f.radarCount = 0;
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
	int stunCooldown;
	int team;//entityType;
	int state; // For busters: 0=idle, 1=carrying a ghost, 2=stunned, 3=in the process of trapping a ghost
	int value; // For busters: Ghost id being carried. According to DeafGecko, this is -1 if not stunned or carrying
	int destX;
	int destY;
	int radarCount;
	
	public Buster (int entityID, int x, int y, int state, int value, int entityType) {
		super(entityID, x, y);
		this.stunCooldown = 0;//if you ever decide to reconstruct the allies list from scratch, be careful with this
		this.state = state;
		this.value = value;
		this.team = entityType; //should only be either TEAM_0_BUSTER or TEAM_1_BUSTER, should never be GHOST
		this.radarCount = 0;
	}
}

class Ghost extends Entity {
	int value; //For ghosts: number of busters attempting to trap this ghost.
	
	public Ghost (int entityID, int x, int y, int value) {
		super(entityID, x, y);
		this.value = value;
	}
}

class Coordinates {
	int x;
	int y;
	
	public Coordinates (int x, int y) {
		this.x = x;
		this.y = y;
	}
}

/*class Matrix {
	ArrayList<ArrayList<Coordinates>> matrix;
	
	public Matrix () {
		matrix = new ArrayList<ArrayList<Coordinates>>();
		
		for (int i = 0; i < 17; i++) {
			ArrayList<Coordinates> row = new ArrayList<Coordinates>();
			for (int j = 0; j < 8; j++) {
				row.add(new Coordinates(1555+i*800,1555+j*800));
			}
		}
	}
	
	public Coordinates get (int matrixX, int matrixY) {
		return matrix.get(matrixX).get(matrixY);
	}
}*/

//At the moment, I'm thinking of having some PQs to store
//what entities can be seen by the current buster
//For each iteration of the dumbAI() loop, we create a new
//PQ, because different AIs will see different things
//So we can have stunnableFoes as a PQ
//and also have a visibleGhosts PQ
//Actually, we might even make the ghosts list just a PQ
//That way, a buster can still chase a ghost that it cannot see
//(but perhaps another buster can see it)
//Hopefully, this will allow for more efficient behaviour,
//as each buster will go for the entity closest to it
//(whether it's trying to stun a foe or trap a ghost)
//However, this also means that the busters will tend to work
//independently, rather than cooperating to capture a ghost faster
//which may or may not be a good thing
/*class distanceComparator<Entity> implements Comparator<Entity> {
   
   @Override
   public int compare (Entity entity1, Entity entity2) {
      if 
   }
   
}*/

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



