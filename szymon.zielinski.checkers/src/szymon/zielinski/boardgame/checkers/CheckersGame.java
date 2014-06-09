/*
 * This class handles the logic of a game of checkers. Uses Piece, CheckersPiece
 * , Square and Board.
 * 
 * The board is represented as a linear array of Squares. A linear array was
 * chosen over a 2d array to simplify the application of game rules. 
 * Below is a diagram of what the board would look like if the linear array of 
 * squares was stretched over it:
 * 
 * [ 0] [ 1] [ 2] [ 3] [ 4] [ 5] [ 6] [ 7]
 * [ 8] [ 9] [10] [11] [12] [13] [14] [15]
 * [16] [17] [18] [19] [20] [21] [22] [23]
 * [24] [25] [26] [27] [28] [29] [30] [31]
 * [32] [33] [34] [35] [36] [37] [38] [39]
 * [40] [41] [42] [43] [44] [45] [46] [47]
 * [48] [49] [50] [51] [52] [53] [54] [55]
 * [56] [57] [58] [59] [60] [61] [62] [63]
 * 
 * Under this setup, a valid move consists of addition or subtraction of 9 or 7.
 * A valid capturing move consists of the addition or subtraction of 14 or 18.
 * For example, to check if a move from 44 to 50 is valid, add 9 to it, check
 * the result, do the same with 7:
 * 50 -  7 = 43, 43 != 44
 * 50 - 9 = 41, 41 != 44
 * Therefore the move is invalid.
 */
package szymon.zielinski.boardgame.checkers;

import java.util.Random;

import szymon.zielinski.boardgame.base.Board;
import szymon.zielinski.boardgame.base.Piece;

public class CheckersGame {

    public static final int BOARD_SIZE = 64;
    public static final int NO_AI = -1;
    public static final int BEGINNER_AI = 0;
    public static final int EASY_AI = 1;
    public static final int MEDIUM_AI = 2;
    public static final int HARD_AI = 3;
    
    
   public static final boolean[] VALID_SQUARE = {
        false, true, false, true, false, true, false, true,
        true, false, true, false, true, false, true, false,
        false, true, false, true, false, true, false, true,
        true, false, true, false, true, false, true, false,
        false, true, false, true, false, true, false, true,
        true, false, true, false, true, false, true, false,
        false, true, false, true, false, true, false, true,
        true, false, true, false, true, false, true, false
    };
    
    public static final double[] POSITION_MULTIPLIER = {
        0, 1.2, 0, 1.2, 0, 1.2, 0, 1.2,
        1.2, 0, 1, 0, 1, 0, 1, 0,
        0, 1, 0, 1, 0, 1, 0, 1.2,
        1.2, 0, 1, 0, 1, 0, 1, 0,
        0, 1, 0, 1, 0, 1, 0, 1.2,
        1.2, 0, 1, 0, 1, 0, 1, 0,
        0, 1, 0, 1, 0, 1, 0, 1.2,
        1.2, 0, 1.2, 0, 1.2, 0, 1.2, 0
    };
    
    private Board checkersBoard;
    private int noHumanPieces, noCompPieces, AIType;
    
    // consecutiveAttack stores conecutive capturing moves, if any are possible
    private String consecutiveAttack;

    // set up the game board
    CheckersGame() {
        checkersBoard = new Board(BOARD_SIZE);
        noHumanPieces = 0;
        noCompPieces = 0;
        AIType = EASY_AI;
        consecutiveAttack = "";

        Piece tempChecker;

        for (int i = 0; i < checkersBoard.size(); i++) {
            if (i < 24
                    && VALID_SQUARE[i] == true) {
                tempChecker = new CheckersPiece(CheckersPiece.COMPUTER_PLAYER, CheckersPiece.CHECKER);
                checkersBoard.addPiece(i, tempChecker);
                noCompPieces++;
                continue;
            }

            if (i > 39 && VALID_SQUARE[i] == true) {
                tempChecker = new CheckersPiece(CheckersPiece.HUMAN_PLAYER, CheckersPiece.CHECKER);
                checkersBoard.addPiece(i, tempChecker);
                noHumanPieces++;
                continue;
            }
        }
    }

    // copy constructor
    public CheckersGame(CheckersGame another) {
        this.checkersBoard = new Board(BOARD_SIZE);
        for (int i = 0; i < this.checkersBoard.size(); i++) {
            if (another.getOwnerAt(i) != -1) {
                this.checkersBoard.addPiece(i, new CheckersPiece(another.checkersBoard.pieceOwnerAt(i), another.checkersBoard.pieceTypeAt(i)));
            }
        }
        this.noHumanPieces = another.noHumanPieces;
        this.noCompPieces = another.noCompPieces;
        this.AIType = another.AIType;
        this.consecutiveAttack = another.consecutiveAttack;
    }

    // returns the winner, or -1 if the game is still in progress
    public int winner(int currentPlayer) {
        String attacksAndMoves = listCaptures(currentPlayer) + listMoves(currentPlayer);
        
        if (noHumanPieces == 0) {
            return 1;
        } 
        
        if (noCompPieces == 0) {
            return 0;
        } 
        
        // if the current player is out of lagal moves, the other player has won
        if (attacksAndMoves.length() == 0) {
            return (++currentPlayer) % 2;
        }
        
        return -1;
    }

    // returns the number of player pieces
    public int noHumanPieces() {
        return noHumanPieces;
    }

    // returns the number of computer pieces
    public int noCompPieces() {
        return noCompPieces;
    }

    /* 
     * moves a piece on the board owned by the given player. return false 
     * if move is invalid  
     */
    public boolean movePiece(int sourceSquare, int destSquare, int player) {

        if (!isValid(sourceSquare, destSquare, player)) {
            return false;
        }

        CheckersPiece movedPiece =
                (CheckersPiece) checkersBoard.removePiece(sourceSquare);

        // coronate moved piece when appropriate
        if (destSquare < 8 && movedPiece.getOwner() == CheckersPiece.HUMAN_PLAYER) {
            movedPiece.coronate();
        } else if (destSquare > 55 && movedPiece.getOwner() == CheckersPiece.COMPUTER_PLAYER) {
            movedPiece.coronate();
        }

        checkersBoard.addPiece(destSquare, movedPiece);

        // check if move was a capturing move
        int difference = Math.abs(sourceSquare - destSquare);
        
        // for non-capturing moves, difference will be less than 10 
        if (difference < 10) {
            consecutiveAttack = "";
            return true;
        }

        // capturing move happened - remove captured piece
        Piece middlePiece = checkersBoard.removePiece((sourceSquare + destSquare) / 2);
        
        if (middlePiece.getOwner() == CheckersPiece.HUMAN_PLAYER) {
            noHumanPieces--;
        } else {
            noCompPieces--;
        }

        // check if consecutive capturing moves are possible
        consecutiveAttack = listCaptures(destSquare, player);

        return true;
    }

    // lists all valid moves available to the player
    public String listAllMoves(int player) {
        if (consecutiveAttack.length() != 0) {
            return consecutiveAttack;
        }
        String legalAttacks = listCaptures(player);
        if (legalAttacks.length() != 0) {
            return legalAttacks;
        }
        String legalMoves = listMoves(player);
        if (legalMoves.length() != 0) {
            return legalMoves;
        }
        return "";
    }
    
    // checks if a move is valid
    public boolean isValid(int source, int dest, int player) {
        
        String move = " " + source + " " + dest + " ";
        String legalMoves = listAllMoves(player);
        
        if (legalMoves.length() != 0) {
            return (" " + legalMoves).contains(move);
        }
        
        return false;
    }

    /* lists all captures available to the given player
     * Attacks are listed in the form: <source> + " " + <destination> + " "
     */
    public String listCaptures(int player) {
        if (consecutiveAttack.length() != 0) {
            return consecutiveAttack;
        }

        String attackList = "";

        for (int i = 0; i < checkersBoard.size(); i++) {
            attackList += listCaptures(i, player);
        }

        return attackList;
    }

    /* List all captures available to the given player from the given position 
     * on the board. Captures are listed in the form: 
     * <source> + " " + <destination> + " "
     */
    public String listCaptures(int square, int player) {
        String attackList = "";

        // try 4 possible moves each time, add to list if valid
        int posAttack[] = {square + 18, square - 18, square + 14, square - 14};

        for (int j = 0; j < posAttack.length; j++) {
            if (validCapture(square, posAttack[j], player)) {
                attackList += square + " " + posAttack[j] + " ";
            }
        }
        return attackList;
    }

    // list all non-capturing moves available for the given player
    public String listMoves(int player) {
        String moveList = "";

        //try 4 possible moves each time, add to list if valid
        for (int i = 0; i < checkersBoard.size(); i++) {
            int posMove[] = {i + 9, i - 9, i + 7, i - 7};

            for (int j = 0; j < posMove.length; j++) {
                if (validMove(i, posMove[j], player)) {
                    moveList += i + " " + posMove[j] + " ";//",";
                }
            }
        }
        return moveList;
    }

    // checks if the given capture is valid
    private boolean validCapture(int source, int dest, int player) {
        int tempOwner = checkersBoard.pieceOwnerAt(source);
        int tempType = checkersBoard.pieceTypeAt(source);

        // check owner of the piece and desination square
        if (tempOwner != player
                || checkersBoard.pieceOwnerAt(dest) != Board.EMPTY_SQUARE) {
            return false;
        }

        // player 1 - starts bottom, moves upwards
        if (tempType == CheckersPiece.CHECKER && player == CheckersPiece.HUMAN_PLAYER) {
            if (source - dest > 10) {
                return validAttackUp(source, dest, player);
            } else {
                return false;
            }
        }

        // computer - starts top, moves downwards
        if (tempType == CheckersPiece.CHECKER && player == CheckersPiece.COMPUTER_PLAYER) {
            if (dest - source > 10) {
                return validAttackDown(source, dest, player);
            } else {
                return false;
            }
        }

        // move up OR down if king
        if (tempType == CheckersPiece.KING) {
            if (Math.abs(source - dest) > 10) {
                return validAttackDown(source, dest, player)
                        || validAttackUp(source, dest, player);
            } else {
                return false;
            }
        }
        return false;
    }

    // checks if the given non-capturing move is valid
    private boolean validMove(int source, int dest, int player) {
        int tempOwner = checkersBoard.pieceOwnerAt(source);
        int tempType = checkersBoard.pieceTypeAt(source);

        // check owner of the piece and destination square
        if (tempOwner != player
                || checkersBoard.pieceOwnerAt(dest) != Board.EMPTY_SQUARE) {
            return false;
        }

        // player 1 - starts bottom, moves upwards
        if (tempType == CheckersPiece.CHECKER && player == CheckersPiece.HUMAN_PLAYER) {
            if (source - dest < 10) {
                return validMoveUp(source, dest);
            } else {
                return false;
            }
        }

        // computer - starts top, moves downwards
        if (tempType == CheckersPiece.CHECKER && player == CheckersPiece.COMPUTER_PLAYER) {
            // test for a move range
            if (dest - source < 10) {
                return validMoveDown(source, dest);
            } else {
                return false;
            }
        }

        // move up OR down
        if (tempType == CheckersPiece.KING) {
            if (Math.abs(source - dest) < 10) {
                return validMoveDown(source, dest)
                        || validMoveUp(source, dest);
            } else {
                return false;
            }
        }
        return false;
    }

    // checks if the given move upwards on the board is valid
    private boolean validMoveUp(int sourceSquare, int destSquare) {
        int difference = sourceSquare - destSquare;

        // the left-border case
        if (sourceSquare % 8 == 0) {
            return difference == 7;
        }

        //right-border case
        if (sourceSquare % 8 == 7) {
            return difference == 9;
        }

        return difference == 7 || difference == 9;
    }

    // checks if the given move downwards on the baord is valid
    private boolean validMoveDown(int sourceSquare, int destSquare) {
        int difference = destSquare - sourceSquare;

        // the left-border case
        if (sourceSquare % 8 == 0) {
            return difference == 9;
        }

        //right-border case
        if (sourceSquare % 8 == 7) {
            return difference == 7;
        }

        return difference == 7 || difference == 9;
    }

    // checks if the capturing move upwards is valid 
    private boolean validAttackUp(int sourceSquare, int destSquare, int player) {
        int difference = sourceSquare - destSquare;
        int midSquare = (sourceSquare + destSquare) / 2;

        if (checkersBoard.pieceOwnerAt(midSquare) == player
                || checkersBoard.pieceOwnerAt(midSquare) == Board.EMPTY_SQUARE) {
            return false;
        }

        // left border case
        if (sourceSquare % 8 == 0 || sourceSquare % 8 == 1) {
            return difference == 14;
        }

        // right border case
        if (sourceSquare % 8 == 7 || sourceSquare % 8 == 6) {
            return difference == 18;
        }

        return difference == 14 || difference == 18;
    }
    
    // checks if the capturing move downwards is valid 
    private boolean validAttackDown(int sourceSquare, int destSquare, int player) {
        int difference = destSquare - sourceSquare;
        int midSquare = (sourceSquare + destSquare) / 2;

        if (checkersBoard.pieceOwnerAt(midSquare) == player
                || checkersBoard.pieceOwnerAt(midSquare) == Board.EMPTY_SQUARE) {
            return false;
        }

        // left border case
        if (sourceSquare % 8 == 0 || sourceSquare % 8 == 1) {
            return difference == 18;
        }

        // right border case
        if (sourceSquare % 8 == 7 || sourceSquare % 8 == 6) {
            return difference == 14;
        }

        return difference == 14 || difference == 18;
    }
    
    // returns a random, valid move
    private String randomAIMove(){
        
        String moves = listAllMoves(CheckersPiece.COMPUTER_PLAYER);         
        String[] splitMoves = moves.split(" ");
            
        Random generator = new Random();
        int randomIndex = generator.nextInt(splitMoves.length);
        if (randomIndex % 2 != 0) {
            randomIndex--;
        }
        return splitMoves[randomIndex] + " " + splitMoves[randomIndex + 1] + " ";
    }
    
    // returns a move appropriate for the selected difficulty
    public String AIMove(){
        switch(AIType()){
            case BEGINNER_AI:
                return randomAIMove();
            case EASY_AI:
                return miniMax(this, EASY_AI);
            case MEDIUM_AI:
                return miniMax(this, MEDIUM_AI);
            case HARD_AI:
                return miniMax(this, HARD_AI);
            default:
                return "";
        }
    }
    
    /*
     * A heuristic evaluation function that takes into account the number of
     * pieces that each player has, the types of pieces and their positions on 
     * the board (pieces on the edge are rated higher because they can't be
     * beaten). 
     * The higher the returned value, the better for the AI and worse
     * for the human player. 
     */
    static double evalGameState(CheckersGame game){
        double humanPieceValue = 0;
        double compPieceValue = 0;
        for(int i=0; i< BOARD_SIZE; i++){
            if(!VALID_SQUARE[i])
                continue;
            if(game.getTypeAt(i) == Board.EMPTY_SQUARE)
                continue;
            double pieceValue = 0;
            if(game.getTypeAt(i) == CheckersPiece.CHECKER)
                pieceValue = 1;
            else if(game.getTypeAt(i) == CheckersPiece.KING)
                pieceValue = 1.4;
            pieceValue *= POSITION_MULTIPLIER[i];
            
            if(game.getOwnerAt(i) == CheckersPiece.COMPUTER_PLAYER)
                compPieceValue += pieceValue;
            else if(game.getOwnerAt(i) == CheckersPiece.HUMAN_PLAYER)
                humanPieceValue += pieceValue;
        }
        return (compPieceValue - humanPieceValue);
    }
    
    /*
     * A MiniMax algorithm that finds the best move for the AI. It uses 
     * alpha-beta pruning for optimisation. 
     * Unrealistic values are used for both alpha and beta when calling maxMove 
     * (-250 and 250) to force the function to replace them as soon as it can.
     */
    private String miniMax(CheckersGame game, int depth) {    
        Move temp = maxMove(game, -250, 250, 0, depth, "");
        return temp.move;
    }
    
    /*
     * Searches and picks the best move for the AI. Tries to maximise the result
     * of the evaluation function.
     */
    private Move maxMove(CheckersGame game, double alpha, double beta, int currentDepth, int depthLimit, String firstMove) {    
        
        // return and evaluate if reached game limit or game ended
        if(game.winner(CheckersPiece.COMPUTER_PLAYER) != -1 || currentDepth >= depthLimit)
            return new Move(firstMove, evalGameState(game));   
        
        // theoretical best move, -250 to be replaced at nearest opportunity
        Move bestMove = new Move("", -250); 
        
        // process all possible computer moves
        String moves = game.listAllMoves(CheckersPiece.COMPUTER_PLAYER);
        String [] splitMoves = moves.split(" ");
        
        for(int i=0; i < splitMoves.length; i+=2){
            CheckersGame temp = new CheckersGame(game);
            int source = Integer.parseInt(splitMoves[i]);
            int dest = Integer.parseInt(splitMoves[i+1]);
            
            String tempFirstMove = firstMove;
            
            // remember first move in the sequence
            if(firstMove.equals(""))                
                tempFirstMove = (source + " " + dest + " ");
            
            // try the move on a theoretical board
            temp.movePiece(source, dest, CheckersPiece.COMPUTER_PLAYER);
            
            // carry out consecutive capturing moves, if any
            if(temp.canAttackAgain()){
                Move move = maxMove(temp, alpha, beta, currentDepth, depthLimit, tempFirstMove);
            
                // remember the best move, save best value into alpha
                if(move.value > bestMove.value){
                    bestMove = move;
                    alpha = move.value;
                }        
            }
            
            /* 
             * once no more conecutive capturing moves are available, continue by 
             * inspecting opponent's moves.
             */
            else{
                // get opponent's best move (worst for AI, best for player)
                Move move = minMove(temp, alpha, beta, (currentDepth), depthLimit, tempFirstMove);
            
                // remember the best move, save best value into alpha
                if(move.value > bestMove.value){
                    bestMove = move;
                    alpha = move.value;
                }
                
                /* 
                 * Carry out alpha-beta pruning.
                 * If the opponent's recent best move is worse than his overall
                 * best move (remember, the opponent is trying to minimise the
                 * value!), then we can assume that it will not go down that path
                 * and instead pick the overall best move - therefore there is
                 * no need to search this path.
                 */
                if(beta != 250.0 && move.value > beta){
                    return bestMove;
                }
            }
        }
        return bestMove;
    }
    
    /*
     * Searches and picks the worst move for the AI. Tries to minimise the result
     * of the evaluation function.
     */    
    private Move minMove(CheckersGame game, double alpha, double beta, int currentDepth, int depthLimit, String firstMove) {
        
        // return and evaluate if reached game limit or game ended
        if(game.winner(CheckersPiece.HUMAN_PLAYER) != -1 || currentDepth >= depthLimit)
            return new Move(firstMove, evalGameState(game));
    
        // theoretical best move, -250 to be replaced at nearest opportunity
        Move worstMove = new Move("", 250); 
        
        // process all possible human moves
        String moves = game.listAllMoves(CheckersPiece.HUMAN_PLAYER);
        String [] splitMoves = moves.split(" ");
        
        for(int i=0; i < splitMoves.length; i+=2){
            CheckersGame temp = new CheckersGame(game);
            int source = Integer.parseInt(splitMoves[i]);
            int dest = Integer.parseInt(splitMoves[i+1]);
            
            // try the move on a theoretical board
            temp.movePiece(source, dest, CheckersPiece.HUMAN_PLAYER);
            
            // carry out consecutive capturing moves, if any
            if(temp.canAttackAgain()){
                Move move = minMove(temp, alpha, beta, currentDepth, depthLimit, firstMove);
            
                // remember the worst move for the AI, save worst value into beta
                if(move.value < worstMove.value){
                    worstMove = move;
                    beta = move.value;
                }
            }
            
            /* 
             * once no more conecutive capturing moves are available, continue by 
             * inspecting opponent's moves.
             */
            else{
                // get opponent's best move (best for AI, worst for player)
                Move move = maxMove(temp, alpha, beta, (currentDepth+1), depthLimit, firstMove);

                // remember the worst move, save wosrt value into beta
                if(move.value < worstMove.value){
                    worstMove = move;
                    beta = move.value;
                }
                
                /* 
                 * Carry out alpha-beta pruning.
                 * If the opponent's recent best move is worse than his overall
                 * best move (remember, the opponent is trying to maximise the
                 * value!), then we can assume that it will not go down that path
                 * and instead pick the overall best move - therefore there is
                 * no need to search this path.
                 */
                if(alpha != -250.0 && move.value < alpha){
                    return worstMove;
                }
            }
        }        
        return worstMove;
    }
    
    /*
     * A class used by the minimax algorithm to store the move's evaluated value
     * as well as the move itself.
     */
    public static class Move{
        public String move;
        public double value;
        
        Move(String argMoves, double argValue){
            move = argMoves;
            value = argValue;
        }
    }

    // returns type of piece at given location
    public int getTypeAt(int square) {
        return checkersBoard.pieceTypeAt(square);
    }

    // returns owner of piece at given location
    public int getOwnerAt(int square) {
        return checkersBoard.pieceOwnerAt(square);
    }

    // changes the AI settings
    public void setAI(int type) {
        AIType = type;
    }

    //returns the AI settings
    public int AIType() {
        return AIType;
    }

    // add a piece to the board at given location
    public void addPieceAt(int square, CheckersPiece piece) {
        checkersBoard.addPiece(square, piece);
    }

    // return list of consecutive moves
    public String getConsecutiveCaptures() {
        return consecutiveAttack;
    }

    // check if a player can capture again
    public boolean canAttackAgain() {
        return consecutiveAttack.length() != 0;
    }

    // clear the list of consecutive moves
    public void clearConsecutive() {
        consecutiveAttack = "";
    }

    // swap players' pieces and rotate the board
    public void swapPlayers() {
        
        /* 
         * Refuse to swap pieces if the player can capture for the second time.
         * This is done to preserve the continuity of the game.
         */
        if (consecutiveAttack.length() != 0) {
            return;
        }
        
        // swap piece numbers
        int temp = noHumanPieces;
        noHumanPieces = noCompPieces;
        noCompPieces = temp;

        // rotate the board
        checkersBoard.swapSquares();
        
        // swap piece owners
        for (int i = 0; i < checkersBoard.size(); i++) {
            if (VALID_SQUARE[i] && checkersBoard.pieceOwnerAt(i) >= 0) {
                CheckersPiece tempP = (CheckersPiece) checkersBoard.removePiece(i);
                tempP.changeOwner();
                checkersBoard.addPiece(i, tempP);
            }
        }
    }
}
