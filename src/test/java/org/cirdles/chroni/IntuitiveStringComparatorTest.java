package org.cirdles.chroni;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;

import static org.junit.Assert.*;

public class IntuitiveStringComparatorTest {

    private static final String[] TEST_LIST_UNORDERED = {
        "foo 10",
        "foo 10boo",
        "1c",
        "foo 10far",
        "IRA00-10",
        "foo~03",
        "1b",
        "foo 03",
        "foo 003",
        "foo!03",
        "1d",
        "foo 10bar",
        "foo 5",
        "IRA00-1",
        "foo 00003",
        "IRA00-2",
    };

    private static final String[] TEST_LIST_ORDERED = {
        "1b",
        "1c",
        "1d",
        "foo 03",
        "foo 003",
        "foo 00003",
        "foo 5",
        "foo 10",
        "foo 10bar",
        "foo 10boo",
        "foo 10far",
        "foo!03",
        "foo~03",
        "IRA00-1",
        "IRA00-2",
        "IRA00-10"
    };

    private Comparator<String> comparator;

    @Before
    public void setupComparator() {
        comparator = new IntuitiveStringComparator<String>();
    }

    @Test
    public void testTreatsNumbersAtomically() {
        assertOrdered("23", "123");
        assertOrdered("a23", "a123");
    }

    @Test
    public void testSortsNumbersFirst() {
        assertOrdered("1", "A");
        assertOrdered("1", " ");
        assertOrdered("1", ".");

        assertOrdered("123", "A");
        assertOrdered("123", " ");
        assertOrdered("123", ".");
    }

    @Test
    public void testSortsWhitespaceAndPunctuationBeforeLetters() {
        assertOrdered(" ", "a");
        assertOrdered(" ", "A");

        assertOrdered(".", "a");
        assertOrdered(".", "A");
    }

    @Test
    public void testSortsLettersCaseInsensitively() {
        assertOrdered("aBc", "AbCD");
    }

    @Test
    public void testConsidersMoreLeadingZerosGreater() {
        assertOrdered("1", "01");
        assertOrdered("01", "001");
        assertOrdered("a1", "a01");

        assertOrdered("000", "1");
        assertOrdered("a00", "a1");
    }

    @Test
    public void testSortsList() {
        Arrays.sort(TEST_LIST_UNORDERED, comparator);
        assertArrayEquals(TEST_LIST_ORDERED, TEST_LIST_UNORDERED);
    }

    public void assertAscending(String string1, String string2) {
        assertEquals(-1, comparator.compare(string1, string2));
    }

    public void assertDescending(String string1, String string2) {
        assertEquals(1, comparator.compare(string1, string2));
    }

    public void assertOrdered(String first, String second) {
        assertAscending(first, second);
        assertDescending(second, first);
    }

}
