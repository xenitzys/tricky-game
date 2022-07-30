package me.cxmilo.tricky.game;

import me.cxmilo.tricky.coordinate.Coordinates;
import me.cxmilo.tricky.entity.UserEntity;
import me.cxmilo.tricky.table.Tables;
import me.cxmilo.tricky.util.ChatColor;
import me.cxmilo.tricky.util.ConsoleUtil;
import me.cxmilo.tricky.verifier.DrawVerifier;
import me.cxmilo.tricky.verifier.HorizontalVerifier;
import me.cxmilo.tricky.verifier.Verifier;
import me.cxmilo.tricky.verifier.VerticalVerifier;

import java.util.logging.Logger;
import java.util.stream.Collectors;

public record GameController(Game game, GameLoop gameLoop) {


    public boolean score(int cell) {
        Logger logger = gameLoop.logger;

        boolean[] score = {false};

        game.entities().stream().filter(entity -> entity.getId() == game.getTurn()).findFirst().ifPresent(entity -> {
            var coordinates = new Coordinates(game.getTable());
            coordinates.getPoint(cell).ifPresent(point -> {
                if (point.getOwner().isPresent()) return;
                point.setOwner((UserEntity) entity);
                point.getOwner().ifPresent(owner -> {
                    owner.getScoredPoints().add(point);
                    checkForWinner(logger, owner);
                });
                score[0] = true;
            });
        });

        return score[0];
    }

    public void printTable() {
        ConsoleUtil.clearScreen();
        Tables.printTable(game.getTable());
        System.out.println();
    }

    /**
     * Checks if the given user has won the game.
     *
     * @param logger the logger to use.
     * @param entity the entity to check for winning.
     */
    public void checkForWinner(Logger logger, UserEntity entity) {
        byte validate = validate(game, entity);
        if (validate != -1) {
            gameLoop.stop();
            printTable();
        }

        switch (validate) {
            case 0 -> logger.info(ChatColor.translateColorCodes("&dIt's a draw!&r"));
            case 1 -> logger.info(ChatColor.translateColorCodes("&d" + entity.getName() + "&b has won the game!&r"));
        }
    }

    /**
     * Returns a byte if the game is over:
     * <p>
     * not over = -1
     * draw = 0
     * player = 1
     *
     * @param game  the game to check
     * @param owner the owner of the point
     * @return a byte if the game is over
     */
    private byte validate(Game game, UserEntity owner) {
        // Vertical verifier
        Verifier verifier = new VerticalVerifier();
        if (verifier.verify(game, owner, owner.getScoredPoints())) {
            return 1;
        }
        // Horizontal verifier
        verifier = new HorizontalVerifier();
        if (verifier.verify(game, owner, owner.getScoredPoints())) {
            return 1;
        }
        // Draw verifier
        verifier = new DrawVerifier();
        if (verifier.verify(game, null, game.getTable().points().stream().filter(point -> point.getOwner().isPresent()).collect(Collectors.toSet()))) {
            return 0;
        }
        return -1;
    }

    public void updateTurn() {
        var nextTurn = game.getTurn() + 1;

        if (nextTurn >= game.entities().size()) {
            nextTurn = 0;
        }

        game.setTurn(nextTurn);
    }
}