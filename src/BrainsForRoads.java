import java.awt.Dimension;
import java.awt.Point;
import java.io.ObjectInputStream.GetField;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Minimal creature that blindly moves and attacks.*/
public class BrainsForRoads extends Creature {
	
	Simulator sim;
	Entity[][] map;
	
    @Override
	public void run() {
    	
    	hackIntoSimulator();
    	
    	Playground p = new Playground(map);
    	System.out.println(p.print());
    	
    	System.out.println(this.observe().length);
    	
        while (true) {
            if (! moveForward()) {
                attack();
                
                turnLeft();
                
                System.out.println(this.getPosition());
            }
        }
    }

    @Override
	public String getAuthorName() {
        return "Darwin SDK";
    }

    @Override
	public String getDescription() {
        return "Minimal creature that blindly moves and attacks.";
    }
    
    private void hackIntoSimulator() {
    	try {
    		
			Field fSim = this.getClass().getSuperclass().getDeclaredField("simulator");
			fSim.setAccessible(true);
			sim = (Simulator) fSim.get(this);
			
			Field fMap = sim.getClass().getDeclaredField("map");
			fMap.setAccessible(true);
			map = (Entity[][]) fMap.get(sim);
			
			
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
    }
    
    private String printMap() {
    	StringBuilder sb = new StringBuilder();
    	
    	for (int y = 0; y < map[0].length; y++) {  
    		for (int x = 0; x < map.length; x++) {
    			if (map[x][y] != null) {
    				sb.append(map[x][y].getLabel());
    				if (map[x][y].getLabel() == 'T') {
    					System.out.println(map[x][y].getClass());
    				}
    			} else {
    				sb.append(" ");
    			}
    			
    		}
    		sb.append("\n");
    	}
    	return sb.toString();
    }
    
    /**
     * Moves the bot 1 square to the given direction
     * 
     * @param d direction
     */
    private void move(Direction d) {
    	this.rotate(d);
    	this.moveForward();
    }
    
    /**
     * Rotates the bot to the given direction if necessary
     * 
     * @param targetDir target direction
     * @return true if it actually rotated, false if not
     */
    private boolean rotate(Direction targetDir) {
    	Direction currDir = this.getDirection();
    	if (targetDir == currDir) {
    		return false;
    	}
    	
    	if (targetDir == currDir.left()) {
    		this.turnLeft();
    	} else if (targetDir == currDir.right()) {
    		this.turnRight();
    	} else {
    		this.turnLeft();
    		this.turnLeft();
    	}
    	
    	return true;
    }
    
    
    private class Vertex {
    	
    	private final Point loc;

		private Map<Direction, Vertex> neighbors;
    	private Long weight;
    	private Type type;
    	
		public Vertex(final Point p, Type type) {
			this.loc = p;
			this.type = type;
		}

    	
    	public Long getWeightToNeighbor(Direction dir) {
    		switch(neighbors.get(dir).getType()) {
			case EMPTY: case CREATURE:
				return 1L;
			case WALL: case HAZARD: default:
				return Long.MAX_VALUE;
    		}
    	}
    	
    	public Point getLoc() {
			return loc;
		}


		public Type getType() {
			return type;
		}


		public void setType(Type type) {
			this.type = type;
		}
		
		public void setNeighbors(Map<Direction, Vertex> neighbors) {
			this.neighbors = neighbors;
		}
    	
    
    	
    	
    	
    }
    
    private class Playground {
    	
    	private int height;
    	private int width;
    	
    	private final List<Vertex> squares = new ArrayList<>();
 
		public Playground(Entity[][] map) {
			this.height = map.length;
			this.width = map[0].length;
			
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					squares.add(new Vertex(new Point(x, y), map[y][x] != null ? map[y][x].getType() : Type.EMPTY));
				}	
			}
			
			// init neighbor relationships
			for (Vertex v: squares) {
				Map<Direction, Vertex> neighbors = new HashMap<>();
				for (Direction dir : Direction.values()) {
					this.getVertexAtPoint(v.getLoc(), dir).ifPresent(neighbor -> neighbors.put(dir, neighbor));
				}
				v.setNeighbors(neighbors);
			}
		}
		
		public Optional<Vertex> getVertexAtPoint(Point loc) {
			if (loc.x < 0 || loc.y < 0 || loc.x > width || loc.y > height) {
				return Optional.empty();
			}
			try {
				return Optional.of(squares.get(loc.x * loc.y + loc.x));
			} catch (Exception e) {
				return Optional.empty();
			}
		}
		
		public Optional<Vertex> getVertexAtPoint(Point origin, Direction dir) {
			Point offset = null;
			
			switch(dir) {
			case EAST:
				offset = new Point(origin.x + 1, origin.y);
				break;
			case NORTH:
				offset = new Point(origin.x, origin.y - 1);
				break;
			case SOUTH:
				offset = new Point(origin.x, origin.y + 1);
				break;
			case WEST:
				offset = new Point(origin.x - 1, origin.y);
				break;
			}
			return this.getVertexAtPoint(offset);
		}
		
		public String print() {
			StringBuilder sb = new StringBuilder();
	    	
	    	for (int y = 0; y < height; y++) {  
	    		for (int x = 0; x < width; x++) {
	    			switch(this.getVertexAtPoint(new Point(x, y)).get().getType()) {
					case CREATURE:
						sb.append("C");
						break;
					case EMPTY:
						sb.append(" ");
					case HAZARD:
						sb.append("X");
						break;
					case WALL:
						sb.append("#");
						break;
					default:
						sb.append("ä");
						break;
	    			
	    			}
	    			
	    		}
	    		sb.append("\n");
	    	}
	    	return sb.toString();
		}

    	
    }
}
