package de.mebibyte.Sonic;

import utill.common.throwable.CantHappenException;

/**
 * Description missing
 * Author: Till Hoeppner
 */
public class T3 {

    public static void main(String[] args) {
        try {
            new T3().test();
        } catch(CantHappenException che) {
            System.out.println("Catch");
        }

        System.out.println("Done");
    }

    public void test() {
        System.out.println("Test");
    }

}
