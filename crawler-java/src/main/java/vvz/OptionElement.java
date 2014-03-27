package vvz;

public final class OptionElement {
    public String id,name;

    public OptionElement(String id,String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return id+":"+name;
    }
}
