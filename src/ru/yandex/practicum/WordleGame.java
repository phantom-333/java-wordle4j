package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */
public class WordleGame {

    private final String answer;
    private int steps;
    private final WordleDictionary dictionary;
    protected PrintWriter pwLog;
    protected final int maxStepsCount;
    protected List<String> inputWord;
    protected String mask;
    protected String hint;
    protected Set<Character> existChars;
    protected Set<Character> nonExistChars;


    public WordleGame(PrintWriter pwLog, WordleDictionary dictionary, int maxStepsCount) throws Exception {
        if (pwLog == null) {
            throw new LogFileException("Лог не должен быть null");
        }
        this.pwLog = pwLog;
        if (dictionary == null || dictionary.isEmpty()) {
            throw new IllegalArgumentException("В конструктор WordleGame передан пустой или не существующий словарь");
        }
        this.dictionary = dictionary;
        if (maxStepsCount < 1) {
            throw new IllegalArgumentException("WordleGame: количество игровых попыток не должно быть < 1");
        }
        this.maxStepsCount = maxStepsCount;
        steps = 0;
        answer = dictionary.getRandomWorld();
        inputWord = new ArrayList<>();
        mask = "*".repeat(answer.length());
        existChars = new LinkedHashSet<>();
        nonExistChars = new LinkedHashSet<>();
        pwLog.printf("%sзагадано слово '%s'\n", GetTime.now(), answer);
    }

    public int getSteps() {
        return steps;
    }

    public String getAnswer() {
        return answer;
    }

    public int getRemainingSteps() {
        return maxStepsCount - steps;
    }

    public boolean gameOver() {
        return getRemainingSteps() == 0;
    }

    public boolean winGame() {
        if (inputWord.isEmpty()) {
            return false;
        }
        return answer.equals(inputWord.getLast());
    }

    public String getLastInputWord() {
        return inputWord.getLast();
    }

    public void makeMove(String word) throws WordInvalidLength, WordNotFoundInDictionary, ExitGame, GameOver {
        if (gameOver()) {
            throw new GameOver("Игра окончена!");
        }

        if ("exit".equals(word)) {
            throw new ExitGame();
        }

        if (word.isEmpty()) {
            pwLog.printf("%sИгрок передал ход компьютеру\n", GetTime.now());
            word = dictionary.getRandomWorld();
            pwLog.printf("%sКомпьютер выбрал слово: %s\n", GetTime.now(), word);
        } else {
            pwLog.printf("%sИгрок набрал слово: %s\n", GetTime.now(), word);

            word = dictionary.normalizeString(word);
            pwLog.printf("%sВыполнена нормализация слова: %s\n", GetTime.now(), word);

            if (word.length() != answer.length()) {
                throw new WordInvalidLength("Недопустимая длина слова");
            }
            if (!dictionary.isExist(word)) {
                throw new WordNotFoundInDictionary("Введенное слово не существует или отсутствует в списке допустимых слов (проверьте отсутствующие буквы)");
            }
        }

        inputWord.add(word);
        steps++;
        checkWord(word);
        dictionary.filter(mask, existChars, nonExistChars);
        pwLog.printf("%sПроверка слова '%s' завершена\n", GetTime.now(), word);
    }

    private void checkWord(String word) {
        mask = "";
        hint = "";
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == answer.charAt(i)) {
                mask += word.charAt(i);
                hint += word.charAt(i);
                existChars.add(word.charAt(i));
            } else {
                mask += "*";
                boolean charIsExist = false;
                for (int j = 0; j < answer.length(); j++) {
                    if (word.charAt(i) == answer.charAt(j)) {
                        charIsExist = true;
                        break;
                    }
                }
                if (charIsExist) {
                    hint += "^";
                    existChars.add(word.charAt(i));
                } else {
                    hint += "-";
                    nonExistChars.add(word.charAt(i));
                }
            }
        }
    }

    public String getHint() {
        return String.format("%s количество возможных комбинаций слов %d, осталось попыток %d, отсутствующие буквы %s",
                hint, dictionary.getWordsCount(), getRemainingSteps(), nonExistChars.toString());
    }

}
