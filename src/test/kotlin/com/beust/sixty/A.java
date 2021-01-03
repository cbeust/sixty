package com.beust.sixty;

/**
 * @author Cedric Beust <cedric@refresh.io>
 * @since 12 26, 2020
 */
    interface I {
        void foo();
    }

    public class A {
        void f() {
            Object a = new String();
            ((I) a).foo();
        }
    }
