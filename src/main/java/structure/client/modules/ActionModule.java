package dev.structure.client.module;

/** A one-shot module (open GUI, panic...). It never stays enabled; toggling it just runs {@link #fire()}. */
public abstract class ActionModule extends Module {
	protected ActionModule(String name, String description, Category category) {
		super(name, description, category);
	}

	protected abstract void fire();

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void setEnabled(boolean on, boolean notify) {
		if (on) {
			try {
				fire();
			} catch (Throwable t) {
				dev.structure.client.StructureClient.LOGGER.error("Action {} failed", name, t);
			}
		}
	}

	@Override
	public void loadEnabled(boolean v) {
	}
}
