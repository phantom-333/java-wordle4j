package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class WordleTest {
    protected static final String dictionaryFileName = "words_ru.txt";
    protected static PrintWriter pwLog = new PrintWriter(System.out);
    protected static final int wordLength = 5;
    protected static final int wordLengthInFile = 67763;
    protected static WordleDictionary wordleDictionary;

    @BeforeEach
    public void beforeEach() {
        try {
            WordleDictionaryLoader wordleDictionaryLoader = new WordleDictionaryLoader(pwLog, dictionaryFileName);
            wordleDictionary = wordleDictionaryLoader.load(wordLength);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void WordDictionaryLoadTest() {
        assertEquals(wordLengthInFile, wordleDictionary.getWordsCount(),
                "Количество загруженных строк не совпадает с количеством в файле");
    }

    @Test
    public void WordDictionaryNormalizeTest() {
        assertEquals(wordLengthInFile, wordleDictionary.getWordsCount());
        wordleDictionary.normalizeDictionary();
        assertTrue(wordleDictionary.getWordsCount() < wordLengthInFile,
                "После нормализации списка количество строк не изменилось");
    }

    @Test
    public void WordDictionaryIsExistTest() {
        wordleDictionary.normalizeDictionary();

        assertTrue(wordleDictionary.isExist("амеба"), "Поиск слова в словаре выполнен некорректно");
        assertTrue(wordleDictionary.isExist("амёба"), "Поиск слова в словаре выполнен некорректно");
        assertTrue(wordleDictionary.isExist(" АМЁБА  "), "Поиск слова в словаре выполнен некорректно");
    }

    @Test
    public void WordDictionaryCompareMaskTest_withoutExistSymbol() {
        final String testWord1 = "БАНДА";
        final String testWord2 = "  бонна   восемь";
        final String testWord3 = "Бонза";
        final String mask = "б*Н*а";
        wordleDictionary.normalizeDictionary();

        assertTrue(wordleDictionary.compareMask(testWord1, mask, new LinkedHashSet<Character>(), new LinkedHashSet<Character>()),
                "Метод проверки на соответствие слова и маски работает некорректно");
        assertTrue(wordleDictionary.compareMask(testWord2, mask, new LinkedHashSet<Character>(), new LinkedHashSet<Character>()),
                "Метод проверки на соответствие слова и маски работает некорректно");
        assertTrue(wordleDictionary.compareMask(testWord3, mask, new LinkedHashSet<Character>(), new LinkedHashSet<Character>()),
                "Метод проверки на соответствие слова и маски работает некорректно");
    }

    @Test
    public void WordDictionaryCompareMaskTest_withExistSymbol() {
        LinkedHashSet<Character> existChar = new LinkedHashSet<>();
        for (char ch : new char[]{'б', 'м', 'а', 'е'}) {
            existChar.add(ch);
        }
        final String testWord = "амеба";
        final String mask = "**е**";
        wordleDictionary.normalizeDictionary();

        assertTrue(wordleDictionary.compareMask(testWord, mask, existChar, new LinkedHashSet<Character>()),
                "Метод проверки на соответствие слова и маски по существующим символам работает некорректно");
    }


    @Test
    public void WordDictionaryCompareMaskTest_withNonExistSymbol() {
        LinkedHashSet<Character> nonExistChar1 = new LinkedHashSet<>();
        for (char ch : new char[]{'о', 'м', 'а', 'е', 'у', 'и', 'ы', 'х', 'ю', 'я'}) {
            nonExistChar1.add(ch);
        }
        LinkedHashSet<Character> nonExistChar2 = new LinkedHashSet<>();
        for (char ch : new char[]{'о', 'м', 'а', 'е', 'у', 'и', 'ы', 'х', 'ю', 'я', 'б'}) {
            nonExistChar2.add(ch);
        }

        final String testWord = "скрэб";
        final String mask = "*****";
        wordleDictionary.normalizeDictionary();

        //список отсутствующих символов определяет единственное возможное слово "скрэб"
        assertTrue(wordleDictionary.compareMask(testWord, mask, new LinkedHashSet<Character>(), nonExistChar1),
                "Метод проверки на соответствие слова и маски по отсутствующим символам работает некорректно");

        //при добавлении в список любого символа из слова "скрэб", оно должно перестать удовлетворять условиям сравнения
        assertFalse(wordleDictionary.compareMask(testWord, mask, new LinkedHashSet<Character>(), nonExistChar2),
                "Метод проверки на соответствие слова и маски по отсутствующим символам работает некорректно");
    }

    @Test
    public void WordDictionaryFilterTest() {
        LinkedHashSet<Character> existChar = new LinkedHashSet<>();
        for (char ch : new char[]{'б', 'а'}) {
            existChar.add(ch);
        }
        LinkedHashSet<Character> existChar2 = new LinkedHashSet<>();
        for (char ch : new char[]{'у'}) {
            existChar2.add(ch);
        }
        LinkedHashSet<Character> nonExistChar1 = new LinkedHashSet<>();
        for (char ch : new char[]{'т'}) {
            nonExistChar1.add(ch);
        }
        final String mask = "**Ё**";
        wordleDictionary.normalizeDictionary();

        //набор фильтров без исключающих символов соответствует списку [учеба, амеба, треба, ябеда]
        wordleDictionary.filter(mask, existChar, new LinkedHashSet<Character>());
        assertEquals(4, wordleDictionary.getWordsCount(),
                "Метод фильтрации словаря работает некорректно");

        //добавим в исключающий фильтр символ 'т' - список в словаре должен сократиться до [учеба, амеба, ябеда]
        wordleDictionary.filter(mask, new LinkedHashSet<Character>(), nonExistChar1);
        assertEquals(3, wordleDictionary.getWordsCount(),
                "Метод фильтрации словаря работает некорректно");

        //добавим в фильтр существующий символ 'у' - список в словаре должен сократиться до [учеба]
        wordleDictionary.filter(mask, existChar2, new LinkedHashSet<Character>());
        assertTrue(wordleDictionary.isExist("учеба"),"Метод фильтрации словаря работает некорректно");
    }

}
