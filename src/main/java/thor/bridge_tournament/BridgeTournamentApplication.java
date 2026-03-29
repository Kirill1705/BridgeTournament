package thor.bridge_tournament;

import org.apache.commons.collections4.iterators.PermutationIterator;
import org.aspectj.weaver.ast.Or;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;

@SpringBootApplication
public class BridgeTournamentApplication {
    public static void main(String[] args) {
        List<Integer> primary = List.of(0, 1, 2, 3, 4, 5, 6, 7);
        Pair[][] pairs = create(primary);
        Iterator<List<Integer>> iterator = new PermutationIterator<>(primary);
        Set<OrderedPair> set = new HashSet<>();
        int count = 0;
        for (Iterator<List<Integer>> it = iterator; it.hasNext(); ) {
            if (count % 100000 == 0) {
                System.out.println(count/100000);
            }
            count++;
            List<Integer> permutation = it.next();
            Pair[][] newPairs = create(permutation);
            //print(newPairs);
            set.add(checkOrthogonal(newPairs, pairs));
        }
        for (OrderedPair pair: set) {
            System.out.println(pair.first() + " " + pair.second());
        }
        //SpringApplication.run(BridgeTournamentApplication.class, args);
    }

    private static OrderedPair checkOrthogonal(Pair[][] pairs, Pair[][] primary) {
        int pairsCount = primary[0].length;
        Map<Pair, Integer> primaryMap = new HashMap<>();
        for (int i = 0; i < primary.length; i++) {
            for (int j = 0; j < primary[i].length; j++) {
                primaryMap.put(primary[i][j], i);
            }
        }
        int globalCount = 0;
        int p = 0;
        for (int i = 0; i < pairs.length; i++) {
            Set<Integer> set = new HashSet<>();
            int count = 0;
            for (int j = 0; j < pairs[i].length; j++) {
                int number = primaryMap.get(pairs[i][j]);
                if (set.contains(number))
                    count++;
                set.add(number);
            }
            if (count == pairsCount - 1) {
                globalCount++;
            }
            else if (count >= 1) {
                p++;
            }
        }
        return new OrderedPair(globalCount, p);
    }

    private static Pair[][] create(List<Integer> data) {
        int pairs = (data.size() + 1)/2;
        Pair[][] result = new Pair[data.size()][pairs];
        for (int i = 0; i < data.size(); i++) {
            result[i][0] = new Pair(data.size(), data.get(i));
            for (int j = 1; j < pairs; j++) {
                result[i][j] = new Pair(data.get(mod(i-j, data.size())), data.get(mod(i + j, data.size())));
            }
        }
        return result;
    }

    private static int mod(int x, int y) {
        int res = x % y;
        if (res < 0) {
            res += y;
        }
        return res;
    }

    private static void print(Pair[][] pairs) {
        for (int i = 0; i < pairs[0].length; i++) {
            for (int j = 0; j < pairs.length; j++) {
                System.out.print(pairs[j][i].first() + "" + pairs[j][i].second() + " ");
            }
            System.out.println();
        }
    }
}

record Pair(int first, int second) {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Pair) obj;
        return this.first == that.first &&
                this.second == that.second ||
                this.first == that.second &&
                        this.second == that.first;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first * first + second * second);
    }
}

record OrderedPair(int first, int second) {}
