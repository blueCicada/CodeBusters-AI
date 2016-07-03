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
 * I'd also normally use the strategy pattern to make it easy
 * to switch between AIs, but that's not going to happen in
 * this single file.
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
    private int turnCount; //does not count enemy turns
    
	
	private List<Ghost> ghosts;
	//private PriorityQueue<Ghost> ghosts;
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
		
		ghosts = new /*PriorityQueue<Ghost>(25, new StaminaComparator());*/ArrayList<Ghost>();
		allies = new ArrayList<Buster>();
		foes = new ArrayList<Buster>();
	}
	
	/**
	 * An AI that does literally nothing except sit
	 * Only use for testing purposes
	 * (eg. testing how another player's AI would behave uninhibited)
	 */
	public void sittingDuckAI() {
		System.err.println("Sitting duck");
		for (int i = 0; i < this.bustersPerPlayer; i++) {
			Buster curr = allies.get(i);
			System.out.println(String.format("MOVE %d %d", curr.x, curr.y));
		}
	}
	

	/**
	 * Does not exactly replicate Romka's AI's behaviour (I couldn't even if I tried)
	 * But retains some elements such as:
	 * 	- rushing out at the start to claim territory
	 *  - prioritising ghosts with low stamina
	 *  - endgame escort + sentry at base
	 */
	public void romkaClone1() {
		System.err.println("Romka Clone 1");
		
		for (int i = 0; i < this.bustersPerPlayer; i++) {
			System.err.println(String.format("\nBuster %d reporting for duty", i));
			Buster curr = allies.get(i);
			
			ArrayList<Buster> stunnableFoes = new ArrayList<Buster>(); 
        	//foes within stunning range which are not already stunned
			ArrayList<Buster> dangerFoes = new ArrayList<Buster>(); 
        	//foes outside stunning range which are not already stunned but within sighting range
        	//inside the 'danger zone'
        	//will be more effective when we later implement a way to approximately keep track of enemy stuncounts
        	for (Buster f : foes) {
    			if (distanceTo(curr.x, curr.y, f.x, f.y) <= 1760 && f.state != 2) {
    				stunnableFoes.add(f);
    			} else if (distanceTo(curr.x, curr.y, f.x, f.y) > 1760 && f.state != 2) {
    				dangerFoes.add(f);
    			}
    		}
		
			if (this.turnCount == 1) {
				int rushRadius = 8000;
				double angle = Math.toRadians(90/(this.allies.size()));
				curr.destX = (int) Math.round(curr.x + ((this.myTeamID == 0) ? 1 : -1)*(rushRadius*Math.cos((i*angle)+(angle/2))));
				curr.destY = (int) Math.round(curr.y + ((this.myTeamID == 0) ? 1 : -1)*(rushRadius*Math.sin((i*angle)+(angle/2))));
				System.err.println(/*Math.round(/*curr.x + /*((this.myTeamID == 0) ? 1 : -1)**/(/*8000**/Math.cos(1*90)))/*)*/;
				System.err.println(/*Math.round(/*curr.y + /*((this.myTeamID == 0) ? 1 : -1)**/(/*8000**/Math.sin(1*angle)))/*)*/;
				System.err.println(String.format("Ang %f, curr loc (%d,%d),"
				+"destX %d, destY %d, %d", angle, curr.x, curr.y, curr.destX, curr.destY, i));
			}
			
			if (curr.stunCooldown == 0 && !stunnableFoes.isEmpty()) {
				curr.stunCooldown += 20;
        		System.err.println(String.format("Stunning Buster %d", stunnableFoes.get(0).entityID));
        		System.out.println(String.format("STUN %d", stunnableFoes.get(0).entityID));
			/*} else if (curr.state == 1) { //if carrying, return to base
				System.err.println("Carrying ghost lol");
        		if (distanceTo(16001*this.myTeamID,9001*this.myTeamID,curr.x,curr.y) <= 1600) {
        			System.out.println("RELEASE");
        		} else { //Move towards base
        			System.out.println(String.format("MOVE %d %d", 16001*this.myTeamID, 9001*this.myTeamID));
        		}*/
			} else if (curr.x != curr.destX || curr.y != curr.destY) {
				System.out.println(String.format("MOVE %d %d %d", curr.destX, curr.destY, curr.entityID));
			} else {
				System.err.println("Transferring control to dumbAI");
				this.dumbAI();
			}
		}
	}
	
	/**
	 * Step 1: Check if we can stun any enemies (and we are not carrying a ghost_
	 * 		Might actually be better if you do it regardless of whether you're carrying a ghost
	 * Step 2: Check
	 */
	public void dumbAI() {
		// Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");
		// MOVE x y | BUST id | RELEASE
		
		System.err.println("initiate AI");
		System.err.println(this.bustersPerPlayer);
		
		boolean enableRush = false;
		
		if (this.turnCount < 11) {
			enableRush = true;
			System.err.println("RUSHING ENABLED");
		}
		
        for (int i = 0; i < this.bustersPerPlayer; i++) {
        	
        	System.err.println(String.format("\nBuster %d reporting for duty", i));
        	Buster curr = allies.get(i); //presumably
        	
        	PriorityQueue<Ghost> sortedGhosts = new PriorityQueue<Ghost>(25, new /*Distance*/TimeComparator(curr.x, curr.y));
        	for (Ghost g: ghosts) {
        		sortedGhosts.add(g);
        	}
        	
        	System.err.println("Check sortedGhost PQ"); //not guaranteed to print in order
            for (Iterator<Ghost> iterator = sortedGhosts.iterator(); iterator.hasNext();) {
                Ghost g = iterator.next();
                System.err.println(
                		String.format("Ghost %d | Stamina %d | %d engaged | Location (%d, %d) | %f units from me | Cost:%f",
                				g.entityID, g.state, g.value, g.x, g.y, distanceTo(g.x, g.y, curr.x, curr.y), this.timeCost(g, curr)));
                /*System.err.println(
                		String.format("Ghost %d | Stamina %d | %d engaged | Location (%d, %d) | %f units away\n"
                				+ "Predicted turn cost: %f", g.entityID, g.state, g.value, 
                				g.x, g.y, distanceTo(g.x, g.y, curr.x, curr.y), 
                				(g.value == 0) ? 0 : (g.state/g.value) + (distanceTo(g.x, g.y, curr.x, curr.y)/800)));*/
            }
            System.err.println("End sorteGhost PQ");
        	
        	ArrayList<Buster> stunnableFoes = new ArrayList<Buster>(); 
        	//foes within stunning range which are not already stunned
        	for (Buster f : foes) {
    			if (distanceTo(curr.x, curr.y, f.x, f.y) <= 1760 && f.state != 2) {
    				if (f.state == 3) stunnableFoes.add(0, f); //prioritise carrying busters
    				else stunnableFoes.add(f);
    			}
    		}
        	
        	if (this.turnCount == 1) {
        		int rushRadius = 8000;
				double angle = Math.toRadians(90/(this.allies.size()));
				curr.destX = (int) Math.round(curr.x + ((this.myTeamID == 0) ? 1 : -1)*(rushRadius*Math.cos((i*angle)+(angle/2))));
				curr.destY = (int) Math.round(curr.y + ((this.myTeamID == 0) ? 1 : -1)*(rushRadius*Math.sin((i*angle)+(angle/2))));
				System.err.println(/*Math.round(/*curr.x + /*((this.myTeamID == 0) ? 1 : -1)**/(/*8000**/Math.cos(1*90)))/*)*/;
				System.err.println(/*Math.round(/*curr.y + /*((this.myTeamID == 0) ? 1 : -1)**/(/*8000**/Math.sin(1*angle)))/*)*/;
				System.err.println(String.format("Ang %f, curr loc (%d,%d),"
				+"destX %d, destY %d, %d", angle, curr.x, curr.y, curr.destX, curr.destY, i));
			}
			
        	/*BEGIN ACTION CONTROL BLOCK***/
        	if (curr.state == 2) {
        		System.err.println("O fuck I am stunned");
        		System.out.println("RELEASE");
        	} else if (curr.stunCooldown == 0 && /*curr.state != 1 && */!stunnableFoes.isEmpty()) {
        		//if we are ready to stun, and we are not carrying a ghost, and there are enemies within stunning range
        		curr.stunCooldown += 20;
        		System.err.println(String.format("Stunning Buster %d", stunnableFoes.get(0).entityID));
        		System.out.println(String.format("STUN %d", stunnableFoes.get(0).entityID));
        		//assuming that the stun worked, the following line should be okay:
        		stunnableFoes.get(0).state = 2; //we set the state manually so other allies don't get confused
        	} else if ((curr.x != curr.destX || curr.y != curr.destY) && enableRush) { //rush to target
				System.out.println(String.format("MOVE %d %d %d", curr.destX, curr.destY, curr.entityID));
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
        		

        		if (i % 2 == 1) /*rectAlpha(curr);*//*cleaverAlpha(curr);*/spiralAlpha(curr);
    			else /*rectBeta(curr);*//*cleaverBeta(curr);*/spiralBeta(curr);
        		
        		
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
        		//Ghost target = ghosts.get(0); //first in list
        		//Ghost target = ghosts.peek();
        		while (!sortedGhosts.isEmpty()) {
	        		Ghost target = sortedGhosts.poll();
	        		double distance = distanceTo(target.x, target.y, curr.x, curr.y);
	        		System.err.println(String.format("I am located at (%d, %d)", curr.x, curr.y));
	        		System.err.println(String.format("Targeting ghost %d, located at (%d, %d)", 
	        				target.entityID, target.x, target.y));
	        		System.err.println(String.format("According to my calculations, distance is %f", distance));
	        		if (900 <= distance && distance <= 1760) {
	        			System.err.println(String.format("Attempting to capture ghost %d", target.entityID));
	        			System.out.println(String.format("BUST %d", target.entityID));//capture
	        			break;
	        		} else if (distance >= 900) { //advance towards present location of target ghost if not too close
	        			System.out.println(String.format("MOVE %d %d", target.x, target.y));
	        			break;
	        		} else if (sortedGhosts.isEmpty()) {
	        			System.err.println("hurr durr");
	        			System.out.println(String.format("MOVE %d %d", ((~myTeamID)&00000001)*16001, ((~myTeamID)&00000001)*8001));
	        			break;
	        		}
        		}
        		/*else {
        			System.err.println("hurr durr");
        			System.out.println("MOVE 8000 4500");
        		}*/
        	} else {
        	    System.err.println("Defaulting, fuck it");
        		//System.out.println("MOVE 8000 4500");
        	    System.out.println(String.format("MOVE %d %d", ((~myTeamID)&00000001)*16001, ((~myTeamID)&00000001)*8001));
        	}
        }
        System.err.println("come at me");
	}
	
	public void spiralAlpha (Buster curr) {
		System.err.println("Patrolling spiralAlpha");
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
		System.err.println("Patrolling spiralBeta");
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
	
	public void cleaverAlpha (Buster curr) {
		System.err.println("Patrolling cleaverAlpha");
		ArrayList<Coordinates> alphaCleaver = new ArrayList<Coordinates>();
		alphaCleaver.add(new Coordinates(1556+9*800+2200, 1556+4*800)); //centre
		alphaCleaver.add(new Coordinates(1556+16*800, 1556+4*800)); //point
		alphaCleaver.add(new Coordinates(1556+17*800, 1556+5*800)); //point
		alphaCleaver.add(new Coordinates(1556+17*800, 1556+4*800));
		alphaCleaver.add(new Coordinates(1556+17*800, 1556));
		alphaCleaver.add(new Coordinates(1556, 1556));
		alphaCleaver.add(new Coordinates(1556, 1556+2*800));
		alphaCleaver.add(new Coordinates(1556+9*800, 1556+2*800));
		
		int lastIndex = alphaCleaver.size() - 1; //should be 5 here since we start counting from 0
		
		boolean initiatingPatrol = true;
		
		for (Coordinates pt: alphaCleaver) {
			if (pt.x == curr.destX && pt.y == curr.destY) {
				initiatingPatrol = false;
				break;
			}
		}
		
		if (initiatingPatrol == true) {
			curr.destX = alphaCleaver.get(0).x;
			curr.destY = alphaCleaver.get(0).y;
		} else {
			for (Coordinates pt: alphaCleaver) {
				int index = alphaCleaver.indexOf(pt);
				if (pt.x == curr.x && pt.y == curr.y) { //then we've reached our location
					if (index == lastIndex) {
						curr.destX = 1556+9*800;
						curr.destY = 1556+4*800;
					} else {
						Coordinates next = alphaCleaver.get(alphaCleaver.indexOf(pt) + 1);
						curr.destX = next.x;
						curr.destY = next.y;
					}
				} 
			}
		}
		System.err.println(String.format("Bound for %d,%d", curr.destX, curr.destY));
		System.out.println(String.format("MOVE %d %d", curr.destX, curr.destY));
	}
	
	public void cleaverBeta (Buster curr) {
		System.err.println("Patrolling cleaverBeta");
		ArrayList<Coordinates> betaCleaver = new ArrayList<Coordinates>();
		betaCleaver.add(new Coordinates(1556+9*800-2200, 1556+4*800)); //centre
		betaCleaver.add(new Coordinates(1556+800, 1556+4*800)); //point
		betaCleaver.add(new Coordinates(1556, 1556+5*800)); //point
		betaCleaver.add(new Coordinates(1556, 1556+4*800));
		betaCleaver.add(new Coordinates(1556, 1556+8*800));
		betaCleaver.add(new Coordinates(1556+17*800, 1556+8*800));
		betaCleaver.add(new Coordinates(1556+17*800, 1556+6*800));
		betaCleaver.add(new Coordinates(1556+9*800, 1556+6*800));
		
		int lastIndex = betaCleaver.size() - 1;//should be 5
		
		boolean initiatingPatrol = true;
		
		for (Coordinates pt: betaCleaver) {
			if (pt.x == curr.destX && pt.y == curr.destY) {
				initiatingPatrol = false;
				break;
			}
		}
		
		if (initiatingPatrol == true) {
			curr.destX = betaCleaver.get(0).x;
			curr.destY = betaCleaver.get(0).y;
		} else {
			for (Coordinates pt: betaCleaver) {
				int index = betaCleaver.indexOf(pt);
				if (pt.x == curr.x && pt.y == curr.y) { //then we've reached our location
					if (index == lastIndex) {
						curr.destX = 1556+9*800;
						curr.destY = 1556+4*800;
					} else {
						Coordinates next = betaCleaver.get(betaCleaver.indexOf(pt) + 1);
						curr.destX = next.x;
						curr.destY = next.y;
					}
				} 
			}
		}
		System.err.println(String.format("Bound for %d,%d", curr.destX, curr.destY));
		System.out.println(String.format("MOVE %d %d", curr.destX, curr.destY));
	}
	
	public double distanceTo (int toX, int toY, int fromX, int fromY) {
		return Math.sqrt(Math.pow((double)toX-fromX, 2) + Math.pow((double)toY-fromY, 2));
	}
	
	public double timeCost(Ghost ghost, Buster curr) {
		//return ((ghost.value == 0) ? 0 : (ghost.state/ghost.value)) + (distanceTo(ghost.x, ghost.y)/800);
		/*
		 * Let one turn be the unit of time, s.
		 * Let one unit the unit of distance, m.
		 * Let one unit of health be hp.
		 * 
		 * Let P be the predicted total time cost to reach and capture ghost. (see note below)
		 * Let R be the distance to the current location of the ghost.
		 * Let D be the distance to the closest point within a 1760m radius of the current location of the ghost. (see comment below)
		 * Let T be the time cost to reach said point.
		 * Let B be the number of busters currently engaging the ghost.
		 * Let H be the current stamina of the ghost
		 * And let H' be the predicted stamina remaining upon arrival (see warning below)
		 * 
		 * P = T (s) + (H' (hp))/(B+1 (hp/s))
		 * 		= (T + H'/(B+1)) (s)
		 * 		In English:
		 * 		The predicted time cost is equal to the number of turns it takes to reach the closest point within
		 * 		a 1760m radius of the ghost PLUS the number of turns required to bring the stamina that remains upon
		 * 		arrival to zero.
		 * 
		 * D = (R > 1760) ? (R - 1760) : R
		 * 		In English:
		 * 		The distance to the closest point within a 1760m radius of the ghost's current location is either
		 * 		the distance to the current location of the ghost MINUS 1760 (if we are currently outside the radius)
		 * 		OR it is zero (if we are already inside the radius)
		 * 
		 * T = (D (m)/800 (m/s))
		 * 		= (D/800) (s)
		 * 		In English:
		 * 		Time taken to reach the closest point within a 1760m radius of the ghost is the distance to reach
		 * 		that point, divided by 800m/s (800m is the maximum distance a buster can travel in a single turn)
		 * 
		 * H' = H (hp) - T (s) * B (hp/s)
		 * 		= (H - T*B) (hp)
		 * 		In English:
		 * 		The amount of stamina the ghost is predicted to have left by the time you arrive is equal to its
		 * 		current stamina MINUS the product to the number of turns required to get there, and the number of
		 * 		busters currently engaging the ghost. 
		 * 
		 * NOTE: This time cost does not take into account the amount of time required to return to base
		 * WARNING: This does not account for deadlocks, in which case s' should be ZERO, and it would
		 * 		probably be beneficial to go for the ghost
		 * COMMENT: May want to consider applying ceil to the distance, since a half move is still a move
		 * 		May also want to modify logic to account for the case where the buster is within 900m of the ghost
		 * 		Though this may not be necessary, since I'm already ignoring where the ghost may move to
		 */
		double distanceToGhost = distanceTo(ghost.x, ghost.y,curr.x,curr.y);
		double distanceToBustingRange = (distanceToGhost >= 1760) ? distanceToGhost - 1760 : 0;
		double travelCost = distanceToBustingRange/800;
		double remainingStamina = ghost.state - (travelCost*ghost.value); //may need ternary operator to handle negative cases
		double predictedTimeCost = travelCost + (remainingStamina/(ghost.value + 1));
		return predictedTimeCost;
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
            p.turnCount++; //turns shall start from 1, and we do not count enemy turns
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
                	
                	p.ghosts.add(new Ghost(entityID, x, y, state, value));
                	
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
            
            /*System.err.println("Check ghost PQ");
            for (Iterator<Ghost> iterator = p.ghosts.iterator(); iterator.hasNext();) {
                Ghost g = iterator.next();
                System.err.println(
                		String.format("Ghost %d | Stamina %d | %d engaged | Location (%d, %d)",
                				g.entityID, g.state, g.value, g.x, g.y));
            }
            System.err.println("End ghost PQ");*/
            
            //p.sittingDuckAI();
            
            //if (p.turnCount < 11)p.romkaClone1();
            /*else */p.dumbAI();
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
	int state; //For ghosts: remaining stamina points.
	
	public Ghost (int entityID, int x, int y, int state, int value) {
		super(entityID, x, y);
		this.state = state;
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
class DistanceComparator implements Comparator<Entity> {
	
	int x, y;
	
	/**
	 * Constructor for DistanceComparator
	 * @param x the abscissa of current buster
	 * @param y the ordinate of current buster
	 */
	public DistanceComparator(int x, int y) {
		this.x = x;
		this.y = y;	
	}
	
	public double distanceTo (int toX, int toY) {
		return Math.sqrt(Math.pow((double)toX-this.x, 2) + Math.pow((double)toY-this.y, 2));
	}

	@Override
	public int compare (Entity entity1, Entity entity2) {
		if (distanceTo(entity1.x, entity1.y) < distanceTo(entity2.x, entity2.y)) return -1;
		if (distanceTo(entity1.x, entity1.y) < distanceTo(entity2.x, entity2.y)) return 1;
		return 0;
	}
   
}
class StaminaComparator implements Comparator<Ghost> {  
	@Override
	public int compare (Ghost ghost1, Ghost ghost2) {
		if (ghost1.state < ghost2.state) return -1;
		if (ghost1.state > ghost2.state) return 1;
		return 0;
	}
}
class TimeComparator implements Comparator<Ghost> {
	
	int x, y;
	
	/**
	 * Constructor for DistanceComparator
	 * @param x the abscissa of current buster
	 * @param y the ordinate of current buster
	 */
	public TimeComparator(int x, int y) {
		this.x = x;
		this.y = y;	
	}
	
	public double timeCost(Ghost ghost) {
		//return ((ghost.value == 0) ? 0 : (ghost.state/ghost.value)) + (distanceTo(ghost.x, ghost.y)/800);
		/*
		 * Let one turn be the unit of time, s.
		 * Let one unit the unit of distance, m.
		 * Let one unit of health be hp.
		 * 
		 * Let P be the predicted total time cost to reach and capture ghost. (see note below)
		 * Let R be the distance to the current location of the ghost.
		 * Let D be the distance to the closest point within a 1760m radius of the current location of the ghost. (see comment below)
		 * Let T be the time cost to reach said point.
		 * Let B be the number of busters currently engaging the ghost.
		 * Let H be the current stamina of the ghost
		 * And let H' be the predicted stamina remaining upon arrival (see warning below)
		 * 
		 * P = T (s) + (H' (hp))/(B+1 (hp/s))
		 * 		= (T + H'/(B+1)) (s)
		 * 		In English:
		 * 		The predicted time cost is equal to the number of turns it takes to reach the closest point within
		 * 		a 1760m radius of the ghost PLUS the number of turns required to bring the stamina that remains upon
		 * 		arrival to zero.
		 * 
		 * D = (R > 1760) ? (R - 1760) : R
		 * 		In English:
		 * 		The distance to the closest point within a 1760m radius of the ghost's current location is either
		 * 		the distance to the current location of the ghost MINUS 1760 (if we are currently outside the radius)
		 * 		OR it is zero (if we are already inside the radius)
		 * 
		 * T = (D (m)/800 (m/s))
		 * 		= (D/800) (s)
		 * 		In English:
		 * 		Time taken to reach the closest point within a 1760m radius of the ghost is the distance to reach
		 * 		that point, divided by 800m/s (800m is the maximum distance a buster can travel in a single turn)
		 * 
		 * H' = H (hp) - T (s) * B (hp/s)
		 * 		= (H - T*B) (hp)
		 * 		In English:
		 * 		The amount of stamina the ghost is predicted to have left by the time you arrive is equal to its
		 * 		current stamina MINUS the product to the number of turns required to get there, and the number of
		 * 		busters currently engaging the ghost. 
		 * 
		 * NOTE: This time cost does not take into account the amount of time required to return to base
		 * WARNING: This does not account for deadlocks, in which case s' should be ZERO, and it would
		 * 		probably be beneficial to go for the ghost
		 * COMMENT: May want to consider applying ceil to the distance, since a half move is still a move
		 * 		May also want to modify logic to account for the case where the buster is within 900m of the ghost
		 * 		Though this may not be necessary, since I'm already ignoring where the ghost may move to
		 */
		double distanceToGhost = distanceTo(ghost.x, ghost.y);
		double distanceToBustingRange = (distanceToGhost >= 1760) ? distanceToGhost - 1760 : 0;
		double travelCost = distanceToBustingRange/800;
		double remainingStamina = ghost.state - (travelCost*ghost.value);
		//may need ternary operator to handle negative cases
		/*^actually, you might not need to, it may even be beneficial in some cases,
		 * eg. this makes it more urgent to get to a deadlocked ghost, as remaining stamina
		 * clocks over into the negative
		 * But on the contrary, this may falsely prioritise ghosts which will be gone before the buster can reach it
		 */
		double predictedTimeCost = travelCost + (remainingStamina/(ghost.value + 1));
		return predictedTimeCost;
	}
	
	public double distanceTo (int toX, int toY) {
		return Math.sqrt(Math.pow((double)toX-this.x, 2) + Math.pow((double)toY-this.y, 2));
	}
	
	@Override
	public int compare (Ghost ghost1, Ghost ghost2) {
		if (timeCost(ghost1) < timeCost(ghost2)) return -1;
		if (timeCost(ghost1) > timeCost(ghost2)) return 1;
		return 0;
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



