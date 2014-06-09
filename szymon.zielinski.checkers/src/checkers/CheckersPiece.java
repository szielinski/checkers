/*
 * A checkers piece class, an implementation of class Piece.
*/
package checkers;

public class CheckersPiece implements Piece {

    public static final int HUMAN_PLAYER = 0;
    public static final int COMPUTER_PLAYER = 1;
    public static final int CHECKER = 0;
    public static final int KING = 1;
    
    private static final int FIRST_OWNER = 0; 
    private static final int LAST_OWNER = 1; 
    private static final int FIRST_TYPE = 0; 
    private static final int LAST_TYPE = 1; 
    private static final String ERROR_MESSAGE = "Illegal argument, out of range:\n";
    
    private int owner, type;
    
    CheckersPiece() {
        owner = FIRST_OWNER;
        type = FIRST_TYPE;
    }
    
    // constructor that sets owner and type to non-default supplied values.
    CheckersPiece(int owner, int type) {
        setOwner(owner);
        setType(type);
    }
    
    // convert normal piece to a king piece
    public void coronate() {
        type = KING;
    }
    
    // accessors & mutators    
    private void setOwner(int owner){
        if (owner < FIRST_OWNER || owner > LAST_OWNER){
            throw new IllegalArgumentException(ERROR_MESSAGE + "\towner cannot be " + owner);            
        }
        this.owner = owner;
    }
    
    @Override
    public int getOwner(){
        return owner;
    }
    
    private void setType(int type){
        if (type < FIRST_TYPE || type > LAST_TYPE){
            throw new IllegalArgumentException(ERROR_MESSAGE + "\ttype cannot be " + type);            
        }
        this.type = type;
    }
    
    @Override
    public int getType(){
        return type;
    }
    
    // changes the owner of the piece. Works only if there are 2 distinct owners.
    public void changeOwner() {
        owner = (owner + 1) %2; 
    }
}