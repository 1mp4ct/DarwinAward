import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

/** Minimal creature that blindly moves and attacks.*/
public class BrainsForRoads extends Creature {
	
	Simulator publicSimulator;

	BattleField battlefield; 
	
    @Override
	public String getAuthorName() {
        return "Heuboe";
    }

    @Override
	public String getDescription() {
        return "Minimal creature that blindly moves and attacks.";
    }
	
    @Override
	public void run() {
    	
    	initBot();
    	
    	File f = new File("C:\\Users\\nikodemusz\\Desktop\\TestData\\teams.txt");
    	
    	try {
			BufferedReader fr = new BufferedReader(new FileReader(f));
			System.out.println(fr.readLine());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println(battlefield.printField());
    	
    	Dijkstra spAlgo = new Dijkstra();
    	
    	this.observeUnkownNeighbors();
    	
    	
        while (true) {
        	this.attack();
            this.move(spAlgo.findShortestPathDir(this.getPosition(), new Point(27, 2), battlefield));
            battlefield.update(this.observe());
            this.observeUnkownNeighbors();
            
            System.out.println(battlefield.printField());
        }
    }

    private void initBot() {
    	System.out.println("Starting BrainsForRoads");
    	battlefield = new BattleField(super.getMapDimensions());
    }
    
    
    /**
     * Moves the bot 1 square to the given direction
     * 
     * @param d direction
     */
    private void move(Direction d) {
    	this.rotate(d);
    	Point prevLoc = this.getPosition();
    	if (this.moveForward()) {
    		battlefield.update(new Observation(prevLoc, this.getGameTime()), this.observeSelf());
    	}
    	
    }
    
    private void observeUnkownNeighbors() {
    	for (Direction dir : Direction.values()) {
    		if (!battlefield.getSquare(this.getPosition(), dir).getObservation().isPresent()) {
    			this.rotate(dir);
    			battlefield.update(this.observe());
    		}
    	}
    }
    
    @Override
    public void onDeath() {
    	System.err.println("Im dead");
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
    
    
    public class BattleField {
    	
    	private int width;
    	private int height;
    	
    	private final List<Square> squares = new ArrayList<>();
    	
    	public BattleField(Dimension dim) {
			this.width = (int) dim.getWidth();
			this.height = (int) dim.getHeight();
			
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					squares.add(new Square(new Point(x, y)));
				}
			}		
		}
    	
    	public Square getSquare(Point p) {
    		return this.getSquare(p.x, p.y);
    	}
    	
    	public Square getSquare(int x, int y) {
    		
    		try {
    			return squares.get(y * width + x);	
    		} catch (IndexOutOfBoundsException e) {
    			return null;
    		}	
    	}
    	
    	public Square getSquare(Point p, Direction dir) {
    		switch(dir) {
			case EAST:
				return this.getSquare(p.x + 1, p.y);
			case NORTH:
				return this.getSquare(p.x, p.y - 1);
			case SOUTH:
				return this.getSquare(p.x, p.y + 1);
			case WEST:
				return this.getSquare(p.x - 1, p.y);
			default:
				throw new IllegalStateException();
    		}	
    	}
    	
    	public Set<Square> getSquares(Point p, Direction... dirs) {
    		Set<Square> neighbors = new HashSet<>();
    		for (Direction dir : dirs) {
    			neighbors.add(this.getSquare(p, dir));
    		}
    		return neighbors;
    	}
    	
    	public void update(Observation... observations) {
    		for (Observation obs : observations) {
    			this.getSquare(obs.position).setObservation(obs);
    		}
    	}
    	
    	public String printField() {
    		StringBuilder sb = new StringBuilder();
    		for (int i = 0; i < squares.size(); i++) {
    			if (i % width == 0 && i > 0) {
    				sb.append("\n");
    			}
    			sb.append(squares.get(i).getLabel());
    		}
    		return sb.toString();
    	}
    	
    	public class Square {
    		
    		private final Point loc;
    		private Observation currObs;
    		
    		public Square(final Point loc) {
				this.loc = loc;
			}
    		
    		public char getLabel() {
    			if (currObs == null) {
    				return 'V';
    			}
    			
    			switch (currObs.type) {
				case CREATURE:
					return 'C';
				case EMPTY:
					return ' ';
				case HAZARD:
					return 'H';
				case WALL:
					return '#';
				default:
					return '?';
    			}
    		}
    		
    		public Optional<Observation> getObservation() {
    			return Optional.ofNullable(currObs);
    		}
    		
    		public boolean setObservation(Observation newObs) {
    			if (newObs == null || !this.getLocation().equals(newObs.position)) {
    				throw new IllegalArgumentException("Invalid observation");
    			}
    			
    			// only update currObs if newObs is more recent
    			if (currObs != null && currObs.time > newObs.time) {
    				return false;
    			} else {
    				currObs = newObs;
    				return true;
    			}
    		}
    		
    		public Point getLocation() {
    			return this.loc;
    		}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + getEnclosingInstance().hashCode();
				result = prime * result + ((loc == null) ? 0 : loc.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Square other = (Square) obj;
				if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
					return false;
				if (loc == null) {
					if (other.loc != null)
						return false;
				} else if (!loc.equals(other.loc))
					return false;
				return true;
			}

			private BattleField getEnclosingInstance() {
				return BattleField.this;
			}
    		
    		
    	}
    }
    
    public class Dijkstra {
    	
    	
    	public Direction findShortestPathDir(Point origin, Point dest, BattleField field) {
    		
    		Map<Point, Vertex> vertices = new HashMap<>();
    		
    		PriorityQueue<Vertex> pq = new PriorityQueue<>(
    				(v1, v2) -> Long.compare(
    						v1.getWeight() + this.getDistance(v1.getSquare().getLocation(), dest) * 10,
    						v2.getWeight() + this.getDistance(v2.getSquare().getLocation(), dest) * 10));
    		
    		Vertex originV = new Vertex(field.getSquare(origin), 0);
    		Vertex destV = new Vertex(field.getSquare(dest));
    		pq.add(originV);
    		pq.add(destV);
    		
    		int steps = 0;
    		
    		while (!pq.isEmpty() && steps < 500 && pq.peek().getWeight() <= destV.getWeight()) {
    			Vertex curr = pq.poll();
    			
    			steps++;
    			
    			for (Direction dir : Direction.values()) {
    				Point neighborLoc = this.getNeighbor(curr.getSquare().getLocation(), dir);
    				Vertex neighbor = vertices.get(neighborLoc);
    				if (neighbor == null) {
    					BattleField.Square s = field.getSquare(neighborLoc);
    					if (s == null) {
    						continue;
    					}
    					neighbor = new Vertex(s);
    					vertices.put(neighbor.getSquare().getLocation(), neighbor);
    				}
    				
    				long weightThroughCurr = evalWeightThroughCurr(curr, neighbor);
    				
    				
    				if (neighbor.getWeight() > weightThroughCurr) {
    					pq.remove(neighbor);
    					neighbor.setWeight(weightThroughCurr);
    					neighbor.setPrev(curr);
    					pq.add(neighbor);
    				}
    			}
    		}
    		
    		for (Direction dir : Direction.values()) {
    			Point neighborLoc = this.getNeighbor(originV.getSquare().getLocation(), dir);
    			Vertex originNeighbor = vertices.get(neighborLoc);
    			if (originNeighbor == null) {
    				continue;
    			}
    			if (originV.equals(originNeighbor.getPrev())) {
    				System.out.println(dir);
    				return dir;
    			}
    		}
    		return null;
    	}
    	
    	private Point getNeighbor(Point p, Direction dir) {
    		switch(dir) {
    		case EAST:
    			return new Point(p.x + 1, p.y);
    		case NORTH:
    			return new Point(p.x, p.y - 1);
    		case SOUTH:
    			return new Point(p.x, p.y + 1);
    		case WEST:
    			return new Point(p.x - 1, p.y);
    		default:
    			throw new IllegalStateException();
    		}
    	}
    	
    	private long getDistance(Point p1, Point p2) {
    		return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    	}
    	
    	private long evalWeightThroughCurr(Vertex curr, Vertex neighbor) {
			Optional<Observation> oObs = neighbor.getSquare().getObservation();
			
			if (oObs.isPresent()) {
				Observation obs = oObs.get();
				switch (obs.type) {
				case CREATURE:
					return curr.getWeight() + 1;
				case EMPTY:
					return curr.getWeight() + 1;
				case HAZARD: case WALL: default:
					return Long.MAX_VALUE;				
				}
			} else {
				return curr.getWeight() + 1000;
			}
		}

		public class Vertex {
    		
    		private final BattleField.Square square;
    		private long weight;
    		private Vertex prev;
    		
    		public Vertex(BattleField.Square square) {
				this(square, Long.MAX_VALUE);
			}
    		
    		public Vertex(BattleField.Square square, long weight) {
				this.square = square;
				this.weight = weight;
			}
    		
    		public BattleField.Square getSquare() {
    			return square;
    		}
    		
    		public long getWeight() {
    			return weight;
    		}
    		
    		public void setWeight(long weight) {
    			this.weight = weight;
    		}
    		
    		public Vertex getPrev() {
    			return prev;
    		}
    		
    		public void setPrev(Vertex v) {
    			this.prev = v;
    		}
    		
    		@Override
    		public String toString() {
    			return "Vertex(" + this.getSquare().getLocation().x + "|" + this.getSquare().getLocation().y + " - " + this.getWeight() + ")";
    		}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + getEnclosingInstance().hashCode();
				result = prime * result + ((square == null) ? 0 : square.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				Vertex other = (Vertex) obj;
				if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
					return false;
				if (square == null) {
					if (other.square != null)
						return false;
				} else if (!square.equals(other.square))
					return false;
				return true;
			}

			private Dijkstra getEnclosingInstance() {
				return Dijkstra.this;
			}
    		
    		
    	}
    }
}
