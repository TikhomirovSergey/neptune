package com.github.toy.constructor.core.api.test;

import org.testng.annotations.Test;

import java.util.function.Function;

import static com.github.toy.constructor.core.api.StoryWriter.toGet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.testng.AssertJUnit.fail;

public class ToGetTest {

    private static final Function<Object, String> GET_TO_STRING = Object::toString;
    private static final Function<String, Integer> GET_STRING_LENGTH = String::length;
    private static final Function<Integer, Boolean> GET_POSITIVITY = integer -> integer.compareTo(0) > 0;

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "It seems given function doesn't describe any value to get. " +
                    "Use method StoryWriter.toGet to describe the value to get after.")
    public void negativeTestWhenTheNextFunctionIsNotDescribed() {
        Function<Object, String> describedToString = toGet("String value of the object",
                GET_TO_STRING);
        describedToString.andThen(GET_STRING_LENGTH);
        fail("The exception thowing was expected");
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "It seems given function doesn't describe any value to get. " +
                    "Use method StoryWriter.toGet to describe the value to get previously.")
    public void negativeTestWhenThePreviousFunctionIsNotDescribed() {
        Function<String, Integer> describedStringLength = toGet("Length of the given string",
                GET_STRING_LENGTH);
        describedStringLength.compose(GET_TO_STRING);
        fail("The exception thowing was expected");
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Description should not be empty")
    public void negativeTestOfEmptyDescription() {
        toGet("", GET_TO_STRING);
        fail("The exception thowing was expected");
    }

    @Test
    public void checkDescriptionOfAFunctionThen() {
        Function<Object, String> describedToString = toGet("String value of the object",
                GET_TO_STRING);
        Function<String, Integer> describedStringLength = toGet("Length of the given string",
                GET_STRING_LENGTH);

        assertThat("Sting value of the function",
                describedToString.andThen(describedStringLength).toString(),
                is("Length of the given string from (String value of the object)"));
    }

    @Test
    public void checkDescriptionOfAFunctionCompose() {
        Function<Object, String> describedToString = toGet("String value of the object",
                GET_TO_STRING);
        Function<String, Integer> describedStringLength = toGet("Length of the given string",
                GET_STRING_LENGTH);

        assertThat("Sting value of the function",
                describedStringLength.compose(describedToString).toString(),
                is("Length of the given string from (String value of the object)"));
    }

    @Test
    public void checkDescriptionOfAFunctionComplex() {
        Function<Object, String> describedToString = toGet("String value of the object",
                GET_TO_STRING);
        Function<String, Integer> describedStringLength = toGet("Length of the given string",
                GET_STRING_LENGTH);
        Function<Integer, Boolean> describedPositivityFunction =
                toGet("Positivity of the calculated value", GET_POSITIVITY);

        assertThat("Sting value of the function",
                describedStringLength.compose(describedToString).andThen(describedPositivityFunction).toString(),
                is("Positivity of the calculated value from " +
                        "(Length of the given string from (String value of the object))"));
    }
}
