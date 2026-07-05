package ru.yandex.practicum;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;


/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */
public class Wordle {

    protected static Scanner scanner;
    protected static final String logFileName = "wordle.log";
    protected static final String dictionaryFileName = "words_ru.txt";
    protected static PrintWriter pwLog;
    protected static final int wordLength = 5;
    protected static final int maxStepsCount = 5;
    protected static WordleGame wordleGame;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        try {
            Path logFile = Paths.get(logFileName);
            if (!Files.exists(logFile)) {
                try {
                    Files.createFile(logFile);
                } catch (IOException e) {
                    throw new LogFileException(e);
                }
            }
            try (FileWriter fileWriter = new FileWriter(logFileName)) {
                pwLog = new PrintWriter(fileWriter);
                pwLog.println(GetTime.now() + "запуск игры");

                try {
                    //подготовка словарей
                    WordleDictionaryLoader wordleDictionaryLoader = new WordleDictionaryLoader(pwLog, dictionaryFileName);
                    WordleDictionary wordleDictionary = wordleDictionaryLoader.load(wordLength);
                    wordleDictionary.normalizeDictionary();

                    wordleGame = new WordleGame(pwLog, wordleDictionary, maxStepsCount);
                    printMenu();
                    //цикл игры запускается отсюда
                    while (!wordleGame.gameOver() && !wordleGame.winGame()) {
                        try {
                            wordleGame.makeMove(inputWord());
                            outputWord(wordleGame.getLastInputWord());
                            if (wordleGame.winGame()) {
                                System.out.printf("Поздравляю!!! Загадано слово '%s', вы выиграли!\n", wordleGame.getAnswer());
                                pwLog.printf("%sИгрок разгадал слово\n", GetTime.now());
                            } else {
                                outputWord(wordleGame.getHint());
                            }
                        } catch (ExitGame e) {
                            pwLog.printf("%sвыход из игры\n", GetTime.now());
                            System.exit(0);
                        } catch (WordInvalidLength | WordNotFoundInDictionary e) {
                            System.out.println("Ошибка ввода слова: " + e.getMessage());
                            pwLog.printf("%sОшибка ввода слова: %s\n", GetTime.now(), e.getMessage());
                        }
                    }
                    if (wordleGame.gameOver() && !wordleGame.winGame()) {
                        System.out.printf("К сожалению, количество попыток закончилось.\n" +
                                "Было загадано слово '%s', попробуйте сыграть еще раз!\n", wordleGame.getAnswer());
                        pwLog.printf("%sИгрок не смог разгадать слово за %d попыток\n", GetTime.now(), maxStepsCount);
                    }
                } catch (Exception e) {
                    pwLog.println(GetTime.now() + e.getMessage());
                    for (StackTraceElement ste : e.getStackTrace()) {
                        pwLog.println(ste);
                    }
                }
            } catch (IOException e) {
                throw new LogFileException(e);
            }
        } catch (LogFileException e) {
            System.out.println("Ошибка работы с лог-файлом: " + e.getMessage());
            System.out.println("Дальнейшая работа программы невозможна");
            System.exit(0);
        }
    }


    static void printMenu() {
        System.out.println("╔" + "═".repeat(102) + "╗");
        System.out.printf("║ Загадано слово из %d букв, вам дается %d попыток его разгадать.%40s║\n", wordLength, maxStepsCount, " ");
        System.out.println("║ Правила игры: после ввода слова на строке ниже выводится подсказка, в которой правильные буквы       ║");
        System.out.println("║ отображаются на своем месте, а символом '-' отмечается буква, которой НЕТ в загаданном слове;        ║");
        System.out.println("║ символом '^' отмечается буква, которая ЕСТЬ в загаданном слове, но находится в другом месте.         ║");
        System.out.println("║ Если ввести пустую строку (нажать Enter), тогда компьютер сделает подсказку (выполнит 1 ход за вас). ║");
        System.out.println("║ Для выхода из игры введите слово 'exit'                                                              ║");
        System.out.println("╚" + "═".repeat(102) + "╝");
    }

    static String inputWord() {
        System.out.print("> ");
        return scanner.nextLine();
    }

    static void outputWord(String word) {
        System.out.printf("> %s\n", word);
    }
}


