# Oveview

This package contains a collection of standard fragments.

## CSS styles

A single non-strict CSS file is maintained by `BaseFragment.FragmentStyles` and is injected via static initialisers in both the `BaseXXX` classes.

Styles adhere to the naming convention where each style is prefixed with `juiXXX` where `XXX` is the fragment name. For examples `juiMyAtom` for the fragment named `MyAtom` (see example below). One should not use dashes in CSS class names but user underscores `_` (this ensures compatibility should they be migrated to strict form).

## Class patterns

There are two patterns that are followed: for fragments that appear in a heirarachy (extendable) and those that do not (terminal).

### Terminal fragments

The general pattern that terminal fragments follow is as follows:

```java
public class MyAtom {
    
    public static MyAtomFragment $() {
        return new MyAtomFragment ();
    }

    public static MyAtomFragment $(IDomInsertableContainer<?> parent) {
        MyAtomFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public static class MyAtomFragment extends BaseFragment<MyAtomFragment> {
        ...
        @Override
        protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
            ...
        }
    }
}
```

Note that `BaseFragment` could equally well replaced by `BaseFragmentWithChildren` and ensuring that the `build(...)` method processes any children that have been added (i.e. by invoking the super class `build(...)` method).

The principle here is to expose the two static `$(...)` methods (be be consistent with other uses) but to do so without conflicting with the `$` method on `IDomInsertableContainer`.

### Extendable fragments

This is a little more complicaed but designed to allow for chaining of fragment configuration (ensuring that the configuration method return the correct type):

```java
public class MyExtendable {

    public static MyExtendableFragment $() {
        return new MyExtendableFragment ();
    }

    public static MyExtendableFragment $(IDomInsertableContainer<?> parent) {
        MyExtendableFragment frg = $ ();
        if (parent != null)
            parent.insert (frg);
        return frg;
    }

    public static class MyExtendableFragment extends AMyExtendableFragment<MyExtendableFragment> {}

    public static abstract class AMyExtendableFragment<T extends AMyExtendableFragment<T>> extends BaseFragmentWithChildren<T> {

        @SuppressWarnings("unchecked")
        public T property1(...) {
            ...
            return (T)this;
        }

        ...
        @Override
        protected ElementBuilder createRoot(ContainerBuilder<?> parent) {
            ...
        }
        
    }
}
```

The primary difference is the introduction of the abstract intermediary `AMyExtendableFragment` which is paramaterised over the sub-class. Any fragment that inherits from this should have its implementation itself extend `AMyExtendableFragment`:

```java
public class MyExtendable2 {

    ...

    public static class MyExtendable2Fragment extends AMyExtendable2Fragment<MyExtendable2Fragment> {}

    public static abstract class AMyExtendable2Fragment<T extends AMyExtendable2Fragment<T>> extends AMyExtendableFragment<T> {
        ...
    }
}
```

Examples of this are `Paper` and `Card` (where the latter extends the former).