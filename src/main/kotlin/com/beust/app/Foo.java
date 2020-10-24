package com.beust.app;

import java.util.HashMap;

/**
 * @author Cedric Beust <cedric@refresh.io>
 * @since 10 23, 2020
 */
public enum Foo {
    A,
    B,
    C;
}
interface Base {}

class X implements Base {
    public X(String a, String b) {
    }
}
class Y implements Base {
    public Y(String a, String b) {
    }
}
class Z implements Base {
    public Z(String a, String b) {
    }
}
interface BaseFactory {
    Base create(String a, String b);
}

class HelloWorld2 {
    private static HashMap<Foo, BaseFactory> map = new HashMap<>();

    public static void main(String[] args) {
        var x = Foo.A;
        switch(x) {
            case A: break;
        }

        map.put(Foo.A, (a, b) -> new X(a, b));
        map.put(Foo.B, (a, b) -> new Y(a, b));
        map.put(Foo.C, (a, b) -> new Z(a, b));
    }

    public static void create(Enum f) {
        var factory = map.get(f);
        if (factory != null) factory.create("a", "b");
        else throw new IllegalArgumentException("Couldn't find a factory for " + f);
    }
}