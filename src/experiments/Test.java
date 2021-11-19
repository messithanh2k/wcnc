package experiments;

import java.util.ArrayList;
import java.util.Random;

public class Test{
    public static void main(String[] args){
        int seed = 5;
        Random rand = new Random(seed);
        int a = rand.nextInt(10);
        double q = rand.nextDouble();
        System.out.println(a);
        System.out.println(q);
    }
}