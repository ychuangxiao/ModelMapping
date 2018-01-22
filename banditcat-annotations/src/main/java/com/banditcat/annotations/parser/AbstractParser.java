
package com.banditcat.annotations.parser;

/**
 * This is the base class to implement a custom parser to your transformations.
 */
public abstract class AbstractParser<T1, T2> {
    
    public  T2 parse(T1 value) {
        return onParse(value);
    }

    /**
     * Override this method to implement the logic of your parser.
     * @param value The value that will to be parse.
     * @return The parser result value.
     */
    protected abstract T2 onParse(T1 value);
}
