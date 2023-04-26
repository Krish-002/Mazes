import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Arrays;
import tester.Tester;
import java.util.HashMap;
import java.util.Comparator;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

//useful methods
class Utils {
  /// In ArrayUtils
  // EFFECT: Sorts the provided list according to the given comparator
  <T> void mergeSort(ArrayList<T> arr, Comparator<T> comp) {
    // Create a temporary array
    ArrayList<T> temp = new ArrayList<T>();
    // Make sure the temporary array is exactly as big as the given array
    for (int i = 0; i < arr.size(); i = i + 1) {
      temp.add(arr.get(i));
    }
    mergeSortHelp(arr, temp, comp, 0, arr.size());
  }

  // EFFECT: Sorts the provided list in the region [loIdx, hiIdx)
  // according to the given comparator.
  // Modifies both lists in the range [loIdx, hiIdx)
  <T> void mergeSortHelp(ArrayList<T> source, ArrayList<T> temp, Comparator<T> comp, int loIdx,
      int hiIdx) {
    // Step 0: stop when finished
    if (hiIdx - loIdx <= 1) {
      return; // nothing to sort
    }
    // Step 1: find the middle index
    int midIdx = (loIdx + hiIdx) / 2;
    // Step 2: recursively sort both halves
    mergeSortHelp(source, temp, comp, loIdx, midIdx);
    mergeSortHelp(source, temp, comp, midIdx, hiIdx);
    // Step 3: merge the two sorted halves
    merge(source, temp, comp, loIdx, midIdx, hiIdx);
  }

  /// Merges the two sorted regions [loIdx, midIdx) and [midIdx, hiIdx) from
  /// source
  // into a single sorted region according to the given comparator
  // EFFECT: modifies the region [loIdx, hiIdx) in both source and temp
  <T> void merge(ArrayList<T> source, ArrayList<T> temp, Comparator<T> comp, int loIdx, int midIdx,
      int hiIdx) {
    int curLo = loIdx; // where to start looking in the lower half-list
    int curHi = midIdx; // where to start looking in the upper half-list
    int curCopy = loIdx; // where to start copying into the temp storage
    while (curLo < midIdx && curHi < hiIdx) {
      if (comp.compare(source.get(curLo), source.get(curHi)) <= 0) {
        // the value at curLo is smaller, so it comes first
        temp.set(curCopy, source.get(curLo));
        curLo = curLo + 1; // advance the lower index
      }
      else {
        // the value at curHi is smaller, so it comes first
        temp.set(curCopy, source.get(curHi));
        curHi = curHi + 1; // advance the upper index
      }
      curCopy = curCopy + 1; // advance the copying index
    }
    // copy everything that's left -- at most one of the two half-lists still has
    // items in it
    while (curLo < midIdx) {
      temp.set(curCopy, source.get(curLo));
      curLo = curLo + 1;
      curCopy = curCopy + 1;
    }
    while (curHi < hiIdx) {
      temp.set(curCopy, source.get(curHi));
      curHi = curHi + 1;
      curCopy = curCopy + 1;
    }
    // copy everything back from temp into source
    for (int i = loIdx; i < hiIdx; i = i + 1) {
      source.set(i, temp.get(i));
    }
  }

  // determines if a given edge creates a cycle with any of the other
  Vertex find(HashMap<Vertex, Vertex> map, Vertex v) {
    return map.get(v);
  }

  // replaces the value for a given vertex key in the given hash map
  void union(HashMap<Vertex, Vertex> map, Vertex v1, Vertex v2) {
    map.put(v1, v2);
  }
}

//compares two edges by weight
class EdgeComp implements Comparator<Edge> {

  // compares two edges by weight: returns -1 if o1 weighs less,
  // 0 if they weigh the same, 1 elsewise
  public int compare(Edge o1, Edge o2) {
    return o1.weight - o2.weight;
  }
}

//represents a vertex in a graph
class Vertex {
  int label;
  int x;
  int y;
  ArrayList<Edge> outEdges;
  boolean left = true;
  boolean right = true;
  boolean top = true;
  boolean bottom = true;
  Color col;

  Vertex(int label) {
    this.label = label;
  }

  Vertex(int label, int x, int y) {
    this.label = label;
    this.x = x;
    this.y = y;
    this.outEdges = new ArrayList<Edge>();
    this.col = Color.white;
  }

  // produces an image of this vertex
  WorldImage drawBlock(int w, int h) {
    return new RectangleImage(w, h, OutlineMode.SOLID, this.col);
  }

  // produces an image of the border around this vertex
  WorldImage drawWalls(int w, int h, Color col) {
    WorldImage block = drawBlock(w, h);
    WorldImage vLine = new LineImage(new Posn(0, h), col);
    WorldImage hLine = new LineImage(new Posn(w, 0), col);

    if (left) {
      block = new BesideImage(vLine, block);
    }
    if (right) {
      block = new BesideImage(block, vLine);
    }
    if (top) {
      block = new AboveImage(hLine, block);
    }
    if (bottom) {
      block = new AboveImage(block, hLine);
    }
    return block;
  }

  // removes lines if edge exists around this vertex
  WorldImage drawWalls2(int w, int h, Color col) {
    WorldImage block = drawBlock(w, h);
    WorldImage vLine = new LineImage(new Posn(0, h), col);
    WorldImage hLine = new LineImage(new Posn(w, 0), col);

    if (!left) {
      block = new BesideImage(vLine, block);
    }
    if (!right) {
      block = new BesideImage(block, vLine);
    }
    if (!top) {
      block = new AboveImage(hLine, block);
    }
    if (!bottom) {
      block = new AboveImage(block, hLine);
    }
    return block;
  }

  // determines if this vertex is the same as the given object
  public boolean equals(Object other) {
    if (other instanceof Vertex) {
      return this.sameVertex((Vertex) other);
    }
    else {
      return false;
    }
  }

  // determines if this vertex is the same as the given one
  public boolean sameVertex(Vertex other) {
    return this.label == other.label && this.x == other.x && this.y == other.y;
  }

  // computes a unique hash code for this vertex
  public int hashCode() {
    return this.label * 1000 + this.x + (10 * this.y);
  }
}

//represents an edge connecting two vertices
class Edge {
  Vertex from;
  Vertex to;
  int weight;
  int x;
  int y;

  Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
    this.x = makeX();
    this.y = makeY();
  }

  // determines if this edge is the same as the given object
  public boolean equals(Object other) {
    if (other instanceof Edge) {
      return this.sameEdge((Edge) other);
    }
    else {
      return false;
    }
  }

  // determines if this edge is the same as the given one
  public boolean sameEdge(Edge other) {
    return this.from.equals(other.from) && this.to.equals(other.to) && this.x == other.x
        && this.y == other.y;
  }

  // computes a unique hash code for this edge
  public int hashCode() {
    return this.from.hashCode() * this.to.hashCode() + this.x + (10 * this.y) + this.weight;
  }

  // assigns a y value to this edge using the y coordinates
  // of its vertices
  public int makeY() {
    if (this.from == null || this.to == null) {
      return 0;
    }
    else {
      return Math.abs(from.y - to.y);
    }
  }

  // assigns an x value to this edge using the x coordinates
  // of its vertices
  public int makeX() {
    if (this.from == null || this.to == null) {
      return 0;
    }
    else {
      return Math.abs(from.x - to.x);
    }
  }

  // visualizes the how we are moving through edges
  // EFFECT: changes the this.col of the 'to' vertex to the given color.
  public void drawEdge(Color col) {
    this.to.col = col;
  }

}

//represents a maze
class Maze extends World {
  ArrayList<Edge> span;
  Random rand;
  ArrayList<Vertex> vertices;
  ArrayList<Edge> edges;
  int width;
  int height;
  int gameSize = 500;
  boolean dfs;
  boolean bfs;
  boolean solveMaze = false;
  ArrayList<Edge> solution = new ArrayList<Edge>();
  ArrayList<Vertex> moves = new ArrayList<Vertex>();
  boolean found = false;
  ArrayList<Edge> path;
  boolean manualMode = false;
  Vertex vPlayable;
  int currIdxOfE = 0;
  boolean gameOver;
  int movesCount = 0;

  Maze(int width, int height, boolean dfs, boolean bfs, boolean manual) {
    this.width = width;
    this.height = height;
    this.rand = new Random();
    this.vertices = new ArrayList<Vertex>();
    this.edges = new ArrayList<Edge>();
    this.span = new ArrayList<Edge>();
    this.path = new ArrayList<Edge>();
    this.dfs = dfs;
    this.bfs = bfs;
    this.manualMode = manual;
    this.gameOver = false;

    this.genVertices();
    this.genEdges();
    this.buildSpan();
    this.genOutEdge();
    this.buildListofShortest();
    this.vPlayable = this.vertices.get(0);
  }

  // convenience constructor for testing
  Maze(int seed, boolean dfs, boolean bfs, boolean manual) {
    this.width = 2;
    this.height = 2;
    this.rand = new Random(seed);
    this.vertices = new ArrayList<Vertex>();
    this.edges = new ArrayList<Edge>();
    this.span = new ArrayList<Edge>();
    this.path = new ArrayList<Edge>();
    this.dfs = dfs;
    this.bfs = bfs;
    this.manualMode = manual;
    this.gameOver = false;

    this.genVertices();
    this.genEdges();
    this.buildSpan();
    this.genOutEdge();
    this.buildListofShortest();
    this.vPlayable = this.vertices.get(0);
  }

  // determines which edges are connected to each vertex in this maze
  // EFFECT: updates the out edges field of the vertices in this maze
  public void genOutEdge() {
    for (Vertex v : this.vertices) {

      // left box
      if (v.x - 1 >= 0) { // left exists
        int idx = indexFinder(v.x - 1, v.y);
        Edge e = new Edge(v, this.vertices.get(idx), this.rand.nextInt(50));
        Edge eRev = new Edge(this.vertices.get(idx), v, this.rand.nextInt(50));
        if (this.span.contains(e) || this.span.contains(eRev)) {
          v.outEdges.add(e);
        }
      }

      // right
      if (v.x + 1 < this.width) { // right exists
        int idx = indexFinder(v.x + 1, v.y);
        Edge e = new Edge(v, this.vertices.get(idx), this.rand.nextInt(50));
        Edge eRev = new Edge(this.vertices.get(idx), v, this.rand.nextInt(50));
        if (this.span.contains(e) || this.span.contains(eRev)) {
          v.outEdges.add(e);
        }
      }

      // top
      if (v.y - 1 >= 0) { // top exists
        int idx = indexFinder(v.x, v.y - 1);
        Edge e = new Edge(v, this.vertices.get(idx), this.rand.nextInt(50));
        Edge eRev = new Edge(this.vertices.get(idx), v, this.rand.nextInt(50));
        if (this.span.contains(e) || this.span.contains(eRev)) {
          v.outEdges.add(e);
        }
      }

      // bottom
      if (v.y + 1 < this.height) { // bottom exists
        int idx = indexFinder(v.x, v.y + 1);
        Edge e = new Edge(v, this.vertices.get(idx), this.rand.nextInt(50));
        Edge eRev = new Edge(this.vertices.get(idx), v, this.rand.nextInt(50));
        if (this.span.contains(e) || this.span.contains(eRev)) {
          v.outEdges.add(e);
        }
      }
      removeDuplicates(v.outEdges);
    }
  }

  // builds a minimum spanning graph for this maze
  // EFFECT: updates the span field of this maze
  void buildSpan() {
    HashMap<Vertex, Vertex> reps = new HashMap<Vertex, Vertex>();
    ArrayList<Edge> worklist = new ArrayList<Edge>(this.edges);
    Utils u = new Utils();
    u.mergeSort(worklist, new EdgeComp());

    // map every node in this maze to itself
    for (int i = 0; i < this.vertices.size(); i++) {
      reps.put(this.vertices.get(i), this.vertices.get(i));
    }

    while (worklist.size() > 0) {
      Edge temp = worklist.get(0);
      worklist.remove(0);

      if ((u.find(reps, temp.from)).equals(temp.from)) {
        this.span.add(temp);
        u.union(reps, temp.from, temp.to);
      }
    }
  }

  // generates a list of vertices
  // EFFECT: updates the vertices field
  void genVertices() {
    int label = 0;
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++) {
        Vertex newV = new Vertex(label, x, y);
        this.vertices.add(newV);
        label++;
      }
    }
  }

  // generates a list of random edges of random weights
  // EFFECT: updates the edges field of this maze
  void genEdges() {
    for (int i = 0; i < this.vertices.size(); i++) {
      Vertex currVer = this.vertices.get(i);
      ArrayList<Vertex> temp = new ArrayList<Vertex>(4);

      if (currVer.x - 1 >= 0) {
        int idx = indexFinder(currVer.x - 1, currVer.y);
        Vertex currVer2 = this.vertices.get(idx);
        temp.add(currVer2);
      }
      if (currVer.x + 1 < this.width) {
        int idx = indexFinder(currVer.x + 1, currVer.y);
        Vertex currVer2 = this.vertices.get(idx);
        temp.add(currVer2);
      }

      if (currVer.y - 1 >= 0) {
        int idx = indexFinder(currVer.x, currVer.y - 1);
        Vertex currVer2 = this.vertices.get(idx);
        temp.add(currVer2);
      }
      if (currVer.y + 1 < this.height) {
        int idx = indexFinder(currVer.x, currVer.y + 1);
        Vertex currVer2 = this.vertices.get(idx);
        temp.add(currVer2);
      }

      for (int j = 0; j < temp.size(); j++) {
        int weight = this.rand.nextInt(50);
        Vertex v1 = temp.get(j);
        Edge e1 = new Edge(currVer, v1, weight);
        Edge eTemp = new Edge(v1, currVer, weight);

        if (!this.edges.contains(eTemp)) {
          this.edges.add(e1);
        }
      }
    }
  }

  // removes any duplicates in the given list
  public void removeDuplicates(ArrayList<Edge> list) {
    for (int i = 0; i < list.size(); i++) {
      Edge e1 = list.get(i);
      for (int j = i + 1; j < list.size(); j++) {
        Edge e2 = list.get(j);
        if (e2.equals(e1)) {
          list.remove(j);
        }
      }
    }
  }

  // finds the index of the vertex with the given coordinates
  int indexFinder(int x, int y) {
    for (int i = 0; i < this.vertices.size(); i++) {
      Vertex v1 = this.vertices.get(i);
      if (v1.x == x && v1.y == y) {
        return i;
      }
    }
    return -1;
  }

  // build a list of the path between the first and last vertex
  // in this maze
  // EFFECT: updates the path field of this maze
  void buildListofShortest() {
    int index = 0;
    boolean flag = false;
    Vertex to = this.vertices.get(this.vertices.size() - 1);
    Vertex v = this.vertices.get(0);

    for (int i = 0; i < this.span.size() && !flag; i++) {

      index = findEdgeinSpan(v);
      Edge e = this.span.get(index);

      if (e.from.equals(to) || e.to.equals(to)) {
        flag = true;
      }
      else {
        this.path.add(e);
        v = e.to;
      }

    }
  }

  // returns the index of the edge going from the given vertex
  public int findEdgeinSpan(Vertex v) {
    for (int i = 0; i < this.span.size(); i++) {
      Edge e = this.span.get(i);
      if (e.from.label == (v.label)) {
        return i;
      }
    }
    return -1; // return -1 if the vertex is not found in the span list
  }

  // produces an image of the current world state
  public WorldScene makeScene() {

    if (!this.bfs && !this.dfs && !this.manualMode) {
      WorldScene background = new WorldScene(500, 500);
      RectangleImage box1 = new RectangleImage(250, 250, OutlineMode.SOLID, Color.PINK);
      RectangleImage box2 = new RectangleImage(250, 250, OutlineMode.SOLID, Color.BLUE);
      RectangleImage box3 = new RectangleImage(500, 250, OutlineMode.SOLID, Color.RED);
      TextImage dfs = new TextImage("Depth First Search", 20, Color.black);
      TextImage bfs = new TextImage("Breadth First Search", 20, Color.WHITE);
      TextImage diy = new TextImage("Play Maze!", 20, Color.WHITE);
      background.placeImageXY(box1, 125, 125);
      background.placeImageXY(box2, 375, 125);
      background.placeImageXY(box3, 250, 375);
      background.placeImageXY(dfs, 125, 125);
      background.placeImageXY(bfs, 375, 125);
      background.placeImageXY(diy, 250, 375);
      return background;
    }

    else if (this.gameOver && this.manualMode) {
      WorldScene background = this.getEmptyScene();
      TextImage msg = new TextImage("Maze Completed", 30, Color.BLACK);
      TextImage wrong = new TextImage(this.movesCount - (this.path.size() + 1) + " Wrong Moves", 30,
          Color.RED);
      background.placeImageXY(msg, 250, 250);
      background.placeImageXY(wrong, 250, 325);
      return background;
    }

    else {
      WorldScene background = this.getEmptyScene();
      int bWidth = this.gameSize / this.width;
      int bHeight = this.gameSize / this.height;

      for (Vertex v : this.vertices) {
        background.placeImageXY(v.drawWalls(bWidth, bHeight, Color.black),
            (bWidth * v.x) + (bWidth / 2), (bHeight * v.y) + (bHeight / 2));
      }

      if (this.manualMode) {
        Vertex currVer = this.vertices.get(0);
        currVer.col = Color.red;
      }

      return background;
    }
  }

  // EFFECT: updates the world state after the mouse of clicked
  public void onMouseClicked(Posn pos) {
    if (!this.bfs && !this.dfs && !this.manualMode) {
      if (pos.x > 0 && pos.x < 250 && pos.y > 0 && pos.y < 250) {
        this.dfs = true;
        this.bfs = false;
        this.manualMode = false;
        this.solution = dfs();
        this.solveMaze = false;
        this.found = false;
        new Maze(this.width, this.height, this.dfs, this.bfs, this.manualMode);
      }

      else if (pos.x > 250 && pos.x < 500 && pos.y > 0 && pos.y < 250) {
        this.dfs = false;
        this.bfs = true;
        this.solveMaze = false;
        this.manualMode = false;
        this.found = false;
        this.solution = bfs();
        new Maze(this.width, this.height, this.dfs, this.bfs, this.manualMode);
      }

      else if (pos.x > 0 && pos.x < 500 && pos.y > 250 && pos.y < 500) {
        this.dfs = false;
        this.bfs = false;
        this.solveMaze = false;
        this.manualMode = true;
        this.found = false;
        this.gameOver = false;
        this.movesCount = 0;
        this.solution = bfs();
        new Maze(this.width, this.height, this.dfs, this.bfs, this.manualMode);
      }
    }
  }

  // EFFECT: updates the world state after each tick
  public void onTick() {
    if (this.currIdxOfE == this.span.size()) {
      this.solveMaze = true;
    }

    if (this.solution.size() > 0 && !this.solveMaze) {
      Edge e = this.span.get(this.currIdxOfE);

      Vertex v1 = e.from;
      Vertex v2 = e.to;

      if (v1.x > v2.x) {
        v1.left = false;
        v2.right = false;
      }

      if (v1.x < v2.x) {
        v1.right = false;
        v2.left = false;
      }

      if (v1.y > v2.y) {
        v1.top = false;
        v2.bottom = false;
      }

      if (v1.y < v2.y) {
        v1.bottom = false;
        v2.top = false;
      }

      this.currIdxOfE++;
    }

    if (this.vertices.get(this.vertices.size() - 1).col.equals(Color.red)) {
      for (Edge e : this.path) {
        e.drawEdge(Color.pink);
      }
      this.vertices.get(this.vertices.size() - 1).col = Color.pink;
      this.vertices.get(0).col = Color.pink;
    }

    else if ((this.bfs || this.dfs) && this.solution.size() > 0 && this.solveMaze) {
      Edge e = this.solution.get(0);
      this.solution.remove(0);
      e.drawEdge(Color.red);
    }
  }

  // updates the world state after a key event
  public void onKeyEvent(String str) {
    if (str.equals("r") && this.gameOver) {
      this.vertices = new ArrayList<Vertex>();
      this.edges = new ArrayList<Edge>();
      this.span = new ArrayList<Edge>();
      this.path = new ArrayList<Edge>();
      this.found = false;
      this.solveMaze = false;
      this.currIdxOfE = 0;
      this.genVertices();
      this.genEdges();
      this.buildSpan();
      this.genOutEdge();
      this.buildListofShortest();
      this.bfs = false;
      this.dfs = false;
      this.manualMode = false;
      this.gameOver = false;
      this.movesCount = 0;
      this.vPlayable = this.vertices.get(0);
    }

    else if (str.equals("r") && !this.gameOver) {
      this.vertices = new ArrayList<Vertex>();
      this.edges = new ArrayList<Edge>();
      this.span = new ArrayList<Edge>();
      this.path = new ArrayList<Edge>();
      this.found = false;
      this.solveMaze = false;
      this.currIdxOfE = 0;
      this.genVertices();
      this.genEdges();
      this.buildSpan();
      this.genOutEdge();
      this.buildListofShortest();
      this.movesCount = 0;
      this.vPlayable = this.vertices.get(0);
      if (this.bfs) {
        this.solution = this.bfs();
      }
      else {
        this.solution = this.dfs();
      }
    }

    if (this.manualMode) {
      if (this.vPlayable.equals(this.vertices.get(this.vertices.size() - 1))
      /* this.vertices.get(this.vertices.size() - 1).col.equals(Color.blue) */) {
        this.gameOver = true;
      }
      else {
        if (str.equals("right")) {
          if (vPlayable.x + 1 < this.width) {
            Vertex r = this.vertices.get(indexFinder(vPlayable.x + 1, vPlayable.y));
            Edge e = new Edge(vPlayable, r, this.rand.nextInt(50));
            if (vPlayable.outEdges.contains(e)) {
              r.col = Color.blue;
              vPlayable.col = Color.red;
              vPlayable = r;
              this.movesCount++;

            }
          }
        }
        else if (str.equals("left")) {
          if (vPlayable.x - 1 >= 0) {
            Vertex l = this.vertices.get(indexFinder(vPlayable.x - 1, vPlayable.y));
            Edge e = new Edge(vPlayable, l, this.rand.nextInt(50));
            if (vPlayable.outEdges.contains(e)) {
              l.col = Color.blue;
              vPlayable.col = Color.red;
              vPlayable = l;
              this.movesCount++;
            }
          }
        }

        else if (str.equals("up")) {
          if (vPlayable.y - 1 >= 0) {
            Vertex u = this.vertices.get(indexFinder(vPlayable.x, vPlayable.y - 1));
            Edge e = new Edge(vPlayable, u, this.rand.nextInt(50));
            if (vPlayable.outEdges.contains(e)) {
              u.col = Color.blue;
              vPlayable.col = Color.red;
              vPlayable = u;
              this.movesCount++;
            }
          }
        }
        else if (str.equals("down")) {
          if (vPlayable.y + 1 < this.height) {
            Vertex d = this.vertices.get(indexFinder(vPlayable.x, vPlayable.y + 1));
            Edge e = new Edge(vPlayable, d, this.rand.nextInt(50));
            if (vPlayable.outEdges.contains(e)) {
              d.col = Color.blue;
              vPlayable.col = Color.red;
              vPlayable = d;
              this.movesCount++;
            }
          }
        }
      }
    }

  }

  // finds a path between the first and last vertices of this maze
  // using breadth first search
  ArrayList<Edge> bfs() {
    return this.searchHelp(this.vertices.get(0), this.vertices.get(this.vertices.size() - 1),
        new Queue<Vertex>());
  }

  // finds a path between the first and last vertices of this maze
  // using depth first search
  ArrayList<Edge> dfs() {
    return this.searchHelp(this.vertices.get(0), this.vertices.get(this.vertices.size() - 1),
        new Stack<Vertex>());
  }

  // finds a path between the two given vertices
  ArrayList<Edge> searchHelp(Vertex from, Vertex to, ICollection<Vertex> worklist) {
    ArrayList<Edge> edgeList = new ArrayList<Edge>();
    Deque<Vertex> alreadySeen = new ArrayDeque<Vertex>();

    // Initialize the worklist with the from vertex
    worklist.add(from);
    from.col = Color.green;
    to.col = Color.orange;
    // As long as the worklist isn't empty...
    while (!worklist.isEmpty() && !this.found) {
      Vertex next = worklist.remove();
      if (next.equals(to)) {
        this.found = true;

      }
      else if (alreadySeen.contains(next)) {
        // do nothing: we've already seen this one
      }
      else {
        // add all the neighbors of next to the worklist for further processing
        for (Edge e : next.outEdges) {
          edgeList.add(e);
          worklist.add(e.to);
        }
        // add next to alreadySeen, since we're done with it
        alreadySeen.addFirst(next);
      }
    }
    // We haven't found the to vertex, and there are no more to try
    return edgeList;
  }

}

//Represents a mutable collection of items
interface ICollection<T> {
  // Is this collection empty?
  boolean isEmpty();

  // EFFECT: adds the item to the collection
  void add(T item);

  // Returns the first item of the collection
  // EFFECT: removes that first item
  T remove();
}

class Queue<T> implements ICollection<T> {
  Deque<T> contents;

  Queue() {
    this.contents = new ArrayDeque<T>();
  }

  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  public T remove() {
    return this.contents.removeFirst();
  }

  public void add(T item) {
    this.contents.addLast(item); // NOTE: Different from Stack!
  }
}

class Stack<T> implements ICollection<T> {
  Deque<T> contents;

  Stack() {
    this.contents = new ArrayDeque<T>();
  }

  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  public T remove() {
    return this.contents.removeFirst();
  }

  public void add(T item) {
    this.contents.addFirst(item);
  }
}

//examples for all tests and classes that represent mazes
class ExamplesMazes {
  Maze maze;
  Maze maze2;
  Edge e1;
  Edge e2;
  Edge e3;
  Edge e4;
  Edge e5;
  Edge e6;
  Edge e7;
  Edge e8;
  Edge e9;
  Edge e10;
  Edge e11;
  Edge e12;
  ArrayList<Edge> edges;
  ArrayList<Edge> span;
  ArrayList<Vertex> verts;
  Vertex v1;
  Vertex v2;
  Vertex v3;
  Vertex v4;

  // initialize the data
  void initData() {
    // example vertices
    this.v1 = new Vertex(0, 0, 0);
    this.v2 = new Vertex(1, 0, 1);
    this.v3 = new Vertex(2, 1, 0);
    this.v4 = new Vertex(3, 1, 1);
    // example edges
    this.e1 = new Edge(this.v1, this.v3, 12);
    this.e2 = new Edge(this.v1, this.v2, 2);
    this.e3 = new Edge(this.v2, this.v4, 3);
    this.e4 = new Edge(this.v3, this.v4, 5);
    this.e5 = new Edge(this.v1, this.v2, 42);
    this.e6 = new Edge(this.v2, this.v1, 12);
    this.e7 = new Edge(this.v2, this.v4, 2);
    this.e8 = new Edge(this.v3, this.v4, 12);
    this.e9 = new Edge(this.v4, this.v2, 25);
    this.e10 = new Edge(this.v4, this.v3, 22);
    // initialize out edges
    this.v1.outEdges = new ArrayList<Edge>(Arrays.asList(e5));
    this.v2.outEdges = new ArrayList<Edge>(Arrays.asList(e7, e6));
    this.v3.outEdges = new ArrayList<Edge>(Arrays.asList(e8));
    this.v4.outEdges = new ArrayList<Edge>(Arrays.asList(e9, e10));
    // example list of edges
    this.edges = new ArrayList<Edge>();
    this.edges.add(e1);
    this.edges.add(e2);
    this.edges.add(e3);
    this.edges.add(e4);
    // example list of vertices
    this.verts = new ArrayList<Vertex>(Arrays.asList(this.v1, this.v2, this.v3, this.v4));
    // example minimum spanning trees
    this.span = new ArrayList<Edge>();
    this.span.add(e2);
    this.span.add(e3);
    this.span.add(e4);
    // example mazes
    this.maze = new Maze(4, false, false, false); // SEED
    this.maze2 = new Maze(20, 20, false, false, false); // RANDOM
  }

  // tests for hash code
  void testHashCode(Tester t) {
    this.initData();
    t.checkExpect(this.v3.hashCode(), 2001);
    t.checkExpect(this.v4.hashCode(), 3011);
    t.checkExpect(this.e4.hashCode(), 6025026);
  }

  // tests for equals
  void testEquals(Tester t) {
    this.initData();
    t.checkExpect(this.v4.equals(new String("Hello")), false);
    t.checkExpect(this.e3.equals(7), false);
    t.checkExpect(this.v4.equals(this.v2), false);
    t.checkExpect(this.v2.equals(new Vertex(1, 0, 1)), true);
    t.checkExpect(this.e3.equals(this.e1), false);
    t.checkExpect(this.e3.equals(new Edge(new Vertex(1, 0, 1), new Vertex(3, 1, 1), 29)), true);
    t.checkExpect(this.e3.equals(new Edge(new Vertex(1, 0, 1), new Vertex(3, 1, 1), 3)), true);
  }

  // tests for make y
  void testMakeY(Tester t) {
    this.initData();
    t.checkExpect(this.e2.makeY(), 1);
    t.checkExpect(this.e4.makeY(), 1);
  }

  // tests for make x
  void testMakeX(Tester t) {
    this.initData();
    t.checkExpect(this.e2.makeX(), 0);
    t.checkExpect(this.e4.makeX(), 0);
  }

  // tests for gen vertices
  void testGenVerts(Tester t) {
    this.initData();
    t.checkExpect(this.maze.vertices, this.verts);
  }

  // tests for gen edges
  void testGenEdges(Tester t) {
    this.initData();
    t.checkExpect(this.maze.edges, this.edges);
  }

  // tests for merge sort
  void testMergeSort(Tester t) {
    this.initData();
    new Utils().mergeSort(this.edges, new EdgeComp());
    t.checkExpect(this.edges,
        new ArrayList<Edge>(Arrays.asList(this.e2, this.e3, this.e4, this.e1)));
  }

  // tests for build span
  void testBuildSpan(Tester t) {
    this.initData();
    t.checkExpect(this.maze.span, this.span);
  }

  // tests for index finder
  void testIndexFinder(Tester t) {
    this.initData();
    t.checkExpect(this.maze.indexFinder(0, 0), 0);
    t.checkExpect(this.maze.indexFinder(1, 0), 2);
  }

  // tests for draw block
  void testDrawBlock(Tester t) {
    this.initData();
    t.checkExpect(this.v4.drawBlock(79, 2),
        new RectangleImage(79, 2, OutlineMode.SOLID, Color.white));
    t.checkExpect(this.v2.drawBlock(7, 9),
        new RectangleImage(7, 9, OutlineMode.SOLID, Color.white));
  }

  // tests for draw walls
  void testDrawWalls(Tester t) {
    this.initData();
    this.v3.top = true;
    this.v3.bottom = false;
    this.v3.left = false;
    this.v3.right = false;
    this.v2.bottom = true;
    this.v2.top = false;
    this.v2.left = false;
    this.v2.right = false;
    this.v1.left = true;
    this.v1.top = false;
    this.v1.bottom = false;
    this.v1.right = false;
    this.v4.right = true;
    this.v4.top = false;
    this.v4.left = false;
    this.v4.bottom = false;
    t.checkExpect(this.v3.drawWalls(7, 9, Color.WHITE),
        new AboveImage(new LineImage(new Posn(7, 0), Color.WHITE),
            new RectangleImage(7, 9, OutlineMode.SOLID, Color.WHITE)));
    t.checkExpect(this.v2.drawWalls(3, 6, Color.WHITE),
        new AboveImage(new RectangleImage(3, 6, OutlineMode.SOLID, Color.WHITE),
            new LineImage(new Posn(3, 0), Color.WHITE)));
    t.checkExpect(this.v1.drawWalls(4, 5, Color.WHITE),
        new BesideImage(new LineImage(new Posn(0, 5), Color.WHITE),
            new RectangleImage(4, 5, OutlineMode.SOLID, Color.WHITE)));
    t.checkExpect(this.v4.drawWalls(3, 7, Color.WHITE),
        new BesideImage(new RectangleImage(3, 7, OutlineMode.SOLID, Color.WHITE),
            new LineImage(new Posn(0, 7), Color.WHITE)));
  }

  // tests for draw walls 2
  void testDrawWalls2(Tester t) {
    this.initData();
    this.v3.top = true;
    this.v3.bottom = true;
    this.v3.left = false;
    this.v3.right = true;
    this.v2.bottom = true;
    this.v2.top = true;
    this.v2.left = true;
    this.v2.right = false;
    this.v1.left = true;
    this.v1.top = false;
    this.v1.bottom = true;
    this.v1.right = true;
    this.v4.right = true;
    this.v4.top = true;
    this.v4.left = true;
    this.v4.bottom = false;
    t.checkExpect(this.v3.drawWalls2(7, 9, Color.GREEN),
        new BesideImage(new LineImage(new Posn(0, 9), Color.GREEN),
            new RectangleImage(7, 9, OutlineMode.SOLID, Color.WHITE)));
    t.checkExpect(this.v2.drawWalls2(12, 99, Color.BLACK),
        new BesideImage(new RectangleImage(12, 99, OutlineMode.SOLID, Color.WHITE),
            new LineImage(new Posn(0, 99), Color.BLACK)));
    t.checkExpect(this.v1.drawWalls2(3, 2, Color.PINK),
        new AboveImage(new LineImage(new Posn(3, 0), Color.PINK),
            new RectangleImage(3, 2, OutlineMode.SOLID, Color.WHITE)));
    t.checkExpect(this.v4.drawWalls2(5, 2, Color.BLUE),
        new AboveImage(new RectangleImage(5, 2, OutlineMode.SOLID, Color.WHITE),
            new LineImage(new Posn(5, 0), Color.BLUE)));
  }

  // tests for make scene
  void testMakeScene(Tester t) {
    this.initData();
    WorldScene background = new WorldScene(500, 500);
    RectangleImage box1 = new RectangleImage(250, 250, OutlineMode.SOLID, Color.PINK);
    RectangleImage box2 = new RectangleImage(250, 250, OutlineMode.SOLID, Color.BLUE);
    RectangleImage box3 = new RectangleImage(500, 250, OutlineMode.SOLID, Color.RED);
    TextImage dfs = new TextImage("Depth First Search", 20, Color.black);
    TextImage bfs = new TextImage("Breadth First Search", 20, Color.WHITE);
    TextImage diy = new TextImage("Play Maze!", 20, Color.WHITE);
    background.placeImageXY(box1, 125, 125);
    background.placeImageXY(box2, 375, 125);
    background.placeImageXY(box3, 250, 375);
    background.placeImageXY(dfs, 125, 125);
    background.placeImageXY(bfs, 375, 125);
    background.placeImageXY(diy, 250, 375);
    t.checkExpect(this.maze.makeScene(), background);
  }

  // tests for big bang
  void testBigBang(Tester t) {
    this.initData();
    this.maze2.bigBang(500, 500, 0.005);
  }

  // tests for union and find
  void testUnionFind(Tester t) {
    this.initData();
    HashMap<Vertex, Vertex> map = new HashMap<Vertex, Vertex>();
    map.put(this.v2, this.v3);
    map.put(this.v1, this.v4);
    map.put(this.v4, this.v2);
    t.checkExpect(new Utils().find(map, this.v1), this.v4);
    t.checkExpect(new Utils().find(map, this.v4), this.v2);
    new Utils().union(map, this.v4, this.v3);
    new Utils().union(map, this.v2, this.v1);
    t.checkExpect(map.get(this.v4), this.v3);
    t.checkExpect(map.get(this.v2), this.v1);
  }

  // tests for search
  void testSearch(Tester t) {
    this.initData();
    int i = 0;
    ArrayList<Edge> list = new ArrayList<Edge>(Arrays.asList(this.e5, this.e7, this.e6));
    for (Edge e : this.maze.searchHelp(this.v1, this.v4, new Stack<Vertex>())) {
      t.checkExpect(list.get(i), e);
      i++;
    }

    i = 0;
    for (Edge e : this.maze.dfs()) {
      t.checkExpect(list.get(i), e);
      i++;
    }

    i = 0;
    for (Edge e : this.maze.bfs()) {
      t.checkExpect(list.get(i), e);
      i++;
    }

  }

  // tests for on mouse clicked
  void testOnMouseClick(Tester t) {
    this.initData();
    this.maze.onMouseClicked(new Posn(200, 125));
    t.checkExpect(this.maze.dfs, true);
    t.checkExpect(this.maze.bfs, false);
    t.checkExpect(this.maze.manualMode, false);
    this.initData();
    this.maze.onMouseClicked(new Posn(300, 240));
    t.checkExpect(this.maze.bfs, true);
    t.checkExpect(this.maze.dfs, false);
    t.checkExpect(this.maze.manualMode, false);
    this.initData();
    this.maze.onMouseClicked(new Posn(400, 350));
    t.checkExpect(this.maze.dfs, false);
    t.checkExpect(this.maze.bfs, false);
    t.checkExpect(this.maze.manualMode, true);
  }

  // tests for remove duplicates
  void testRemoveDuplicates(Tester t) {
    this.initData();
    ArrayList<Edge> edges = new ArrayList<Edge>(Arrays.asList(this.e1, this.e1, this.e3));
    this.maze.removeDuplicates(edges);
    t.checkExpect(edges, new ArrayList<Edge>(Arrays.asList(this.e1, this.e3)));
    ArrayList<Edge> edges2 = new ArrayList<Edge>(Arrays.asList(this.e3, this.e4, this.e1, this.e3));
    this.maze.removeDuplicates(edges2);
    t.checkExpect(edges2, new ArrayList<Edge>(Arrays.asList(this.e3, this.e4, this.e1)));
  }

  // tests for on key
  void testOnKeyEvent(Tester t) {
    this.initData();
    this.maze.dfs = false;
    this.maze.bfs = false;
    this.maze.solveMaze = false;
    this.maze.manualMode = true;
    this.maze.found = false;
    this.maze.solution = this.maze.bfs();
    WorldScene test = this.maze.makeScene();
    this.maze.onKeyEvent("up"); // no change
    t.checkExpect(this.maze.makeScene(), test);
    this.maze.onKeyEvent("right");
    t.checkExpect(this.maze.makeScene(), test);
    this.maze.onKeyEvent("left");
    t.checkExpect(this.maze.makeScene(), test);
    this.maze.onKeyEvent("down");
    t.checkExpect(this.maze.vertices.get(1).col, Color.blue);
    t.checkExpect(this.maze.vertices.get(0).col, Color.red);
    this.initData();
    this.maze.dfs = false;
    this.maze.bfs = true;
    this.maze.solveMaze = false;
    this.maze.manualMode = false;
    this.maze.found = false;
    this.maze.solution = this.maze.bfs();
    this.maze.vertices.get(0).col = Color.red;
    this.maze.vertices.get(3).col = Color.white;
    this.maze.onKeyEvent("r");
    t.checkExpect(this.maze.vertices.get(0).col, Color.green);
    t.checkExpect(this.maze.vertices.get(3).col, Color.orange);
  }

  // tests for find edge in span
  void testFindEdge(Tester t) {
    this.initData();
    t.checkExpect(this.maze.findEdgeinSpan(this.v1), 0);
    t.checkExpect(this.maze.findEdgeinSpan(this.v3), 2);
    t.checkExpect(this.maze.findEdgeinSpan(this.v4), -1);
  }

  // tests for build list of shortest
  void testBuildShortest(Tester t) {
    this.initData();
    this.maze.buildListofShortest();
    t.checkExpect(this.maze.path, new ArrayList<Edge>(Arrays.asList(this.e2, this.e2)));
  }

  // tests for gen out edge
  void testGenOutEdge(Tester t) {
    this.initData();
    for (int i = 0; i < this.maze.span.size(); i++) {
      t.checkExpect(this.maze.span.get(i).from.outEdges, this.span.get(i).from.outEdges);
      t.checkExpect(this.maze.span.get(i).to.outEdges, this.span.get(i).to.outEdges);
    }
  }
}