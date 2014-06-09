/*
 * A class that represents a game board. It consists of Squares that can contain
 * objects of type Piece. The game board is amde up of a linear array of Squares.
 */

package szymon.zielinski.boardgame.base;


public class Board {
    
    public static final int EMPTY_SQUARE = -1;
    public static final int INPUT_ERROR = -2;
    
    private static final String ERROR_MESSAGE = "Illegal board size. Board size cannot be < 0. Found: ";
    
    private int size;            
    private Square[] squareArray;
    
    // create board of given size
    public Board(int size) {   
        if (size <= 0){
            throw new IllegalArgumentException(ERROR_MESSAGE + size);
        }
        
        this.size = size;
        squareArray = new Square[size];
        
        for(int i=0; i<size; i++){
            squareArray[i] = new Square();
        }
    }
    
    // return size of the board
    public int size() {
        return size;
    }
    
    // add the Piece at given position
    public boolean addPiece(int square, Piece piece) {        
        if (square >= size || square < 0 || !squareArray[square].isEmpty()){
            return false;     
        }
        
        squareArray[square].setPiece(piece);
        
        return true;
    }
    
    // remove AND return the Piece at the given position.
    public Piece removePiece(int square) {      
        if (square >= size || square < 0 || squareArray[square].isEmpty()){
            return null;  
        }
        
        Piece temp = squareArray[square].getPiece();
        squareArray[square].empty();        
        
        return temp;
    }
    
    // return the owner of the piece at the given position
    public int pieceOwnerAt(int square) {   
        
        // signal input error
        if (square >= size || square < 0){
            return INPUT_ERROR;  
        }
        
        // signal empty square error
        if (squareArray[square].isEmpty()){
            return EMPTY_SQUARE;  
        }
        
        return squareArray[square].getPieceOwner();        
    }
    
    // return the type of the piece at the given position
    public int pieceTypeAt(int square) {    
        
        // signal input error
        if (square >= size || square < 0){
            return INPUT_ERROR;  
        }
        
        // signal empty square error
        if (squareArray[square].isEmpty()){
            return EMPTY_SQUARE;  
        }
        
        return squareArray[square].getPieceType();      
    }
    
    // rotates the board, so that it can be viewed from the other player's perspective
    public void swapSquares() {
        for(int i = 0; i<(size/2); i++) {
            Square temp = squareArray[size-1-i];
            squareArray[size-1-i] = squareArray[i];
            squareArray[i] = temp;
        }
    }
}