package com.effacy.jui.rpc.handler.client.query;

/**
 * A type of query that involves the specification of a single object (via a
 * reference).
 * <p>
 * This encapsulate an object that allows for a lookup on the object under
 * consideration. Often this will be a {@link Ref} (as is used in commands)
 * however can be any serialisable type.
 */
public abstract class Lookup<T,R> extends Query<T> {

    /**
     * See {@link #setRef(Object)} and constructor.
     */
    private R ref;

    /**
     * Serialisation constructor.
     */
    protected Lookup() {
        super();
    }

    /**
     * Copy constructor.
     * 
     * @param copy
     *             the copy to make.
     */
    protected Lookup(Lookup<T,R> copy) {
        this.ref = copy.ref();
    }

    /**
     * Construct with lookup reference.
     * 
     * @param ref
     *               the lookup reference.
     */
    protected Lookup(R ref) {
        this.ref = ref;
    }

    /**
     * The reference to the object being looked up.
     * 
     * @return the reference.
     */
    public R ref() {
        return ref;
    }

    /**
     * Serialisation.
     */
    public R getRef() {
        return ref;
    }

    /**
     * Serialisation.
     */
    public void setRef(R ref) {
        this.ref = ref;
    }
}
