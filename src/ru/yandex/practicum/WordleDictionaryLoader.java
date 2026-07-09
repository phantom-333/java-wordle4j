package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public class WordleDictionaryLoader {
    protected PrintWriter pwLog;
    protected String filePath = "";

    public WordleDictionaryLoader(PrintWriter pwLog, String filePath) throws LogFileException {
        if (pwLog == null) {
            throw new LogFileException("Лог не должен быть null");
        }
        this.pwLog = pwLog;
        if (filePath == null || filePath.isBlank()) {
            pwLog.printf("%sимя файла словаря в классе WordleDictionaryLoader не может null или пустым\n", GetTime.now());
        } else {
            if (!Files.exists(Paths.get(filePath))) {
                pwLog.printf("%sне найден файл словаря '%s'\n",GetTime.now(), filePath);
            } else {
                this.filePath = filePath;
            }
        }
    }

    public WordleDictionary load(int wordLength) throws Exception {
        if (filePath.isBlank()) {
            throw new Exception("файл словаря отсутствует");
        }
        try (BufferedReader reader = new BufferedReader(
                new FileReader(filePath, StandardCharsets.UTF_8))) {
            List<String> words = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line);
            }
            pwLog.printf("%sзагрузка словаря завершена, загружено %d слов\n", GetTime.now(), words.size());
            return new WordleDictionary(pwLog, words, wordLength);
        } catch (IOException e) {
            throw new Exception(e);
        }
    }
}
