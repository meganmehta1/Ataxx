/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.ArrayList;
import java.util.Random;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

/**
 * A Player that computes its own moves.
 *
 * @author Megan Jay Mehta
 */
class AI extends Player {

    /**
     * Maximum minimax search depth before going to static evaluation.
     */
    private static final int MAX_DEPTH = 4;
    /**
     * A position magnitude indicating a win (for red if positive, blue
     * if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     * a random-number generator for use in move computations.  Identical
     * seeds produce identical behaviour.
     */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /**
     * The move found by the last call to the findMove method
     * above.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _foundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _foundMove. If the game is over
     * on BOARD, does not set _foundMove.
     */
    private int minMax(Board board, int depth, boolean saveMove,
                       int sense, int alpha, int beta) {
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        Move best = null;
        int bestScore = 0;
        ArrayList<Move> possibleMoves = new ArrayList<>();
        possibleMoves = getPossibleMoves(board, possibleMoves);

        if (sense == -1) {
            bestScore = INFTY;
            for (int j = 0; j < possibleMoves.size(); j++) {
                Board boardCopy = new Board(board);
                boardCopy.makeMove(possibleMoves.get(j));
                int response = minMax(boardCopy, depth - 1,
                        false, 1, alpha, beta);
                if (response < bestScore) {
                    bestScore = response;
                    best = possibleMoves.get(j);
                    beta = min(beta, bestScore);
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
        } else {
            bestScore = -INFTY;
            for (int j = 0; j < possibleMoves.size(); j++) {
                Board boardCopy = new Board(board);
                boardCopy.makeMove(possibleMoves.get(j));
                int response = minMax(boardCopy, depth - 1,
                        false, -1, alpha, beta);
                if (response > bestScore) {
                    bestScore = response;
                    best = possibleMoves.get(j);
                    alpha = max(alpha, bestScore);
                    if (alpha >= beta) {
                        break;
                    }
                }
            }
        }
        if (best == null && possibleMoves.isEmpty()) {
            best = Move.PASS;
            Board boardCopy = new Board(board);
            boardCopy.makeMove((best));
            minMax(boardCopy, depth - 1, false, -1, alpha, beta);
        }
        if (saveMove) {
            _lastFoundMove = best;
        }
        return bestScore;
    }

    /**
     * Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     * won positions, and 0 for ties.
     */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return (
            switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            });
        }

        if (winningValue == 0) {
            return 0;
        } else if (board.redPieces() > board.bluePieces()) {
            winner = RED;
            return winningValue + board.redPieces();
        } else {
            winner = BLUE;
            return winningValue + board.bluePieces();
        }
    }

    /**
     * Pseudo-random number generator for move computation.
     */
    private Random _random = new Random();

    private ArrayList<Move> getPossibleMoves(Board board,
                                             ArrayList<Move> possibleMoves) {
        for (char c = 'a'; c <= 'g'; c++) {
            for (char r = '1'; r <= '7'; r++) {
                if (board.get(c, r) == board.whoseMove()) {
                    for (char dc = ((char) (c - 2));
                         dc <= ((char) (c + 2)); dc++) {
                        for (char dr = ((char) (r - 2));
                             dr <= ((char) (r + 2)); dr++) {
                            if (dc >= 'a' && dr >= '1'
                                    && dc <= 'g' && dr <= '7') {
                                if (!(c == dc && r == dr)) {
                                    Move thisMove = Move.move(c, r, dc, dr);
                                    if (board.legalMove(thisMove)) {
                                        possibleMoves.add(thisMove);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return possibleMoves;
    }
}
