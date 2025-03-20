package de.innfactory.afps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ImperativeExample {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);
        List<Integer> result = new ArrayList<>();

        for (int number : numbers) {
            if (number % 2 == 0) {
                result.add(number * number);
            }
        }

        numbers.stream()
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer n) {
                        return n % 2 == 0;
                    }
                })
                .map(n -> n * n)
                .toList();

        System.out.println(result);
    }
}
