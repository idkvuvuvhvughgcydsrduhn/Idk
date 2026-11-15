package structure.client.core;

/** A module that is a one-shot action (open GUI, panic...). Never stays enabled. */
public abstract class TriggerMod extends Mod {
    protected TriggerMod(String n, String d, Category c) { super(n, d, c); }
    protected abstract void fire();
    @Override public boolean isEnabled() { return false; }
    @Override public void setEnabled(boolean v) { if (v) fire(); }
    @Override public void loadEnabled(boolean v) {}
}
