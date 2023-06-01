package hangman.controllers;

import com.google.gson.Gson;
import hangman.interfaces.IdentifierGeneration;
import hangman.models.Game;
import hangman.models.Guess;
import spark.Request;
import spark.Response;

import java.util.*;

public class GameController {

    private static HashMap<UUID, Game> games = new HashMap();
    private static List<String> words = Arrays.asList("Banana", "Canine", "Unosquare", "Airport");

    private final IdentifierGeneration identifierGeneration;

    public GameController(IdentifierGeneration identifierGeneration) {
        this.identifierGeneration = identifierGeneration;
    }

    public UUID createGame() {
        var newGameId = identifierGeneration.retrieveIdentifier();
        var newGame = new Game(3, retrieveWord());

        games.put(newGameId, newGame);

        return newGameId;
    }

    public Game getGame(Request request, Response response) {
        var gameArgument = request.params("game_id");
        var gameId = UUID.fromString(gameArgument);
        if (gameId == null || !games.containsKey(gameId)) {
            response.status(404);
            return null;
        }

        return games.get(gameId);
    }

    public Game makeGuess(Request request, Response response) {
        var game = getGame(request, response);
        if (game != null) {
            var guess = new Gson().fromJson(request.body(), Guess.class);

            if (guess == null || guess.getLetter() == null || guess.getLetter().length() != 1) {
                throw new IllegalArgumentException("Guess must be supplied with 1 letter");
            }

            //Variables to hold the games words
            String unmaskedWord = game.getUnmaskedWord();
            String displayedWord = game.getWord();
            var incorrectGuesses = game.getIncorrectGuesses();

            // Check if the guess is in the answer
            if (unmaskedWord.toLowerCase().contains(guess.getLetter().toLowerCase())) {
                for (int x = 0; x < unmaskedWord.length(); x++) {
                    if (Character.toLowerCase(unmaskedWord.charAt(x)) == Character.toLowerCase(guess.getLetter().charAt(0))) {
                        // this replaces the underscore with the correct letter
                        displayedWord = displayedWord.substring(0, x) + Character.toLowerCase(guess.getLetter().charAt(0)) + displayedWord.substring(x + 1);
                    }
                }
            } else {
                incorrectGuesses.add(guess.getLetter());
                int currentGuesses = game.getRemainingGuesses();
                game.setRemainingGuesses(currentGuesses - 1);
                if (currentGuesses == 0) {
                    game.setStatus("Game Over");
                }
            }

            game.setWord(displayedWord);
            game.setIncorrectGuesses(incorrectGuesses);
            if (displayedWord == unmaskedWord) {
                game.setStatus("Congratulations... you won!!");
            }
            return game;
        }
        return null;
    }

    public Game deleteGame(Request request, Response response) {
        return null;
    }

    private static String retrieveWord() {
        var rand = new Random();
        return words.get(rand.nextInt(words.size() - 1));
    }
}
