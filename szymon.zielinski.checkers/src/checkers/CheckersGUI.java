package checkers;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Hashtable;
import java.applet.*;
import java.net.*;

public class CheckersGUI extends javax.swing.JApplet {

    private static final long serialVersionUID = -2882145680123623451L;
    private CheckersGame game, previousGameState;
    private BoardLayeredPane boardPanel;
    private JButton undoButton, gameType;
    private JLabel redPlayerNoPieces, bluePlayerNoPieces, currentPlayerLabel;
    private JSlider difficultySlider;
    private JMenu diffSubmenu;
    private JCheckBoxMenuItem musicCheck;
    private JMenuItem undoMenuItem, swapMenuItem;
    private JRadioButtonMenuItem onePlayerMenuItem, twoPlayerMenuItem, easyMenuItem, beginnerMenuItem, medMenuItem, hardMenuItem;

    private ImageIcon redChecker = new javax.swing.ImageIcon(getClass().getResource(("/images/RedChecker.gif")));
    private ImageIcon blueChecker = new javax.swing.ImageIcon(getClass().getResource(("/images/BlueChecker.gif")));
    private ImageIcon redKing = new javax.swing.ImageIcon(getClass().getResource(("/images/RedKingChecker.gif")));
    private ImageIcon blueKing = new javax.swing.ImageIcon(getClass().getResource(("/images/BlueKingChecker.gif")));

    private int currentPlayer = 0;
    private boolean displayedWinner = false;
    private AudioClip backgroundMusic;
    
    public void setSize(int width, int height){
        super.setSize(width, height);
        validate();
    }
     
    /* Stop the music manually when application closes. 
     * Prevents the music from continuously playing even after the user
     * closes the web browser tab for Checkers Online.
     */
    @Override
    public void stop() {
        backgroundMusic.stop();
    }
    
    // initialize basic objects and variables
    @Override
    public void init() {

        game = new CheckersGame();
        previousGameState = new CheckersGame(game);

        backgroundMusic = getAudioClip(getCodeBase());
        try {
            URL url = new URL(StaticVars.MUSIC_URL);

            backgroundMusic = getAudioClip(url);
            backgroundMusic.loop();
        } catch (MalformedURLException e) {
            System.err.println(e);
        }

        //set up the look and feel of the applet
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CheckersGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CheckersGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CheckersGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CheckersGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    initComponents();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // set up the game board and side menus
    private void initComponents() {

        JPanel mainPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        JPanel menuPanel = new JPanel();
        JPanel currentPlayerPanel = new JPanel();
        JPanel pieceNoPanel = new JPanel();
        Font pieceNoFont = new Font("Dialog", 0, 20);
        GridLayout menuPanelLayout = new java.awt.GridLayout(4, 1, 20, 8);
        GridLayout bottomRightPanelLayout = new java.awt.GridLayout(2, 1, 5, 5);
        Dimension diffSliderSize = new Dimension(10, 10);
        int appletWidth = 606;
        int appletHeight = 536;

        boardPanel = new BoardLayeredPane();

        // set up the number of pieces display
        redPlayerNoPieces = new JLabel();
        bluePlayerNoPieces = new JLabel();

        updatePieceNoDisplay();
        redPlayerNoPieces.setIcon(redChecker);
        redPlayerNoPieces.setFont(pieceNoFont);
        bluePlayerNoPieces.setIcon(blueChecker);
        bluePlayerNoPieces.setFont(pieceNoFont);

        // set up the current player display
        currentPlayerLabel = new JLabel();
        currentPlayerLabel.setIcon(blueChecker);

        // set layout for panels
        mainPanel.setLayout(new java.awt.BorderLayout());
        mainPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        rightPanel.setLayout(new java.awt.BorderLayout());

        menuPanel.setLayout(menuPanelLayout);
        menuPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, null, java.awt.Color.black, java.awt.Color.black));

        pieceNoPanel.setLayout(bottomRightPanelLayout);

        // set up the "new game" button
        JButton restartGameButton = new javax.swing.JButton();
        restartGameButton.setText("New Game");
        restartGameButton.addActionListener(new NewGameListener());

        // set up the "undo" button
        undoButton = new javax.swing.JButton();
        undoButton.setText("Undo");
        undoButton.addActionListener(new UndoListener());

        // set up the "change game type" button
        gameType = new JButton();
        gameType.setText("Two Player");
        gameType.addActionListener(new GameTypeButtonListener());
        
        // set up the "diffculty" slider
        difficultySlider  = new JSlider(JSlider.VERTICAL, CheckersGame.BEGINNER_AI, CheckersGame.HARD_AI, CheckersGame.EASY_AI);
        difficultySlider.setPreferredSize(diffSliderSize);
        difficultySlider.setSnapToTicks(true);

        difficultySlider.addChangeListener(new javax.swing.event.ChangeListener() {

            @Override
            public void stateChanged(javax.swing.event.ChangeEvent ce) {
                JSlider source = (JSlider) ce.getSource();
                int value = source.getValue();
                switch (value) {
                    case CheckersGame.BEGINNER_AI:
                        beginnerMenuItem.setSelected(true);
                        break;                        
                    case CheckersGame.EASY_AI:
                        easyMenuItem.setSelected(true);
                        break;
                    case CheckersGame.MEDIUM_AI:
                        medMenuItem.setSelected(true);
                        break;
                    case CheckersGame.HARD_AI:
                        hardMenuItem.setSelected(true);
                        break;
                }
                game.setAI(value);
                boardPanel.refreshBoardWithBorders(true);
                boardPanel.paintAttackBorders();
            }
        });

        // set up the labels for the difficulty slider
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(new Integer(CheckersGame.BEGINNER_AI), new JLabel("Beginner"));
        labelTable.put(new Integer(CheckersGame.EASY_AI), new JLabel("Easy"));
        labelTable.put(new Integer(CheckersGame.MEDIUM_AI), new JLabel("Medium"));
        labelTable.put(new Integer(CheckersGame.HARD_AI), new JLabel("Hard"));
        
        difficultySlider.setLabelTable(labelTable);
        difficultySlider.setPaintLabels(true);

        // add contents to the main panel
        mainPanel.add(boardPanel, java.awt.BorderLayout.CENTER);
        mainPanel.add(rightPanel, java.awt.BorderLayout.LINE_END);
        
        // add contents to the right panel
        rightPanel.add(currentPlayerPanel, java.awt.BorderLayout.PAGE_START);
        rightPanel.add(menuPanel, java.awt.BorderLayout.CENTER);
        rightPanel.add(pieceNoPanel, java.awt.BorderLayout.PAGE_END);
        
        // set the current player icon
        currentPlayerPanel.add(currentPlayerLabel, java.awt.BorderLayout.CENTER);
        
        // populate the menu panel
        menuPanel.add(restartGameButton);
        menuPanel.add(undoButton);
        menuPanel.add(gameType);
        menuPanel.add(difficultySlider);
        
        // set up the number of pieces display
        pieceNoPanel.add(redPlayerNoPieces);
        pieceNoPanel.add(bluePlayerNoPieces);

        // set size of the applet
        setSize(appletWidth, appletHeight);

        // set layout for the abckground panel
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());

        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 604, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0, 478, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 506, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0, 467, Short.MAX_VALUE)));

        // add a top, drop-down menu bar
        setJMenuBar(createTopMenus());    
    }
    
    /* 
     * Returns a top menu bar.
     * The menu bar contains two menu trees - "Game" and "Settings".
     * Game has the following options: {"New Game","Undo","Game Type","Swap Players","About"}
     * Settings has the following options: {"Difficulty","Music"}
     */
    public JMenuBar createTopMenus() {

        JMenuBar menuBar = new JMenuBar();
        
        // add menu tree Game
        JMenu menu = new JMenu("Game");
        menu.setMnemonic(KeyEvent.VK_Q);
        menuBar.add(menu);

        // New Game menu item with keyboard shortcut Alt + N
        JMenuItem menuItem = new JMenuItem("New Game");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, ActionEvent.ALT_MASK));
        menuItem.addActionListener(new NewGameListener());
        menu.add(menuItem);

        // Undo menu item with keyboard shortcut Alt + U
        undoMenuItem = new JMenuItem("Undo");
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_U, ActionEvent.ALT_MASK));
        undoMenuItem.addActionListener(new UndoListener());
        menu.add(undoMenuItem);

        menu.addSeparator();

        // Game Type submenu
        JMenu submenu = new JMenu("Game Type");        
        ButtonGroup gameTypeButtons = new ButtonGroup();        
        
        // One Player submenu item with keyboard shortcut Alt + 1
        onePlayerMenuItem = new JRadioButtonMenuItem("One Player");
        onePlayerMenuItem.setSelected(true);
        onePlayerMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_1, ActionEvent.ALT_MASK));
        gameTypeButtons.add(onePlayerMenuItem);
        submenu.add(onePlayerMenuItem);

        onePlayerMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (game.AIType() != CheckersGame.NO_AI) {
                    return;
                }
                diffSubmenu.setEnabled(true);
                game = new CheckersGame();
                game.setAI(CheckersGame.EASY_AI);
                gameType.setText("Two Player");
                difficultySlider.setValue(CheckersGame.EASY_AI);
                easyMenuItem.setSelected(true);
                undoButton.setEnabled(true);
                undoMenuItem.setEnabled(true);
                swapMenuItem.setEnabled(true);
                difficultySlider.setVisible(true);
                currentPlayer = 0;
                boardPanel.refreshBoardWithBorders(true);
                displayedWinner = false;
            }
        });

        // Two Player submenu item with keyboard shortcut Alt + 2
        twoPlayerMenuItem = new JRadioButtonMenuItem("Two Player");
        twoPlayerMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_2, ActionEvent.ALT_MASK));

        gameTypeButtons.add(twoPlayerMenuItem);
        submenu.add(twoPlayerMenuItem);

        twoPlayerMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (game.AIType() == CheckersGame.NO_AI) {
                    return;
                }
                diffSubmenu.setEnabled(false);
                game = new CheckersGame();
                game.setAI(CheckersGame.NO_AI);
                gameType.setText("One Player");
                undoButton.setEnabled(false);
                undoMenuItem.setEnabled(false);
                swapMenuItem.setEnabled(false);
                difficultySlider.setVisible(false);
                currentPlayer = 0;
                boardPanel.refreshBoardWithBorders(true);
                displayedWinner = false;
            }
        });

        menu.add(submenu);

        menu.addSeparator();

        // Swap Players menu item with keyboard shortcut Alt + S
        swapMenuItem = new JMenuItem("Swap Players");
        swapMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.ALT_MASK));

        swapMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                game.swapPlayers();
                currentPlayer = (currentPlayer + 1) % 2;
                boardPanel.AIMove();
                swapMenuItem.setEnabled(false);
                previousGameState = game;
                boardPanel.refreshBoardWithBorders(true);
                boardPanel.paintAttackBorders();
                validate();
                repaint();
            }
        });
        menu.add(swapMenuItem);

        menu.addSeparator();

        // About menu item with keyboard shortcut Alt + I
        menuItem = new JMenuItem("About");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_I, ActionEvent.ALT_MASK));

        menuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JOptionPane.showMessageDialog(getContentPane(), StaticVars.ABOUT, "About Checkers Online", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(menuItem);

        // add menu tree Settings
        menu = new JMenu("Settings");
        menuBar.add(menu);

        // Difficulty submenu
        diffSubmenu = new JMenu("Difficulty");
        ButtonGroup difficultyOptions = new ButtonGroup();

        // Hard submenu item with keyboard shortcut Alt + H
        hardMenuItem = new JRadioButtonMenuItem("Hard");
        hardMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H, ActionEvent.ALT_MASK));
        difficultyOptions.add(hardMenuItem);

        hardMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                game.setAI(CheckersGame.HARD_AI);
                difficultySlider.setValue(CheckersGame.HARD_AI);
                boardPanel.refreshBoardWithBorders(true);
                boardPanel.paintAttackBorders();
            }
        });

        diffSubmenu.add(hardMenuItem);

        // Medium submenu item with keyboard shortcut Alt + M
        medMenuItem = new JRadioButtonMenuItem("Medium");
        medMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_M, ActionEvent.ALT_MASK));
        difficultyOptions.add(medMenuItem);

        medMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                game.setAI(CheckersGame.MEDIUM_AI);
                difficultySlider.setValue(CheckersGame.MEDIUM_AI);
                boardPanel.refreshBoardWithBorders(true);
                boardPanel.paintAttackBorders();
            }
        });

        diffSubmenu.add(medMenuItem);

        // Easy submenu item with keyboard shortcut Alt + E
        easyMenuItem = new JRadioButtonMenuItem("Easy");
        easyMenuItem.setSelected(true);
        easyMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_E, ActionEvent.ALT_MASK));

        easyMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                game.setAI(CheckersGame.EASY_AI);
                difficultySlider.setValue(CheckersGame.EASY_AI);
                boardPanel.refreshBoardWithBorders(true);
                boardPanel.paintAttackBorders();
            }
        });

        difficultyOptions.add(easyMenuItem);
        diffSubmenu.add(easyMenuItem);

        // Beginner submenu item with keyboard shortcut Alt + B
        beginnerMenuItem = new JRadioButtonMenuItem("Beginner");
        beginnerMenuItem.setSelected(true);
        beginnerMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_B, ActionEvent.ALT_MASK));

        beginnerMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                game.setAI(CheckersGame.BEGINNER_AI);
                difficultySlider.setValue(CheckersGame.BEGINNER_AI);
                boardPanel.refreshBoardWithBorders(true);
                boardPanel.paintAttackBorders();
            }
        });

        difficultyOptions.add(beginnerMenuItem);
        diffSubmenu.add(beginnerMenuItem);        

        menu.add(diffSubmenu);

        menu.addSeparator();

        // music menu item with keyboard shortcut Alt + 0
        musicCheck = new JCheckBoxMenuItem("Music");
        musicCheck.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_0, ActionEvent.ALT_MASK));
        musicCheck.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (musicCheck.isSelected()) {
                    backgroundMusic.loop();
                } else {
                    backgroundMusic.stop();
                }
            }
        });
        musicCheck.setSelected(true);
        menu.add(musicCheck);

        return menuBar;
    }

    // A JLabel that remembers its position on the checkers board
    public class PositionedLabel extends JLabel {
        private static final long serialVersionUID = 8259625158276346535L;
        private int position;

        PositionedLabel(Icon image, int horizontalAlignment, int argPosition) {
            super(image, horizontalAlignment);
            position = argPosition;
        }

        public int getPos() {
            return position;
        }

        public void setPos(int argPos) {
            position = argPos;
        }
    }

    // update the Piece Number Display
    private void updatePieceNoDisplay() {
        redPlayerNoPieces.setText("" + game.noCompPieces());
        bluePlayerNoPieces.setText("" + game.noHumanPieces());
    }

    // Set current player icon and disable menus if the game has ended
    private void setCurrentPlayerIcon() {
        if (game.winner(currentPlayer) == CheckersPiece.HUMAN_PLAYER) {
            currentPlayerLabel.setIcon(blueKing);
            displayWinnerMessage(CheckersPiece.HUMAN_PLAYER);
            undoButton.setEnabled(false);
            undoMenuItem.setEnabled(false);
            swapMenuItem.setEnabled(false);
        } else if (game.winner(currentPlayer) == CheckersPiece.COMPUTER_PLAYER) {
            currentPlayerLabel.setIcon(redKing);
            displayWinnerMessage(CheckersPiece.COMPUTER_PLAYER);
            undoButton.setEnabled(false);
            undoMenuItem.setEnabled(false);
            swapMenuItem.setEnabled(false);
        } else if (currentPlayer == CheckersPiece.HUMAN_PLAYER) {
            currentPlayerLabel.setIcon(blueChecker);
        } else if (currentPlayer == CheckersPiece.COMPUTER_PLAYER) {
            currentPlayerLabel.setIcon(redChecker);
        }
    }

    // display a message when the game has ended
    private void displayWinnerMessage(int winner) {
        if (displayedWinner == true) {
            return;
        }
        
        String title = "The game has ended!";
        String message;         
        ImageIcon winnerImage;
        
        // decide on message contents
        if (winner == CheckersPiece.HUMAN_PLAYER) {
            message = "BLUE won the game.";
            winnerImage = blueKing;
        } else {
            message = "RED won the game.";
            winnerImage = redKing;
        }
        
        // display the message 
        JOptionPane.showMessageDialog(getContentPane(),
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE,
                winnerImage);
        
        // remember that the message was displayed
        displayedWinner = true;
    }

    // A layered panel for the checkers board
    public class BoardLayeredPane extends JLayeredPane {
        
        private static final long serialVersionUID = -7159931441732047527L;
        private static final int BOARD_WIDTH = 500;
        private static final int GRID_ROWS = 8;
        private static final int GRID_COLS = 8;
        private static final int GAP = 4;
        private GridLayout gridlayout = new GridLayout(GRID_ROWS, GRID_COLS, GAP, GAP);
        private JPanel squarePanel = new JPanel(gridlayout);
        private JPanel[] squares = new JPanel[CheckersGame.BOARD_SIZE];
        private final Color darkSquare = new Color(51, 51, 51);
        private final Color brightSquare = new Color(204, 204, 225);

        public BoardLayeredPane() {

            squarePanel.setSize(new Dimension(BOARD_WIDTH, BOARD_WIDTH));
            squarePanel.setBackground(Color.black);
            for (int i = 0; i < CheckersGame.BOARD_SIZE; i++) {
                squares[i] = new JPanel(new GridBagLayout());

                squares[i].setBackground(darkSquare);

                if (!CheckersGame.VALID_SQUARE[i]) {
                    squares[i].setBackground(brightSquare);
                } else if (game.getOwnerAt(i) == 0 && game.getTypeAt(i) == 0) {
                    squares[i].add(new PositionedLabel(blueChecker, SwingConstants.CENTER, i));
                } else if (game.getOwnerAt(i) == 1 && game.getTypeAt(i) == 0) {
                    squares[i].add(new PositionedLabel(redChecker, SwingConstants.CENTER, i));
                }

                squarePanel.add(squares[i]);
            }
            squarePanel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
            add(squarePanel, JLayeredPane.DEFAULT_LAYER);
            
            // add mouse listeners for drag and drop
            BoardMouseAdapter myMouseAdapter = new BoardMouseAdapter();
            addMouseListener(myMouseAdapter);
            addMouseMotionListener(myMouseAdapter);
        }

        /*
         * Repaint the board based on the status of the game. 
         * If boolean "borders" is set to true, borders around squares will be 
         * removed.
         */
        public void refreshBoardWithBorders(boolean borders) {
            for (int i = 0; i < CheckersGame.BOARD_SIZE; i++) {
                squares[i].removeAll();
                if(borders)
                    squares[i].setBorder(javax.swing.BorderFactory.createEmptyBorder());

                if (!CheckersGame.VALID_SQUARE[i]) {
                    squares[i].setBackground(brightSquare);
                } 
                
                else if (game.getOwnerAt(i) == CheckersPiece.HUMAN_PLAYER && game.getTypeAt(i) == CheckersPiece.CHECKER) {
                    squares[i].add(new PositionedLabel(blueChecker, SwingConstants.CENTER, i));
                } 
                
                else if (game.getOwnerAt(i) == CheckersPiece.COMPUTER_PLAYER && game.getTypeAt(i) == CheckersPiece.CHECKER) {
                    squares[i].add(new PositionedLabel(redChecker, SwingConstants.CENTER, i));
                } 
                
                else if (game.getOwnerAt(i) == CheckersPiece.HUMAN_PLAYER && game.getTypeAt(i) == CheckersPiece.KING) {
                    squares[i].add(new PositionedLabel(blueKing, SwingConstants.CENTER, i));
                } 
                
                else if (game.getOwnerAt(i) == CheckersPiece.COMPUTER_PLAYER && game.getTypeAt(i) == CheckersPiece.KING) {
                    squares[i].add(new PositionedLabel(redKing, SwingConstants.CENTER, i));
                }

                squarePanel.add(squares[i]);
            }
            updatePieceNoDisplay();
            setCurrentPlayerIcon();
            validate();
            repaint();
        }

        // A mouse adapter for the board that enables Drag and Drop
        private class BoardMouseAdapter extends MouseAdapter {

            private PositionedLabel draggedLabel;
            private JPanel clickedPanel;
            private int labelMiddle;
            private int originalLabelPos = -1;
            
            @Override
            public void mousePressed(MouseEvent e) {

                // if the game has ended, return
                if (game.winner(currentPlayer) != -1) {
                    return;
                }

                clickedPanel = (JPanel) squarePanel.getComponentAt(e.getPoint());

                // if the clicked panel has no labels
                if (!(clickedPanel instanceof JPanel) ||clickedPanel.getComponentCount() == 0) {
                    return;
                }

                // retrieve item from clicked panel, check if it's a label
                Component clickedComp = clickedPanel.getComponent(0);
                       
                if (!(clickedComp instanceof PositionedLabel)){
                    return;
                } 
                
                // retrieve the label
                draggedLabel = (PositionedLabel) clickedComp;
                originalLabelPos = draggedLabel.getPos();

                // check owner of the picked up piece/label
                if (game.getOwnerAt(originalLabelPos) != currentPlayer) {
                    return;
                }

                // remove the label from the board
                clickedPanel.remove(draggedLabel);

                //position the label on the mouse cursor
                labelMiddle = draggedLabel.getWidth() / 2;
                int x = e.getPoint().x - labelMiddle;
                int y = e.getPoint().y - labelMiddle;                
                draggedLabel.setLocation(x, y);
                
                // add the label to the drag layer
                add(draggedLabel, JLayeredPane.DRAG_LAYER);
                
                validate();
                repaint();
            }


            // update the position of the label as mouse cursor moves
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedLabel == null
                        || game.getOwnerAt(originalLabelPos) != currentPlayer) {
                    return;
                }
                
                int x = e.getPoint().x - labelMiddle;
                int y = e.getPoint().y - labelMiddle;
                draggedLabel.setLocation(x, y);
                repaint();
            }

            // act when the label is dropped
            @Override
            public void mouseReleased(MouseEvent e) {

                // if no label, or incorrect owner of the piece, quit
                if (draggedLabel == null
                        || game.getOwnerAt(originalLabelPos) != currentPlayer) {
                    return;
                }
                
                int tempType = game.getTypeAt(originalLabelPos);
                boolean gotCoronated = false;

                // remove dragLabel from the drag layer
                remove(draggedLabel);
                draggedLabel = null;                

                // locate where the label was dropped
                JPanel droppedPanel = (JPanel) squarePanel.getComponentAt(e.getPoint());

                // find the position of the dropped item, return -1 if off the game board 
                int droppedPos = -1;
                for (int i = 0; i < squares.length; i++) {
                    if (squares[i] == droppedPanel) {
                        droppedPos = i;
                        break;
                    }
                }

                // if the move was invalid put label in original position
                CheckersGame temp = new CheckersGame(game);
                if (droppedPos == -1 || !game.movePiece(originalLabelPos, droppedPos, currentPlayer)) {
                    boardPanel.refreshBoardWithBorders(false);
                    return;
                }

                // store the previous game state for the "Undo" option
                previousGameState = temp;
                
                boardPanel.refreshBoardWithBorders(true);               
                
                // remember if the moved piece was just coronated
                if (game.getTypeAt(droppedPos) == CheckersPiece.KING && tempType == CheckersPiece.CHECKER) {
                    gotCoronated = true;
                }

                updatePieceNoDisplay();
                validate();
                repaint();

                // check if the piece can jump again, change current player
                if (!game.canAttackAgain() || gotCoronated) {
                    game.clearConsecutive();
                    currentPlayer = (++currentPlayer) % 2;
                }

                // move AI, if any active
                AIMove();

                setCurrentPlayerIcon();
                
                // paint capturing borders
                paintAttackBorders();
            }
        }

        // perform the appropriate AI move
        public void AIMove() {
            
            if(game.AIType() == CheckersGame.NO_AI){
                return;
            }
            
            // loop while the computer can jump again
            while (currentPlayer == CheckersPiece.COMPUTER_PLAYER) {
                
                // check if the game has ended
                if (game.winner(currentPlayer) != -1) {
                    break;
                }
                
                // get AI move
                String aiMove = game.AIMove();
                            
                // process the AI move
                String[] split = aiMove.split(" ");                
                
                int source = Integer.parseInt(split[0]);
                int destination = Integer.parseInt(split[1]);
                            
                int tempType = game.getTypeAt(source);
                boolean gotCoronated = false;

                // carry out the move, update the displayed GUI
                game.movePiece(source, destination, currentPlayer);
                
                boardPanel.refreshBoardWithBorders(false);               

                // set borders around the AI move
                squares[source].setBorder(BorderFactory.createLineBorder(Color.RED));
                squares[destination].setBorder(BorderFactory.createLineBorder(Color.RED));
                
                // remember if the moved piece got coronated 
                if (game.getTypeAt(destination) == CheckersPiece.KING && tempType == CheckersPiece.CHECKER) {
                    gotCoronated = true;
                }
                
                // check if the piece can jump again, change current player
                if (!game.canAttackAgain() || gotCoronated) {
                    game.clearConsecutive();
                    currentPlayer = (++currentPlayer) % 2;
                }

                updatePieceNoDisplay();
                repaint();
            }
        }

        // paints borders around pieces that must capture pieces
        private void paintAttackBorders() {

            String consecutiveCaptures = game.getConsecutiveCaptures();
            String validAttacks = game.listCaptures(currentPlayer);
            
            if (consecutiveCaptures.length() != 0) {
                String[] splitStr = consecutiveCaptures.split(" ");
                for (int i = 0; i < splitStr.length; i+=2) {
                    int sourceSqaure = Integer.parseInt(splitStr[i]);
                    int targetSqaure = Integer.parseInt(splitStr[i+1]);
                    squares[sourceSqaure].setBorder(BorderFactory.createLineBorder(Color.GREEN));
                    squares[targetSqaure].setBorder(BorderFactory.createLineBorder(Color.YELLOW));
                }
            } else if (validAttacks.length() != 0) {
                String[] splitStr = validAttacks.split(" ");
                for (int i = 0; i < splitStr.length; i+=2) {
                    int sourceSqaure = Integer.parseInt(splitStr[i]);
                    int targetSqaure = Integer.parseInt(splitStr[i+1]);
                    squares[sourceSqaure].setBorder(BorderFactory.createLineBorder(Color.GREEN));
                    squares[targetSqaure].setBorder(BorderFactory.createLineBorder(Color.YELLOW));
                }
            }
        }
    }

    /*
     * Below is a list of COMMON action listeners - listeners that are used
     * more than once in the application - e.g. NewGameListener is used in the
     * top drop-down menu as well as the right-hand menu panel. 
     */
    
    // A listener for the new game button 
    class NewGameListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int tempAI = game.AIType();
            game = new CheckersGame();
            previousGameState = new CheckersGame(game);
            currentPlayer = 0;
            boardPanel.refreshBoardWithBorders(true);
            game.setAI(tempAI);
            displayedWinner = false;
            if (game.AIType() != CheckersGame.NO_AI) {
                swapMenuItem.setEnabled(true);
                undoMenuItem.setEnabled(true);
                undoButton.setEnabled(true);
            }
        }
    }

    // A listener for the undo button 
    class UndoListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            game = new CheckersGame(previousGameState);
            currentPlayer = 0;
            boardPanel.refreshBoardWithBorders(true);
            boardPanel.paintAttackBorders();
            undoButton.setEnabled(false);
            undoMenuItem.setEnabled(false);
        }
    }

    // A listener for the game type selection button 
    class GameTypeButtonListener implements ActionListener {

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            int ai = game.AIType();
            game = new CheckersGame();

            if (ai != CheckersGame.NO_AI) {
                game.setAI(CheckersGame.NO_AI);
                gameType.setText("One Player");
                twoPlayerMenuItem.setSelected(true);
                diffSubmenu.setEnabled(false);
                undoButton.setEnabled(false);
                undoMenuItem.setEnabled(false);
                swapMenuItem.setEnabled(false);
                difficultySlider.setVisible(false);
            } else {
                game.setAI(CheckersGame.EASY_AI);
                gameType.setText("Two Player");
                onePlayerMenuItem.setSelected(true);
                difficultySlider.setValue(CheckersGame.EASY_AI);
                easyMenuItem.setSelected(true);
                diffSubmenu.setEnabled(true);
                undoButton.setEnabled(true);
                undoMenuItem.setEnabled(true);
                swapMenuItem.setEnabled(true);
                difficultySlider.setVisible(true);
            }
            currentPlayer = 0;
            boardPanel.refreshBoardWithBorders(true);
            displayedWinner = false;
        }
    }
}