package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.*;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {
    protected PrintWriter pwLog;
    private List<String> words;
    private final int wordLength;

    public WordleDictionary(PrintWriter pwLog, List<String> words, int wordLength) throws Exception {
        if (pwLog == null) {
            throw new LogFileException("Лог не должен быть null");
        }
        this.pwLog = pwLog;
        if (wordLength < 3) {
            throw new Exception("длина слов менее 3 символов");
        }
        this.wordLength = wordLength;
        if (words == null || words.isEmpty()) {
            throw new Exception("загруженный список слов пуст или null");
        } else {
            this.words = words;
        }
    }

    public String normalizeString(String source) {
        source = source.trim().toLowerCase().replaceAll("[^\\p{L}\\s]", "").replace("ё", "е");
        String[] items = source.split(" ");
        return items[0];
    }

    private String normalizeMask(String mask) {
        mask = mask.trim().toLowerCase().replaceAll("[^\\p{L}\\s*]", "").replace("ё", "е");
        String[] items = mask.split(" ");
        return items[0];
    }

    public void normalizeDictionary() {
        Set<String> normalizeList = new HashSet<>();
        for (String word : words) {
            String normalizedWord = normalizeString(word);
            if (normalizedWord.length() == wordLength) {
                normalizeList.add(normalizedWord);
            }
        }
        words = new ArrayList<>(normalizeList);
        pwLog.printf("%sнормализация словаря по %d символам выполнена, в словаре %d слов\n", GetTime.now(), wordLength, words.size());
    }

    public int getWordsCount() {
        return words.size();
    }

    public boolean isEmpty() {
        return words.isEmpty();
    }

    public boolean isExist(String word) {
        return words.contains(normalizeString(word));
    }

    public void filter(String mask, Set<Character> existChars, Set<Character> nonExistChars) {
        List<String> filteredList = new ArrayList<>();
        for (String word : words) {
            if (compareMask(word, mask, existChars, nonExistChars)) {
                filteredList.add(word);
            }
        }
        words = filteredList;
        pwLog.println(GetTime.now() + "Словарь отфильтрован, в словаре " + words.size() + " слов");
    }

    public boolean compareMask(String source, String mask, Set<Character> existChars, Set<Character> nonExistChars) {
        source = normalizeString(source);
        mask = normalizeMask(mask);
        if (!mask.contains("*") || source.length() != mask.length()) {
            return source.equals(mask);
        } else {
            boolean isEqual = true;
            int charNum = 0;
            while (isEqual && charNum < source.length()) {
                // проверка слова на соответствие маске, где * означает любой символ, а буква в маске -
                // обязательное соответствие символа у проверяемого слова в заданной позиции
                isEqual = mask.charAt(charNum) == '*' || source.charAt(charNum) == mask.charAt(charNum);
                if (isEqual) {
                    // проверка на наличие в слове 'обязательных' букв из списка. Если в проверяемом
                    // слове есть все обязательные буквы из списка, тогда переходим к следующему этапу проверки
                    for (Character existChar : existChars) {
                        if (!source.contains(existChar.toString())) {
                            isEqual = false;
                            break;
                        }
                    }
                }
                if (isEqual) {
                    // проверка на отсутствие в слове 'запрещенных' букв из списка, если в проверяемом
                    // слове есть любая запрещенная буква из списка, тогда выдаем результат false
                    for (Character nonExistChar : nonExistChars) {
                        if (source.charAt(charNum) == (char)nonExistChar) {
                            isEqual = false;
                            break;
                        }
                    }
                }
                charNum++;
            }
            return isEqual;
        }
    }

    public String getRandomWorld() {
        Random random = new Random();
        if (words.isEmpty()) {
            return "";
        }
        return words.get(random.nextInt(words.size()));
    }

}


