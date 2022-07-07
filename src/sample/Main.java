package sample;

import java.util.*;
import java.io.*;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    static Pane pane = new Pane();
    static final double block_size = 70;

    @Override
    public void start(Stage primaryStage) throws Exception {
        File map = new File("src/icons/map.txt");
        Scanner sc = new Scanner(map);

        Game game = new Game(new Map(sc));

        MyPlayer player = new MyPlayer(game.map, new Position(1, 1), new Controls(KeyCode.W, KeyCode.S, KeyCode.D, KeyCode.A, KeyCode.SPACE));
        game.addPlayer(player);

        ImageView[][] blocks = game.map.blocks;
        ArrayList<MyPlayer> players = game.players;

        game.map.print();

        for (int i = 0; i < game.map.size; i++) {
            for (int j = 0; j < game.map.size; j++)
                pane.getChildren().add(blocks[i][j]);
        }

        for (MyPlayer p : players)
            pane.getChildren().add(p.tank.image);

        Scene scene = new Scene(pane, 700, 700);

        primaryStage.setScene(scene);
        primaryStage.show();
        pane.requestFocus();
        pane.setOnKeyPressed(keyEvent -> {
            System.out.println("HERE");
            for (MyPlayer p : players) {
                p.tank.move(keyEvent.getCode());
                game.map.print();
            }
        });
    }
}

class Game {
    Map map;
    ArrayList<MyPlayer> players = new ArrayList<>();

    public Game(Map map) {
        this.map = map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public void addPlayer(MyPlayer player) {
        this.players.add(player);
        this.map.setChar(player.getPosition(), 'P');
        player.setMap(this.map);
    }
}

class Position {
    int x, y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

    public boolean equals(Position bas) {
        if((x == ((Position) bas).getX()) && (y == ((Position) bas).getY()))
            return true;
        else
            return false;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

class Map {
    int size;
    char[][] cells;
    ImageView[][] blocks;
    static double block_size;
    char last_char = '0';

    public Map(Scanner sc) throws InvalidMapException {
//        block_size = Main.block_size;
        fileToMap(sc);
        createRectangleMap();
    }

    public void createRectangleMap() {
        ImageView[][] blocks = new ImageView[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                blocks[i][j] = getImageView(cells[j][i], new Position(i, j));
            }
        }
        this.blocks = blocks;
    }

    public ImageView getImageView(char symbol, Position position) {
        ImageView iv;
        switch (symbol) {
            case '0' :
                iv = Texture.get(Texture.empty, position);
                break;
//                iv.setViewOrder(3);

            case '4' :
                iv = Texture.get(Texture.brick, position);
                break;
//                iv.setViewOrder(1);
            case 'S' :
                iv = Texture.get(Texture.steel, position);
                break;
//                iv.setViewOrder(1);
            case 'W' :
                iv = Texture.get(Texture.water, position);
                break;
//                iv.setViewOrder(3);
            case 'T' :
                iv = Texture.get(Texture.tree, position);
                break;
//                iv.setViewOrder(1);
            default :
                throw new IllegalStateException("Unexpected value: " + symbol);
        }
        return iv;
    }


    public void fileToMap(Scanner sc) throws InvalidMapException {
        int size;
        try {
            size = Integer.parseInt(sc.nextLine());
        }
        catch(Exception e) {
            throw new InvalidMapException("Map size can not be zero");
        }
        char[][] cells = new char[size][size];

        try {
            fillMapData(sc, size, cells);
        }
        catch (Exception e) {
            throw new InvalidMapException("Not enough map elements");
        }
        this.cells = cells;
        this.size = size;
    }

    public void fillMapData(Scanner input, int n, char[][] cells) {
        for (int i = 0; i < n; i++) {
            String[] ln = input.nextLine().split(" ");
            for (int j = 0; j < n; j++) {
                cells[i][j] = ln[j].charAt(0);
            }
        }
    }

    public int getSize() {
        return size;
    }

    public char getValueAt(Position position) {
        return cells[position.y][position.x];
    }

    public void print() {
        for (char[] aChar : cells) {
            for (int j = 0; j < size; j++) {
                System.out.print(aChar[j] + " ");
            }
            System.out.println();
        }
    }


    public void setChar(Position position, char symbol) {
        cells[position.y][position.x] = symbol;
    }

    public boolean isInMap(Position position) {
        int x = position.x;
        int y = position.y;
        int size = this.size;
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public boolean isEmptyCell(Position position) {
        return getValueAt(position) == '0' || getValueAt(position) == 'T';
    }


    public void savePosition(Position position, Position next_pos) {
        setChar(position, last_char);
        last_char = getValueAt(next_pos);
        setChar(next_pos, 'P');
    }
}

interface Player {
    void setMap(Map map);
    void moveRight();
    void moveLeft();
    void moveUp();
    void moveDown();
    Position getPosition();
}

class MyPlayer implements Player {
    Position position;
    Map map;
    Controls controls;
    Tank tank;
    Direction direction;

    public MyPlayer() {
        this.position = new Position(0, 0);
    }

    MyPlayer(Map map, Position position, Controls controls) {
        this.map = map;
        this.position = position;
        this.controls = controls;
        this.tank = new Tank(this);
    }

    @Override
    public void setMap(Map map) {
        this.map = map;
    }

    public Position getNextPosition(Direction direction) {
        Position next_pos;
        switch (direction) {
            case RIGHT :
                next_pos = new Position(position.x + 1, position.y);
                break;
            case LEFT :
                next_pos = new Position(position.x - 1, position.y);
                break;
            case UP :
                next_pos = new Position(position.x, position.y - 1);
                break;
            case DOWN :
                next_pos = new Position(position.x, position.y + 1);
                break;
            default :
                next_pos = new Position(0, 0);
                break;
        }
        return next_pos;
    }

    public boolean isValidMove(Position position) {
        return map.isInMap(position) && map.isEmptyCell(position);
    }

    public void handleMove(Direction direction) {
        Position next_pos = getNextPosition(direction);
        if (isValidMove(next_pos)) {
            map.savePosition(position, next_pos);
            this.position = next_pos;
        }
    }

    @Override
    public void moveRight() {
        handleMove(Direction.RIGHT);
        direction = Direction.RIGHT;
    }

    @Override
    public void moveLeft() {
        handleMove(Direction.LEFT);
        direction = Direction.LEFT;
    }

    @Override
    public void moveUp() {
        handleMove(Direction.UP);
        direction = Direction.UP;
    }

    @Override
    public void moveDown() {
        handleMove(Direction.DOWN);
        direction = Direction.DOWN;
    }

    @Override
    public Position getPosition() {
        return position;
    }
}

class InvalidMapException extends Exception {
    public InvalidMapException(String str) {
        super(str);
    }
}

class Tank extends MyPlayer{
    ImageView image;
    MyPlayer player;
    static double block_size = Main.block_size;
    ArrayList<Bullet> bullets = new ArrayList<>();

    public Tank(MyPlayer myPlayer){
        this.player = myPlayer;
        this.image = Texture.get(Texture.tank, myPlayer.position);
    }

    public void move(KeyCode code) {
        switch (code) {
            case W :
                player.moveUp();
                break;
            case D :
                player.moveRight();
                break;
            case S :
                player.moveDown();
                break;
            case A :
                player.moveLeft();
                break;
            case SPACE :
                shoot();
                break;
        }
        changeImgCoords();
        setImgRotate();
        setInvisible();
    }

    public void setInvisible() {
        boolean visible = player.map.last_char != 'T';
        System.out.println(visible);
        image.setVisible(visible);
    }

    public void setImgRotate() {
        switch (player.direction) {
            case UP :
                image.setRotate(0);
                break;
            case RIGHT :
                image.setRotate(90);
                break;
            case DOWN :
                image.setRotate(180);
                break;
            case LEFT :
                image.setRotate(270);
                break;
        }
    }

    public void changeImgCoords() {
        image.setX(player.position.x * block_size);
        image.setY(player.position.y * block_size);
    }

    public void shoot() {
        bullets.add(new Bullet(player.map, player.position, player.direction));
        //  bullets.remove(0);
    }

}

class Bullet {
    ImageView image;
    Direction direction;
    Position position;
    Map map;
    static double block_size = Main.block_size;

    public Bullet(Map map, Position position, Direction direction) {
        this.map = map;
        this.direction = direction;
        this.position = new Position(position.x, position.y);
        image = Texture.get(Texture.bullet, position);
        image.setScaleX(0.2);
        image.setScaleY(0.2);
        int speed = 1;
        move();
        Path path = new Path();
        double x = (this.position.x * block_size) + block_size / 2;
        double y = (this.position.y * block_size) + block_size / 2;
        path.getElements().add(new MoveTo(x, y));

        while (notHitObstacle()) {
            speed++;
            move();
        }
        x = (this.position.x * block_size) + block_size / 2;
        y = (this.position.y * block_size) + block_size / 2;

        PathTransition pathTransition = new PathTransition();
        path.getElements().add(new LineTo(x, y));
        pathTransition.setDuration(Duration.millis(100 * speed));
        pathTransition.setPath(path);
        pathTransition.setNode(image);
        // pathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        pathTransition.setCycleCount(1);
        Main.pane.getChildren().add(image);
        pathTransition.play();
//        image.setViewOrder(2);

        pathTransition.setOnFinished(event -> {
            image.setVisible(false);
            if (map.getValueAt(this.position) != '0' && Character.isDigit(map.getValueAt(this.position))) {
                if (map.getValueAt(this.position) == '1') {
//                    map.blocks[this.position.x][this.position.y].setViewOrder(3);
                    map.blocks[this.position.x][this.position.y].setImage(Texture.empty);
                }

                int val = map.getValueAt(this.position);
                val--;
                map.setChar(this.position, (char) val);
            }
        });
    }

    public boolean notHitObstacle() {
        return (map.getValueAt(position) + "").matches("[0TW]");
    }

    public void move() {
        switch (direction) {
            case UP :
                position.y--;
                break;
            case DOWN :
                position.y++;
                break;
            case RIGHT :
                position.x++;
                break;
            case LEFT :
                position.x--;
                break;
        }
    }
}

enum Direction {
    RIGHT, LEFT, UP, DOWN
}

class Controls {
    KeyCode UP;
    KeyCode DOWN;
    KeyCode RIGHT;
    KeyCode LEFT;
    KeyCode SHOOT;

    public Controls(KeyCode UP, KeyCode DOWN, KeyCode RIGHT, KeyCode LEFT, KeyCode SHOOT) {
        this.DOWN = DOWN;
        this.UP = UP;
        this.RIGHT = RIGHT;
        this.LEFT = LEFT;
        this.SHOOT = SHOOT;
    }
}

class Texture {
    static double block_size = Main.block_size;
    static final Image tank = new Image("icons/tank.png");
    static final Image empty = new Image("icons/empty.png");
    static final Image brick = new Image("icons/brick.png");
    static final Image water = new Image("icons/water.png");
    static final Image steel = new Image("icons/steel.png");
    static final Image tree = new Image("icons/tree.png");
    static final Image bullet = new Image("icons/bullet.png");

    public static ImageView get(Image image, Position position) {

        ImageView r = new ImageView();
        r.setFitWidth(block_size);
        r.setFitHeight(block_size);
        r.setImage(image);
        r.setX(block_size * position.x);
        r.setY(block_size * position.y);
        return r;
    }
}